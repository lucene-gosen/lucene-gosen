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

import net.java.sen.SenFactory;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.StreamFilter;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.gosen.tokenAttributes.BasicFormAttribute;
import org.apache.lucene.analysis.gosen.tokenAttributes.ConjugationAttribute;
import org.apache.lucene.analysis.gosen.tokenAttributes.CostAttribute;
import org.apache.lucene.analysis.gosen.tokenAttributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.gosen.tokenAttributes.PronunciationsAttribute;
import org.apache.lucene.analysis.gosen.tokenAttributes.ReadingsAttribute;
import org.apache.lucene.analysis.gosen.tokenAttributes.SentenceStartAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;

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
 *   <li>{@link CostAttribute}
 *   <li>{@link SentenceStartAttribute}
 * </ul>
 */
public final class GosenTokenizer extends Tokenizer {
  private final StreamTagger2 tagger;

  // Term attributes
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
  
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

  // Default value for UNKNOWN Katakana tokenization
  public static final boolean DEFAULT_UNKNOWN_KATAKANA_TOKENIZATION = false;


  /**
   * Constructors
   */
  public GosenTokenizer(StreamFilter filter, String dictionaryDir, boolean tokenizeUnknownKatakana) {
    this(DEFAULT_TOKEN_ATTRIBUTE_FACTORY, filter, dictionaryDir, tokenizeUnknownKatakana);
  }

  /**
   * Create A new GosenTokenizer
   *
   * @param factory the AttributeFactory to use
   * @param filter stream filter
   * @param dictionaryDir lucene-gosen dictionary directory
   * @param tokenizeUnknownKatakana determine whether segmenting unknown katakana or not
   */
  public GosenTokenizer(AttributeFactory factory, StreamFilter filter, String dictionaryDir, boolean tokenizeUnknownKatakana) {
    super(factory);
    StringTagger stringTagger = SenFactory.getStringTagger(dictionaryDir, tokenizeUnknownKatakana);
    if (filter != null) {
      stringTagger.addFilter(filter);
    }
    tagger = new StreamTagger2(stringTagger, this.input);
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
      basicFormAtt.setMorpheme(m);
      conjugationAtt.setMorpheme(m);
      partOfSpeechAtt.setMorpheme(m);
      pronunciationsAtt.setMorpheme(m);
      readingsAtt.setMorpheme(m);
      offsetAtt.setOffset(correctOffset(token.getStart()), correctOffset(token.end()));
      return true;
    }
  }

  @Override
  public void close() throws IOException {
    super.close();
    tagger.reset(input);
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    tagger.reset(input);
    accumulatedCost = 0;
  }

  @Override
  public void end() throws IOException {
    super.end();
    // set final offset
    final int finalOffset = correctOffset(tagger.end());
    offsetAtt.setOffset(finalOffset, finalOffset);
  }
}
