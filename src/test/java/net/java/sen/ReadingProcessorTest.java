/*
 * Copyright (C) 2006-2007
 * Matt Francis <asbel@neosheffield.co.uk>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package net.java.sen;

import java.util.List;

import net.java.sen.dictionary.Reading;

import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

import static net.java.sen.SenTestUtil.*;

/**
 * Tests the usage of ReadingProcessor
 */
public class ReadingProcessorTest extends LuceneTestCase {
  /**
   * Test simple reading analysis
   */
  @Test
  public void testBasicAnalysis() {
    String testString = "皆様、只今より映画を上映いたします";
    
    Reading[] expectedReadings = new Reading[] {
        new Reading (0, 2, "みなさま"),
        new Reading (3, 2, "ただいま"),
        new Reading (7, 2, "えいが"),
        new Reading (10, 2, "じょうえい")
    };
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
  
  /**
   * Test analysis of a compound token
   */
  @Test
  public void testCompoundAnalysis() {
    String testString = "空を飛び越える";
    
    Reading[] expectedReadings = new Reading[] {
        new Reading (0, 1, "そら"),
        new Reading (2, 1, "と"),
        new Reading (4, 1, "こ")
    };
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
  
  /**
   * Test analysis of kanji tokens with constraints
   */
  @Test
  public void testKanjiConstraint() {
    String testString = "昨日";
    
    Reading[] expectedReadings = new Reading[] {
        new Reading (0, 1, "さく"),
        new Reading (1, 1, "ひ")
    };
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    processor.setReadingConstraint(new Reading(0, 1, "さく"));
    // "jitsu" might be better but is an alternative in ipadic
    processor.setReadingConstraint(new Reading(1, 1, "ひ"));
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
  
  /**
   * Test analysis of constraints on kanji tokens that have alternatives
   */
  @Test
  public void testKanjiAlternativesConstraint() {
    String testString = "今日は";
    
    Reading[] expectedReadings = new Reading[] {
        new Reading (0, 2, "こんにち")
    };
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    processor.setReadingConstraint(new Reading(0, 2, "こんにち"));
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
  
  /**
   * Test analysis of constraints on kana
   */
  @Test
  public void testKanaConstraint() {
    String testString = "こんにちは";
    
    Reading[] expectedReadings = new Reading[] {
        new Reading (0, 4, "今日")
    };
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    processor.setReadingConstraint(new Reading(0, 4, "今日"));
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
  
  /**
   * Test analysis of constraints on romaji
   */
  @Test
  public void testRomajiConstraint() {
    String testString = "Good day";
    
    Reading[] expectedReadings = new Reading[] {
        new Reading (0, 8, "今日")
    };
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    processor.setReadingConstraint(new Reading(0, 8, "今日"));
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
  
  /**
   * Test Alternative matching constraint on compound token
   */
  @Test
  public void testAlternativeCompoundConstraint() {
    String testString = "駆け出し";
    
    Reading[] expectedReadings = new Reading[] {
        new Reading (0, 1, "ま"),
        new Reading (2, 1, "つぶ")
    };
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    processor.setReadingConstraint(new Reading(0, 4, "まけつぶし"));
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
  
  /**
   * Test Fallback on non-matching reading on compound
   */
  @Test
  public void testCompoundFallback() {
    String testString = "駆け出し";
    
    Reading[] expectedReadings = new Reading[] {
        new Reading (0, 4, "完全に違う")
    };
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    processor.setReadingConstraint(new Reading(0, 4, "完全に違う"));
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
  
  /**
   * Test constraint causing a blank kanji reading
   */
  @Test
  public void testBlankKanjiReading() {
    String testString = "あたらしい構築";
    
    Reading[] expectedReadings = new Reading[] {
        new Reading (6, 1, "ちく")
    };
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    processor.setReadingConstraint(new Reading(6, 1, "ちく"));
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
  
  /**
   * Test non-override constraint on a kana morpheme - should not cause reading 
   */
  @Test
  public void testConstrainedKanaReading() {
    String testString = "ある";
    
    Reading[] expectedReadings = new Reading[] {};
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    processor.setReadingConstraint(new Reading(0, 1, "あ"));
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
  
  /**
   * Test override constraint on a kana token - should cause reading 
   */
  @Test
  public void testConstrainedKanaReading2() {
    String testString = "ある";
    
    Reading[] expectedReadings = new Reading[] {
        new Reading (0, 1, "く")
    };
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    processor.setReadingConstraint(new Reading(0, 1, "く"));
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
  
  /**
   * Test non-override constraint on a katakana token - should not cause reading 
   */
  @Test
  public void testConstrainedKanaReading3() {
    String testString = "アル";
    
    Reading[] expectedReadings = new Reading[] {};
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    processor.setReadingConstraint(new Reading(0, 1, "あ"));
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
  
  /**
   * Tests a blank constraint on a kanji token 
   */
  @Test
  public void testBlankConstraintKanji() {
    String testString = "見当";
    
    Reading[] expectedReadings = new Reading[] {};
    
    ReadingProcessor processor = getReadingProcessor();
    
    processor.setText (testString);
    processor.setReadingConstraint(new Reading(0, 2, ""));
    
    List<Reading> readings = processor.getDisplayReadings();
    
    compareReadings (expectedReadings, readings);
  }
}
