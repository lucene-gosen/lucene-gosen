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
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.java.sen.StringTagger;
import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.stream.CompoundWordFilter;
import net.java.sen.tools.CompoundWordTableCompiler;

import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

/**
 * Test Compound Word Filter
 */
public class CompoundWordFilterTest extends LuceneTestCase {
  /**
   * Single compound token
   * 
   * @throws IOException 
   */
  @Test
  public void testCompound1() throws IOException {
    String testCompound = "駆け出し,3649,名詞,一般,*,*,*,*,*,カケダシ,カケダシ,\"駆け,3649,名詞,一般,*,*,*,*,*,カケ,カケ, 出し,3649,名詞,一般,*,*,*,*,*,ダシ,ダシ,\"";
    File tempCompoundFile = File.createTempFile("tmp", "tmp");
    tempCompoundFile.deleteOnExit();
    
    BufferedReader reader = new BufferedReader (new StringReader (testCompound));
    CompoundWordTableCompiler.buildTable(reader, 2, 7, tempCompoundFile.getAbsolutePath());
    
    String testString = "駆け出し";
    
    Token[] testTokens = new Token[] {
        new Token ("駆け", 4281, 0, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"カケ"}, new String[]{"カケ,"}, "")),
        new Token ("出し", 4281, 2, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"ダシ"}, new String[]{"ダシ,"}, ""))
    };
    
    
    StringTagger tagger = getStringTagger();
    CompoundWordFilter filter = new CompoundWordFilter (tempCompoundFile.getAbsolutePath());
    tagger.addFilter(filter);
    
    List<Token> tokens = tagger.analyze(testString);
    
    compareTokens (testTokens, tokens);
  }
  
  /**
   * Single compound token with conjugation type/form set
   * 
   * @throws IOException 
   */
  @Test
  public void testCompound2() throws IOException {
    
    String testCompound = "駆け出し,3508,動詞,自立,*,*,五段・サ行,連用形,駆け出す,カケダシ,カケダシ,\"駆け,3649,名詞,一般,*,*,*,*,*,カケ,カケ, 出し,3649,名詞,一般,*,*,*,*,*,ダシ,ダシ,\"";
    File tempCompoundFile = File.createTempFile("tmp", "tmp");
    tempCompoundFile.deleteOnExit();
    
    BufferedReader reader = new BufferedReader (new StringReader (testCompound));
    CompoundWordTableCompiler.buildTable(reader, 2, 7, tempCompoundFile.getAbsolutePath());
    
    String testString = "駆け出している";
    
    Token[] testTokens = new Token[] {
        new Token ("駆け", 5205, 0, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"カケ"}, new String[]{"カケ,"}, "")),
        new Token ("出し", 5205, 2, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"ダシ"}, new String[]{"ダシ,"}, "")),
        new Token ("て", 5551, 4, 1, new Morpheme ("助詞-接続助詞", "*", "*", "*", new String[]{"テ"}, new String[]{"テ"}, null)),
        new Token ("いる", 5794, 5, 2, new Morpheme ("動詞-非自立", "一段", "基本形", "*", new String[]{"イル"}, new String[]{"イル"}, null))
    };
    
    
    StringTagger tagger = getStringTagger();
    CompoundWordFilter filter = new CompoundWordFilter (tempCompoundFile.getAbsolutePath());
    tagger.addFilter(filter);
    
    List<Token> tokens = tagger.analyze(testString);
    
    compareTokens (testTokens, tokens);
  }
}
