package org.apache.solr.analysis;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapanesePunctuationFilter;

public class JapanesePunctuationFilterFactory extends BaseTokenFilterFactory {
  private boolean enablePositionIncrements;

  @Override
  public void init(Map<String,String> args) {
    super.init(args);
    enablePositionIncrements = getBoolean("enablePositionIncrements", false);
  }

  @Override
  public TokenStream create(TokenStream stream) {
    return new JapanesePunctuationFilter(enablePositionIncrements, stream);
  }
}
