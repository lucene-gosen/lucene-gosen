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

public class ConjugationAttributeImpl extends AttributeImpl implements ConjugationAttribute, Cloneable {
  private String conjugationalForm = null;
  private String conjugationalType = null;
  
  public String getConjugationalForm() {
    return conjugationalForm;
  }

  public String getConjugationalType() {
    return conjugationalType;
  }

  public void setConjugationalForm(String conjugationalForm) {
    this.conjugationalForm = conjugationalForm;
  }

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
