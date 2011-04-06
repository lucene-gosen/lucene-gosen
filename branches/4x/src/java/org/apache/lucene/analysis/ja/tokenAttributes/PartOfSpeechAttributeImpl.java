package org.apache.lucene.analysis.ja.tokenAttributes;

import org.apache.lucene.util.AttributeImpl;

public class PartOfSpeechAttributeImpl extends AttributeImpl implements PartOfSpeechAttribute, Cloneable {
  private String partOfSpeech = null;
  
  @Override
  public String getPartOfSpeech() {
    return partOfSpeech;
  }
  
  @Override
  public void setPartOfSpeech(String partOfSpeech) {
    this.partOfSpeech = partOfSpeech;
  }

  @Override
  public void clear() {
    partOfSpeech = null;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    PartOfSpeechAttribute t = (PartOfSpeechAttribute) target;
    t.setPartOfSpeech(partOfSpeech);
  }
}
