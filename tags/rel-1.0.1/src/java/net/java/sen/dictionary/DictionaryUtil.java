package net.java.sen.dictionary;

/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * encoding methods for packing the POS file (mostly from Lucene) 
 */
public class DictionaryUtil {
 
  /** Reads an int stored in variable-length format.  Reads between one and
   * five bytes.  Smaller values take fewer bytes.  Negative numbers are not
   * supported.
   * @see DataOutput#writeVInt(int)
   */
  public static int readVInt(ByteBuffer bb) {
    /* This is the original code of this method,
     * but a Hotspot bug (see LUCENE-2975) corrupts the for-loop if
     * readByte() is inlined. So the loop was unwinded!
    byte b = readByte();
    int i = b & 0x7F;
    for (int shift = 7; (b & 0x80) != 0; shift += 7) {
      b = readByte();
      i |= (b & 0x7F) << shift;
    }
    return i;
    */
    byte b = bb.get();
    int i = b & 0x7F;
    if ((b & 0x80) == 0) return i;
    b = bb.get();
    i |= (b & 0x7F) << 7;
    if ((b & 0x80) == 0) return i;
    b = bb.get();
    i |= (b & 0x7F) << 14;
    if ((b & 0x80) == 0) return i;
    b = bb.get();
    i |= (b & 0x7F) << 21;
    if ((b & 0x80) == 0) return i;
    b = bb.get();
    assert (b & 0x80) == 0;
    return i | ((b & 0x7F) << 28);
  }
  
  /** Writes an int in a variable-length format.  Writes between one and
   * five bytes.  Smaller values take fewer bytes.  Negative numbers are not
   * supported.
   * @see DataInput#readVInt()
   */
  public static void writeVInt(DataOutput d, int i) throws IOException {
    while ((i & ~0x7F) != 0) {
      d.writeByte((byte)((i & 0x7f) | 0x80));
      i >>>= 7;
    }
    d.writeByte((byte)i);
  }
  
  public static void writeKatakana(DataOutput d, String s) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      d.writeByte(s.charAt(i) - 0x30A0);
    }
  }
  
  public static void readString(ByteBuffer b, char s[], int off, int len) {
    while (off < len) {
      s[off++] = b.getChar();
    }
  }
  
  public static void readKatakana(ByteBuffer b, char s[], int off, int len) {
    while(off < len) {
      s[off++] = (char) (0x30A0 + (b.get() & 0xff));
    }
  }
}
