package org.apache.solr.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapaneseKatakanaStemFilter;

public class JapaneseKatakanaStemFilterFactory extends BaseTokenFilterFactory {

  @Override
  public TokenStream create(TokenStream stream) {
    return new JapaneseKatakanaStemFilter(stream);
  }
}
