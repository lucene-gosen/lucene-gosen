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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class FeatureTemplateParserTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private File writeFeatureDef(String content) throws Exception {
    File f = tmp.newFile("feature.def");
    try (OutputStreamWriter w = new OutputStreamWriter(
        new FileOutputStream(f), StandardCharsets.UTF_8)) {
      w.write(content);
    }
    return f;
  }

  @Test
  public void testEmptyFile() throws Exception {
    File f = writeFeatureDef("");
    FeatureTemplateParser parser = new FeatureTemplateParser();
    parser.load(f);
    assertTrue(parser.getUnigramTemplates().isEmpty());
    assertTrue(parser.getBigramTemplates().isEmpty());
  }

  @Test
  public void testCommentsAndBlankLinesIgnored() throws Exception {
    File f = writeFeatureDef("# this is a comment\n\n  \n# another comment\n");
    FeatureTemplateParser parser = new FeatureTemplateParser();
    parser.load(f);
    assertTrue(parser.getUnigramTemplates().isEmpty());
    assertTrue(parser.getBigramTemplates().isEmpty());
  }

  @Test
  public void testSingleUnigramTemplate() throws Exception {
    File f = writeFeatureDef("UNIGRAM A_A01:%F[0]/%F[1]\n");
    FeatureTemplateParser parser = new FeatureTemplateParser();
    parser.load(f);

    List<FeatureTemplateParser.Template> uni = parser.getUnigramTemplates();
    assertEquals(1, uni.size());
    assertEquals("A_A01", uni.get(0).name);
    assertEquals("%F[0]/%F[1]", uni.get(0).pattern);
    assertFalse(uni.get(0).bigram);
    assertTrue(parser.getBigramTemplates().isEmpty());
  }

  @Test
  public void testSingleBigramTemplate() throws Exception {
    File f = writeFeatureDef("BIGRAM B_A01:%L[0]/%R[0]\n");
    FeatureTemplateParser parser = new FeatureTemplateParser();
    parser.load(f);

    List<FeatureTemplateParser.Template> bi = parser.getBigramTemplates();
    assertEquals(1, bi.size());
    assertEquals("B_A01", bi.get(0).name);
    assertEquals("%L[0]/%R[0]", bi.get(0).pattern);
    assertTrue(bi.get(0).bigram);
    assertTrue(parser.getUnigramTemplates().isEmpty());
  }

  @Test
  public void testMultipleTemplates() throws Exception {
    String content =
        "# header comment\n" +
        "UNIGRAM U1:%F[0]\n" +
        "UNIGRAM U2:%F[0]/%F[1]\n" +
        "\n" +
        "# bigrams\n" +
        "BIGRAM  B1:%L[0]/%R[0]\n" +
        "BIGRAM  B2:%L[0]/%L[1]/%R[0]\n";
    File f = writeFeatureDef(content);
    FeatureTemplateParser parser = new FeatureTemplateParser();
    parser.load(f);

    assertEquals(2, parser.getUnigramTemplates().size());
    assertEquals(2, parser.getBigramTemplates().size());
    assertEquals("U1", parser.getUnigramTemplates().get(0).name);
    assertEquals("U2", parser.getUnigramTemplates().get(1).name);
    assertEquals("B1", parser.getBigramTemplates().get(0).name);
    assertEquals("B2", parser.getBigramTemplates().get(1).name);
  }

  @Test
  public void testLineWithoutColonIsSkipped() throws Exception {
    File f = writeFeatureDef("UNIGRAM noColon\nUNIGRAM valid:pattern\n");
    FeatureTemplateParser parser = new FeatureTemplateParser();
    parser.load(f);
    // "noColon" has no ':' so it should be skipped; only "valid" survives
    assertEquals(1, parser.getUnigramTemplates().size());
    assertEquals("valid", parser.getUnigramTemplates().get(0).name);
  }

  @Test
  public void testGetTemplatesReturnsUnmodifiableView() throws Exception {
    File f = writeFeatureDef("UNIGRAM A:%F[0]\n");
    FeatureTemplateParser parser = new FeatureTemplateParser();
    parser.load(f);
    try {
      parser.getUnigramTemplates().clear();
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }
    try {
      parser.getBigramTemplates().clear();
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  @Test
  public void testPatternPreservesColonInValue() throws Exception {
    // Pattern itself may contain colons after the name:pattern split
    File f = writeFeatureDef("UNIGRAM X:%F[0]:%F[1]\n");
    FeatureTemplateParser parser = new FeatureTemplateParser();
    parser.load(f);
    // The split is at first colon only; pattern should be "%F[0]:%F[1]"
    assertEquals("%F[0]:%F[1]", parser.getUnigramTemplates().get(0).pattern);
  }
}
