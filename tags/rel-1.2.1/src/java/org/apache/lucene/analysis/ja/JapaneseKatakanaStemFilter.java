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

import org.apache.lucene.analysis.KeywordMarkerFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

/**
 * Convert a katakana word to a normalized form by stemming KATAKANA-HIRAGANA
 * PROLONGED SOUND MARK (U+30FC) which exists at the last of the string. In
 * general, most of Japanese full-text search engine uses more complicated
 * method which needs dictionaries. I think they are better than this filter in
 * quality, but they needs a well-tuned dictionary. In contract, this filter is
 * simple and maintenance-free.
 * <p>
 * Note: This filter don't supports hankaku katakana characters, so you must
 * convert them before using this filter. And this filter support only
 * pre-composed characters.
 * <p>
 * To prevent terms from being stemmed use an instance of
 * {@link KeywordMarkerFilter} or a custom {@link TokenFilter} that sets
 * the {@link KeywordAttribute} before this {@link TokenStream}.
 */
public final class JapaneseKatakanaStemFilter extends TokenFilter {
  static final char COMBINING_KATAKANA_HIRAGANA_VOICED_SOUND_MARK = '\u3099';
  static final char COMBINING_KATAKANA_HIRAGANA_SEMI_VOICED_SOUND_MARK = '\u309A';
  static final char KATAKANA_HIRAGANA_VOICED_SOUND_MARK = '\u309B';
  static final char KATAKANA_HIRAGANA_SEMI_VOICED_SOUND_MARK = '\u309C';
  static final char KATAKANA_HIRAGANA_PROLONGED_SOUND_MARK = '\u30FC';

  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

  public JapaneseKatakanaStemFilter(TokenStream in) {
    super(in);
  }

  /**
   * Returns the next input Token, after being stemmed
   */
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (!keywordAtt.isKeyword()) {
        final char buffer[] = termAtt.buffer();
        int length = termAtt.length();
        if (length > 3 && buffer[length-1] == KATAKANA_HIRAGANA_PROLONGED_SOUND_MARK && isKatakanaString(buffer, length)) {
          termAtt.setLength(length - 1);
        }
      }
      return true;
    } else {
      return false;
    }
  }
  
  boolean isKatakanaString(char s[], int length) {
    for (int i = 0; i < length; i++) {
      final char c = s[i];
      if (Character.UnicodeBlock.of(c) != Character.UnicodeBlock.KATAKANA
          && c != COMBINING_KATAKANA_HIRAGANA_VOICED_SOUND_MARK
          && c != COMBINING_KATAKANA_HIRAGANA_SEMI_VOICED_SOUND_MARK
          && c != KATAKANA_HIRAGANA_VOICED_SOUND_MARK
          && c != KATAKANA_HIRAGANA_SEMI_VOICED_SOUND_MARK)
        return false;
    }
    return true;
  }
}