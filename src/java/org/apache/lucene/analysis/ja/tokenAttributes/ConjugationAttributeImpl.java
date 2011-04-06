package org.apache.lucene.analysis.ja.tokenAttributes;

import org.apache.lucene.util.AttributeImpl;

public class ConjugationAttributeImpl extends AttributeImpl implements ConjugationAttribute, Cloneable {
  private String conjugationalForm = null;
  private String conjugationalType = null;
  
  @Override
  public String getConjugationalForm() {
    return conjugationalForm;
  }

  @Override
  public String getConjugationalType() {
    return conjugationalType;
  }

  @Override
  public void setConjugationalForm(String conjugationalForm) {
    this.conjugationalForm = conjugationalForm;
  }

  @Override
  public void setConjugationalType(String conjugationalType) {
    this.conjugationalType = conjugationalType;
  }

  @Override
  public void clear() {
    this.conjugationalForm = null;
    this.conjugationalType = null;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    ConjugationAttribute t = (ConjugationAttribute) target;
    t.setConjugationalForm(conjugationalForm);
    t.setConjugationalType(conjugationalType);
  }
}
