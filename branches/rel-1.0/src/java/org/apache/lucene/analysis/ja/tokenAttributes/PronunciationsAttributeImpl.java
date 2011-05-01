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

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.ja.ToStringUtil;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

public class PronunciationsAttributeImpl extends AttributeImpl implements PronunciationsAttribute, Cloneable {
  private List<String> pronunciations = null;
  
  public List<String> getPronunciations() {
    return pronunciations;
  }
  
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
  
  @Override
  public void reflectWith(AttributeReflector reflector) {
    List<String> enPronunciations = null;
    if (pronunciations != null) {
      enPronunciations = new ArrayList<String>(pronunciations.size());
      for (String kana : pronunciations) {
        enPronunciations.add(ToStringUtil.getRomanization(kana));
      }
    }
    reflector.reflect(PartOfSpeechAttribute.class, "pronunciations", pronunciations);
    reflector.reflect(PartOfSpeechAttribute.class, "pronunciations (en)", enPronunciations);
  }
}
