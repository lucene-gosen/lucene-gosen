package org.apache.lucene.analysis.ja;

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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * NormalizeReader.java
 *
 */
public class NormalizeReader extends FilterReader {
  private boolean prevRead=false;
  private char prevChar;
  enum CHARACTERTYPE {
    ZENKAKU_SPACE,
    VOICED_SOUND_MARK,
    SEMI_VOICED_SOUND_MARK,
    KA_TO,
    HA_HO,
    U,
    CONVERT_CHAR,
    OTHER_CHAR
  }
  public static final int[] CONVERSION_TABLE = {-1, '!', '"', '#', '$', '%',
    '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3',
    '4', '5', '6', '7', '8', '9', ':', ';', '<', '+', '>', '?', '@', 'A',
    'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
    'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']',
    '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
    'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
    'z', '{', '|', '}', '~', -1, -1,'。', '「', '」', '、', '・', 'ヲ', 'ァ', 'ィ',
    'ゥ', 'ェ', 'ォ', 'ャ', 'ュ', 'ョ', 'ッ', 'ー', 'ア', 'イ', 'ウ', 'エ', 'オ', 'カ',
    'キ', 'ク', 'ケ', 'コ', 'サ', 'シ', 'ス', 'セ', 'ソ', 'タ', 'チ', 'ツ', 'テ', 'ト',
    'ナ', 'ニ', 'ヌ', 'ネ', 'ノ', 'ハ', 'ヒ', 'フ', 'ヘ', 'ホ', 'マ', 'ミ', 'ム', 'メ',
    'モ', 'ヤ', 'ユ', 'ヨ', 'ラ', 'リ', 'ル', 'レ', 'ロ', 'ワ', 'ン', '゛', '゜',};
  public static final int[] GA_BO_CONVERSION_TABLE = {
    'ガ','ギ','グ','ゲ','ゴ',
    'ザ','ジ','ズ','ゼ','ゾ',
    'ダ','ヂ','ヅ','デ','ド',
    -1,-1,-1,-1,-1,
    'バ','ビ','ブ','ベ','ボ'
  }; 
  public static final int[] PA_PO_CONVERSION_TABLE = {'パ','ピ','プ','ペ','ポ'}; 
  /**
   * Create a new normalization reader .
   *
   * @param in
   *          The reader from which characters will be read.
   */
  public NormalizeReader(Reader in) {
    super(in);
    this.in = in;
  }

  /**
   * Read a single character.
   *
   * @exception IOException
   *              If an I/O error occurs
   */
  public int read() throws IOException {
    char ch = readChar();
    return (int) convert(ch);
  }

  private char readChar() throws IOException {
    char c;
    if(prevRead){
      c = prevChar;
      prevRead=false;
    }else{
      c = (char) in.read();
    }
    CHARACTERTYPE ct = getCharType(c);

    switch(ct){
    case ZENKAKU_SPACE:
      c = ' ';
      break;
    case U:
    case KA_TO:
    case HA_HO:
      char nc = (char) in.read();
      CHARACTERTYPE nct = getCharType(nc);
      if(CHARACTERTYPE.VOICED_SOUND_MARK.equals(nct)){
        if(CHARACTERTYPE.KA_TO.equals(ct)||
            CHARACTERTYPE.HA_HO.equals(ct)){
          c = (char) GA_BO_CONVERSION_TABLE[c - 0xFF76];
        }else{
          // U + dakuten
          c = 'ヴ';
        }
      }else if(CHARACTERTYPE.SEMI_VOICED_SOUND_MARK.equals(nct)){
        if(CHARACTERTYPE.HA_HO.equals(ct)){
          c = (char) PA_PO_CONVERSION_TABLE[c - 0xFF8A];
        }else{
          prevRead = true;
          prevChar = nc;        
        }
      }else{
        prevRead = true;
        prevChar = nc;        
      }
      break;
    case VOICED_SOUND_MARK:
    case SEMI_VOICED_SOUND_MARK:
    case CONVERT_CHAR:
    case OTHER_CHAR:
      break;
    }
    return c;
  }
  private CHARACTERTYPE getCharType(char ch) {
    if(ch==0x3000){
      return CHARACTERTYPE.ZENKAKU_SPACE;
    }else if(ch==0xFF9E){
      return CHARACTERTYPE.VOICED_SOUND_MARK;
    }else if(ch==0xFF9F){
      return CHARACTERTYPE.SEMI_VOICED_SOUND_MARK;
    }else if(ch==0xFF73){
      return CHARACTERTYPE.U;
    }else if(ch>=0xFF76&&ch<=0xFF84){
      return CHARACTERTYPE.KA_TO;
    }else if(ch>=0xFF8A&&ch<=0xFF8E){
      return CHARACTERTYPE.HA_HO;
    }else if((ch >= 0xFF01 && ch <= 0xFF5E) || (ch >= 0xFF61 && ch <= 0xFF9F)){
      return CHARACTERTYPE.CONVERT_CHAR;
    }else {
      return CHARACTERTYPE.OTHER_CHAR;
    }
  }

  public int read(char cbuf[], int off, int len) throws IOException {
    //int l = in.read(cbuf, off, len);
    int l = 0;
    for (int i = off; i < off + len; i++){
      char ch = readChar();
      if(ch==0xFFFF){
        return -1;
      }else{
        cbuf[i] = convert(ch);
        l++;
      }
    }
    return l;
  }

  /**
   * Convert HALFWIDTH_AND_FULLWIDTH_FORM characters.
   */
  char convert(char c) {
    if ((c >= 0xFF01 && c <= 0xFF5E) || (c >= 0xFF61 && c <= 0xFF9F)) {
      c = (char) CONVERSION_TABLE[c - 0xFF00];
    }
    return c;
  }

  /**
   * Tell whether this stream supports the mark() operation.
   *
   */
  public boolean markSupported() {
    return false;
  }

  /**
   * Mark the present position in the stream.
   *
   * @exception IOException
   *              If an I/O error occurs
   */
  public void mark(int readAheadLimit) throws IOException {
    throw new IOException("mark/reset not supported");
  }

  /**
   * Reset the stream.
   *
   * @exception IOException
   *              If an I/O error occurs
   */
  public void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }
}
