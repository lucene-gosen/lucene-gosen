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

import static org.junit.Assert.*;

public class CostCalculatorTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private FeatureExtractor makeExtractor(String featureDef) throws Exception {
    File f = tmp.newFile("feature.def");
    try (OutputStreamWriter w = new OutputStreamWriter(
        new FileOutputStream(f), StandardCharsets.UTF_8)) {
      w.write(featureDef);
    }
    FeatureTemplateParser parser = new FeatureTemplateParser();
    parser.load(f);
    return new FeatureExtractor(parser);
  }

  private ModelWeightIndex makeModel(String modelContent) throws Exception {
    File f = tmp.newFile("model.def");
    try (OutputStreamWriter w = new OutputStreamWriter(
        new FileOutputStream(f), StandardCharsets.UTF_8)) {
      w.write(modelContent);
    }
    ModelWeightIndex model = new ModelWeightIndex();
    model.load(f);
    return model;
  }

  @Test
  public void testWordCostWithKnownFeature() throws Exception {
    FeatureExtractor extractor = makeExtractor("UNIGRAM A:%F[0]\n");
    ModelWeightIndex model = makeModel("\n1.0\tA:名詞\n");
    CostCalculator calc = new CostCalculator(extractor, model, 700);

    // sum = 1.0, cost = round(1.0 * 700) = 700
    int cost = calc.computeWordCost(new String[]{"名詞"}, 2);
    assertEquals(700, cost);
  }

  @Test
  public void testWordCostWithUnknownFeatureIsZero() throws Exception {
    FeatureExtractor extractor = makeExtractor("UNIGRAM A:%F[0]\n");
    ModelWeightIndex model = makeModel("\n");
    CostCalculator calc = new CostCalculator(extractor, model, 700);

    int cost = calc.computeWordCost(new String[]{"名詞"}, 2);
    assertEquals(0, cost);
  }

  @Test
  public void testWordCostSumsMultipleFeatures() throws Exception {
    FeatureExtractor extractor = makeExtractor(
        "UNIGRAM A:%F[0]\nUNIGRAM B:%F[1]\n");
    ModelWeightIndex model = makeModel("\n1.0\tA:名詞\n0.5\tB:普通名詞\n");
    CostCalculator calc = new CostCalculator(extractor, model, 700);

    // sum = 1.0 + 0.5 = 1.5, cost = round(1.5 * 700) = 1050
    int cost = calc.computeWordCost(new String[]{"名詞", "普通名詞"}, 2);
    assertEquals(1050, cost);
  }

  @Test
  public void testWordCostRoundsCorrectly() throws Exception {
    FeatureExtractor extractor = makeExtractor("UNIGRAM A:%F[0]\n");
    // 0.0015 * 700 = 1.05 → rounds to 1
    ModelWeightIndex model = makeModel("\n0.0015\tA:x\n");
    CostCalculator calc = new CostCalculator(extractor, model, 700);
    int cost = calc.computeWordCost(new String[]{"x"}, 0);
    assertEquals(1, cost);
  }

  @Test
  public void testNegativeWordCost() throws Exception {
    FeatureExtractor extractor = makeExtractor("UNIGRAM A:%F[0]\n");
    ModelWeightIndex model = makeModel("\n-2.0\tA:動詞\n");
    CostCalculator calc = new CostCalculator(extractor, model, 700);

    // sum = -2.0, cost = round(-2.0 * 700) = -1400
    int cost = calc.computeWordCost(new String[]{"動詞"}, 2);
    assertEquals(-1400, cost);
  }

  @Test
  public void testConnectionCostWithKnownBigramFeature() throws Exception {
    FeatureExtractor extractor = makeExtractor("BIGRAM B:%L[0]/%R[0]\n");
    ModelWeightIndex model = makeModel("\n0.5\tB:動詞/助詞\n");
    CostCalculator calc = new CostCalculator(extractor, model, 700);

    // sum = 0.5, cost = round(0.5 * 700) = 350
    int cost = calc.computeConnectionCost(new String[]{"動詞"}, new String[]{"助詞"});
    assertEquals(350, cost);
  }

  @Test
  public void testConnectionCostUnknownFeatureIsZero() throws Exception {
    FeatureExtractor extractor = makeExtractor("BIGRAM B:%L[0]/%R[0]\n");
    ModelWeightIndex model = makeModel("\n");
    CostCalculator calc = new CostCalculator(extractor, model, 700);

    int cost = calc.computeConnectionCost(new String[]{"動詞"}, new String[]{"助詞"});
    assertEquals(0, cost);
  }

  @Test
  public void testConnectionCostSumsMultipleFeatures() throws Exception {
    FeatureExtractor extractor = makeExtractor(
        "BIGRAM B1:%L[0]/%R[0]\nBIGRAM B2:%L[1]/%R[1]\n");
    ModelWeightIndex model = makeModel("\n0.3\tB1:動詞/助詞\n0.2\tB2:一般/格助詞\n");
    CostCalculator calc = new CostCalculator(extractor, model, 700);

    // sum = 0.3 + 0.2 = 0.5, cost = round(0.5 * 700) = 350
    int cost = calc.computeConnectionCost(
        new String[]{"動詞", "一般"},
        new String[]{"助詞", "格助詞"});
    assertEquals(350, cost);
  }

  @Test
  public void testCostFactorScalesResult() throws Exception {
    FeatureExtractor extractor = makeExtractor("UNIGRAM A:%F[0]\n");
    ModelWeightIndex model = makeModel("\n1.0\tA:x\n");

    CostCalculator calc100 = new CostCalculator(extractor, model, 100);
    CostCalculator calc700 = new CostCalculator(extractor, model, 700);

    assertEquals(100, calc100.computeWordCost(new String[]{"x"}, 0));
    assertEquals(700, calc700.computeWordCost(new String[]{"x"}, 0));
  }

  @Test
  public void testEmptyTemplateListProducesZeroCost() throws Exception {
    // No templates defined → no features extracted → sum = 0 → cost = 0
    FeatureExtractor extractor = makeExtractor("");
    ModelWeightIndex model = makeModel("\n1.0\tA:x\n");
    CostCalculator calc = new CostCalculator(extractor, model, 700);

    assertEquals(0, calc.computeWordCost(new String[]{"x"}, 0));
    assertEquals(0, calc.computeConnectionCost(new String[]{"x"}, new String[]{"y"}));
  }
}
