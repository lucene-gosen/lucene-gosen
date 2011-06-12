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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import net.java.sen.filter.stream.CompositeTokenFilter;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.util.plugin.ResourceLoaderAware;

/**
 * Factory for {@link JapaneseTokenizer}.
 * <pre class="prettyprint" >
 * &lt;fieldType name="text_ja" class="solr.TextField"&gt;
 *   &lt;analyzer&gt;
 *     &lt;tokenizer class="solr.JapaneseTokenizerFactory" compositePOS="compositePOS.txt"/&gt;
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;</pre>
 */
public class JapaneseTokenizerFactory extends BaseTokenizerFactory implements ResourceLoaderAware {
  
  private CompositeTokenFilter compositeTokenFilter;

  public void init(Map<String,String> args) {
    super.init(args);
  }

  public void inform(ResourceLoader loader) {
    String compositePosFile = args.get("compositePOS");
    if(compositePosFile != null){
      compositeTokenFilter = new CompositeTokenFilter();
      BufferedReader reader = null;
      try{
        reader = new BufferedReader( new InputStreamReader(
            loader.openResource(compositePosFile), "UTF-8"));
        compositeTokenFilter.readRules(reader);
      }
      catch(IOException e){
        throw new RuntimeException(e);
      }
      finally {
        try {
          if(reader != null)
            reader.close();
        } catch (IOException e) {
        }
      }
    }
  }

  public Tokenizer create(Reader reader) {
    return new JapaneseTokenizer(reader, compositeTokenFilter);
  }
}
