/*
 * Copyright (C) 2002-2007
 * Tsuyoshi Fukui <fukui556@oki.com>
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

package net.java.sen.filter.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Sentence;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.StreamFilter;

/**
 * A Filter that replaces multiple similar <code>Token</code>s with a single
 * composite <code>Token</code>
 */
public class CompositeTokenFilter implements StreamFilter {
  
  /**
   * A list of rules defining the tokens that are to be combined, and the
   * part-of-speech string to be used for the combined tokens 
   */
  private List<Rule> rules = new ArrayList<Rule>();
  
  /**
   * A rule defining the tokens that are to be combined, and the
   * part-of-speech string to be used for the combined tokens 
   */
  static class Rule {
    /**
     * The set of part-of-speech codes to be merged
     */
    private Set<String> ruleSet;
    
    /**
     * The part-of-speech code to be substituted for a sequence of matching
     * <code>Token</code>s by this rule
     */
    private String partOfSpeech;
    
    /**
     * Returns the part-of-speech code substituted for a sequence of
     * matching <code>Token</code>s by this rule
     *
     * @return The part-of-speech code
     */
    public String getPartOfSpeech() {
      return partOfSpeech;
    }
    
    /**
     * Determines if this rule contains a given part-of-speech code
     *
     * @param partOfSpeech The part-of-speech code
     * @return <code>true</code if the rule contains the part-of-speech
     *     code; <code>false</code> otherwise
     */
    public boolean contains(String partOfSpeech) {
      return ruleSet.contains(partOfSpeech);
    }
    
    /**
     * Removes a given part-of-speech code from this rule
     *
     * @param partOfSpeech The part-of-speech code
     */
    public void remove(String partOfSpeech) {
      ruleSet.remove(partOfSpeech);
    }
    
    @Override
    public String toString() {
      StringBuffer buffer = new StringBuffer(partOfSpeech);
      Iterator<String> iterator = ruleSet.iterator();
      while (iterator.hasNext()) {
        buffer.append(" ").append(iterator.next());
      }
      
      return new String(buffer);
    }
    
    /**
     * @param ruleSet The part-of-speech codes of the tokens that are to be combined
     * @param partOfSpeech The part-of-speech code to use for the combined token
     */
    public Rule(Set<String> ruleSet, String partOfSpeech) {
      this.ruleSet = ruleSet;
      this.partOfSpeech = partOfSpeech;
    }
  }
  
  /**
   * Removes a part-of-speech code from the definition of all existing rules
   *
   * @param partOfSpeech The part-of-speech code to remove
   */
  private void removeFromOtherRules(String partOfSpeech) {
    for (int i = 0; i < rules.size(); i++) {
      Rule rule = rules.get(i);
      if (rule.contains(partOfSpeech)) {
        rule.remove(partOfSpeech);
        return;
      }
    }
  }
  
  /**
   * Reads the rules to apply as space-delimited text
   *
   * @param reader The reader from which to read the rules
   * @throws IOException
   */
  public void readRules(BufferedReader reader) throws IOException {
    String line = null;
    while ((line = reader.readLine()) != null) {
      
      if (line.startsWith("#")) continue;
      StringTokenizer tokenizer = new StringTokenizer(line);
      if (!tokenizer.hasMoreTokens()) {
        continue;
      }
      
      Set<String> ruleSet = new HashSet<String>();
      String first = tokenizer.nextToken();
      if (!tokenizer.hasMoreTokens()) {
        // 1個しか無い場合は、連結品詞名であり、構成品詞名でもある
        removeFromOtherRules(first);
        ruleSet.add(first);
        rules.add(new Rule(ruleSet, first));
        continue;
      }
      
      while (tokenizer.hasMoreTokens()) {
        String partOfSpeech = tokenizer.nextToken();
        removeFromOtherRules(partOfSpeech);
        ruleSet.add(partOfSpeech);
      }
      
      rules.add(new Rule(ruleSet, first));
    }
  }
  
