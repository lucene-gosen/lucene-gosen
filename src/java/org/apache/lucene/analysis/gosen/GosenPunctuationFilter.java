package org.apache.lucene.analysis.gosen;

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

import java.io.IOException;

import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 * Removes punctuation tokens
 */
public final class GosenPunctuationFilter extends FilteringTokenFilter {
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private CharArraySet protectedSet;

  /** @deprecated enablePositionIncrements=false is not supported anymore as of Lucene 4.4. */
  @Deprecated
  public GosenPunctuationFilter(Version version, boolean enablePositionIncrements, CharArraySet protectedSet, TokenStream input) {
    super(version, enablePositionIncrements, input);
    this.protectedSet = protectedSet;
  }

  public GosenPunctuationFilter(TokenStream input) {
    this(null, input);
  }

  public GosenPunctuationFilter(CharArraySet protectedSet, TokenStream input) {
    super(input);
    this.protectedSet = protectedSet;
  }

  @Override
  protected boolean accept() throws IOException {
    if (protectedSet != null && protectedSet.contains(termAtt.buffer()[0])) {
      return true;
    }
    return termAtt.length() > 0 && !isPunctuation(termAtt.buffer()[0]);
  }
  
  static final boolean isPunctuation(char ch) {
    switch(Character.getType(ch)) {
      case Character.SPACE_SEPARATOR:
      case Character.LINE_SEPARATOR:
      case Character.PARAGRAPH_SEPARATOR:
      case Character.CONTROL:
      case Character.FORMAT:
      case Character.DASH_PUNCTUATION:
      case Character.START_PUNCTUATION:
      case Character.END_PUNCTUATION:
      case Character.CONNECTOR_PUNCTUATION:
      case Character.OTHER_PUNCTUATION:
      case Character.MATH_SYMBOL:
      case Character.CURRENCY_SYMBOL:
      case Character.MODIFIER_SYMBOL:
      case Character.OTHER_SYMBOL:
      case Character.INITIAL_QUOTE_PUNCTUATION:
      case Character.FINAL_QUOTE_PUNCTUATION:
        return true;
      default:
        return false;
    }
  }
}
