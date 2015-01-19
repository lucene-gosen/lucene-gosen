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

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

public class TestGosenPunctuationFilterFactory extends BaseTokenStreamTestCase {

  String protectedTokens = "+\n-\n\\#\n【\n】\n℃\n";

  /**
   * Testing voiced fricatives mapping.
   */
  GosenPunctuationFilterFactory factory;
  GosenPunctuationFilterFactory protected_factory;
  GosenPunctuationFilterFactory posinc_factory;
  GosenTokenizerFactory jatok_factory = new GosenTokenizerFactory(new HashMap<String, String>());

  @Override
  public void setUp() throws Exception {
    super.setUp();
    // Create a tokenizer
    jatok_factory.inform(new StringMockResourceLoader(""));

    // Create a filter
    factory = new GosenPunctuationFilterFactory(new HashMap<String, String>() {{
      put("protectedTokens", "lang/ja/punctuation-protected.txt");
      put("enablePositionIncrements", "false");
      put("luceneMatchVersion", Version.LUCENE_43.toString());
    }});
    factory.inform(new StringMockResourceLoader(""));

    // Create a filter
    protected_factory = new GosenPunctuationFilterFactory(new HashMap<String, String>() {{
      put("protectedTokens", "lang/ja/punctuation-protected.txt");
      put("enablePositionIncrements", "false");
      put("luceneMatchVersion", Version.LUCENE_43.toString());
    }});
    protected_factory.inform(new StringMockResourceLoader(protectedTokens));
    protected_factory.loadProtectedTokens(new StringMockResourceLoader(protectedTokens));

    // Create a filter
    posinc_factory = new GosenPunctuationFilterFactory(new HashMap<String, String>() {{
      put("protectedTokens", "lang/ja/punctuation-protected.txt");
      put("enablePositionIncrements", "true");
      put("luceneMatchVersion", TEST_VERSION_CURRENT.toString());
    }});
    posinc_factory.inform(new StringMockResourceLoader(protectedTokens));
    posinc_factory.loadProtectedTokens(new StringMockResourceLoader(protectedTokens));
  }

  public void testBogusArgments() throws Exception{
    try{
      new GosenPunctuationFilterFactory(new HashMap<String, String>() {{
        put("bogusArg", "bogusValue");
      }});
      fail();
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Unknown parameters"));
    }
  }

  /**
   *
   */
  public void testEmptyTerm() throws IOException {
    Reader reader = new StringReader("");
    TokenStream stream = jatok_factory.create(reader);
    stream = factory.create(stream);
    assertTokenStreamContents(stream, new String[]{});
  }

  public void testPunctuationFilter01() throws Exception {
    Reader reader = new StringReader("日本語・英語");
    TokenStream stream = jatok_factory.create(reader);
    stream = factory.create(stream);
    assertTokenStreamContents(stream,
            new String[]{"日本語", "英語"},
            new int[]{0,4},
            new int[]{3,6},
            new int[]{1,1});
  }

  public void testPunctuationFilter02() throws Exception {
    Reader reader = new StringReader("アパッチ★ソーラー");
    TokenStream stream = jatok_factory.create(reader);
    stream = factory.create(stream);
    assertTokenStreamContents(stream, new String[]{"アパッチ", "ソーラー"});
  }

  public void testPunctuationFilter03() throws Exception {
    Reader reader = new StringReader("#0001");
    TokenStream stream = jatok_factory.create(reader);
    stream = factory.create(stream);
    assertTokenStreamContents(stream,
            new String[]{"0001"},
            new int[]{1},
            new int[]{5},
            new int[]{1});
  }

  public void testPunctuationFilter04() throws Exception {
    Reader reader = new StringReader("(0001)");
    TokenStream stream = jatok_factory.create(reader);
    stream = factory.create(stream);
    assertTokenStreamContents(stream, new String[]{"0001"});
  }

  public void testPunctuationFilter05() throws Exception {
    Reader reader = new StringReader("C++Guide");
    TokenStream stream = jatok_factory.create(reader);
    stream = factory.create(stream);
    assertTokenStreamContents(stream,
            new String[]{"C", "Guide"},
            new int[]{0,3},
            new int[]{1,8},
            new int[]{1,1});
  }

