/*
 * Copyright (C) 2002-2007
 * Takashi Okamoto <tora@debian.org>
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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.java.sen.util.IOUtils;

import net.java.sen.dictionary.Dictionary;
import net.java.sen.dictionary.Tokenizer;
import net.java.sen.dictionary.Viterbi;
import net.java.sen.tokenizers.ja.JapaneseTokenizer;

/**
 * A factory to manage creation of {@link Viterbi}, {@link StringTagger}, and
 * {@link ReadingProcessor} objects<br><br>
 * 
 * <b>Thread Safety:</b> This class and all its public methods are thread safe.
 * The objects constructed by the factory are <b>NOT</b> thread safe and should
 * not be accessed simultaneously by multiple threads
 */
public class SenFactory {
  
  private static final Map<String,SenFactory> map = new ConcurrentHashMap<String,SenFactory>();
  
  private static final String EMPTY_DICTIONARYDIR_KEY = "NO_DICTIONARY_INSTANCE";

  /**
   * Get the singleton factory instance
   * @param dictionaryDir a directory of dictinaries
   */
  public synchronized static SenFactory getInstance(String dictionaryDir) {
    
    String key = (dictionaryDir == null || dictionaryDir.trim().length() == 0) ? EMPTY_DICTIONARYDIR_KEY : dictionaryDir; 
    SenFactory instance = map.get(key);
    if (instance == null) {
      try {
        instance = new SenFactory(dictionaryDir);
        map.put(key, instance);
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
    
    return instance;
  }
  
  private SenFactory(String dictionaryDir) throws IOException {
    InputStream in = null;
    DataInputStream din = null;
    // read main data files
    try {
      in = getInputStream("header.sen", dictionaryDir);
      din = new DataInputStream(in);
      costs = loadBuffer("connectionCost.sen", din.readInt(), dictionaryDir).asReadOnlyBuffer();
      pos = loadBuffer("partOfSpeech.sen", din.readInt(), dictionaryDir).asReadOnlyBuffer();
      tokens = loadBuffer("token.sen", din.readInt(), dictionaryDir).asReadOnlyBuffer();
      trie = loadBuffer("trie.sen", din.readInt(), dictionaryDir).asReadOnlyBuffer();
    } finally {
      IOUtils.closeWhileHandlingException(din, in);
    }

      
    
    // read index files
    try {
      in = getInputStream("posIndex.sen", dictionaryDir);
      din = new DataInputStream(in);
      posIndex = new String[din.readChar()];
      for (int i = 0; i < posIndex.length; i++) {
        posIndex[i] = din.readUTF();
      }
    
      conjTypeIndex = new String[din.readChar()];
      for (int i = 0; i < conjTypeIndex.length; i++) {
        conjTypeIndex[i] = din.readUTF();
      }
    
      conjFormIndex = new String[din.readChar()];
      for (int i = 0; i < conjFormIndex.length; i++) {
        conjFormIndex[i] = din.readUTF();
      }
    } finally {
      IOUtils.closeWhileHandlingException(din, in);
    }
  }
  
  private static InputStream getInputStream(String name, String dictionaryDir) throws IOException{
    InputStream in = null;
    if(dictionaryDir == null || dictionaryDir.trim().length() == 0){
      in = SenFactory.class.getResourceAsStream(name);
    }else{
      in = new FileInputStream(new File(dictionaryDir, name));
    }
    if(in == null){
      throw new RuntimeException("Not found resource["+name+"]. dictionaryDir=["+dictionaryDir+"]");
    }
    return in;
  }

  private final String[] posIndex, conjTypeIndex, conjFormIndex;
  private final ByteBuffer costs, pos, tokens, trie;

  public static final String unknownPOS = "未知語";
  private static boolean compatibilityMode = true;

  private static ByteBuffer loadBuffer(String resource, int size, String dictionaryDir) throws IOException {
    InputStream in = null;
    try {
      in = getInputStream(resource, dictionaryDir);
    ByteBuffer buffer = ByteBuffer.allocateDirect(size);
    buffer.limit(size);
    
    byte[] buf = new byte[1024];
    
    while (true) {
      int numBytes = in.read(buf);
      if (numBytes == -1) break;
      
      buffer.put(buf, 0, numBytes);
    }
    
    buffer.rewind();
    
    return buffer;
    } finally {
      IOUtils.closeWhileHandlingException(in);
    }
  }
  
  /**
   * Builds a Tokenizer for the given dictionary configuration
   *
   * @param dictionaryDir The dictionary configuration filename
   * @return The constructed Tokenizer
   */
  private static Tokenizer getTokenizer(String dictionaryDir) {
    SenFactory localInstance = SenFactory.getInstance(dictionaryDir);
    
    return new JapaneseTokenizer(
            new Dictionary(localInstance.costs.asShortBuffer(),
                    localInstance.pos.duplicate(),
                    localInstance.tokens.duplicate(),
                    localInstance.trie.asIntBuffer(),
                    localInstance.posIndex,
                    localInstance.conjTypeIndex,
                    localInstance.conjFormIndex),
            unknownPOS,
            compatibilityMode);
  }
  
  /**
   * Creates a Viterbi from the given configuration
   *
   * @param dictionaryDir a directory of dictionary
   * @return A Viterbi
   */
  static Viterbi getViterbi(String dictionaryDir) {
    // for test only
    return new Viterbi(getTokenizer(dictionaryDir));
  }
  
  /**
   * Creates a StringTagger from the given configuration
   *
   * @param dictionaryDir a directory of dictionary
   * @return A StringTagger
   */
  public static StringTagger getStringTagger(String dictionaryDir) {
    return new StringTagger(getTokenizer(dictionaryDir));
  }
  
  /**
   * Creates a ReadingProcessor from the given configuration
   *
   * @param dictionaryDir a directory of dictionary
   * @return A ReadingProcessor
   */
  static ReadingProcessor getReadingProcessor(String dictionaryDir) {
    //for test only
    return new ReadingProcessor(getTokenizer(dictionaryDir));
  }

  public static void setCompatibilityMode(boolean flag) {
    compatibilityMode = flag;
  }
}
