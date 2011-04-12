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

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;

public class JapaneseAnalyzer extends StopwordAnalyzerBase {
  private final Set<String> stopTags;

  public JapaneseAnalyzer(Version version, Set<?> stopwords, Set<String> stopTags) {
    super(version, stopwords);
    this.stopTags = stopTags;
  }

  @Override
  protected TokenStreamComponents createComponents(String field, Reader reader) {
    Tokenizer tokenizer = new JapaneseTokenizer(reader);
    TokenStream stream = new JapanesePunctuationFilter(true, tokenizer);
    stream = new JapaneseBasicFormFilter(tokenizer);
    stream = new JapanesePartOfSpeechStopFilter(true, tokenizer, stopTags);
    stream = new StopFilter(matchVersion, stream, stopwords);
    stream = new LowerCaseFilter(matchVersion, stream);
    stream = new JapaneseKatakanaStemFilter(stream);
    return new TokenStreamComponents(tokenizer, stream);
  }
}
