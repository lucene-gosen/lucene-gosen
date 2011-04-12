package org.apache.lucene.analysis.ja.tokenAttributes;

/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.util.AttributeImpl;

public class BasicFormAttributeImpl extends AttributeImpl implements BasicFormAttribute, Cloneable {
  private String basicForm = null;
  
  public String getBasicForm() {
    return basicForm;
  }
  
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
