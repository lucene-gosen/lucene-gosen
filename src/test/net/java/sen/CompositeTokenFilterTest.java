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

import static net.java.sen.SenTestUtil.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.java.sen.StringTagger;
import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.stream.CompositeTokenFilter;

import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

/**
 * Test Composite Token filter
 */
public class CompositeTokenFilterTest extends LuceneTestCase {
  /**
   * Number composite
   * 
   * @throws IOException 
   */
  @Test
  public void testCompositeFilter1() throws IOException {
    String testString = "１１０";
    
    Token[] testTokens = new Token[] {
        new Token ("１１０", 8288, 0, 3, new Morpheme ("名詞-数", "*", "*", "*", new String[]{"イチイチゼロ"}, new String[]{"イチイチゼロ"}, null))
    };
    
    
    StringTagger tagger = getStringTagger();
    CompositeTokenFilter filter = new CompositeTokenFilter();
    filter.readRules (new BufferedReader (new StringReader ("名詞-数 名詞-数 名詞-数記号")));
    tagger.addFilter (filter);
    
    List<Token> tokens = tagger.analyze(testString);
    
    compareTokens (testTokens, tokens);
  }
  
  /**
   * Number composite
   * 
   * @throws IOException 
   */
  @Test
  public void testCompositeFilter2() throws IOException {
    String testString = "ロンドン０１７１ー１２３４５６７";
    
    Token[] testTokens = new Token[] {
        new Token ("ロンドン", 3040, 0, 4, new Morpheme ("名詞-固有名詞-地域-一般", "*", "*", "*", new String[]{"ロンドン"}, new String[]{"ロンドン"}, null)),
        new Token ("０１７１", 26418, 4, 4, new Morpheme ("名詞-数", "*", "*", "*", new String[]{"ゼロイチナナイチ"}, new String[]{"ゼロイチナナイチ"}, null)),
        new Token ("ー", 40038, 8, 1, new Morpheme ("未知語", null, null, "*", new String[]{}, new String[]{}, null)),
        new Token ("１２３４５６７", 322155, 9, 7, new Morpheme ("名詞-数", "*", "*", "*", new String[]{"イチニサンヨンゴロクナナ"}, new String[]{"イチニサンヨンゴロクナナ"}, null)),
    };
    
    
    StringTagger tagger = getStringTagger();
    CompositeTokenFilter filter = new CompositeTokenFilter();
    filter.readRules (new BufferedReader (new StringReader ("名詞-数 名詞-数 名詞-数記号")));
    tagger.addFilter (filter);
    
    List<Token> tokens = tagger.analyze(testString);
    
    compareTokens (testTokens, tokens);
  }
  
  /**
   * UnkownPOS composite
   * 
   * @throws IOException 
   */
  @Test
  public void testUnkownWordCompositeFilter() throws IOException {
    String testString = "ニンテンドーDSi";
    
    Token[] testTokens = new Token[] {
        new Token ("ニンテンドーDSi", 93001, 0, 9, new Morpheme ("未知語", null, null, "*", new String[]{}, new String[]{}, null))
    };
    
    
    StringTagger tagger = getStringTagger();
    CompositeTokenFilter filter = new CompositeTokenFilter();
    filter.readRules (new BufferedReader (new StringReader ("未知語 未知語")));
    tagger.addFilter (filter);
    
    List<Token> tokens = tagger.analyze(testString);
    
    compareTokens (testTokens, tokens);
  }
}
