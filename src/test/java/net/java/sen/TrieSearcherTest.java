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

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.NoSuchElementException;

import net.java.sen.trie.CharIterator;
import net.java.sen.trie.TrieBuilder;
import net.java.sen.trie.TrieSearcher;

import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

/**
 * Test for TrieSearcher
 */
public class TrieSearcherTest extends LuceneTestCase {
  
  /**
   * Tests basic TrieSearcher functionality
   *
   * @throws Exception 
   */
  @Test
  public void testCharIterator() throws Exception {
    String[] keys = new String[] {
        "a",
        "ab",
        "abc",
        "c",
        "cd",
        "cde",
        "q",
        "qw",
        "qwe",
        "qwer",
        "qwert",
        "qwerty",
        "qwertyu"
    };
    
    int[] values = new int[] {
        101,
        201,
        301,
        401,
        501,
        601,
        701,
        801,
        901,
        1001,
        1101,
        1201,
        1301,
    };
    
    TrieBuilder builder = new TrieBuilder(keys, values, 13);
    File tempFile = Files.createTempFile("tst", null).toFile();
    builder.build (tempFile.getAbsolutePath());
    
    RandomAccessFile trieFile = new RandomAccessFile(tempFile, "r");
    MappedByteBuffer trieBuffer = trieFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, trieFile.length());
    trieFile.close();
    IntBuffer intBuffer = trieBuffer.asIntBuffer();
    
    final String testString = "qwerty";
    CharIterator iterator = new CharIterator() {
      int i = 0;
      
      public boolean hasNext() {
        return this.i < testString.length();
      }
      
      public char next() throws NoSuchElementException {
        char nextChar = testString.charAt(this.i);
        this.i++;
        return nextChar;
      }
    };
    
    int[] results = new int[256];
    int count = TrieSearcher.commonPrefixSearch(intBuffer, iterator, results);
    
    assertEquals (6, count);
    for (int i = 0; i < 6; i++) {
      assertEquals (values[i + 6], results[i]);
    }
  }
}
