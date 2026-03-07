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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses MeCab-format feature template files ({@code feature.def}),
 * extracting {@code UNIGRAM} and {@code BIGRAM} template definitions.
 *
 * <p>Each non-comment, non-blank line has the form:
 * <pre>
 *   UNIGRAM &lt;name&gt;:&lt;pattern&gt;
 *   BIGRAM  &lt;name&gt;:&lt;pattern&gt;
 * </pre>
 *
 * <p>Pattern placeholders:
 * <ul>
 *   <li>{@code %F[n]}  – mandatory field n of the current token (F[] from lex.csv)</li>
 *   <li>{@code %F?[n]} – optional field n; omitted (with its preceding separator)
 *                        when the value is {@code *}</li>
 *   <li>{@code %L[n]}, {@code %L?[n]} – left-context fields (bigram; from left-id.def)</li>
 *   <li>{@code %R[n]}, {@code %R?[n]} – right-context fields (bigram; from right-id.def)</li>
 *   <li>{@code %t}  – integer character-type of the surface form</li>
 * </ul>
 */
public class FeatureTemplateParser {

  /** A single parsed template (unigram or bigram). */
  public static final class Template {

    /** Template name, used as the prefix before {@code :} in the expanded feature string. */
    public final String name;

    /** Raw pattern string from {@code feature.def} after the {@code :}. */
    public final String pattern;

    /** {@code true} for BIGRAM templates, {@code false} for UNIGRAM. */
    public final boolean bigram;

    Template(String name, String pattern, boolean bigram) {
      this.name    = name;
      this.pattern = pattern;
      this.bigram  = bigram;
    }
  }

  private final List<Template> unigramTemplates = new ArrayList<>();
  private final List<Template> bigramTemplates  = new ArrayList<>();

  /**
   * Loads and parses a {@code feature.def} file.
   *
   * @param file the feature definition file
   * @throws IOException on I/O error
   */
  public void load(File file) throws IOException {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        if (line.startsWith("UNIGRAM ")) {
          parseTemplate(line.substring(8), false);
        } else if (line.startsWith("BIGRAM ")) {
          parseTemplate(line.substring(7), true);
        }
      }
    }
  }

  private void parseTemplate(String spec, boolean bigram) {
    int colon = spec.indexOf(':');
    if (colon < 0) return;
    String name    = spec.substring(0, colon).trim();
    String pattern = spec.substring(colon + 1);
    if (bigram) {
      bigramTemplates.add(new Template(name, pattern, true));
    } else {
      unigramTemplates.add(new Template(name, pattern, false));
    }
  }

  /** Returns an unmodifiable view of the parsed unigram templates. */
  public List<Template> getUnigramTemplates() {
    return Collections.unmodifiableList(unigramTemplates);
  }

  /** Returns an unmodifiable view of the parsed bigram templates. */
  public List<Template> getBigramTemplates() {
    return Collections.unmodifiableList(bigramTemplates);
  }
}
