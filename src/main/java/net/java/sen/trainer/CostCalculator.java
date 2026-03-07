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

import java.util.List;

/**
 * Converts CRF feature weights into the integer word costs and connection costs
 * used by the Sen dictionary.
 *
 * <h2>MeCab cost formula</h2>
 * <pre>
 *   wordCost(token)           = round(Σ α[f]  for f ∈ unigramFeatures(token))  × costFactor
 *   connectionCost(left,right)= round(Σ α[f]  for f ∈ bigramFeatures(left,right)) × costFactor
 * </pre>
 *
 * where {@code α[f]} is the weight for feature string {@code f} from
 * {@link ModelWeightIndex} and {@code costFactor} is the integer scaling factor
 * declared in {@code dicrc} (typically {@code 700} for UniDic).
 */
public class CostCalculator {

  private final FeatureExtractor extractor;
  private final ModelWeightIndex model;
  private final int costFactor;

  /**
   * @param extractor  expands feature templates for tokens / token pairs
   * @param model      provides alpha weights for feature strings
   * @param costFactor integer multiplier (from {@code dicrc cost-factor}, e.g. 700)
   */
  public CostCalculator(FeatureExtractor extractor, ModelWeightIndex model, int costFactor) {
    this.extractor  = extractor;
    this.model      = model;
    this.costFactor = costFactor;
  }

  /**
   * Computes the integer word cost for a token using its unigram CRF features.
   *
   * @param features F[] array: lex.csv columns 4-19 (indices 0-15)
   * @param charType MeCab character type (0-7) of the surface form
   * @return integer word cost (may be negative)
   */
  public int computeWordCost(String[] features, int charType) {
    List<String> feats = extractor.extractUnigram(features, charType);
    double sum = 0.0;
    for (String feat : feats) {
      sum += model.getWeight(feat);
    }
    return (int) Math.round(sum * costFactor);
  }

  /**
   * Computes the integer connection cost between two adjacent tokens using
   * their bigram CRF features.
   *
   * @param leftRightFeatures  R[] features of the LEFT (preceding) token,
   *                           obtained from {@code right-id.def[word.rightId]}
   * @param rightLeftFeatures  L[] features of the RIGHT (following) token,
   *                           obtained from {@code left-id.def[word.leftId]}
   * @return integer connection cost (may be negative)
   */
  public int computeConnectionCost(String[] leftRightFeatures, String[] rightLeftFeatures) {
    List<String> feats = extractor.extractBigram(leftRightFeatures, rightLeftFeatures);
    double sum = 0.0;
    for (String feat : feats) {
      sum += model.getWeight(feat);
    }
    return (int) Math.round(sum * costFactor);
  }
}
