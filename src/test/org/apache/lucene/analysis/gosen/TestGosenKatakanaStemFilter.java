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

package org.apache.lucene.analysis.gosen;

import java.io.IOException;

import net.java.sen.SenTestUtil;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.junit.Test;

public class TestGosenKatakanaStemFilter extends BaseTokenStreamTestCase {
  private Analyzer analyzer = new Analyzer() {
    @Override
    protected TokenStreamComponents createComponents(String field) {
      Tokenizer tokenizer = new GosenTokenizer(newAttributeFactory(), null, SenTestUtil.IPADIC_DIR, false);
      TokenStream stream = new GosenKatakanaStemFilter(tokenizer);
      return new TokenStreamComponents(tokenizer, stream);
    }
  };

  @Test
  public void testBasics() throws IOException {
    assertAnalyzesTo(analyzer, "スパゲッティー",
        new String[] { "スパゲッティ" }
    );
  }

  @Test
  public void testRandomData() throws IOException {
    checkRandomData(random(), analyzer, 10000);
  }
}
