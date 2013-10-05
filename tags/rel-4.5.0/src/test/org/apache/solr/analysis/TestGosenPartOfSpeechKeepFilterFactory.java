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

import net.java.sen.SenTestUtil;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.solr.core.SolrResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class TestGosenPartOfSpeechKeepFilterFactory extends BaseTokenStreamTestCase {

  private File baseDir;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    File testRoot = new File(System.getProperty("java.io.tmpdir")).getCanonicalFile();
    baseDir = new File(testRoot, "core-test");
    baseDir.mkdir();
  }

  @Override
  public void tearDown() throws Exception {
    baseDir.delete();
    super.tearDown();
  }

  public void testBasics() throws IOException {
    String tags =
        "#  verb-main:\n" +
            "動詞-自立\n";

    GosenTokenizerFactory tokenizerFactory = new GosenTokenizerFactory(new HashMap<String,String>(){{
      put("dictionaryDir", SenTestUtil.IPADIC_DIR);
    }});
    SolrResourceLoader loader = new SolrResourceLoader(baseDir.getAbsolutePath(), GosenTokenizerFactory.class.getClassLoader());
    tokenizerFactory.inform(loader);
    TokenStream ts = tokenizerFactory.create(new StringReader("私は制限スピードを超える。"));
    Map<String,String> args = new HashMap<String,String>();
    args.put("luceneMatchVersion", TEST_VERSION_CURRENT.toString());
    args.put("tags", "stoptags.txt");
    GosenPartOfSpeechKeepFilterFactory factory = new GosenPartOfSpeechKeepFilterFactory(args);
    factory.inform(new StringMockResourceLoader(tags));
    ts = factory.create(ts);
    assertTokenStreamContents(ts,
        new String[] { "超える" }
    );
  }

  public void testRequireArguments() throws Exception{
    try{
      new GosenPartOfSpeechKeepFilterFactory(new HashMap<String, String>() {{
        put("bogusArg", "bogusValue");
      }});
      fail();
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Configuration Error: missing parameter "));
    }
  }


  public void testBogusArguments() throws Exception{
    try{
      new GosenPartOfSpeechKeepFilterFactory(new HashMap<String, String>() {{
        put("tags", "tags");
        put("bogusArg", "bogusValue");
      }});
      fail();
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Unknown parameters"));
    }
  }
}
