package org.apache.lucene.analysis.ja.tokenAttributes;

import java.util.List;

import org.apache.lucene.util.AttributeImpl;

public class PronunciationsAttributeImpl extends AttributeImpl implements PronunciationsAttribute, Cloneable {
  private List<String> pronunciations = null;
  
  @Override
  public List<String> getPronunciations() {
    return pronunciations;
  }
  
  @Override
  public void setPronunciations(List<String> pronunciations) {
    this.pronunciations = pronunciations;
  }

  @Override
  public void clear() {
    pronunciations = null;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    PronunciationsAttribute t = (PronunciationsAttribute) target;
    t.setPronunciations(pronunciations);
  }
}
