/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.java.sen.compiler;

import net.java.sen.trainer.CostCalculator;
import net.java.sen.util.CSVParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Converts a UniDic dictionary (MeCab format) into the intermediate CSV files
 * consumed by {@link DictionaryBuilder}:
 * <ul>
 *   <li>{@code dictionary.csv} — one entry per word</li>
 *   <li>{@code connection.csv} — connection cost matrix (condensed)</li>
 * </ul>
 *
 * <h2>Input files (from unidic-cwj-202512_full/)</h2>
 * <dl>
 *   <dt>{@code lex.csv}</dt>
 *   <dd>Word entries. Column layout:
 *       surface(0), leftId(1), rightId(2), wordCost(3),
 *       pos1(4)…aModType(19), …</dd>
 *   <dt>{@code left-id.def}</dt>
 *   <dd>Maps leftId → 15-element L[] feature array used when the word appears
 *       as the RIGHT token in a connection.</dd>
 *   <dt>{@code right-id.def}</dt>
 *   <dd>Maps rightId → 15-element R[] feature array used when the word appears
 *       as the LEFT token in a connection.</dd>
 *   <dt>{@code matrix.def}</dt>
 *   <dd>Connection cost matrix. Header line: {@code right_size left_size}.
 *       Subsequent lines: {@code rightId leftId cost}.</dd>
 * </dl>
 *
 * <h2>dictionary.csv column layout</h2>
 * <pre>
 * col 0  surface
 * col 1  wordCost
 * col 2  pos1          (F[0])
 * col 3  pos2          (F[1])
 * col 4  pos3          (F[2])
 * col 5  pos4          (F[3])
 * col 6  cType         (F[4])
 * col 7  cForm         (F[5])
 * col 8  orthBase      (F[9])  ← used as basicForm by DictionaryBuilder
 * col 9  pron          (F[10]) ← reading
 * col 10 pronBase      (F[11]) ← pronunciation
 * </pre>
 * This satisfies {@code PART_OF_SPEECH_START=2}, {@code PART_OF_SPEECH_SIZE=7}.
 *
 * <h2>connection.csv column layout</h2>
 * <pre>
 * "rightPosKey", "rightPosKey", "leftPosKey", cost
 * </pre>
 * Each POS key is the 7-field string
 * {@code pos1,pos2,pos3,pos4,cType,cForm,orthBase}
 * built from the R[]/L[] arrays in right-id.def / left-id.def.
 * Costs are averaged over all (rightId, leftId) pairs that share the same
 * (rightPosKey, leftPosKey) combination.
 *
 * <h2>BOS / EOS / Unknown mapping</h2>
 * UniDic's ID 0 (BOS/EOS) is mapped to the string constants in
 * {@link DictionaryBuilder} ({@code "文頭,*,*,*,*,*,*"} for BOS,
 * {@code "文末,*,*,*,*,*,*"} for EOS) so that the compiled dictionary
 * is compatible without modifying the existing compiler.
 */
public class UnidicPreprocessor {

  // lex.csv column indices
  private static final int LEX_SURFACE   = 0;
  private static final int LEX_LEFT_ID   = 1;
  private static final int LEX_RIGHT_ID  = 2;
  private static final int LEX_WORD_COST = 3;
  private static final int LEX_POS1      = 4;   // F[0]
  private static final int LEX_POS2      = 5;   // F[1]
  private static final int LEX_POS3      = 6;   // F[2]
  private static final int LEX_POS4      = 7;   // F[3]
  private static final int LEX_CTYPE     = 8;   // F[4]
  private static final int LEX_CFORM     = 9;   // F[5]
  // F[6]=lForm(10), F[7]=lemma(11), F[8]=orth(12)
  private static final int LEX_ORTH_BASE = 13;  // F[9]  → basicForm
  private static final int LEX_PRON      = 14;  // F[10] → reading
  private static final int LEX_PRON_BASE = 15;  // F[11] → pronunciation

  // Number of F[] fields starting at LEX_POS1 (needed by CostCalculator)
  static final int F_COUNT = 16;

  // Minimum number of lex.csv columns for a valid entry
  private static final int LEX_MIN_COLS = 16;

  // ID 0 in left-id.def / right-id.def represents BOS/EOS
  private static final int BOS_EOS_ID = 0;

