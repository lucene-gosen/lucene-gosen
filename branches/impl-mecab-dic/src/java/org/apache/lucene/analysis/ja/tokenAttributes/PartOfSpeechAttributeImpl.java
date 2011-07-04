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

import net.java.sen.dictionary.Morpheme;

import org.apache.lucene.analysis.ja.ToStringUtil;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

public class PartOfSpeechAttributeImpl extends AttributeImpl implements PartOfSpeechAttribute, Cloneable {
  private Morpheme morpheme;
  
  public String getPartOfSpeech() {
    return morpheme == null ? null : morpheme.getPartOfSpeech();
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
    PartOfSpeechAttribute t = (PartOfSpeechAttribute) target;
    t.setMorpheme(morpheme);
  }
  
  @Override
  public void reflectWith(AttributeReflector reflector) {
    String partOfSpeech = getPartOfSpeech();
    String en = partOfSpeech == null ? null : ToStringUtil.getPOSTranslation(partOfSpeech);
    reflector.reflect(PartOfSpeechAttribute.class, "partOfSpeech", partOfSpeech);
    reflector.reflect(PartOfSpeechAttribute.class, "partOfSpeech (en)", en);
  }
}
