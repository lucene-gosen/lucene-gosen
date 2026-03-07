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

package net.java.sen.tools;

import net.java.sen.compiler.UnidicPreprocessor;
import net.java.sen.trainer.CostCalculator;
import net.java.sen.trainer.FeatureExtractor;
import net.java.sen.trainer.FeatureTemplateParser;
import net.java.sen.trainer.ModelWeightIndex;

import java.io.File;
import java.io.IOException;

/**
 * CLI entry point for preprocessing a UniDic dictionary into the intermediate
 * CSV files ({@code dictionary.csv}, {@code connection.csv}) consumed by
 * {@link DictionaryCompiler}.
 *
 * <h2>Usage</h2>
 * <pre>
 * DictionaryTrainer --dict-dir &lt;path&gt; --output-dir &lt;path&gt; [options]
 *
 * Required:
 *   --dict-dir   &lt;path&gt;   Path to unidic-cwj-202512_full/
 *   --output-dir &lt;path&gt;   Directory for dictionary.csv and connection.csv output
 *
 * Optional:
 *   --use-model            Recalculate costs from CRF weights in model.def
 *                          (Phase 2). Default: off (raw costs from lex.csv /
 *                          matrix.def are used).
 *   --model      &lt;path&gt;   Path to model.def. Default: &lt;dict-dir&gt;/model.def
 *   --feature-def &lt;path&gt;  Path to feature.def. Default: &lt;dict-dir&gt;/feature.def
 *   --cost-factor &lt;n&gt;     Integer multiplier for float→int cost conversion.
 *                          Default: 700 (from UniDic dicrc)
 * </pre>
 *
 * <h2>Two-phase pipeline</h2>
 * <ol>
 *   <li><b>Phase 1</b> (always): {@link UnidicPreprocessor} converts
 *       {@code lex.csv} and {@code matrix.def} into {@code dictionary.csv}
 *       and {@code connection.csv} using the raw costs stored in those files.</li>
 *   <li><b>Phase 2</b> (with {@code --use-model}): A {@link CostCalculator}
 *       replaces raw costs with values derived from the CRF feature weights in
 *       {@code model.def}, applying the MeCab formula
 *       {@code cost = round(Σ α[f] × costFactor)}.</li>
 * </ol>
 *
 * <p>After this tool completes, run {@link DictionaryCompiler} in the
 * {@code --output-dir} to compile the CSV files into binary {@code .sen} files.
 * The Gradle task {@code compileUnidic} does this automatically.
 */
public class DictionaryTrainer {

  public static void main(String[] args) throws IOException {
    File    dictDir     = null;
    File    outputDir   = null;
    File    modelFile   = null;
    File    featureFile = null;
    boolean useModel    = false;
    int     costFactor  = 700;

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if ("--dict-dir".equals(arg)) {
        dictDir = new File(args[++i]);
      } else if ("--output-dir".equals(arg)) {
        outputDir = new File(args[++i]);
      } else if ("--model".equals(arg)) {
        modelFile = new File(args[++i]);
      } else if ("--feature-def".equals(arg)) {
        featureFile = new File(args[++i]);
      } else if ("--use-model".equals(arg)) {
        useModel = true;
      } else if ("--cost-factor".equals(arg)) {
        costFactor = Integer.parseInt(args[++i]);
      } else {
        printUsage();
        throw new IllegalArgumentException("Unknown argument: " + arg);
      }
    }

    if (dictDir == null || outputDir == null) {
      printUsage();
      throw new IllegalArgumentException("--dict-dir and --output-dir are required");
    }

    if (!dictDir.isDirectory()) {
      throw new IllegalArgumentException(
          "--dict-dir does not exist or is not a directory: " + dictDir);
    }

    // Apply defaults
    if (modelFile   == null) modelFile   = new File(dictDir, "model.def");
    if (featureFile == null) featureFile = new File(dictDir, "feature.def");

    // Build CostCalculator if Phase 2 is requested
    CostCalculator costCalculator = null;
    if (useModel) {
      if (!featureFile.exists()) {
        throw new IllegalArgumentException("feature.def not found: " + featureFile);
      }
      if (!modelFile.exists()) {
        throw new IllegalArgumentException("model.def not found: " + modelFile);
      }

      System.out.println("[DictionaryTrainer] Loading feature templates from: " + featureFile);
      FeatureTemplateParser parser = new FeatureTemplateParser();
      parser.load(featureFile);
      System.out.println("  " + parser.getUnigramTemplates().size()
          + " unigram templates, " + parser.getBigramTemplates().size() + " bigram templates");

      System.out.println("[DictionaryTrainer] Loading CRF weights from: " + modelFile);
      ModelWeightIndex model = new ModelWeightIndex();
      model.load(modelFile);
      System.out.println("  " + model.size() + " feature weights loaded");

      FeatureExtractor extractor = new FeatureExtractor(parser);
      costCalculator = new CostCalculator(extractor, model, costFactor);
      System.out.println("[DictionaryTrainer] Phase 2 enabled: costs recalculated from CRF weights"
          + " (costFactor=" + costFactor + ")");
    } else {
      System.out.println("[DictionaryTrainer] Phase 1 only: using raw costs from lex.csv / matrix.def");
    }

    // Run preprocessor
    UnidicPreprocessor preprocessor = new UnidicPreprocessor(dictDir, costCalculator);
    preprocessor.build(outputDir);

    System.out.println("[DictionaryTrainer] Preprocessing complete. "
        + "Run DictionaryCompiler in " + outputDir.getAbsolutePath()
        + " to compile to .sen files.");
  }

  private static void printUsage() {
    System.err.println("Usage: DictionaryTrainer --dict-dir <path> --output-dir <path> [options]");
    System.err.println("  --use-model             recalculate costs from model.def CRF weights");
    System.err.println("  --model      <path>     model.def (default: <dict-dir>/model.def)");
    System.err.println("  --feature-def <path>    feature.def (default: <dict-dir>/feature.def)");
    System.err.println("  --cost-factor <n>       float→int scale factor (default: 700)");
  }
}
