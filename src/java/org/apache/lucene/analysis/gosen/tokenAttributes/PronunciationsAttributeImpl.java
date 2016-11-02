/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.analysis.gosen.tokenAttributes;

import java.util.Arrays;
import java.util.List;

import net.java.sen.dictionary.Morpheme;

import org.apache.lucene.analysis.gosen.ToStringUtil;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

public class PronunciationsAttributeImpl extends AttributeImpl implements PronunciationsAttribute, Cloneable {
  
  private static final long serialVersionUID = 1L;
  
  private transient Morpheme morpheme;
  
  public List<String> getPronunciations() {
    return morpheme == null ? null : morpheme.getPronunciations();
  }
  
  public void setMorpheme(Morpheme morpheme) {
    this.morpheme = morpheme;
  }

  @Override
  public void clear() {
    morpheme = null;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    PronunciationsAttribute t = (PronunciationsAttribute) target;
    t.setMorpheme(morpheme);
  }
  
  @Override
  public void reflectWith(AttributeReflector reflector) {
    final List<String> pronunciations = getPronunciations();
    List<String> enPronunciations = null;
    if (pronunciations != null) {
      final String[] p = new String[pronunciations.size()];
      int i = 0;
      for (String kana : pronunciations) {
        p[i++] = ToStringUtil.getRomanization(kana);
      }
      enPronunciations = Arrays.asList(p);
    }
    reflector.reflect(PronunciationsAttribute.class, "pronunciations", pronunciations);
    reflector.reflect(PronunciationsAttribute.class, "pronunciations (en)", enPronunciations);
  }
}
