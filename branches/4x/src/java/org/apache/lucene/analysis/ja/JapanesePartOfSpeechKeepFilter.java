package org.apache.lucene.analysis.ja;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.tokenAttributes.PartOfSpeechAttribute;

public final class JapanesePartOfSpeechKeepFilter extends FilteringTokenFilter {
  private final Set<String> keepTags;
  private final PartOfSpeechAttribute posAtt = addAttribute(PartOfSpeechAttribute.class);

  public JapanesePartOfSpeechKeepFilter(boolean enablePositionIncrements, TokenStream input, Set<String> keepTags) {
    super(enablePositionIncrements, input);
    this.keepTags = keepTags;
  }

  @Override
  protected boolean accept() throws IOException {
    final String pos = posAtt.getPartOfSpeech();
    return pos != null && keepTags.contains(pos);
  }
}
