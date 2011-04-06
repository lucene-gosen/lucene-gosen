package org.apache.lucene.analysis.ja;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.tokenAttributes.PartOfSpeechAttribute;

public final class JapanesePartOfSpeechStopFilter extends FilteringTokenFilter {
  private final Set<String> stopTags;
  private final PartOfSpeechAttribute posAtt = addAttribute(PartOfSpeechAttribute.class);

  public JapanesePartOfSpeechStopFilter(boolean enablePositionIncrements, TokenStream input, Set<String> stopTags) {
    super(enablePositionIncrements, input);
    this.stopTags = stopTags;
  }

  @Override
  protected boolean accept() throws IOException {
    final String pos = posAtt.getPartOfSpeech();
    return pos == null || !stopTags.contains(pos);
  }
}
