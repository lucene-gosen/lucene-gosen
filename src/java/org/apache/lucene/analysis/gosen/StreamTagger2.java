/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.analysis.gosen;

import java.io.IOException;
import java.io.Reader;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.java.sen.StringTagger;
import net.java.sen.dictionary.Token;

/**
 * Breaks text into sentences according to UAX #29: Unicode Text Segmentation
 * (http://www.unicode.org/reports/tr29/)
 * <p>
 */
public final class StreamTagger2 {
  private static final int IOBUFFER = 4096;
  private final char buffer[] = new char[IOBUFFER];
  /** true length of text in the buffer */
  private int length = 0; 
  /** length in buffer that can be evaluated safely, up to a safe end point */
  private int usableLength = 0; 
  /** accumulated offset of previous buffers for this reader, for offsetAtt */
  private int offset = 0;
  
  private StringTagger tagger;
  private Reader input;
  private final BreakIterator breaker = BreakIterator.getSentenceInstance(Locale.JAPANESE); /* tokenizes a char[] of text */
  private final CharArrayIterator iterator = new CharArrayIterator();
  private List<Token> tokens = new ArrayList<Token>();
  private int index = 0;

  /**
   * Construct a new StreamTagger2 that breaks text into words from the given Reader.
   */
  public StreamTagger2(StringTagger tagger, Reader input) {
    this.tagger = tagger;
    this.input = input;
  }

  public Token next() throws IOException {
    if (tokens == null || index >= tokens.size()) {
      if (length == 0)
        refill();
      while (!incrementTokenBuffer()) {
        refill();
        if (length <= 0) // no more bytes to read;
          return null;
      }
    }
    return tokens.get(index++);
  }
  
  public void reset() throws IOException {
    iterator.setText(buffer, 0, 0);
    breaker.setText(iterator);
    length = usableLength = offset = index = 0;
    tokens.clear();
  }

  public void reset(Reader input) throws IOException {
    this.input = input;
    reset();
  }
  
  public int end() throws IOException {
    return (length < 0) ? offset : offset + length;
  }  

  /*
   * This tokenizes text based upon the longest matching rule, and because of 
   * this, isn't friendly to a Reader.
   * 
   * Text is read from the input stream in 4kB chunks. Within a 4kB chunk of
   * text, the last unambiguous break point is found. Any remaining characters 
   * represent possible partial sentences, so are appended to the front of the 
   * next chunk.
   * 
   * There is the possibility that there are no unambiguous break points within
   * an entire 4kB chunk of text (binary data). So there is a maximum word limit
   * of 4kB since it will not try to grow the buffer in this case.
   * 
   * Note: this is much more sophisticated than StreamTagger, which will just
   * truncate on its 256 char buffer!
   */

  /**
   * Returns the last unambiguous break position in the text.
   * 
   * @return position of character, or -1 if one does not exist
   */
  private int findSafeEnd() {
    for (int i = length - 1; i >= 0; i--)
      if (isSafeEnd(buffer[i]))
        return i + 1;
    return -1;
  }
  
  private boolean isSafeEnd(char ch) {
    switch(ch) {
      case 0x000D:
      case 0x000A:
      case 0x0085:
      case 0x2028:
      case 0x2029:
        return true;
      default:
        return false;
    }
  }

  /**
   * Refill the buffer, accumulating the offset and setting usableLength to the
   * last unambiguous break position
   * 
   * @throws IOException
   */
  private void refill() throws IOException {
    offset += usableLength;
    int leftover = length - usableLength;
    System.arraycopy(buffer, usableLength, buffer, 0, leftover);
    int requested = buffer.length - leftover;
    int returned = read(input, buffer, leftover, requested);
    length = returned < 0 ? leftover : returned + leftover;
    if (returned < requested) /* reader has been emptied, process the rest */
      usableLength = length;
    else { /* still more data to be read, find a safe-stopping place */
      usableLength = findSafeEnd();
      if (usableLength < 0)
        usableLength = length; /*
                                * more than IOBUFFER of text without breaks,
                                * gonna possibly truncate tokens
                                */
    }

    iterator.setText(buffer, 0, Math.max(0, usableLength));
    breaker.setText(iterator);
  }
  

  private static int read(Reader input, char[] buffer, int offset, int length) throws IOException {
    assert length >= 0 : "length must not be negative: " + length;
 
    int remaining = length;
    while ( remaining > 0 ) {
      int location = length - remaining;
      int count = input.read( buffer, offset + location, remaining );
      if ( -1 == count ) { // EOF
        break;
      }
      remaining -= count;
    }
    return length - remaining;
  }

  /*
   * return true if there is a token from the buffer, or null if it is
   * exhausted.
   */
  private boolean incrementTokenBuffer() throws IOException {
    while (true) {
      int start = breaker.current();

      if (start == BreakIterator.DONE)
        return false; // BreakIterator exhausted

      // find the next set of boundaries
      int end = breaker.next();

      if (end == BreakIterator.DONE)
        return false; // BreakIterator exhausted

      String text = new String(buffer, start, end - start);
      tokens = tagger.analyze(text, tokens);

      if (tokens != null && !tokens.isEmpty()) {
        for (int i = 0; i < tokens.size(); i++) {
          Token token = tokens.get(i);
          token.setSentenceStart(i == 0);
          token.setStart(token.getStart() + start + offset);
        }
        index = 0;
        return true;
      }
    }
  }
}
