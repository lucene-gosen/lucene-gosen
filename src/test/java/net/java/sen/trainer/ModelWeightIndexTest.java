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
import java.util.Map;

import static org.junit.Assert.*;

public class ModelWeightIndexTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private File writeModelDef(String content) throws Exception {
    File f = tmp.newFile("model.def");
    try (OutputStreamWriter w = new OutputStreamWriter(
        new FileOutputStream(f), StandardCharsets.UTF_8)) {
      w.write(content);
    }
    return f;
  }

  @Test
  public void testEmptyFile() throws Exception {
    File f = writeModelDef("");
    ModelWeightIndex model = new ModelWeightIndex();
    model.load(f);
    assertEquals(0, model.size());
    assertEquals(0.0, model.getWeight("anything"), 0.0);
  }

  @Test
  public void testHeaderParsed() throws Exception {
    String content =
        "eta: 0.001\n" +
        "freq: 5\n" +
        "C: 1.0\n" +
        "eval-size: 8\n" +
        "unk-eval-size: 4\n" +
        "charset: UTF-8\n" +
        "\n" +
        "1.5\tA:名詞\n";
    File f = writeModelDef(content);
    ModelWeightIndex model = new ModelWeightIndex();
    model.load(f);

    Map<String, String> header = model.getHeader();
    assertEquals("0.001", header.get("eta"));
    assertEquals("5", header.get("freq"));
    assertEquals("UTF-8", header.get("charset"));
  }

  @Test
  public void testWeightLoaded() throws Exception {
    String content =
        "C: 1.0\n" +
        "\n" +
        "1.5\tA:名詞\n" +
        "-0.3\tB:動詞\n";
    File f = writeModelDef(content);
    ModelWeightIndex model = new ModelWeightIndex();
    model.load(f);

    assertEquals(2, model.size());
    assertEquals(1.5, model.getWeight("A:名詞"), 1e-9);
    assertEquals(-0.3, model.getWeight("B:動詞"), 1e-9);
  }

  @Test
  public void testUnknownFeatureReturnsZero() throws Exception {
    File f = writeModelDef("\n1.5\tA:名詞\n");
    ModelWeightIndex model = new ModelWeightIndex();
    model.load(f);
    assertEquals(0.0, model.getWeight("UNKNOWN:feature"), 0.0);
  }

  @Test
  public void testMalformedWeightLineSkipped() throws Exception {
    String content =
        "\n" +
        "not-a-number\tA:feature\n" +
        "2.0\tB:valid\n";
    File f = writeModelDef(content);
    ModelWeightIndex model = new ModelWeightIndex();
    model.load(f);
    assertEquals(1, model.size());
    assertEquals(2.0, model.getWeight("B:valid"), 1e-9);
  }

  @Test
  public void testLineWithoutTabSkipped() throws Exception {
    String content =
        "\n" +
        "no-tab-here\n" +
        "1.0\tgood:feature\n";
    File f = writeModelDef(content);
    ModelWeightIndex model = new ModelWeightIndex();
    model.load(f);
    assertEquals(1, model.size());
    assertEquals(1.0, model.getWeight("good:feature"), 1e-9);
  }

  @Test
  public void testBlankWeightLinesSkipped() throws Exception {
    String content = "\n\n1.0\tA:f\n\n2.0\tB:g\n\n";
    File f = writeModelDef(content);
    ModelWeightIndex model = new ModelWeightIndex();
    model.load(f);
    assertEquals(2, model.size());
  }

  @Test
  public void testGetCostFactor() throws Exception {
    ModelWeightIndex model = new ModelWeightIndex();
    assertEquals(700, model.getCostFactor());
  }

  @Test
  public void testGetHeaderReturnsUnmodifiableView() throws Exception {
    File f = writeModelDef("C: 1.0\n\n");
    ModelWeightIndex model = new ModelWeightIndex();
    model.load(f);
    try {
      model.getHeader().clear();
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  @Test
  public void testDuplicateFeatureLaterValueWins() throws Exception {
    String content = "\n1.0\tA:f\n2.0\tA:f\n";
    File f = writeModelDef(content);
    ModelWeightIndex model = new ModelWeightIndex();
    model.load(f);
    // later value should override
    assertEquals(2.0, model.getWeight("A:f"), 1e-9);
  }

  @Test
  public void testNegativeWeight() throws Exception {
    File f = writeModelDef("\n-5.25\tA:negative\n");
    ModelWeightIndex model = new ModelWeightIndex();
    model.load(f);
    assertEquals(-5.25, model.getWeight("A:negative"), 1e-9);
  }
}
