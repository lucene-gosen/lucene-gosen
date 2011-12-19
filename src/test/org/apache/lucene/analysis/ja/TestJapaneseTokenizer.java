package org.apache.lucene.analysis.ja;

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
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import net.java.sen.SenTestUtil;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util._TestUtil;

/**
 * Tests for {@link JapaneseTokenizer}
 */
public class TestJapaneseTokenizer extends BaseTokenStreamTestCase {
  private Analyzer analyzer = new ReusableAnalyzerBase() {
    @Override
    protected TokenStreamComponents createComponents(String field, Reader reader) {
      Tokenizer tokenizer = new JapaneseTokenizer(reader, null, SenTestUtil.IPADIC_DIR);
      return new TokenStreamComponents(tokenizer, tokenizer);
    }
  };
  
  public void testDecomposition1() throws IOException {
    assertAnalyzesTo(analyzer, "本来は、貧困層の女性や子供に医療保護を提供するために創設された制度である、" +
                         "アメリカ低所得者医療援助制度が、今日では、その予算の約３分の１を老人に費やしている。",
     new String[] { "本来", "は", "、", "貧困", "層", "の", "女性", "や", "子供", "に", "医療", "保護", "を",      
                    "提供", "する", "ため", "に", "創設", "さ", "れ", "た", "制度", "で", "ある", "、", "アメリカ", 
                    "低", "所得", "者", "医療", "援助", "制度", "が", "、", "今日", "で", "は", "、", "その",
                    "予算", "の", "約", "３", "分の", "１", "を", "老人", "に", "費やし", "て", "いる", "。" },
     new int[] { 0, 2, 3, 4, 6, 7,  8, 10, 11, 13, 14, 16, 18, 19, 21, 23, 25, 26, 28, 29, 30, 
                 31, 33, 34, 36, 37, 41, 42, 44, 45, 47, 49, 51, 52, 53, 55, 56, 57, 58, 60,
                 62, 63, 64, 65, 67, 68, 69, 71, 72, 75, 76, 78 },
     new int[] { 2, 3, 4, 6, 7, 8, 10, 11, 13, 14, 16, 18, 19, 21, 23, 25, 26, 28, 29, 30, 31,
                 33, 34, 36, 37, 41, 42, 44, 45, 47, 49, 51, 52, 53, 55, 56, 57, 58, 60, 62,
                 63, 64, 65, 67, 68, 69, 71, 72, 75, 76, 78, 79 }
    );
  }

  public void testDecomposition2() throws IOException {
    assertAnalyzesTo(analyzer, "麻薬の密売は根こそぎ絶やさなければならない",
      new String[] { "麻薬", "の", "密売", "は", "根こそぎ", "絶やさ", "な", "けれ", "ば", "なら", "ない" },
      new int[] { 0, 2, 3, 5, 6,  10, 13, 14, 16, 17, 19 },
      new int[] { 2, 3, 5, 6, 10, 13, 14, 16, 17, 19, 21 }
    );
  }
  
  public void testDecomposition3() throws IOException {
    assertAnalyzesTo(analyzer, "魔女狩大将マシュー・ホプキンス。",
      new String[] { "魔女", "狩", "大将", "マシュー", "・", "ホプキンス", "。" },
      new int[] { 0, 2, 3, 5,  9, 10, 15 },
      new int[] { 2, 3, 5, 9, 10, 15, 16 }
    );
  }
  
  public void testDecomposition4() throws IOException {
    assertAnalyzesTo(analyzer, "これは本ではない",
      new String[] { "これ", "は", "本", "で", "は", "ない" },
      new int[] { 0, 2, 3, 4, 5, 6 },
      new int[] { 2, 3, 4, 5, 6, 8 }
    );
  }

  public void testDecomposition5() throws IOException {
    assertAnalyzesTo(analyzer, "くよくよくよくよくよくよくよくよくよくよくよくよくよくよくよくよくよくよくよくよ",
      new String[] { "くよくよ", "くよくよ", "くよくよ", "くよくよ", "くよくよ", "くよくよ", "くよくよ", "くよくよ", "くよくよ", "くよくよ" },
      new int[] { 0, 4, 8, 12, 16, 20, 24, 28, 32, 36},
      new int[] { 4, 8, 12, 16, 20, 24, 28, 32, 36, 40 }
    );
  }
  
  /** Tests that sentence offset is incorporated into the resulting offsets */
  public void testTwoSentences() throws IOException {
    assertAnalyzesTo(analyzer, "魔女狩大将マシュー・ホプキンス。 魔女狩大将マシュー・ホプキンス。",
      new String[] { "魔女", "狩", "大将", "マシュー", "・", "ホプキンス", "。", "魔女", "狩", "大将", "マシュー", "・", "ホプキンス", "。" },
      new int[] { 0, 2, 3, 5,  9, 10, 15, 17, 19, 20, 22, 26, 27, 32 },
      new int[] { 2, 3, 5, 9, 10, 15, 16, 19, 20, 22, 26, 27, 32, 33 }
    );
  }
  
  /** Tests that for large documents the buffer offset is accumulated */
  public void testOffsetAccumulation() throws IOException {
      StringBuilder sb = new StringBuilder();
      char whitespace[] = new char[4094];
      Arrays.fill(whitespace, '\n');
      sb.append(whitespace);
      sb.append("testing 1234");
      String input = sb.toString();
      assertAnalyzesTo(analyzer, input, 
       new String[] { "testing", "1234" },
       new int[] { 4094, 4102 },
       new int[] { 4101, 4106 });
  }
  
  /**
   * Tokenizes random unicode strings, to ensure no exception
   * (results could be completely bogus, but makes sure we don't crash on some input)
   */
  public void testReliability() throws IOException {
    checkRandomData(random, analyzer, 10000);
  }
  
  public void testLargeDocReliability() throws IOException {
    for (int i = 0; i < 100; i++) {
      String s = _TestUtil.randomUnicodeString(random, 10000);
      TokenStream ts = analyzer.reusableTokenStream("foo", new StringReader(s));
      ts.reset();
      while (ts.incrementToken()) {
      }
    }
  }
  
  public void testEnd() throws IOException {
    assertTokenStreamContents(analyzer.reusableTokenStream("foo", new StringReader("これは本ではない")),
        new String[] { "これ", "は", "本", "で", "は", "ない" },
        new int[] { 0, 2, 3, 4, 5, 6 },
        new int[] { 2, 3, 4, 5, 6, 8 },
        new Integer(8)
    );
    
    assertTokenStreamContents(analyzer.reusableTokenStream("foo", new StringReader("これは本ではない    ")),
        new String[] { "これ", "は", "本", "で", "は", "ない" },
        new int[] { 0, 2, 3, 4, 5, 6 },
        new int[] { 2, 3, 4, 5, 6, 8 },
        new Integer(12)
    );
  }
}
