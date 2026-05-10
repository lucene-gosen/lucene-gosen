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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class FeatureExtractorTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private FeatureExtractor extractor;

  private FeatureExtractor extractorFromDef(String content) throws Exception {
    File f = tmp.newFile("feature.def");
    try (OutputStreamWriter w = new OutputStreamWriter(
        new FileOutputStream(f), StandardCharsets.UTF_8)) {
      w.write(content);
    }
    FeatureTemplateParser parser = new FeatureTemplateParser();
    parser.load(f);
    return new FeatureExtractor(parser);
  }

  @Test
  public void testMandatoryFieldExpanded() throws Exception {
    FeatureExtractor ex = extractorFromDef("UNIGRAM T:%F[0]\n");
    String[] features = {"名詞", "普通名詞", "*", "*", "*", "*"};
    List<String> result = ex.extractUnigram(features, 2);
    assertEquals(1, result.size());
    assertEquals("T:名詞", result.get(0));
  }

  @Test
  public void testMandatoryFieldOutOfBoundsBecomesWildcard() throws Exception {
    FeatureExtractor ex = extractorFromDef("UNIGRAM T:%F[10]\n");
    String[] features = {"名詞"};  // index 10 doesn't exist
    List<String> result = ex.extractUnigram(features, 2);
    assertEquals(1, result.size());
    assertEquals("T:*", result.get(0));
  }

  @Test
  public void testOptionalFieldOmittedWhenWildcard() throws Exception {
    // When the field value is "*", the optional placeholder and its separator are dropped
    FeatureExtractor ex = extractorFromDef("UNIGRAM T:%F[0]/%F?[1]\n");
    String[] features = {"名詞", "*"};
    List<String> result = ex.extractUnigram(features, 2);
    assertEquals(1, result.size());
    // The "/" before %F?[1] should be suppressed along with the "*" field
    assertEquals("T:名詞", result.get(0));
  }

  @Test
  public void testOptionalFieldIncludedWhenNotWildcard() throws Exception {
    FeatureExtractor ex = extractorFromDef("UNIGRAM T:%F[0]/%F?[1]\n");
    String[] features = {"名詞", "普通名詞"};
    List<String> result = ex.extractUnigram(features, 2);
    assertEquals(1, result.size());
    assertEquals("T:名詞/普通名詞", result.get(0));
  }

  @Test
  public void testCharTypeExpanded() throws Exception {
    FeatureExtractor ex = extractorFromDef("UNIGRAM T:%t\n");
    List<String> result = ex.extractUnigram(new String[0], 6);
    assertEquals(1, result.size());
    assertEquals("T:6", result.get(0));
  }

  @Test
  public void testMultipleMandatoryFields() throws Exception {
    FeatureExtractor ex = extractorFromDef("UNIGRAM T:%F[0]/%F[1]/%F[2]\n");
    String[] features = {"動詞", "一般", "五段-カ行"};
    List<String> result = ex.extractUnigram(features, 2);
    assertEquals("T:動詞/一般/五段-カ行", result.get(0));
  }

  @Test
  public void testNullResultWhenNothingProducedBeyondPrefix() throws Exception {
    // Template with only optional fields that are all "*" → null → excluded from result
    FeatureExtractor ex = extractorFromDef("UNIGRAM T:%F?[0]\n");
    String[] features = {"*"};
    List<String> result = ex.extractUnigram(features, 2);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testMultipleTemplatesInResult() throws Exception {
    FeatureExtractor ex = extractorFromDef(
        "UNIGRAM A:%F[0]\nUNIGRAM B:%F[0]/%F[1]\n");
    String[] features = {"名詞", "固有名詞"};
    List<String> result = ex.extractUnigram(features, 2);
    assertEquals(2, result.size());
    assertEquals("A:名詞", result.get(0));
    assertEquals("B:名詞/固有名詞", result.get(1));
  }

  @Test
  public void testBigramLRFields() throws Exception {
    FeatureExtractor ex = extractorFromDef("BIGRAM B:%L[0]/%R[0]\n");
    String[] left  = {"動詞", "五段"};
    String[] right = {"助詞", "格助詞"};
    List<String> result = ex.extractBigram(left, right);
    assertEquals(1, result.size());
    assertEquals("B:動詞/助詞", result.get(0));
  }

  @Test
  public void testBigramWithMultipleFields() throws Exception {
    FeatureExtractor ex = extractorFromDef("BIGRAM B:%L[0]/%L[1]/%R[0]/%R[1]\n");
    String[] left  = {"動詞", "五段"};
    String[] right = {"助詞", "格助詞"};
    List<String> result = ex.extractBigram(left, right);
    assertEquals("B:動詞/五段/助詞/格助詞", result.get(0));
  }

  @Test
  public void testBigramNullArraysProduceWildcard() throws Exception {
    FeatureExtractor ex = extractorFromDef("BIGRAM B:%L[0]/%R[0]\n");
    List<String> result = ex.extractBigram(null, null);
    assertEquals("B:*/*", result.get(0));
  }

  @Test
  public void testExpandUnknownTypeChar() throws Exception {
    // An unknown type char (e.g. 'X') should be skipped — pendingSep is cleared
    FeatureExtractor ex = extractorFromDef("UNIGRAM T:%X[0]\n");
    String[] features = {"名詞"};
    List<String> result = ex.extractUnigram(features, 2);
    // Nothing produced beyond the prefix
    assertTrue(result.isEmpty());
  }

  @Test
  public void testExpandDirectInternalMethod() throws Exception {
    // Test the package-private expand() directly
    FeatureExtractor ex = extractorFromDef("");
    String result = ex.expand("A", "%F[0]/%F[1]",
        new String[]{"名詞", "普通名詞"}, null, null, 0);
    assertEquals("A:名詞/普通名詞", result);
  }

  @Test
  public void testExpandReturnsNullForEmptyContent() throws Exception {
    FeatureExtractor ex = extractorFromDef("");
    // Empty pattern → only prefix, so result should be null
    String result = ex.expand("A", "", new String[]{"名詞"}, null, null, 0);
    assertNull(result);
  }

  @Test
  public void testExtractBigramEmptyTemplateList() throws Exception {
    FeatureExtractor ex = extractorFromDef("UNIGRAM U:%F[0]\n");
    List<String> result = ex.extractBigram(new String[]{"a"}, new String[]{"b"});
    assertTrue(result.isEmpty());
  }

  @Test
  public void testExtractUnigramEmptyTemplateList() throws Exception {
    FeatureExtractor ex = extractorFromDef("BIGRAM B:%L[0]\n");
    List<String> result = ex.extractUnigram(new String[]{"a"}, 0);
    assertTrue(result.isEmpty());
  }
}
