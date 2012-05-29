package org.apache.lucene.analysis.gosen.tokenAttributes;

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

import net.java.sen.dictionary.Morpheme;

import org.apache.lucene.analysis.gosen.ToStringUtil;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

public class ConjugationAttributeImpl extends AttributeImpl implements ConjugationAttribute, Cloneable {
  
  private static final long serialVersionUID = 1L;
  
  private transient Morpheme morpheme;
  
  public String getConjugationalForm() {
    return morpheme == null ? null : morpheme.getConjugationalForm();
  }

  public String getConjugationalType() {
    return morpheme == null ? null : morpheme.getConjugationalType();
  }

  public void setMorpheme(Morpheme morpheme) {
    this.morpheme = morpheme;
  }

  @Override
  public void clear() {
    this.morpheme = null;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    ConjugationAttribute t = (ConjugationAttribute) target;
    t.setMorpheme(morpheme);
  }
  
  @Override
  public void reflectWith(AttributeReflector reflector) {
    String conjugationalForm = getConjugationalForm();
    String conjugationalType = getConjugationalType();
    String enForm = conjugationalForm == null ? null : ToStringUtil.getConjFormTranslation(conjugationalForm);
    String enType = conjugationalType == null ? null : ToStringUtil.getConjTypeTranslation(conjugationalType);
    reflector.reflect(ConjugationAttribute.class, "conjugationalForm", conjugationalForm);
    reflector.reflect(ConjugationAttribute.class, "conjugationalForm (en)", enForm);
    reflector.reflect(ConjugationAttribute.class, "conjugationalType", conjugationalType);
    reflector.reflect(ConjugationAttribute.class, "conjugationalType (en)", enType);
  }
}
