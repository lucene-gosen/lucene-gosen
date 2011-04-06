package org.apache.lucene.analysis.ja.tokenAttributes;

import java.util.List;

import org.apache.lucene.util.Attribute;

public interface PronunciationsAttribute extends Attribute {
  public List<String> getPronunciations();
  public void setPronunciations(List<String> pronunciations);
}
