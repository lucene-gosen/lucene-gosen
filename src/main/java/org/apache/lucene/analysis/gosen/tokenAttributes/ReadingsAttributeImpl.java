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

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.gosen.ToStringUtil;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

import net.java.sen.dictionary.Morpheme;

/**
 * Attribute for {@link Morpheme#getReadings()}.
 */
public class ReadingsAttributeImpl extends AttributeImpl implements ReadingsAttribute, Cloneable {
  
  private static final long serialVersionUID = 1L;
  
  private transient Morpheme morpheme;

  public List<String> getReadings() {
    return morpheme == null ? null : morpheme.getReadings();
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
    ReadingsAttribute t = (ReadingsAttribute) target;
    t.setMorpheme(morpheme);
  }
  
  @Override
  public void reflectWith(AttributeReflector reflector) {
    List<String> readings = getReadings();
    List<String> enReadings = null;
    if (readings != null) {
      enReadings = new ArrayList<String>(readings.size());
      for (String kana : readings) {
        enReadings.add(ToStringUtil.getRomanization(kana));
      }
    }
    reflector.reflect(ReadingsAttribute.class, "readings", readings);
    reflector.reflect(ReadingsAttribute.class, "readings (en)", enReadings);
  }
}