  public void testPunctuationFilter06() throws Exception {
    Reader reader = new StringReader("C#");
    TokenStream stream = jatok_factory.create(reader);
    stream = factory.create(stream);
    assertTokenStreamContents(stream, new String[]{"C"});
  }

  public void testPunctuationFilter07() throws Exception {
    Reader reader = new StringReader("【Marketing-Keyword】");
    TokenStream stream = jatok_factory.create(reader);
    stream = factory.create(stream);
    assertTokenStreamContents(stream,
            new String[]{"Marketing", "Keyword"},
            new int[]{1,11},
            new int[]{10,18},
            new int[]{1,1});
  }

  //------------------------------------------------------------------------------------------------------------

  public void testPunctuationFilter08() throws Exception {
    Reader reader = new StringReader("#0001");
    TokenStream stream = jatok_factory.create(reader);
    stream = protected_factory.create(stream);
    assertTokenStreamContents(stream, new String[]{"#", "0001"});
  }

  public void testPunctuationFilter09() throws Exception {
    Reader reader = new StringReader("C++GUIDE");
    TokenStream stream = jatok_factory.create(reader);
    stream = protected_factory.create(stream);
    assertTokenStreamContents(stream, new String[]{"C", "++", "GUIDE"});
  }

  public void testPunctuationFilter10() throws Exception {
    Reader reader = new StringReader("C#");
    TokenStream stream = jatok_factory.create(reader);
    stream = protected_factory.create(stream);
    assertTokenStreamContents(stream, new String[]{"C", "#"});
  }

  public void testPunctuationFilter11() throws Exception {
    Reader reader = new StringReader("【Marketing-Keyword】");
    TokenStream stream = jatok_factory.create(reader);
    stream = protected_factory.create(stream);
    assertTokenStreamContents(stream, new String[]{"【", "Marketing", "-", "Keyword", "】"});
  }

  public void testPunctuationFilter12() throws Exception {
    Reader reader = new StringReader("【Marketing=Keyword】");
    TokenStream stream = jatok_factory.create(reader);
    stream = protected_factory.create(stream);
    assertTokenStreamContents(stream, new String[]{"【", "Marketing", "Keyword", "】"});
  }

  //------------------------------------------------------------------------------------------------------------

  public void testPunctuationFilter13() throws Exception {
    Reader reader = new StringReader("日本語・英語");
    TokenStream stream = jatok_factory.create(reader);
    stream = posinc_factory.create(stream);
    assertTokenStreamContents(stream,
            new String[]{"日本語", "英語"},
            new int[]{0,4},
            new int[]{3,6},
            new int[]{1,2});
  }

  public void testPunctuationFilter14() throws Exception {
    Reader reader = new StringReader("$00&&||01"); //=> /$/00/&&/||/01/
    TokenStream stream = jatok_factory.create(reader);
    stream = posinc_factory.create(stream);
    assertTokenStreamContents(stream,
            new String[]{"00", "01"},
            new int[]{1,7},
            new int[]{3,9},
            new int[]{2,3});
  }

  public void testPunctuationFilter15() throws Exception {
    Reader reader = new StringReader("C**Guide");
    TokenStream stream = jatok_factory.create(reader);
    stream = posinc_factory.create(stream);
    assertTokenStreamContents(stream,
            new String[]{"C", "Guide"},
            new int[]{0,3},
            new int[]{1,8},
            new int[]{1,2});
  }

  public void testPunctuationFilter16() throws Exception {
    Reader reader = new StringReader("[Marketing=Keyword]"); //=> /[/Marketing/=/Keyword/]/
    TokenStream stream = jatok_factory.create(reader);
    stream = posinc_factory.create(stream);
    assertTokenStreamContents(stream,
            new String[]{"Marketing", "Keyword"},
            new int[]{1,11},
            new int[]{10,18},
            new int[]{2,2});
  }

  public void testPunctuationFilter17() throws Exception {
    Reader reader = new StringReader("4℃"); //=> /4/℃/
    TokenStream stream = jatok_factory.create(reader);
    stream = posinc_factory.create(stream);
    assertTokenStreamContents(stream,
            new String[]{"4", "℃"});
  }

}
