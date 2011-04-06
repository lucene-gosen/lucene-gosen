package org.apache.lucene.analysis.ja.tokenAttributes;

import org.apache.lucene.util.Attribute;

public interface ConjugationAttribute extends Attribute {
  public String getConjugationalForm();
  public String getConjugationalType();
  public void setConjugationalForm(String conjugationalForm);
  public void setConjugationalType(String conjugationalType);
}
