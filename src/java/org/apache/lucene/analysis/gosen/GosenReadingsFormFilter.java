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

package org.apache.lucene.analysis.gosen;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.gosen.tokenAttributes.ReadingsAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

/**
 * Replaces term text with the {@link ReadingsAttribute}.
 * <p>
 * To prevent terms from being replaced use an instance of
 * {@link SetKeywordMarkerFilter} or a custom {@link TokenFilter} that sets
 * the {@link KeywordAttribute} before this {@link TokenStream}.
 * </p>
 */
public final class GosenReadingsFormFilter extends TokenFilter {
  
  private boolean romanized;
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final ReadingsAttribute readingsAtt = addAttribute(ReadingsAttribute.class);
  private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

  public GosenReadingsFormFilter(TokenStream input) {
    this(input, false);
  }

  public GosenReadingsFormFilter(TokenStream input, boolean romanized) {
    super(input);
    this.romanized = romanized;
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (!keywordAtt.isKeyword()) {
        List<String> readings = readingsAtt.getReadings();
        if (readings != null){ 
          StringBuilder sb = new StringBuilder();
          for(String reading : readings){
            sb.append(romanized ? ToStringUtil.getRomanization(reading) : reading);
          }
          termAtt.setEmpty().append(sb.toString());
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
