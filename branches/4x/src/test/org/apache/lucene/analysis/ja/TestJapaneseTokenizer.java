package org.apache.lucene.analysis.ja;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.Tokenizer;

public class TestJapaneseTokenizer extends BaseTokenStreamTestCase {
  private Analyzer analyzer = new ReusableAnalyzerBase() {
    @Override
    protected TokenStreamComponents createComponents(String field, Reader reader) {
      Tokenizer tokenizer = new JapaneseTokenizer(reader);
      return new TokenStreamComponents(tokenizer, tokenizer);
    }
  };
  
  public void testDecomposition4() throws IOException {
    assertAnalyzesTo(analyzer, "これは本ではない",
      new String[] { "これ", "は", "本", "で", "は", "ない" });

    /*
    Token[] testTokens = new Token[] {
        new Token ("これ", 1848, 0, 2, new Morpheme ("名詞-代名詞-一般", "*", "*", "これ", new String[]{"コレ"}, new String[]{"コレ"}, null)),
        new Token ("は", 2445, 2, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
        new Token ("本", 5181, 3, 1, new Morpheme ("名詞-一般", "*", "*", "本", new String[]{"ホン", "モト"}, new String[]{"ホン", "モト"}, null)),
        new Token ("で", 6466, 4, 1, new Morpheme ("助動詞", "特殊・ダ", "連用形", "だ", new String[]{"デ"}, new String[]{"デ"}, null)),
        new Token ("は", 6978, 5, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
        new Token ("ない", 7098, 6, 2, new Morpheme ("助動詞", "特殊・ナイ", "基本形", "ない", new String[]{"ナイ"}, new String[]{"ナイ"}, null))
    };
    */
  }
}
