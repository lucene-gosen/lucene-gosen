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
import net.java.sen.StreamTagger;
import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Token;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ja.tokenAttributes.BasicFormAttribute;
import org.apache.lucene.analysis.ja.tokenAttributes.ConjugationAttribute;
import org.apache.lucene.analysis.ja.tokenAttributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.ja.tokenAttributes.PronunciationsAttribute;
import org.apache.lucene.analysis.ja.tokenAttributes.ReadingsAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;


/**
 * This is a Japanese tokenizer which uses "Sen" morphological
 * analyzer.
 *
 */
public final class JapaneseTokenizer extends Tokenizer {
  private StreamTagger tagger = null;
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

  // TODO: make normalization a charfilter
  public JapaneseTokenizer(Reader in) {
    tagger = new StreamTagger(SenFactory.getStringTagger(), new NormalizeReader(in));
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
      basicFormAtt.setBasicForm(m.getBasicForm());
      conjugationAtt.setConjugationalForm(m.getConjugationalForm());
      conjugationAtt.setConjugationalType(m.getConjugationalType());
      partOfSpeechAtt.setPartOfSpeech(m.getPartOfSpeech());
      pronunciationsAtt.setPronunciations(m.getPronunciations());
      readingsAtt.setReadings(m.getReadings());
      offsetAtt.setOffset(token.getStart(), token.end());
      typeAtt.setType(m.getPartOfSpeech());
      return true;
    }
  }

  @Override
  public void reset(Reader input) throws IOException {
    super.reset(input);
    tagger = new StreamTagger(SenFactory.getStringTagger(), new NormalizeReader(input));
  }
}
