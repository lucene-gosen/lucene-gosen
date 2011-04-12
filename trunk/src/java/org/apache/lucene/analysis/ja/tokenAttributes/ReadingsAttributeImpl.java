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

import java.util.List;

import org.apache.lucene.util.AttributeImpl;

import net.java.sen.dictionary.Morpheme;

/**
 * Attribute for {@link Morpheme#getReadings()}.
 */
public class ReadingsAttributeImpl extends AttributeImpl implements ReadingsAttribute, Cloneable {
  private List<String> readings = null;

  public List<String> getReadings() {
    return readings;
  }
  
  public void setReadings(List<String> readings) {
    this.readings = readings;
  }

  @Override
  public void clear() {
    readings = null;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    ReadingsAttribute t = (ReadingsAttribute) target;
    t.setReadings(readings);
  }
}
