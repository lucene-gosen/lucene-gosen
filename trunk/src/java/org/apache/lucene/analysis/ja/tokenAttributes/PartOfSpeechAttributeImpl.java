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

import java.util.HashMap;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

public class PartOfSpeechAttributeImpl extends AttributeImpl implements PartOfSpeechAttribute, Cloneable {
  private String partOfSpeech = null;
  
  public String getPartOfSpeech() {
    return partOfSpeech;
  }
  
  public void setPartOfSpeech(String partOfSpeech) {
    this.partOfSpeech = partOfSpeech;
  }

  @Override
  public void clear() {
    partOfSpeech = null;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    PartOfSpeechAttribute t = (PartOfSpeechAttribute) target;
    t.setPartOfSpeech(partOfSpeech);
  }
  
  @Override
  public void reflectWith(AttributeReflector reflector) {
    String en = partOfSpeech == null ? null : posTranslations.get(partOfSpeech);
    reflector.reflect(PartOfSpeechAttribute.class, "partOfSpeech", partOfSpeech);
    reflector.reflect(PartOfSpeechAttribute.class, "partOfSpeech (en)", en);
  }
  
  // a translation map for parts of speech, only used for reflectWith
  private static final HashMap<String,String> posTranslations = new HashMap<String,String>();
  static {
    posTranslations.put("名詞", "noun");
    posTranslations.put("名詞-一般", "noun-common");
    posTranslations.put("名詞-固有名詞", "noun-proper");
    posTranslations.put("名詞-固有名詞-一般", "noun-proper-misc");
    posTranslations.put("名詞-固有名詞-人名", "noun-proper-person");
    posTranslations.put("名詞-固有名詞-人名-一般", "noun-proper-person-misc");
    posTranslations.put("名詞-固有名詞-人名-姓", "noun-proper-person-surname");
    posTranslations.put("名詞-固有名詞-人名-名", "noun-proper-person-given_name");
    posTranslations.put("名詞-固有名詞-組織", "noun-proper-organization");
    posTranslations.put("名詞-固有名詞-地域", "noun-proper-place");
    posTranslations.put("名詞-固有名詞-地域-一般", "noun-proper-place-misc");
    posTranslations.put("名詞-固有名詞-地域-国", "noun-proper-place-country");
    posTranslations.put("名詞-代名詞", "noun-pronoun");
    posTranslations.put("名詞-代名詞-一般", "noun-pronoun-misc");
    posTranslations.put("名詞-代名詞-縮約", "noun-pronoun-contraction");
    posTranslations.put("名詞-副詞可能", "noun-adverbial");
    posTranslations.put("名詞-サ変接続", "noun-verbal");
    posTranslations.put("名詞-形容動詞語幹", "noun-adjective-base");
    posTranslations.put("名詞-数", "noun-numeric");
    posTranslations.put("名詞-非自立", "noun-affix");
    posTranslations.put("名詞-非自立-一般", "noun-affix-misc");
    posTranslations.put("名詞-非自立-副詞可能", "noun-affix-adverbial");
    posTranslations.put("名詞-非自立-助動詞語幹", "noun-affix-aux");
    posTranslations.put("名詞-非自立-形容動詞語幹", "noun-affix-adjective-base");
    posTranslations.put("名詞-特殊", "noun-special");
    posTranslations.put("名詞-特殊-助動詞語幹", "noun-special-aux");
    posTranslations.put("名詞-接尾", "noun-suffix");
    posTranslations.put("名詞-接尾-一般", "noun-suffix-misc");
    posTranslations.put("名詞-接尾-人名", "noun-suffix-person");
    posTranslations.put("名詞-接尾-地域", "noun-suffix-place");
    posTranslations.put("名詞-接尾-サ変接続", "noun-suffix-verbal");
    posTranslations.put("名詞-接尾-助動詞語幹", "noun-suffix-aux");
    posTranslations.put("名詞-接尾-形容動詞語幹", "noun-suffix-adjective-base");
    posTranslations.put("名詞-接尾-副詞可能", "noun-suffix-adverbial");
    posTranslations.put("名詞-接尾-助数詞", "noun-suffix-classifier");
    posTranslations.put("名詞-接尾-特殊", "noun-suffix-special");
    posTranslations.put("名詞-接続詞的", "noun-suffix-conjunctive");
    posTranslations.put("名詞-動詞非自立的", "noun-verbal_aux");
    posTranslations.put("名詞-引用文字列", "noun-quotation");
    posTranslations.put("名詞-ナイ形容詞語幹", "noun-nai_adjective");
    posTranslations.put("接頭詞", "prefix");
    posTranslations.put("接頭詞-名詞接続", "prefix-nominal");
    posTranslations.put("接頭詞-動詞接続", "prefix-verbal");
    posTranslations.put("接頭詞-形容詞接続", "prefix-adjectival");
    posTranslations.put("接頭詞-数接続", "prefix-numerical");
    posTranslations.put("動詞", "verb");
    posTranslations.put("動詞-自立", "verb-main");
    posTranslations.put("動詞-非自立", "verb-auxiliary");
    posTranslations.put("動詞-接尾", "verb-suffix");
    posTranslations.put("形容詞", "adjective");
    posTranslations.put("形容詞-自立", "adjective-main");
    posTranslations.put("形容詞-非自立", "adjective-auxiliary");
    posTranslations.put("形容詞-接尾", "adjective-suffix");
    posTranslations.put("副詞", "adverb");
    posTranslations.put("副詞-一般", "adverb-misc");
    posTranslations.put("副詞-助詞類接続", "adverb-particle_conjunction");
    posTranslations.put("連体詞", "adnominal");
    posTranslations.put("接続詞", "conjunction");
    posTranslations.put("助詞", "particle");
    posTranslations.put("助詞-格助詞", "particle-case");
    posTranslations.put("助詞-格助詞-一般", "particle-case-misc");
    posTranslations.put("助詞-格助詞-引用", "particle-case-quote");
    posTranslations.put("助詞-格助詞-連語", "particle-case-compound");
    posTranslations.put("助詞-接続助詞", "particle-conjunctive");
    posTranslations.put("助詞-係助詞", "particle-dependency");
    posTranslations.put("助詞-副助詞", "particle-adverbial");
    posTranslations.put("助詞-間投助詞", "particle-interjective");
    posTranslations.put("助詞-並立助詞", "particle-coordinate");
    posTranslations.put("助詞-終助詞", "particle-final");
    posTranslations.put("助詞-副助詞／並立助詞／終助詞", "particle-adverbial/conjunctive/final");
    posTranslations.put("助詞-連体化", "particle-adnominalizer (no)");
    posTranslations.put("助詞-副詞化", "particle-adnominalizer (ni/to)");
    posTranslations.put("助詞-特殊", "particle-special");
    posTranslations.put("助動詞", "auxiliary-verb");
    posTranslations.put("感動詞", "interjection");
    posTranslations.put("記号", "symbol");
    posTranslations.put("記号-一般", "symbol-misc");
    posTranslations.put("記号-句点", "symbol-comma");
    posTranslations.put("記号-読点", "symbol-period");
    posTranslations.put("記号-空白", "symbol-space");
    posTranslations.put("記号-括弧開", "symbol-open_bracket");
    posTranslations.put("記号-括弧閉", "symbol-close_bracket");
    posTranslations.put("記号-アルファベット", "symbol-alphabetic");
    posTranslations.put("その他", "other");
    posTranslations.put("その他-間投", "other-interjection");
    posTranslations.put("フィラー", "filler");
    posTranslations.put("非言語音", "non-verbal");
    posTranslations.put("語断片", "fragment");
    posTranslations.put("未知語", "unknown");
  }
}
