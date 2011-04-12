package org.apache.lucene.analysis.ja;

/**
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

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.util.ReusableAnalyzerBase;

/**
 * Tests for {@link JapaneseWidthFilter}
 */
public class TestJapaneseWidthFilter extends BaseTokenStreamTestCase {
  private Analyzer analyzer = new ReusableAnalyzerBase() {
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
      Tokenizer source = new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader);
      return new TokenStreamComponents(source, new JapaneseWidthFilter(source));
    }
  };
  
  /**
   * Full-width ASCII forms normalized to half-width (basic latin)
   */
  public void testFullWidthASCII() throws IOException {
    assertAnalyzesTo(analyzer, "Ｔｅｓｔ １２３４",
      new String[] { "Test",  "1234" });
  }
  
  /**
   * Half-width katakana forms normalized to standard katakana.
   * A bit trickier in some cases, since half-width forms are decomposed
   * and voice marks need to be recombined with a preceding base form. 
   */
  public void testHalfWidthKana() throws IOException {
    assertAnalyzesTo(analyzer, "ｶﾀｶﾅ",
      new String[] { "カタカナ" });
    assertAnalyzesTo(analyzer, "ｳﾞｨｯﾂ",
      new String[] { "ヴィッツ" });
    assertAnalyzesTo(analyzer, "ﾊﾟﾅｿﾆｯｸ",
      new String[] { "パナソニック" });
  }
}