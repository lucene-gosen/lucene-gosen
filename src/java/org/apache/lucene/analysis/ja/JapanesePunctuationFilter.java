package org.apache.lucene.analysis.ja;

import java.io.IOException;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public final class JapanesePunctuationFilter extends FilteringTokenFilter {
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

  public JapanesePunctuationFilter(boolean enablePositionIncrements, TokenStream input) {
    super(enablePositionIncrements, input);
  }

  @Override
  protected boolean accept() throws IOException {
    return termAtt.length() > 0 && !isPunctuation(termAtt.buffer()[0]);
  }
  
  static final boolean isPunctuation(char ch) {
    switch(Character.getType(ch)) {
      case Character.SPACE_SEPARATOR:
      case Character.LINE_SEPARATOR:
      case Character.PARAGRAPH_SEPARATOR:
      case Character.CONTROL:
      case Character.FORMAT:
      case Character.DASH_PUNCTUATION:
      case Character.START_PUNCTUATION:
      case Character.END_PUNCTUATION:
      case Character.CONNECTOR_PUNCTUATION:
      case Character.OTHER_PUNCTUATION:
      case Character.MATH_SYMBOL:
      case Character.CURRENCY_SYMBOL:
      case Character.MODIFIER_SYMBOL:
      case Character.OTHER_SYMBOL:
      case Character.INITIAL_QUOTE_PUNCTUATION:
      case Character.FINAL_QUOTE_PUNCTUATION:
        return true;
      default:
        return false;
    }
  }
}
