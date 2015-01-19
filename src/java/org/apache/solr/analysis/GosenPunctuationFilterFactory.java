package org.apache.solr.analysis;

/**
 * Copyright 2004 The Apache Software Foundation
 *
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

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.gosen.GosenPunctuationFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.util.Version;

/**
 * Factory for {@link GosenPunctuationFilter}.
 * <pre class="prettyprint" >
 * &lt;fieldType name="text_ja" class="solr.TextField"&gt;
 *   &lt;analyzer&gt;
 *     &lt;tokenizer class="solr.GosenTokenizerFactory"/&gt;
 *     &lt;filter class="solr.GosenPunctuationFilterFactory"
 *         enablePositionIncrements="true"
 *         protectedTokens="lang/ja/punctuation-filter-protected.txt"/&gt;
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;</pre>
 */
public class GosenPunctuationFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {
  private final boolean enablePositionIncrements;
  private final String protectedFile;
  private CharArraySet protectedSet;

  public GosenPunctuationFilterFactory(Map<String,String> args) {
    super(args);
    enablePositionIncrements = getBoolean(args, "enablePositionIncrements", true);
    protectedFile = get(args, "protectedTokens");
    if (enablePositionIncrements == false &&
        (luceneMatchVersion == null || luceneMatchVersion.onOrAfter(Version.LUCENE_4_4_0))) {
      throw new IllegalArgumentException("enablePositionIncrements=false is not supported anymore as of Lucene 4.4");
    }
    if (!args.isEmpty()) {
      throw new IllegalArgumentException("Unknown parameters: " + args);
    }
  }

  public TokenStream create(TokenStream stream) {
    @SuppressWarnings("deprecation")
    final GosenPunctuationFilter filter = new GosenPunctuationFilter(luceneMatchVersion, enablePositionIncrements, protectedSet, stream);
    return filter;
  }

  public void inform(ResourceLoader resourceLoader) throws IOException {
    if (protectedFile != null) {
      loadProtectedTokens(resourceLoader);
    }
  }

  public void loadProtectedTokens(ResourceLoader loader) throws IOException {
    CharArraySet casTriggerWords = getWordSet(loader, protectedFile, false);
    protectedSet = new CharArraySet(casTriggerWords.size(), false);
    for (Object elem : casTriggerWords) {
      char chars[] = (char[]) elem;
      if (chars[0] == '\\' && chars[1] == '#') {
        // Special treatment for '#' that is processed as a comment symbol in getWordSet method.
        protectedSet.add('#');
      } else {
        protectedSet.add(elem);
      }
    }
  }

}
