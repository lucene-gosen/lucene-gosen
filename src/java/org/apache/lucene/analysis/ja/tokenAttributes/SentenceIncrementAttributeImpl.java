package org.apache.lucene.analysis.ja.tokenAttributes;

import org.apache.lucene.util.AttributeImpl;

public class SentenceIncrementAttributeImpl extends AttributeImpl implements SentenceIncrementAttribute, Cloneable {
  private int sentenceIncrement;

  public int getSentenceIncrement() {
    return sentenceIncrement;
  }

  @Override
  public void setSentenceIncrement(int sentenceIncrement) {
    this.sentenceIncrement = sentenceIncrement;
  }

  @Override
  public void clear() {
    this.sentenceIncrement = 0;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    SentenceIncrementAttribute t = (SentenceIncrementAttribute) target;
    t.setSentenceIncrement(sentenceIncrement);
  }
}
