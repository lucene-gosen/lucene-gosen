/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import net.java.sen.SenTestUtil;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;
import org.apache.solr.core.SolrResourceLoader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class TestGosenPartOfSpeechStopFilterFactory extends BaseTokenStreamTestCase {

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

  @Test
  public void testBasics() throws IOException {
    String tags =
        "#  verb-main:\n" +
            "動詞-自立\n";

    GosenTokenizerFactory tokenizerFactory = new GosenTokenizerFactory(new HashMap<String,String>(){{
      put("dictionaryDir", SenTestUtil.IPADIC_DIR);
    }});

    SolrResourceLoader loader = new SolrResourceLoader(
            baseDir.getAbsoluteFile().toPath(), GosenTokenizerFactory.class.getClassLoader());
    tokenizerFactory.inform(loader);
    Tokenizer tokenizer = tokenizerFactory.create();
    tokenizer.setReader(new StringReader("私は制限スピードを超える。"));
    Map<String,String> args = new HashMap<String,String>();
    args.put("luceneMatchVersion", Version.LATEST.toString());
    args.put("tags", "stoptags.txt");
    GosenPartOfSpeechStopFilterFactory factory = new GosenPartOfSpeechStopFilterFactory(args);
    factory.inform(new StringMockResourceLoader(tags));
    TokenStream ts = factory.create(tokenizer);
    assertTokenStreamContents(ts,
        new String[] { "私", "は", "制限", "スピード", "を", "。" }
    );
  }

  @Test
  public void testRequireArguments() throws Exception{
    try{
      new GosenPartOfSpeechStopFilterFactory(new HashMap<String, String>() {{
        put("bogusArg", "bogusValue");
      }});
      fail();
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Configuration Error: missing parameter "));
    }
  }

  @Test
  public void testBogusArguments() throws Exception{
    try{
      new GosenPartOfSpeechStopFilterFactory(new HashMap<String, String>() {{
        put("tags","tags");
        put("bogusArg", "bogusValue");
      }});
      fail();
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Unknown parameters"));
    }
  }
}
