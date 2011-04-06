package org.apache.lucene.analysis.ja.tokenAttributes;

import org.apache.lucene.util.AttributeImpl;

public class BasicFormAttributeImpl extends AttributeImpl implements BasicFormAttribute, Cloneable {
  private String basicForm = null;
  
  @Override
  public String getBasicForm() {
    return basicForm;
  }
  
  @Override
  public void setBasicForm(String basicForm) {
    this.basicForm = basicForm;
  }

  @Override
  public void clear() {
    basicForm = null;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    BasicFormAttribute t = (BasicFormAttribute) target;
    t.setBasicForm(basicForm);
  }
}
