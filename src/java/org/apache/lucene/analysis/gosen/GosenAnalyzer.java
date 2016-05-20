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

package org.apache.lucene.analysis.gosen;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

/**
 * Analyzer for Japanese which uses "Sen" morphological analyzer.
 */
public class GosenAnalyzer extends StopwordAnalyzerBase {
  private final Set<String> stoptags;
  private final CharArraySet stemExclusionSet;
  private final String dictionaryDir;
  private final boolean compatibilityMode;

  public static Set<?> getDefaultStopSet(){
    return DefaultSetHolder.DEFAULT_STOP_SET;
  }
  
  public static Set<String> getDefaultStopTags(){
    return DefaultSetHolder.DEFAULT_STOP_TAGS;
  }
  
  /**
   * Atomically loads DEFAULT_STOP_SET, DEFAULT_STOP_TAGS in a lazy fashion once the 
   * outer class accesses the static final set the first time.
   */
  private static class DefaultSetHolder {
    static final CharArraySet DEFAULT_STOP_SET;
    static final Set<String> DEFAULT_STOP_TAGS;

    static {
      try {
        DEFAULT_STOP_SET = loadStopwordSet(false, GosenAnalyzer.class, "stopwords_ja.txt", "#");
        final CharArraySet tagset = loadStopwordSet(false, GosenAnalyzer.class, "stoptags_ja.txt", "#");
        DEFAULT_STOP_TAGS = new HashSet<String>();
        for (Object element : tagset) {
          char chars[] = (char[]) element;
          DEFAULT_STOP_TAGS.add(new String(chars));
        }
      } catch (IOException ex) {
        // default set should always be present as it is part of the
        // distribution (JAR)
        throw new RuntimeException("Unable to load default stopword set");
      }
    }
  }
  
  /**
   * Create a GosenAnalyzer with the default stopwords and stoptags and no stemExclusionSet
   */
  public GosenAnalyzer() {
    this(DefaultSetHolder.DEFAULT_STOP_SET, DefaultSetHolder.DEFAULT_STOP_TAGS, CharArraySet.EMPTY_SET, null);
  }

  /**
   * Create a GosenAnalyzer with the default stopwords and stoptags and no stemExclusionSet<br>
   * and argument of dictionaryDir.
   */
  public GosenAnalyzer(String dictionaryDir) {
    this(DefaultSetHolder.DEFAULT_STOP_SET, DefaultSetHolder.DEFAULT_STOP_TAGS, CharArraySet.EMPTY_SET, dictionaryDir);
  }
  
  /**
   * Create a GosenAnalyzer with the specified stopwords, stoptags, and stemExclusionSet
   */
  public GosenAnalyzer(CharArraySet stopwords, Set<String> stoptags, CharArraySet stemExclusionSet, String dictionaryDir) {
    this(stopwords, stoptags, stemExclusionSet, dictionaryDir, true);
  }

  /**
   * Create a GosenAnalyzer with the specified stopwords, stoptags, stemExclusionSet, dictionaryDir and compatibilityMode
   *
   * @param stopwords         a stopword set: words matching these words will be removed from the stream.
   * @param stoptags          a stoptags set: words containing these parts of speech will be removed from the stream.
   * @param stemExclusionSet  a stemming exclusion set: these words are ignored by
   *                           {@link GosenBasicFormFilter} and {@link GosenKatakanaStemFilter}
   * @param dictionaryDir     a directory of dictionarr
   * @param compatibilityMode a flag that control segmentation behaviour :
   *                           if false, will not concatenate consecutive Katakana tokens when one of them is an UNKNOWN.
   */
  public GosenAnalyzer(CharArraySet stopwords, Set<String> stoptags, CharArraySet stemExclusionSet, String dictionaryDir,
                       boolean compatibilityMode) {
    super(stopwords);
    this.stoptags = stoptags;
    this.stemExclusionSet = stemExclusionSet;
    this.dictionaryDir = dictionaryDir;
    this.compatibilityMode = compatibilityMode;
  }

  /**
   * Creates
   * {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
   * used to tokenize all the text in the provided {@link Reader}.
   * 
   * @return {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
   *         built from a {@link GosenTokenizer} filtered with
   *         {@link GosenWidthFilter}, {@link GosenPunctuationFilter},
   *         {@link GosenPartOfSpeechStopFilter}, {@link StopFilter},
   *         {@link SetKeywordMarkerFilter} if a stem exclusion set is provided,
   *         {@link GosenBasicFormFilter}, {@link GosenKatakanaStemFilter},
   *         and  {@link LowerCaseFilter}
   */
  @Override
  protected TokenStreamComponents createComponents(String field) {
    Tokenizer tokenizer = new GosenTokenizer(null, dictionaryDir, compatibilityMode);
    TokenStream stream = new GosenWidthFilter(tokenizer);
    stream = new GosenPunctuationFilter(stream);
    stream = new GosenPartOfSpeechStopFilter(stream, stoptags);
    stream = new StopFilter(stream, stopwords);
    if (!stemExclusionSet.isEmpty())
      stream = new SetKeywordMarkerFilter(stream, stemExclusionSet);
    stream = new GosenBasicFormFilter(stream);
    stream = new GosenKatakanaStemFilter(stream);
    stream = new LowerCaseFilter(stream);
    return new TokenStreamComponents(tokenizer, stream);
  }
}
