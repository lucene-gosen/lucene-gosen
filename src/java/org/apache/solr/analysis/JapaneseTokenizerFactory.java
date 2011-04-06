package org.apache.solr.analysis;

import java.io.Reader;
import java.util.Map;

import net.java.sen.SenFactory;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;

public class JapaneseTokenizerFactory extends BaseTokenizerFactory {

  @Override
  public void init(Map<String,String> args) {
    super.init(args);
    // force Sen to load its files up front, can be slow
    SenFactory.getInstance();
  }

  @Override
  public Tokenizer create(Reader reader) {
    return new JapaneseTokenizer(reader);
  }
}
