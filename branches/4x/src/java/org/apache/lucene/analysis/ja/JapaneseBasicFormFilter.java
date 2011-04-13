package org.apache.lucene.analysis.ja;

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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.tokenAttributes.BasicFormAttribute;
import org.apache.lucene.analysis.miscellaneous.KeywordMarkerFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

/**
 * Replaces term text with the {@link BasicFormAttribute}.
 * <p>
 * This acts as a lemmatizer for verbs and adjectives.
 * <p>
 * To prevent terms from being stemmed use an instance of
 * {@link KeywordMarkerFilter} or a custom {@link TokenFilter} that sets
 * the {@link KeywordAttribute} before this {@link TokenStream}.
 * </p>
 */
public final class JapaneseBasicFormFilter extends TokenFilter {
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final BasicFormAttribute basicFormAtt = addAttribute(BasicFormAttribute.class);
  private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

  public JapaneseBasicFormFilter(TokenStream input) {
    super(input);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (!keywordAtt.isKeyword()) {
        termAtt.setEmpty().append(basicFormAtt.getBasicForm());
      }
      return true;
    } else {
      return false;
    }
  }
}
