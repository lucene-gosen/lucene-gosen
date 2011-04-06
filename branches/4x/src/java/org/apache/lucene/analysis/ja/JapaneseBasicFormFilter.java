package org.apache.lucene.analysis.ja;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.tokenAttributes.BasicFormAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public final class JapaneseBasicFormFilter extends TokenFilter {
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final BasicFormAttribute basicFormAtt = addAttribute(BasicFormAttribute.class);

  public JapaneseBasicFormFilter(TokenStream input) {
    super(input);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      termAtt.setEmpty().append(basicFormAtt.getBasicForm());
      return true;
    } else {
      return false;
    }
  }
}
