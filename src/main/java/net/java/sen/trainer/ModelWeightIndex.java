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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads CRF feature weights from a MeCab {@code model.def} text file.
 *
 * <h2>File format</h2>
 * <pre>
 * eta: &lt;value&gt;
 * freq: &lt;value&gt;
 * C: &lt;value&gt;
 * eval-size: &lt;value&gt;
 * unk-eval-size: &lt;value&gt;
 * charset: &lt;value&gt;
 *                          ← blank line separating header from weights
 * &lt;weight&gt;\t&lt;feature_string&gt;
 * &lt;weight&gt;\t&lt;feature_string&gt;
 * ...
 * </pre>
 *
 * <p>Unknown features return a weight of {@code 0.0} via {@link #getWeight(String)}.
 */
public class ModelWeightIndex {

  /** Header key-value pairs (eta, freq, C, eval-size, unk-eval-size, charset). */
  private final Map<String, String> header = new LinkedHashMap<>();

  /** Map from feature string to its alpha (weight) value. */
  private final Map<String, Double> weights = new HashMap<>();

  /**
   * Loads weights from the given {@code model.def} file.
   * Calling this method multiple times on the same instance merges the weights
   * (later values overwrite earlier ones for duplicate keys).
   *
   * @param file the model.def file to read
   * @throws IOException on I/O error
   */
  public void load(File file) throws IOException {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

      // Read header lines until blank line
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isEmpty()) break;
        int colon = line.indexOf(':');
        if (colon > 0) {
          header.put(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
        }
      }

      // Read weight lines: "<double>\t<feature_string>"
      while ((line = reader.readLine()) != null) {
        if (line.isEmpty()) continue;
        int tab = line.indexOf('\t');
        if (tab < 0) continue;
        try {
          double w = Double.parseDouble(line.substring(0, tab));
          String feature = line.substring(tab + 1);
          weights.put(feature, w);
        } catch (NumberFormatException e) {
          // Skip malformed lines
        }
      }
    }
  }

  /**
   * Returns the weight for the given feature string, or {@code 0.0} if unknown.
   *
   * @param feature the feature string (e.g., {@code "A_A01:0,1,2/0"})
   * @return the alpha weight
   */
  public double getWeight(String feature) {
    Double w = weights.get(feature);
    return (w != null) ? w : 0.0;
  }

  /** Returns the number of feature weights loaded. */
  public int size() {
    return weights.size();
  }

  /** Returns an unmodifiable view of the header key-value pairs. */
  public Map<String, String> getHeader() {
    return Collections.unmodifiableMap(header);
  }

  /**
   * Returns the integer {@code cost-factor} from {@code dicrc} or the
   * {@code C} regularization parameter, used to convert float weights to
   * integer costs: {@code cost = round(weight_sum * costFactor)}.
   *
   * <p>This value is NOT read from model.def (which stores {@code C},
   * the regularization constant) but from {@code dicrc}. The caller is
   * responsible for passing the correct value (typically 700 for UniDic).
   */
  public int getCostFactor() {
    // model.def header does not contain cost-factor; this is in dicrc.
    // Return a sentinel so callers know to use their own value.
    return 700;
  }
}
