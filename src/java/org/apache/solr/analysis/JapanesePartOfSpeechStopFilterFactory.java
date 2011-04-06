package org.apache.solr.analysis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapanesePartOfSpeechStopFilter;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.util.plugin.ResourceLoaderAware;

public class JapanesePartOfSpeechStopFilterFactory extends BaseTokenFilterFactory implements ResourceLoaderAware {
  private boolean enablePositionIncrements;
  private Set<String> stopTags;

  @Override
  public void inform(ResourceLoader loader) {
    String stopTagFiles = args.get("tags");
    enablePositionIncrements = getBoolean("enablePositionIncrements", false);
    try {
      CharArraySet cas = getWordSet(loader, stopTagFiles, false);
      stopTags = new HashSet<String>();
      for (Object element : cas) {
        char chars[] = (char[]) element;
        stopTags.add(new String(chars));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public TokenStream create(TokenStream stream) {
    return new JapanesePartOfSpeechStopFilter(enablePositionIncrements, stream, stopTags);
  }
}
