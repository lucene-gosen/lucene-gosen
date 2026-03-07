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

package net.java.sen.trainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Expands MeCab feature templates against token feature arrays, producing
 * the feature strings used as keys in {@link ModelWeightIndex}.
 *
 * <h2>Expansion rules</h2>
 * <ul>
 *   <li>Each placeholder ({@code %F[n]}, {@code %L[n]}, etc.) is replaced
 *       by the corresponding element of the supplied feature array.</li>
 *   <li>Mandatory placeholders ({@code %F[n]}) are always included.</li>
 *   <li>Optional placeholders ({@code %F?[n]}) and their immediately
 *       preceding literal separator are <em>omitted</em> when the field
 *       value is {@code *}.</li>
 *   <li>The expanded string is prefixed with {@code "<name>:"}.</li>
 *   <li>If nothing is appended after the prefix, the result is {@code null}
 *       (template produces no feature for this token).</li>
 * </ul>
 *
 * <h2>Bigram template context</h2>
 * For BIGRAM templates:
 * <ul>
 *   <li>{@code L[]} is the R[] array of the <em>left</em> (preceding) token
 *       (from {@code right-id.def}).</li>
 *   <li>{@code R[]} is the L[] array of the <em>right</em> (following) token
 *       (from {@code left-id.def}).</li>
 * </ul>
 */
public class FeatureExtractor {

  private final List<FeatureTemplateParser.Template> unigramTemplates;
  private final List<FeatureTemplateParser.Template> bigramTemplates;

  public FeatureExtractor(FeatureTemplateParser parser) {
    this.unigramTemplates = parser.getUnigramTemplates();
    this.bigramTemplates  = parser.getBigramTemplates();
  }

  /**
   * Expands all unigram templates for a single token.
   *
   * @param features F[] array: lex.csv columns 4-19 (F[0]=pos1 … F[15]=aModType)
   * @param charType MeCab character type (0-7) of the surface form
   * @return list of non-null expanded feature strings
   */
  public List<String> extractUnigram(String[] features, int charType) {
    List<String> result = new ArrayList<>(unigramTemplates.size());
    for (FeatureTemplateParser.Template tmpl : unigramTemplates) {
      String s = expand(tmpl.name, tmpl.pattern, features, null, null, charType);
      if (s != null) {
        result.add(s);
      }
    }
    return result;
  }

  /**
   * Expands all bigram templates for a (left, right) token pair.
   *
   * @param leftRightFeatures R[] of the left (preceding) token, from right-id.def
   * @param rightLeftFeatures L[] of the right (following) token, from left-id.def
   * @return list of non-null expanded feature strings
   */
  public List<String> extractBigram(String[] leftRightFeatures, String[] rightLeftFeatures) {
    List<String> result = new ArrayList<>(bigramTemplates.size());
    for (FeatureTemplateParser.Template tmpl : bigramTemplates) {
      String s = expand(tmpl.name, tmpl.pattern, null, leftRightFeatures, rightLeftFeatures, 0);
      if (s != null) {
        result.add(s);
      }
    }
    return result;
  }

  /**
   * Expands a single template pattern.
   *
   * <p>The algorithm accumulates literal characters in {@code pendingSep} before
   * each placeholder. When a mandatory placeholder is encountered the pending
   * separator is flushed unconditionally. When an optional placeholder is
   * encountered it is flushed only if the field value is not {@code *};
   * otherwise the pending separator is discarded.
   *
   * @param name      template name (becomes the prefix before {@code :})
   * @param pattern   raw pattern string
   * @param f         F[] array (unigram), may be null for bigrams
   * @param l         L[] array (bigram left context / right-id.def features)
   * @param r         R[] array (bigram right context / left-id.def features)
   * @param charType  character type for {@code %t}
   * @return expanded feature string, or {@code null} if no content was produced
   */
  String expand(String name, String pattern,
                String[] f, String[] l, String[] r,
                int charType) {

    StringBuilder sb         = new StringBuilder(64);
    StringBuilder pendingSep = new StringBuilder(4);

    sb.append(name).append(':');
    final int prefixLen = sb.length();

    int i = 0;
    while (i < pattern.length()) {
      char c = pattern.charAt(i);

      if (c != '%') {
        // Literal character: accumulate as a potential separator
        pendingSep.append(c);
        i++;
        continue;
      }

      // Consume '%'
      i++;
      if (i >= pattern.length()) break;

      char typeChar = pattern.charAt(i++);

      // Handle %t (character type)
      if (typeChar == 't') {
        sb.append(pendingSep);
        pendingSep.setLength(0);
        sb.append(charType);
        continue;
      }

      // Detect optional flag: %F? vs %F
      boolean optional = (i < pattern.length() && pattern.charAt(i) == '?');
      if (optional) i++;

      // Parse bracketed index: [n]
      if (i >= pattern.length() || pattern.charAt(i) != '[') continue;
      int closeIdx = pattern.indexOf(']', i + 1);
      if (closeIdx < 0) continue;
      int fieldIdx = Integer.parseInt(pattern.substring(i + 1, closeIdx));
      i = closeIdx + 1;

      // Select the right feature array
      String[] arr;
      if (typeChar == 'F') {
        arr = f;
      } else if (typeChar == 'L') {
        arr = l;
      } else if (typeChar == 'R') {
        arr = r;
      } else {
        // Unknown type: skip
        pendingSep.setLength(0);
        continue;
      }

      String val = (arr != null && fieldIdx < arr.length && arr[fieldIdx] != null)
          ? arr[fieldIdx] : "*";

      if (optional) {
        if (!"*".equals(val) && !val.isEmpty()) {
          sb.append(pendingSep);
          pendingSep.setLength(0);
          sb.append(val);
        } else {
          // Discard the preceding separator — this optional field is suppressed
          pendingSep.setLength(0);
        }
      } else {
        // Mandatory: always include
        sb.append(pendingSep);
        pendingSep.setLength(0);
        sb.append(val);
      }
    }

    // Return null if nothing was produced beyond the "name:" prefix
    return (sb.length() > prefixLen) ? sb.toString() : null;
  }
}