  // POS key constants that match DictionaryBuilder's hardcoded strings
  static final String BOS_POS_KEY     = "文頭,*,*,*,*,*,*";
  static final String EOS_POS_KEY     = "文末,*,*,*,*,*,*";
  static final String UNKNOWN_POS_KEY = "名詞,サ変接続,*,*,*,*,*";

  private final File dictDir;

  /**
   * If non-null, costs are recalculated from CRF weights (Phase 2).
   * If null, raw costs from lex.csv / matrix.def are used (Phase 1).
   */
  private final CostCalculator costCalculator;

  /**
   * @param dictDir        path to the unidic-cwj-202512_full directory
   * @param costCalculator use CRF weights for costs; pass {@code null} to use
   *                       the raw costs from lex.csv and matrix.def
   */
  public UnidicPreprocessor(File dictDir, CostCalculator costCalculator) {
    this.dictDir        = dictDir;
    this.costCalculator = costCalculator;
  }

  // --------------------------------------------------------------------------
  // Public entry point
  // --------------------------------------------------------------------------

  /**
   * Runs the full preprocessing pipeline, writing {@code dictionary.csv} and
   * {@code connection.csv} into {@code outputDir}.
   *
   * @param outputDir directory to write intermediate CSV files into
   * @throws IOException on any I/O error
   */
  public void build(File outputDir) throws IOException {
    outputDir.mkdirs();

    System.out.println("[UnidicPreprocessor] Loading left-id.def ...");
    Map<Integer, String[]> leftIdFeatures  = loadIdDef(new File(dictDir, "left-id.def"));
    System.out.println("  " + leftIdFeatures.size() + " left-context IDs loaded");

    System.out.println("[UnidicPreprocessor] Loading right-id.def ...");
    Map<Integer, String[]> rightIdFeatures = loadIdDef(new File(dictDir, "right-id.def"));
    System.out.println("  " + rightIdFeatures.size() + " right-context IDs loaded");

    // Pass 1: lex.csv → dictionary.csv; collect (rightId, leftId) pairs
    System.out.println("[UnidicPreprocessor] Processing lex.csv → dictionary.csv ...");
    // rightId → set of leftIds that appear together in lex.csv
    Map<Integer, Set<Integer>> usedPairs = new LinkedHashMap<>();
    File dictCsvFile = new File(outputDir, "dictionary.csv");
    writeDictionaryCsv(dictCsvFile, rightIdFeatures, usedPairs);
    System.out.println("  Written: " + dictCsvFile.getAbsolutePath());

    // Pass 2: matrix.def → connection.csv (streaming, only needed pairs)
    System.out.println("[UnidicPreprocessor] Processing matrix.def → connection.csv ...");
    File connCsvFile = new File(outputDir, "connection.csv");
    writeConnectionCsv(connCsvFile, leftIdFeatures, rightIdFeatures, usedPairs);
    System.out.println("  Written: " + connCsvFile.getAbsolutePath());
  }

  // --------------------------------------------------------------------------
  // dictionary.csv generation
  // --------------------------------------------------------------------------

