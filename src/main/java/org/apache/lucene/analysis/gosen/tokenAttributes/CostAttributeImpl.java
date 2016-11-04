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

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

public class CostAttributeImpl extends AttributeImpl implements CostAttribute, Cloneable {
  
  private static final long serialVersionUID = 1L;
  
  private int cost;

  public int getCost() {
    return cost;
  }

  public void setCost(int cost) {
    this.cost = cost;
  }

  @Override
  public void clear() {
    this.cost = 0;
  }

  @Override
  public void reflectWith(AttributeReflector attributeReflector) {
    attributeReflector.reflect(CostAttribute.class, "cost", getCost());
  }

  @Override
  public void copyTo(AttributeImpl target) {
    CostAttribute t = (CostAttribute) target;
    t.setCost(cost);
  }
}
