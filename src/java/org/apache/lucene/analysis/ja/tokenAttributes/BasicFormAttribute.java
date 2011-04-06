package org.apache.lucene.analysis.ja.tokenAttributes;

import org.apache.lucene.util.Attribute;

public interface BasicFormAttribute extends Attribute {
  public String getBasicForm();
  public void setBasicForm(String basicForm);
}
