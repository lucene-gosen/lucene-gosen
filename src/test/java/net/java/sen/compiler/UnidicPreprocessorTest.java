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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.java.sen.trainer.CostCalculator;
import net.java.sen.trainer.FeatureExtractor;
import net.java.sen.trainer.FeatureTemplateParser;
import net.java.sen.trainer.ModelWeightIndex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link UnidicPreprocessor}.
 *
 * <p>The {@code charType()} method is package-private, so it is tested directly.
 * The private {@code parseCsvLine()} method is tested via reflection.
 */
public class UnidicPreprocessorTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  // -----------------------------------------------------------------------
  // charType() — package-private static method
  // -----------------------------------------------------------------------

  @Test
  public void testCharTypeNull() {
    assertEquals(0, UnidicPreprocessor.charType(null));
  }

  @Test
  public void testCharTypeEmpty() {
    assertEquals(0, UnidicPreprocessor.charType(""));
  }

  @Test
  public void testCharTypeSpace() {
    assertEquals(1, UnidicPreprocessor.charType(" "));
    assertEquals(1, UnidicPreprocessor.charType("\t"));
    assertEquals(1, UnidicPreprocessor.charType("\u3000"));  // ideographic space
  }

  @Test
  public void testCharTypeKanji() {
    assertEquals(2, UnidicPreprocessor.charType("漢"));   // CJK Unified
    assertEquals(2, UnidicPreprocessor.charType("語"));
    assertEquals(2, UnidicPreprocessor.charType("々"));   // U+3005 repetition mark
    assertEquals(2, UnidicPreprocessor.charType("〇"));   // U+3007
  }

  @Test
  public void testCharTypeKanjiExtensionA() {
    // Extension A: U+3400–U+4DBF
    assertEquals(2, UnidicPreprocessor.charType("\u3400"));
    assertEquals(2, UnidicPreprocessor.charType("\u4DBF"));
  }

  @Test
  public void testCharTypeCJKCompatibilityIdeographs() {
    // CJK Compatibility Ideographs: U+F900–U+FAFF
    assertEquals(2, UnidicPreprocessor.charType("\uF900"));
    assertEquals(2, UnidicPreprocessor.charType("\uFAFF"));
  }

  @Test
  public void testCharTypeNumeric() {
    assertEquals(4, UnidicPreprocessor.charType("0"));
    assertEquals(4, UnidicPreprocessor.charType("9"));
    assertEquals(4, UnidicPreprocessor.charType("\uFF10"));  // fullwidth 0
    assertEquals(4, UnidicPreprocessor.charType("\uFF19"));  // fullwidth 9
  }

  @Test
  public void testCharTypeAlpha() {
    assertEquals(5, UnidicPreprocessor.charType("a"));
    assertEquals(5, UnidicPreprocessor.charType("z"));
    assertEquals(5, UnidicPreprocessor.charType("A"));
    assertEquals(5, UnidicPreprocessor.charType("Z"));
    assertEquals(5, UnidicPreprocessor.charType("\uFF41"));  // fullwidth a
    assertEquals(5, UnidicPreprocessor.charType("\uFF3A"));  // fullwidth Z
  }

  @Test
  public void testCharTypeHiragana() {
    assertEquals(6, UnidicPreprocessor.charType("あ"));
    assertEquals(6, UnidicPreprocessor.charType("ん"));
    assertEquals(6, UnidicPreprocessor.charType("\u3041"));  // ぁ small a
    assertEquals(6, UnidicPreprocessor.charType("\u3096"));  // ゖ
  }

  @Test
  public void testCharTypeKatakana() {
    assertEquals(7, UnidicPreprocessor.charType("ア"));
    assertEquals(7, UnidicPreprocessor.charType("ン"));
    assertEquals(7, UnidicPreprocessor.charType("\u30A1"));  // ァ small a
    assertEquals(7, UnidicPreprocessor.charType("\u30FA"));  // ヺ
    assertEquals(7, UnidicPreprocessor.charType("\u30FD"));  // ヽ
    assertEquals(7, UnidicPreprocessor.charType("\u30FE"));  // ヾ
  }

  @Test
  public void testCharTypeSymbol() {
    // Punctuation → 3 (SYMBOL / other)
    assertEquals(3, UnidicPreprocessor.charType("！"));
    assertEquals(3, UnidicPreprocessor.charType("@"));
    assertEquals(3, UnidicPreprocessor.charType("。"));
  }

  // -----------------------------------------------------------------------
  // Constants
  // -----------------------------------------------------------------------

  @Test
  public void testPosKeyConstants() {
    assertEquals("文頭,*,*,*,*,*,*", UnidicPreprocessor.BOS_POS_KEY);
    assertEquals("文末,*,*,*,*,*,*", UnidicPreprocessor.EOS_POS_KEY);
    assertEquals("名詞,サ変接続,*,*,*,*,*", UnidicPreprocessor.UNKNOWN_POS_KEY);
  }

  // -----------------------------------------------------------------------
  // parseCsvLine() — private method, tested via reflection
  // -----------------------------------------------------------------------

  private String[] parseCsvLine(String csv) throws Exception {
    // UnidicPreprocessor(File, CostCalculator) — pass nulls; we're not calling build()
    UnidicPreprocessor prep = new UnidicPreprocessor(null, null);
    Method m = UnidicPreprocessor.class.getDeclaredMethod("parseCsvLine", String.class);
    m.setAccessible(true);
    return (String[]) m.invoke(prep, csv);
  }

  @Test
  public void testParseCsvLineSimple() throws Exception {
    String[] fields = parseCsvLine("a,b,c");
    assertArrayEquals(new String[]{"a", "b", "c"}, fields);
  }

  @Test
  public void testParseCsvLineQuotedField() throws Exception {
    String[] fields = parseCsvLine("\"hello, world\",b");
    assertArrayEquals(new String[]{"hello, world", "b"}, fields);
  }

  @Test
  public void testParseCsvLineEscapedQuote() throws Exception {
    String[] fields = parseCsvLine("\"say \"\"hi\"\"\",b");
    assertArrayEquals(new String[]{"say \"hi\"", "b"}, fields);
  }

  @Test
  public void testParseCsvLineSingleField() throws Exception {
    String[] fields = parseCsvLine("only");
    assertArrayEquals(new String[]{"only"}, fields);
  }

  @Test
  public void testParseCsvLineEmptyString() throws Exception {
    String[] fields = parseCsvLine("");
    assertArrayEquals(new String[0], fields);
  }

  @Test
  public void testParseCsvLineEmptyFields() throws Exception {
    // Two commas → three fields, middle one empty
    String[] fields = parseCsvLine("a,,c");
    assertArrayEquals(new String[]{"a", "", "c"}, fields);
  }

  @Test
  public void testParseCsvLineMixedQuotedAndUnquoted() throws Exception {
    String[] fields = parseCsvLine("名詞,\"普通名詞\",*");
    assertArrayEquals(new String[]{"名詞", "普通名詞", "*"}, fields);
  }

  // -----------------------------------------------------------------------
  // build() — integration test with minimal in-memory dictionary files
  // -----------------------------------------------------------------------

  private File writeFakeDictDir() throws Exception {
    File dictDir = tmp.newFolder("unidic");

    // left-id.def: id <space> csv_fields (7 or more fields)
    // Format used by UnidicPreprocessor.loadIdDef()
    writeFile(new File(dictDir, "left-id.def"),
        "0 BOS,*,*,*,*,*,*,*,*,*,*,*,*,*,*\n" +
        "1 名詞,普通名詞,一般,*,*,*,*,*,*,*,*,*,*,*,*\n" +
        "2 動詞,一般,*,*,五段-カ行,連用形-一般,*,*,*,*,*,*,*,*,*\n");

    // right-id.def: same format
    writeFile(new File(dictDir, "right-id.def"),
        "0 BOS,*,*,*,*,*,*,*,*,*,*,*,*,*,*\n" +
        "1 名詞,普通名詞,一般,*,*,*,*,*,*,*,*,*,*,*,*\n" +
        "2 動詞,一般,*,*,五段-カ行,連用形-一般,*,*,*,*,*,*,*,*,*\n");

    // lex.csv: surface,leftId,rightId,wordCost,pos1,pos2,pos3,pos4,cType,cForm,
    //          lForm,lemma,orth,orthBase,pron,pronBase,...
    // That's 16 columns starting at index 0
    writeFile(new File(dictDir, "lex.csv"),
        "\"東京\",1,1,1234,名詞,固有名詞,地名,一般,*,*,*,東京,東京,東京,トーキョー,トーキョー\n" +
        "\"行く\",2,2,-500,動詞,一般,*,*,五段-カ行,連用形-一般,*,行く,行く,行く,イク,イク\n");

    // matrix.def: header line then "rightId leftId cost"
    writeFile(new File(dictDir, "matrix.def"),
        "3 3\n" +
        "0 0 0\n" +
        "0 1 100\n" +
        "0 2 200\n" +
        "1 0 300\n" +
        "1 1 50\n" +
        "1 2 150\n" +
        "2 0 400\n" +
        "2 1 75\n" +
        "2 2 25\n");

    return dictDir;
  }

  private void writeFile(File f, String content) throws Exception {
    try (OutputStreamWriter w = new OutputStreamWriter(
        new FileOutputStream(f), StandardCharsets.UTF_8)) {
      w.write(content);
    }
  }

  @Test
  public void testBuildProducesDictionaryAndConnectionCsv() throws Exception {
    File dictDir   = writeFakeDictDir();
    File outputDir = tmp.newFolder("output");

    UnidicPreprocessor prep = new UnidicPreprocessor(dictDir, null);
    prep.build(outputDir);

    File dictCsv = new File(outputDir, "dictionary.csv");
    File connCsv = new File(outputDir, "connection.csv");

    assertTrue("dictionary.csv must be created", dictCsv.exists());
    assertTrue("connection.csv must be created", connCsv.exists());
    assertTrue("dictionary.csv must not be empty", dictCsv.length() > 0);
    assertTrue("connection.csv must not be empty", connCsv.length() > 0);
  }

  @Test
  public void testBuildDictionaryCsvContainsExpectedWords() throws Exception {
    File dictDir   = writeFakeDictDir();
    File outputDir = tmp.newFolder("output2");

    UnidicPreprocessor prep = new UnidicPreprocessor(dictDir, null);
    prep.build(outputDir);

    String content = readFile(new File(outputDir, "dictionary.csv"));
    assertTrue("dictionary.csv should contain 東京", content.contains("東京"));
    assertTrue("dictionary.csv should contain 行く", content.contains("行く"));
  }

  @Test
  public void testBuildConnectionCsvContainsCatchAll() throws Exception {
    File dictDir   = writeFakeDictDir();
    File outputDir = tmp.newFolder("output3");

    UnidicPreprocessor prep = new UnidicPreprocessor(dictDir, null);
    prep.build(outputDir);

    String content = readFile(new File(outputDir, "connection.csv"));
    // The wildcard catch-all row is always written first
    assertTrue("connection.csv should contain wildcard row",
        content.contains("\"*,*,*,*,*,*,*\""));
  }

  @Test
  public void testBuildCreatesOutputDir() throws Exception {
    File dictDir   = writeFakeDictDir();
    File outputDir = new File(tmp.getRoot(), "auto-created");
    assertFalse(outputDir.exists());

    UnidicPreprocessor prep = new UnidicPreprocessor(dictDir, null);
    prep.build(outputDir);

    assertTrue("outputDir should be created automatically", outputDir.exists());
    assertTrue(new File(outputDir, "dictionary.csv").exists());
  }

  private String readFile(File f) throws Exception {
    byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
    return new String(bytes, StandardCharsets.UTF_8);
  }

  // -----------------------------------------------------------------------
  // Custom dictionary integration tests
  // -----------------------------------------------------------------------

  @Test
  public void testCustomDicEntriesAppendedToDictionaryCsv() throws Exception {
    File dictDir   = writeFakeDictDir();
    File outputDir = tmp.newFolder("custom-output");

    // Write a custom dictionary file
    File customDic = tmp.newFile("custom.csv");
    writeFile(customDic,
        "バラク,0,0,0,名詞,固有名詞,人名,名,*,*,バラク,バラク,バラク\n" +
        "オバマ,0,0,0,名詞,固有名詞,人名,姓,*,*,オバマ,オバマ,オバマ\n");

    UnidicPreprocessor prep = new UnidicPreprocessor(
        dictDir, null, Collections.singletonList(customDic));
    prep.build(outputDir);

    String content = readFile(new File(outputDir, "dictionary.csv"));
    // Original entries still present
    assertTrue("should contain 東京", content.contains("東京"));
    assertTrue("should contain 行く", content.contains("行く"));
    // Custom entries appended
    assertTrue("should contain バラク", content.contains("バラク"));
    assertTrue("should contain オバマ", content.contains("オバマ"));
  }

  @Test
  public void testCustomDicRawCostUsedWithoutModel() throws Exception {
    File dictDir   = writeFakeDictDir();
    File outputDir = tmp.newFolder("custom-raw");

    File customDic = tmp.newFile("custom-raw.csv");
    writeFile(customDic,
        "テスト,0,0,5000,名詞,普通名詞,一般,*,*,*,テスト,テスト,テスト\n");

    UnidicPreprocessor prep = new UnidicPreprocessor(
        dictDir, null, Collections.singletonList(customDic));
    prep.build(outputDir);

    String content = readFile(new File(outputDir, "dictionary.csv"));
    // The raw cost 5000 should be used as-is
    assertTrue("should contain テスト with raw cost",
        content.contains("テスト,5000,"));
  }

  @Test
  public void testCustomDicCostRecomputedWithModel() throws Exception {
    File dictDir   = writeFakeDictDir();
    File outputDir = tmp.newFolder("custom-model");

    // Set up a simple CRF model: weight 1.0 for "A:名詞" unigram
    File featureFile = tmp.newFile("feature.def");
    writeFile(featureFile, "UNIGRAM A:%F[0]\n");

    File modelFile = tmp.newFile("model.def");
    writeFile(modelFile, "\n1.0\tA:名詞\n");

    FeatureTemplateParser parser = new FeatureTemplateParser();
    parser.load(featureFile);
    ModelWeightIndex model = new ModelWeightIndex();
    model.load(modelFile);
    FeatureExtractor extractor = new FeatureExtractor(parser);
    CostCalculator calc = new CostCalculator(extractor, model, 700);

    File customDic = tmp.newFile("custom-model.csv");
    // Raw cost is 0, but CRF should compute 700 (1.0 * 700)
    writeFile(customDic,
        "バラク,0,0,0,名詞,固有名詞,人名,名,*,*,バラク,バラク,バラク\n");

    UnidicPreprocessor prep = new UnidicPreprocessor(
        dictDir, calc, Collections.singletonList(customDic));
    prep.build(outputDir);

    String content = readFile(new File(outputDir, "dictionary.csv"));
    // CRF-computed cost: round(1.0 * 700) = 700
    assertTrue("custom entry should have CRF-computed cost 700",
        content.contains("バラク,700,"));
  }

  @Test
  public void testMultipleCustomDicFiles() throws Exception {
    File dictDir   = writeFakeDictDir();
    File outputDir = tmp.newFolder("multi-custom");

    File customDic1 = tmp.newFile("custom1.csv");
    writeFile(customDic1,
        "バラク,0,0,0,名詞,固有名詞,人名,名,*,*,バラク,バラク,バラク\n");

    File customDic2 = tmp.newFile("custom2.csv");
    writeFile(customDic2,
        "オバマ,0,0,0,名詞,固有名詞,人名,姓,*,*,オバマ,オバマ,オバマ\n");

    UnidicPreprocessor prep = new UnidicPreprocessor(
        dictDir, null, Arrays.asList(customDic1, customDic2));
    prep.build(outputDir);

    String content = readFile(new File(outputDir, "dictionary.csv"));
    assertTrue("should contain バラク from first custom dic", content.contains("バラク"));
    assertTrue("should contain オバマ from second custom dic", content.contains("オバマ"));
  }

  @Test
  public void testEmptyCustomDicListProducesSameOutput() throws Exception {
    File dictDir    = writeFakeDictDir();
    File outputDir1 = tmp.newFolder("no-custom");
    File outputDir2 = tmp.newFolder("empty-custom");

    new UnidicPreprocessor(dictDir, null).build(outputDir1);
    new UnidicPreprocessor(dictDir, null, Collections.<File>emptyList()).build(outputDir2);

    String dict1 = readFile(new File(outputDir1, "dictionary.csv"));
    String dict2 = readFile(new File(outputDir2, "dictionary.csv"));
    assertEquals("empty custom list should produce identical output", dict1, dict2);
  }
}
