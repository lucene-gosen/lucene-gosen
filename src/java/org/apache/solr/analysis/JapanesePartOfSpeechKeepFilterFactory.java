package org.apache.solr.analysis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapanesePartOfSpeechKeepFilter;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.util.plugin.ResourceLoaderAware;

public class JapanesePartOfSpeechKeepFilterFactory extends BaseTokenFilterFactory implements ResourceLoaderAware {
  private boolean enablePositionIncrements;
  private Set<String> keepTags;

  @Override
  public void inform(ResourceLoader loader) {
    String keepTagFiles = args.get("tags");
    enablePositionIncrements = getBoolean("enablePositionIncrements", false);
    try {
      CharArraySet cas = getWordSet(loader, keepTagFiles, false);
      keepTags = new HashSet<String>();
      for (Object element : cas) {
        char chars[] = (char[]) element;
        keepTags.add(new String(chars));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public TokenStream create(TokenStream stream) {
    return new JapanesePartOfSpeechKeepFilter(enablePositionIncrements, stream, keepTags);
  }
}
