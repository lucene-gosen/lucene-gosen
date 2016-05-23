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

package org.apache.solr.analysis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.gosen.GosenPartOfSpeechKeepFilter;
import org.apache.lucene.util.Version;

/** 
 * Factory for {@link GosenPartOfSpeechKeepFilter}.
 * <pre class="prettyprint" >
 * &lt;fieldType name="text_ja" class="solr.TextField"&gt;
 *   &lt;analyzer&gt;
 *     &lt;tokenizer class="solr.GosenTokenizerFactory"/&gt;
 *     &lt;filter class="solr.GosenPartOfSpeechKeepFilterFactory" 
 *             tags="keepTags.txt"/&gt;
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;</pre>
 */
public class GosenPartOfSpeechKeepFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {
  private boolean enablePositionIncrements;
  private final String keepTagFiles;
  private Set<String> keepTags;

  public GosenPartOfSpeechKeepFilterFactory(Map<String, String> args) {
    super(args);

    keepTagFiles = require(args, "tags");
    enablePositionIncrements = getBoolean(args, "enablePositionIncrements", true);

    if (args.containsKey("enablePositionIncrements")) {
      throw new IllegalArgumentException("enablePositionIncrements is not a valid option as of Lucene 5.0");
    }
    if (!args.isEmpty()) {
      throw new IllegalArgumentException("Unknown parameters: " + args);
    }
  }

  public void inform(ResourceLoader loader) {
    try {
      CharArraySet cas = getWordSet(loader, keepTagFiles, false);
      keepTags = new HashSet<String>();
      for (Object element : cas) {
        char chars[] = (char[]) element;
        keepTags.add(new String(chars));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public TokenStream create(TokenStream stream) {
   if (keepTags != null) {
      @SuppressWarnings("deprecation")
      final TokenStream filter = new GosenPartOfSpeechKeepFilter(stream, keepTags);
      return filter;
    } else {
      return stream;
    }
  }
}
