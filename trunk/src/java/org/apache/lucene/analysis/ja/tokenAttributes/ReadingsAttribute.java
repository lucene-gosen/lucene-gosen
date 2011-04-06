package org.apache.lucene.analysis.ja.tokenAttributes;

import java.util.List;

import org.apache.lucene.util.Attribute;

public interface ReadingsAttribute extends Attribute {
  public List<String> getReadings();
  public void setReadings(List<String> readings);
}