  /**
   * Merges two tokens, giving the combined token a new part-of-speech code
   * NOTE: Only the first reading and pronunciation will be taken from each
   * merged token. Any alternate readings or pronunciations will be discarded
   *
   * @param token1 The first token
   * @param token2 The second token
   * @param newPartOfSpeech The part-of-speech code to use for the combined token
   */
  private void merge(Token token1, Token token2, String newPartOfSpeech) {
    
    if (token1 == null) {
      return;
    }
    
    token1.setCost(token1.getCost() + token2.getCost());
    token1.setLength(token1.getLength() + token2.getLength());
    token1.setSurface(token1.getSurface() + token2.getSurface());
    String basicForm1 = token1.getMorpheme().getBasicForm();
    String basicForm2 = token2.getMorpheme().getBasicForm();
    
    final String mergedBasicForm;
    if (basicForm1.equals("*") && basicForm2.equals("*")) {
      mergedBasicForm = "*";
    } else {
      mergedBasicForm = 
          (basicForm1.equals("*") ? token1.getSurface() : basicForm1) + 
          (basicForm2.equals("*") ? token2.getSurface() : basicForm2);
    }

    Morpheme mergedMorpheme = new Morpheme(newPartOfSpeech,
                                           token1.getMorpheme().getConjugationalType(),
                                           token1.getMorpheme().getConjugationalForm(),
                                           mergedBasicForm,
                                           getReadings(token1, token2),
                                           getPronunciations(token1, token2),
                                           token1.getMorpheme().getAdditionalInformation());
    
    token1.setMorpheme(mergedMorpheme);
  }
  
  private String[] getReadings(Token token1, Token token2) {
    StringBuilder readings = null;
    
    if (token1.getMorpheme().getReadings().size() > 0) {
      if (readings == null) {
        readings = new StringBuilder();
      }
      readings.append(token1.getMorpheme().getReadings().get(0));
    }
    if (token2.getMorpheme().getReadings().size() > 0) {
      if (readings == null) {
        readings = new StringBuilder();
      }
      readings.append(token2.getMorpheme().getReadings().get(0));
    }
    return readings == null ? new String[0] : new String[] {readings.toString()};
  }
  
  private String[] getPronunciations(Token token1, Token token2) {
    StringBuilder pronunciations = null;
    
    if (token1.getMorpheme().getPronunciations().size() > 0) {
      if (pronunciations == null) {
        pronunciations = new StringBuilder();
      }
      pronunciations.append(token1.getMorpheme().getPronunciations().get(0));
    }
    if (token2.getMorpheme().getPronunciations().size() > 0) {
      if (pronunciations == null) {
        pronunciations = new StringBuilder();
      }
      pronunciations.append(token2.getMorpheme().getPronunciations().get(0));
    }
    return pronunciations == null ? new String[0] : new String[] {pronunciations.toString()};
  }
  
  public void preProcess(Sentence sentence) {
    // Do nothing
  }
  
  public List<Token> postProcess(List<Token> tokens) {
    
    if (tokens.size() == 0) {
      return tokens;
    }
    
    List<Token> newTokens = new ArrayList<Token>();
    Token prevToken = null;
    Rule currentRule = null;
    outer_loop: for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (currentRule != null) {
        if ((prevToken.end() != token.getStart()) || (!currentRule.contains(token.getMorpheme().getPartOfSpeech()))) {
          currentRule = null;
          newTokens.add(prevToken);
          prevToken = null;
        } else {
          merge(prevToken, token, currentRule.getPartOfSpeech());
          if (i == tokens.size() - 1) {
            newTokens.add(prevToken);
            prevToken = null;
          }
          continue;
        }
      }
      for (int j = 0; j < rules.size(); j++) {
        Rule rule = rules.get(j);
        if (rule.contains(token.getMorpheme().getPartOfSpeech())) {
          currentRule = rule;
          prevToken = token;
          continue outer_loop;
        }
      }
      currentRule = null;
      newTokens.add(token);
    }
    
    if (prevToken != null) {
      newTokens.add(prevToken);
    }
    
    return newTokens;
  }
}
