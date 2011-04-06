package org.apache.lucene.analysis.ja;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
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
