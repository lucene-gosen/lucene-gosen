package org.apache.lucene.analysis.ja;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

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

public class TestJapaneseBasicFormFilter extends BaseTokenStreamTestCase {
  private Analyzer analyzer = new ReusableAnalyzerBase() {
    @Override
    protected TokenStreamComponents createComponents(String field, Reader reader) {
      Tokenizer tokenizer = new JapaneseTokenizer(reader);
      TokenStream stream = new JapaneseBasicFormFilter(tokenizer);
      return new TokenStreamComponents(tokenizer, stream);
    }
  };
  
  public void testBasics() throws IOException {
    assertAnalyzesTo(analyzer, "それはまだ実験段階にあります。",
        new String[] { "それ", "は", "まだ", "実験", "段階", "に", "ある", "ます", "。" }
    );
  }
  
  public void testRandomStrings() throws IOException {
    checkRandomData(random, analyzer, 10000);
  }
}
