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
import java.io.Reader;

import net.java.sen.SenFactory;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.StreamFilter;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ja.tokenAttributes.BasicFormAttribute;
import org.apache.lucene.analysis.ja.tokenAttributes.ConjugationAttribute;
import org.apache.lucene.analysis.ja.tokenAttributes.CostAttribute;
import org.apache.lucene.analysis.ja.tokenAttributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.ja.tokenAttributes.PronunciationsAttribute;
import org.apache.lucene.analysis.ja.tokenAttributes.ReadingsAttribute;
import org.apache.lucene.analysis.ja.tokenAttributes.SentenceStartAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

/**
 * This is a Japanese tokenizer which uses "Sen" morphological
 * analyzer.
 * <p>
 * sets the surface form as the term text, but also sets these attributes:
 * <ul>
 *   <li>{@link BasicFormAttribute}
 *   <li>{@link ConjugationAttribute}
 *   <li>{@link PartOfSpeechAttribute}
 *   <li>{@link PronunciationsAttribute}
 *   <li>{@link ReadingsAttribute}
 *   <li>{@link TypeAttribute}
 *   <li>{@link CostAttribute}
 *   <li>{@link SentenceStartAttribute}
 * </ul>
 * <p>
 * TypeAttribute is set to the POS for simplicity, so you can use 
 * TypeAsPayloadTokenFilterFactory if you desire to index the POS 
 * into the payload
 */
public final class JapaneseTokenizer extends Tokenizer {
  private final StreamTagger2 tagger;
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
  // we set the type attribute to be the POS for simplicity (e.g. payloads searching)
  private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
  
  // morphological attributes
  private final BasicFormAttribute basicFormAtt = addAttribute(BasicFormAttribute.class);
  private final ConjugationAttribute conjugationAtt = addAttribute(ConjugationAttribute.class);
  private final PartOfSpeechAttribute partOfSpeechAtt = addAttribute(PartOfSpeechAttribute.class);
  private final PronunciationsAttribute pronunciationsAtt = addAttribute(PronunciationsAttribute.class);
  private final ReadingsAttribute readingsAtt = addAttribute(ReadingsAttribute.class);
  
  // sentence increment
  private final SentenceStartAttribute sentenceAtt = addAttribute(SentenceStartAttribute.class);

  // viterbi cost
  private final CostAttribute costAtt = addAttribute(CostAttribute.class);
  // viterbi costs from Token.getCost() are cumulative,
  // so we accumulate this so we can then subtract to present an absolute cost.
  private int accumulatedCost = 0;

  public JapaneseTokenizer(Reader in) {
    this(in, null);
  }

  public JapaneseTokenizer(Reader in, StreamFilter filter) {
    super(in);
    StringTagger stringTagger = SenFactory.getStringTagger();
    if(filter != null)
      stringTagger.addFilter(filter);
    tagger = new StreamTagger2(stringTagger, in);
  }

  @Override
  public boolean incrementToken() throws IOException {
    Token token = tagger.next();
    if (token == null) {
      return false;
    } else {
      clearAttributes();
      final Morpheme m = token.getMorpheme();
    
      // note, unlike the previous implementation, we set the surface form
      termAtt.setEmpty().append(token.getSurface());
      final int cost = token.getCost();
      
      if (token.isSentenceStart()) {
        accumulatedCost = 0;
        sentenceAtt.setSentenceStart(true);
      }
      
      costAtt.setCost(cost - accumulatedCost);
      accumulatedCost = cost;
      basicFormAtt.setBasicForm(m.getBasicForm());
      conjugationAtt.setConjugationalForm(m.getConjugationalForm());
      conjugationAtt.setConjugationalType(m.getConjugationalType());
      partOfSpeechAtt.setPartOfSpeech(m.getPartOfSpeech());
      pronunciationsAtt.setPronunciations(m.getPronunciations());
      readingsAtt.setReadings(m.getReadings());
      offsetAtt.setOffset(correctOffset(token.getStart()), correctOffset(token.end()));
      typeAtt.setType(m.getPartOfSpeech());
      return true;
    }
  }

  @Override
  public void reset(Reader in) throws IOException {
    super.reset(in);
    tagger.reset(in);
    accumulatedCost = 0;
  }

  @Override
  public void end() throws IOException {
    // set final offset
    final int finalOffset = correctOffset(tagger.end());
    offsetAtt.setOffset(finalOffset, finalOffset);
  }
}
