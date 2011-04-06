package org.apache.lucene.analysis.ja.tokenAttributes;

import java.util.List;

import org.apache.lucene.util.AttributeImpl;

public class ReadingsAttributeImpl extends AttributeImpl implements ReadingsAttribute, Cloneable {
  private List<String> readings = null;
  
  @Override
  public List<String> getReadings() {
    return readings;
  }
  
  @Override
  public void setReadings(List<String> readings) {
    this.readings = readings;
  }

  @Override
  public void clear() {
    readings = null;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    ReadingsAttribute t = (ReadingsAttribute) target;
    t.setReadings(readings);
  }
}
