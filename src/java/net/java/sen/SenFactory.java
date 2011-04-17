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
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

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
  private static SenFactory instance = null;
  
  /**
   * Get the singleton factory instance
   */
  public synchronized static SenFactory getInstance() {
    if (instance == null) {
      try {
        instance = new SenFactory();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
    
    return instance;
  }
  
  private SenFactory() throws IOException {
    // read main data files
    DataInputStream in = new DataInputStream(SenFactory.class.getResourceAsStream("header.sen"));
    costs = loadBuffer("connectionCost.sen", in.readInt()).asReadOnlyBuffer();
    pos = loadBuffer("partOfSpeech.sen", in.readInt()).asReadOnlyBuffer();
    tokens = loadBuffer("token.sen", in.readInt()).asReadOnlyBuffer();
    trie = loadBuffer("trie.sen", in.readInt()).asReadOnlyBuffer();
    in.close();
    
    // read index files
    in = new DataInputStream(SenFactory.class.getResourceAsStream("posIndex.sen"));
    posIndex = new String[in.readChar()];
    for (int i = 0; i < posIndex.length; i++) {
      posIndex[i] = in.readUTF();
    }
    
    conjTypeIndex = new String[in.readChar()];
    for (int i = 0; i < conjTypeIndex.length; i++) {
      conjTypeIndex[i] = in.readUTF();
    }
    
    conjFormIndex = new String[in.readChar()];
    for (int i = 0; i < conjFormIndex.length; i++) {
      conjFormIndex[i] = in.readUTF();
    }
    in.close();
  }
  
  private final String[] posIndex, conjTypeIndex, conjFormIndex;
  private final ByteBuffer costs, pos, tokens, trie;

  public static ShortBuffer getConnectionCostBuffer() {
    return getInstance().costs.asShortBuffer();
  }
  
  public static ByteBuffer getPOSBuffer() {
    return getInstance().pos.duplicate();
  }
  
  public static ByteBuffer getTokenBuffer() {
    return getInstance().tokens.duplicate();
  }
  
  public static IntBuffer getTrieBuffer() {
    return getInstance().trie.asIntBuffer();
  }
  
  public static String[] getPOSIndex() {
    return getInstance().posIndex;
  }
  
  public static String[] getConjTypeIndex() {
    return getInstance().conjTypeIndex;
  }
  
  public static String[] getConjFormIndex() {
    return getInstance().conjFormIndex;
  }
  
  public static final String unknownPOS = "未知語";

	private static ByteBuffer loadBuffer(String resource, int size) throws IOException {
	  InputStream in = SenFactory.class.getResourceAsStream(resource);
	  ByteBuffer buffer = ByteBuffer.allocateDirect(size);
	  buffer.limit(size);
	  
	  byte[] buf = new byte[1024];

	  while (true) {
	      int numBytes = in.read(buf);
	      if (numBytes == -1) break;
	      
	      buffer.put(buf, 0, numBytes);
	  }
	  
	  buffer.rewind();
	  in.close();
	  
	  return buffer;
	}

	/**
	 * Builds a Tokenizer for the given dictionary configuration
	 *
	 * @param configurationFilename The dictionary configuration filename
	 * @return The constructed Tokenizer
	 */
	private static Tokenizer getTokenizer() {
		return new JapaneseTokenizer(new Dictionary(), unknownPOS);
	}


	/**
	 * Creates a Viterbi from the given configuration
	 *
	 * @return A Viterbi
	 */
	public static Viterbi getViterbi() {
	  return new Viterbi(getTokenizer());
	}


	/**
	 * Creates a StringTagger from the given configuration
	 *
	 * @return A StringTagger
	 */
	public static StringTagger getStringTagger() {
	  return new StringTagger(getTokenizer());
	}


	/**
	 * Creates a ReadingProcessor from the given configuration
	 *
	 * @return A ReadingProcessor
	 */
	public static ReadingProcessor getReadingProcessor() {
	  return new ReadingProcessor(getTokenizer());
	}
}
