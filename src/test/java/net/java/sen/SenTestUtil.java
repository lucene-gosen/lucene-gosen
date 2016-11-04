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

import junit.framework.Assert;

import net.java.sen.dictionary.Reading;
import net.java.sen.dictionary.Token;
import net.java.sen.dictionary.Viterbi;

/**
 * Test utilities
 */
public class SenTestUtil {
  /**
   * A StringTagger for testing
   */
  private static StringTagger stringTagger = null;
  
  /**
   * A Viterbi for testing
   */
  private static Viterbi viterbi = null;
  
  /**
   * A Reading Processor for testing
   */
  private static ReadingProcessor readingProcessor = null;
  
  /**
   * IPADIC dictionary directory path
   */
  public static final String IPADIC_DIR = "./dictionary/ipadic";
  
  /**
   * Returns a StringTagger for testing
   *
   * @return The StringTagger
   */
  public static StringTagger getStringTagger() {
    if (stringTagger == null) {
      stringTagger = SenFactory.getStringTagger(IPADIC_DIR, false);
    }
    
    stringTagger.removeFilters();
    
    return stringTagger;
  }
  
  /**
   * Returns a Viterbi for testing
   *
   * @return The Viterbi
   */
  public static Viterbi getViterbi() {
    if (viterbi == null) {
      viterbi = SenFactory.getViterbi(IPADIC_DIR, false);
    }
    
    return viterbi;
  }
  
  /**
   * Returns a Reading Processor for testing
   *
   * @return The Reading Processor
   */
  public static ReadingProcessor getReadingProcessor() {
    if (readingProcessor == null) {
      readingProcessor = SenFactory.getReadingProcessor(IPADIC_DIR, false);
    }
    
    readingProcessor.clearFilters();
    
    return readingProcessor;
  }
  
  /**
   * Returns a string surrounded with quotes, or an unquoted string "null"
   *
   * @param string The string to quote
   * @return The quoted string
   */
  public static String quotedStringOrNull (String string) {
    return (string == null) ? "null" : ("\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
  }
  
  /**
   * Returns a quoted string array initialiser, or an unquoted string "null"
   *
   * @param stringList The list of strings to quote
   * @return The quoted string array
   */
  public static String quotedStringArrayOrNull (List<String> stringList) {
    if (stringList == null) {
      return "null";
    }
    
    String quotedStringArray = "new String[]{";
    
    for (int i = 0; i < stringList.size(); i++) {
      quotedStringArray += "\"" + stringList.get(i).replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
      if (i < (stringList.size() - 1)) {
        quotedStringArray += ", ";
      }
    }
    
    quotedStringArray += "}";
    
    return quotedStringArray;
  }
  
  /**
   * Print code to create an array of tokens
   *
   * @param tokens The tokens to encode
   */
  public static void printTokenCode (List<Token> tokens) {
    for (Token token : tokens) {
      String morphemeString = String.format (
          "new Morpheme (%1$s, %2$s, %3$s, %4$s, %5$s, %6$s, %7$s)",
          quotedStringOrNull (token.getMorpheme().getPartOfSpeech()),
          quotedStringOrNull (token.getMorpheme().getConjugationalType()),
          quotedStringOrNull (token.getMorpheme().getConjugationalForm()),
          quotedStringOrNull (token.getMorpheme().getBasicForm()),
          quotedStringArrayOrNull (token.getMorpheme().getReadings()),
          quotedStringArrayOrNull (token.getMorpheme().getPronunciations()),
          quotedStringOrNull (token.getMorpheme().getAdditionalInformation())
      );
      
      String tokenString = String.format(
          "new Token (%1$s, %2$d, %3$d, %4$d, %5$s),",
          quotedStringOrNull (token.getSurface()),
          token.getCost(),
          token.getStart(),
          token.getLength(),
          morphemeString
      );
      
      System.out.println (tokenString);
    }
  }
  
  /**
   * Compare two arrays of <code>Token</code>s
   *
   * @param expectedTokens The expected tokens
   * @param actualTokens The actual tokens
   */
  public static void compareTokens (Token[] expectedTokens, List<Token> actualTokens) {
    Assert.assertEquals (expectedTokens.length, actualTokens.size());
    
    for (int i = 0; i < expectedTokens.length; i++) {  
      if (!expectedTokens[i].equals(actualTokens.get(i))) {
        String expectedMorpheme = expectedTokens[i].getMorpheme() == null ? "" : expectedTokens[i].getMorpheme().toString();
        String actualMorpheme = actualTokens.get(i).getMorpheme() == null ? "" : actualTokens.get(i).getMorpheme().toString();
        
        String error = "";
        error += String.format ("surface: %1$20s : %2$20s\n", expectedTokens[i].getSurface(), actualTokens.get(i).getSurface());
        error += String.format ("cost: %1$20s : %2$20s\n", expectedTokens[i].getCost(), actualTokens.get(i).getCost());
        error += String.format ("start: %1$20s : %2$20s\n", expectedTokens[i].getStart(), actualTokens.get(i).getStart());
        error += String.format ("length: %1$20s : %2$20s\n", expectedTokens[i].getLength(), actualTokens.get(i).getLength());
        error += String.format ("morpheme: %1$20s : %2$20s\n", expectedMorpheme, actualMorpheme);
        
        Assert.fail("Tokens don't match\n" + error);
      }
    }
  }
  
  /**
   * Compare two arrays of <code>Token</code>s
   *
   * @param expectedReadings The expected tokens
   * @param actualReadings The actual tokens
   */
  public static void compareReadings (Reading[] expectedReadings, List<Reading> actualReadings) {
    Assert.assertEquals (expectedReadings.length, actualReadings.size());
    
    for (int i = 0; i < expectedReadings.length; i++) {
      if (!expectedReadings[i].equals(actualReadings.get(i))) {
        String error = "";
        error += String.format ("start:  %1$20d : %2$20d\n", expectedReadings[i].start, actualReadings.get(i).start);
        error += String.format ("length: %1$20d : %2$20d\n", expectedReadings[i].length, actualReadings.get(i).length);
        error += String.format ("text:   %1$20s : %2$20s\n", expectedReadings[i].text, actualReadings.get(i).text);
        
        Assert.fail("Readings don't match\n" + error);
      }
    }
  }
  
  /**
   * Assert that two arrays of shorts are equal
   *
   * @param expected The expected array
   * @param actual The actual array
   */
  public static void assertEqualsShortArray (short[] expected, short[] actual) {
    Assert.assertEquals ("Array lengths not equal", expected.length, actual.length);
    
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals ("Arrays not equal at index " + i, expected[i], actual[i]);
    }
  }
}