  private void writeDictionaryCsv(File outputFile,
                                   Map<Integer, String[]> rightIdFeatures,
                                   Map<Integer, Set<Integer>> usedPairs) throws IOException {

    try (BufferedWriter writer = new BufferedWriter(
             new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8));
         FileInputStream fis = new FileInputStream(new File(dictDir, "lex.csv"));
         CSVParser parser = new CSVParser(fis, "UTF-8")) {

      String[] row;
      long count = 0;
      while ((row = parser.nextTokens()) != null) {
        if (row.length < LEX_MIN_COLS) continue;

        // Build F[] array (16 elements, F[0]=pos1 … F[15]=aModType)
        String[] f = new String[F_COUNT];
        for (int j = 0; j < F_COUNT; j++) {
          int col = LEX_POS1 + j;
          f[j] = (col < row.length) ? row[col] : "*";
        }

        String surface  = row[LEX_SURFACE];
        int    leftId   = parseInt(row[LEX_LEFT_ID]);
        int    rightId  = parseInt(row[LEX_RIGHT_ID]);

        // Compute word cost (raw or CRF)
        int wordCost;
        if (costCalculator != null) {
          wordCost = costCalculator.computeWordCost(f, charType(surface));
        } else {
          wordCost = parseInt(row[LEX_WORD_COST]);
        }

        String pos1     = f[0];
        String pos2     = f[1];
        String pos3     = f[2];
        String pos4     = f[3];
        String cType    = f[4];
        String cForm    = f[5];
        String orthBase = row[LEX_ORTH_BASE];
        String pron     = row[LEX_PRON];
        String pronBase = row[LEX_PRON_BASE];

        // Record this (rightId, leftId) pair for connection matrix condensation
        usedPairs.computeIfAbsent(rightId, k -> new HashSet<>()).add(leftId);

        // Write row: "surface",cost,pos1,pos2,pos3,pos4,cType,cForm,"orthBase","pron","pronBase"
        writer.write(enquote(surface));
        writer.write(',');
        writer.write(Integer.toString(wordCost));
        writer.write(',');
        writer.write(escapeField(pos1));
        writer.write(',');
        writer.write(escapeField(pos2));
        writer.write(',');
        writer.write(escapeField(pos3));
        writer.write(',');
        writer.write(escapeField(pos4));
        writer.write(',');
        writer.write(escapeField(cType));
        writer.write(',');
        writer.write(escapeField(cForm));
        writer.write(',');
        writer.write(enquote(orthBase));
        writer.write(',');
        writer.write(enquote(pron));
        writer.write(',');
        writer.write(enquote(pronBase));
        writer.write('\n');
        count++;
      }
      System.out.println("  " + count + " word entries written");
    }
  }

  // --------------------------------------------------------------------------
  // connection.csv generation
  // --------------------------------------------------------------------------

  /**
   * Streams {@code matrix.def} and produces a condensed {@code connection.csv}.
   *
   * <p>Only (rightId, leftId) pairs that actually appear in {@code lex.csv}
   * are processed (plus BOS/EOS ID 0 transitions). Costs are accumulated per
   * (rightPosKey, leftPosKey) pair and averaged, yielding a compact matrix
   * compatible with {@link CostMatrixBuilder}.
   */
  private void writeConnectionCsv(File outputFile,
                                   Map<Integer, String[]> leftIdFeatures,
                                   Map<Integer, String[]> rightIdFeatures,
                                   Map<Integer, Set<Integer>> usedPairs) throws IOException {

    // Build the set of all leftIds that appear in lex.csv
    Set<Integer> allLeftIds = new HashSet<>();
    for (Set<Integer> s : usedPairs.values()) {
      allLeftIds.addAll(s);
    }

    // Accumulate costs: compositeKey → [sum, count]
    // compositeKey = rightPosKey + '\0' + leftPosKey  (NUL cannot appear in a POS key)
    Map<String, long[]> costAccum = new LinkedHashMap<>();

    File matrixDef = new File(dictDir, "matrix.def");
    System.out.println("  Streaming " + matrixDef.getName() + " ...");

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(matrixDef), StandardCharsets.UTF_8))) {

      // First line: "rightSize leftSize"
      String headerLine = reader.readLine();
      if (headerLine == null) {
        throw new IOException("matrix.def is empty");
      }

      String line;
      long processed = 0;
      long skipped   = 0;

      while ((line = reader.readLine()) != null) {
        if (line.isEmpty()) continue;

        // Parse "rightId leftId cost"
        int sp1 = line.indexOf(' ');
        int sp2 = (sp1 >= 0) ? line.indexOf(' ', sp1 + 1) : -1;
        if (sp1 < 0 || sp2 < 0) continue;

        int rightId = parseInt(line.substring(0, sp1));
        int leftId  = parseInt(line.substring(sp1 + 1, sp2));
        int cost    = parseInt(line.substring(sp2 + 1));

        boolean bosRight = (rightId == BOS_EOS_ID);
        boolean bosLeft  = (leftId  == BOS_EOS_ID);

        // Skip pairs whose IDs don't appear in lex.csv (and aren't BOS/EOS)
        if (!bosRight && !usedPairs.containsKey(rightId)) { skipped++; continue; }
        if (!bosLeft  && !allLeftIds.contains(leftId))    { skipped++; continue; }

        // Map IDs to POS keys
        String rightPosKey = bosRight
            ? BOS_POS_KEY
            : posKey7(rightIdFeatures.get(rightId));
        String leftPosKey  = bosLeft
            ? EOS_POS_KEY
            : posKey7(leftIdFeatures.get(leftId));

        if (rightPosKey == null || leftPosKey == null) { skipped++; continue; }

        // If CRF calculator is available, override raw matrix cost
        if (costCalculator != null && !bosRight && !bosLeft) {
          String[] rFeats = rightIdFeatures.get(rightId);
          String[] lFeats = leftIdFeatures.get(leftId);
          if (rFeats != null && lFeats != null) {
            cost = costCalculator.computeConnectionCost(rFeats, lFeats);
          }
        }

        String compositeKey = rightPosKey + '\0' + leftPosKey;
        long[] acc = costAccum.get(compositeKey);
        if (acc == null) {
          acc = new long[2];
          costAccum.put(compositeKey, acc);
        }
        acc[0] += cost;
        acc[1]++;
        processed++;

        if (processed % 500_000 == 0) {
          System.out.println("  " + processed + " matrix entries processed, "
              + costAccum.size() + " unique POS pairs so far ...");
        }
      }
      System.out.println("  Total: " + processed + " entries used, " + skipped + " skipped, "
          + costAccum.size() + " unique (rightPosKey, leftPosKey) pairs");
    }

    // Write connection.csv
    try (BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

      // Wildcard catch-all rule — ensures CostMatrixBuilder always has a fallback
      writeConnectionRow(writer, "*,*,*,*,*,*,*", "*,*,*,*,*,*,*", 10000);

      for (Map.Entry<String, long[]> entry : costAccum.entrySet()) {
        long[] acc   = entry.getValue();
        int avgCost  = (int) (acc[0] / acc[1]);
        String key   = entry.getKey();
        int sep      = key.indexOf('\0');
        String rKey  = key.substring(0, sep);
        String lKey  = key.substring(sep + 1);
        writeConnectionRow(writer, rKey, lKey, avgCost);
      }
    }
    System.out.println("  " + (costAccum.size() + 1) + " connection rows written");
  }

  private void writeConnectionRow(BufferedWriter writer,
                                   String rightPosKey,
                                   String leftPosKey,
                                   int cost) throws IOException {
    // Format: "rightPosKey","rightPosKey","leftPosKey",cost
    // The same rightPosKey is used for both rcAttr2 (col 0) and rcAttr1 (col 1).
    writer.write(enquote(rightPosKey));
    writer.write(',');
    writer.write(enquote(rightPosKey));
    writer.write(',');
    writer.write(enquote(leftPosKey));
    writer.write(',');
    writer.write(Integer.toString(cost));
    writer.write('\n');
  }

  // --------------------------------------------------------------------------
  // left-id.def / right-id.def parsing
  // --------------------------------------------------------------------------

  /**
   * Loads a MeCab id-def file ({@code left-id.def} or {@code right-id.def}).
   *
   * <p>Each line: {@code <id> <csv_fields>}
   * where {@code csv_fields} is a comma-separated list of 15 fields
   * (some may be quoted with double-quotes).
   *
   * @return map from integer ID to feature string array (15 elements)
   */
  private Map<Integer, String[]> loadIdDef(File file) throws IOException {
    Map<Integer, String[]> result = new HashMap<>();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isEmpty()) continue;
        int sp = line.indexOf(' ');
        if (sp < 0) continue;
        int id = parseInt(line.substring(0, sp));
        if (id < 0) continue;
        String csv = line.substring(sp + 1);
        String[] fields = parseCsvLine(csv);
        result.put(id, fields);
      }
    }
    return result;
  }

  /**
   * Parses a single CSV line, handling double-quoted fields that may contain
   * commas. Uses a simple hand-rolled parser to avoid allocating a CSVParser
   * per line (performance-critical for large id-def files).
   */
  private String[] parseCsvLine(String csv) {
    java.util.List<String> fields = new java.util.ArrayList<>(16);
    int i = 0;
    int len = csv.length();
    while (i < len) {
      if (csv.charAt(i) == '"') {
        // Quoted field
        i++; // skip opening quote
        StringBuilder sb = new StringBuilder();
        while (i < len) {
          char c = csv.charAt(i++);
          if (c == '"') {
            if (i < len && csv.charAt(i) == '"') {
              sb.append('"'); // escaped double-quote
              i++;
            } else {
              break; // end of quoted field
            }
          } else {
            sb.append(c);
          }
        }
        fields.add(sb.toString());
        if (i < len && csv.charAt(i) == ',') i++; // skip delimiter
      } else {
        // Unquoted field: read until comma or end
        int start = i;
        while (i < len && csv.charAt(i) != ',') i++;
        fields.add(csv.substring(start, i));
        if (i < len) i++; // skip comma
      }
    }
    return fields.toArray(new String[0]);
  }

  // --------------------------------------------------------------------------
  // POS key construction
  // --------------------------------------------------------------------------

  /**
   * Builds a condensed 7-field POS key string compatible with
   * {@link CostMatrixBuilder} from an id-def feature array.
   *
   * <p>To keep the connection matrix compact (and within the memory budget
   * of {@link DictionaryBuilder}), only two fields are preserved:
   * <ul>
   *   <li>Index 0 = pos1 (major POS category: 名詞, 動詞, 助詞, …)</li>
   *   <li>Index 5 = cForm (conjugation form: 連用形-一般, 終止形-一般, …)</li>
   * </ul>
   * All other positions are wildcarded ({@code *}).  This yields at most
   * ~80-100 unique patterns per matrix dimension, keeping the 3-D matrix
   * well under 10 MB.
   *
   * <p>The wildcard {@code *} in positions 1-4 and 6 is matched by
   * {@link CostMatrixBuilder}'s wildcard-aware rule lookup, so dictionary
   * entries whose full 7-field key contains specific values in those
   * positions will still match the correct connection-cost rule.
   *
   * @param features 15-element feature array from loadIdDef()
   * @return comma-joined 7-field POS key, or {@code null} if features is null
   */
  private String posKey7(String[] features) {
    if (features == null) return null;
    // pos1, *, *, *, *, cForm, *
    return safeGet(features, 0) + ",*,*,*,*," +
           safeGet(features, 5) + ",*";
  }

  private String safeGet(String[] arr, int idx) {
    return (arr != null && idx < arr.length && arr[idx] != null) ? arr[idx] : "*";
  }

  // --------------------------------------------------------------------------
  // CSV output utilities
  // --------------------------------------------------------------------------

  /**
   * Surrounds {@code s} with double-quotes if it contains a double-quote or
   * comma, mirroring the logic in {@link net.java.sen.util.CSVData#enquote}.
   */
  private String enquote(String s) {
    if (s == null || s.isEmpty()) return (s == null ? "" : s);
    boolean needsQuote = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '"' || c == ',') { needsQuote = true; break; }
    }
    if (!needsQuote) return s;
    StringBuilder sb = new StringBuilder(s.length() + 2).append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '"') sb.append('"');
      sb.append(c);
    }
    return sb.append('"').toString();
  }

  /** Escapes a single POS field value, quoting only if necessary. */
  private String escapeField(String s) {
    return (s == null) ? "*" : enquote(s);
  }

  // --------------------------------------------------------------------------
  // Misc utilities
  // --------------------------------------------------------------------------

  private int parseInt(String s) {
    if (s == null) return 0;
    try { return Integer.parseInt(s.trim()); }
    catch (NumberFormatException e) { return 0; }
  }

  /**
   * Returns a MeCab-compatible character type (0-7) for the first character
   * of the given surface form.
   *
   * <ul>
   *   <li>0 = DEFAULT</li>
   *   <li>1 = SPACE</li>
   *   <li>2 = KANJI</li>
   *   <li>3 = SYMBOL</li>
   *   <li>4 = NUMERIC</li>
   *   <li>5 = ALPHA</li>
   *   <li>6 = HIRAGANA</li>
   *   <li>7 = KATAKANA</li>
   * </ul>
   */
  static int charType(String surface) {
    if (surface == null || surface.isEmpty()) return 0;
    char c = surface.charAt(0);
    if (c == ' ' || c == '\t' || c == '\u3000') return 1; // SPACE / ideographic space
    if ((c >= '\u4E00' && c <= '\u9FFF')                  // CJK Unified
        || (c >= '\u3400' && c <= '\u4DBF')               // Extension A
        || (c >= '\uF900' && c <= '\uFAFF')               // CJK Compatibility Ideographs
        || c == '\u3005' || c == '\u3007') return 2;      // 々, 〇
    if (c >= '0' && c <= '9') return 4;
    if (c >= '\uFF10' && c <= '\uFF19') return 4;         // fullwidth digits
    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) return 5;
    if ((c >= '\uFF41' && c <= '\uFF5A') || (c >= '\uFF21' && c <= '\uFF3A')) return 5; // fullwidth
    if (c >= '\u3041' && c <= '\u3096') return 6;         // HIRAGANA
    if ((c >= '\u30A1' && c <= '\u30FA')
        || c == '\u30FD' || c == '\u30FE') return 7;      // KATAKANA
    return 3; // SYMBOL / other
  }
}
