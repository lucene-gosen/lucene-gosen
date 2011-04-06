package org.apache.solr.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapaneseBasicFormFilter;

public class JapaneseBasicFormFilterFactory extends BaseTokenFilterFactory {

  @Override
  public TokenStream create(TokenStream stream) {
    return new JapaneseBasicFormFilter(stream);
  }
}
