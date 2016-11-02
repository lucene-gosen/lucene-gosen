/*
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

import java.text.CharacterIterator;

/**
 * Wraps a char[] as CharacterIterator for processing with a BreakIterator
 */
final class CharArrayIterator implements CharacterIterator {
  private char array[];
  private int start;
  private int index;
  private int length;
  private int limit;

  public char [] getText() {
    return array;
  }
  
  public int getStart() {
    return start;
  }
  
  public int getLength() {
    return length;
  }
  
  /**
   * Set a new region of text to be examined by this iterator
   * 
   * @param array text buffer to examine
   * @param start offset into buffer
   * @param length maximum length to examine
   */
  void setText(final char array[], int start, int length) {
    this.array = array;
    this.start = start;
    this.index = start;
    this.length = length;
    this.limit = start + length;
  }

  public char current() {
    return (index == limit) ? DONE : jvmBugWorkaround(array[index]);
  }
  
  // on modern jvms, supplementary codepoints with [:Sentence_Break=Format:]
  // trigger a bug in RulebasedBreakIterator! work around this for now
  // by lying about all surrogates to the sentence tokenizer, instead
  // we treat them all as SContinue so we won't break around them.
  public static final char jvmBugWorkaround(char ch) {
    return ch >= 0xD800 && ch <= 0xDFFF ? 0x002C : ch;
  }

  public char first() {
    index = start;
    return current();
  }

  public int getBeginIndex() {
    return 0;
  }

  public int getEndIndex() {
    return length;
  }

  public int getIndex() {
    return index - start;
  }

  public char last() {
    index = (limit == start) ? limit : limit - 1;
    return current();
  }

  public char next() {
    if (++index >= limit) {
      index = limit;
      return DONE;
    } else {
      return current();
    }
  }

  public char previous() {
    if (--index < start) {
      index = start;
      return DONE;
    } else {
      return current();
    }
  }

  public char setIndex(int position) {
    if (position < getBeginIndex() || position > getEndIndex())
      throw new IllegalArgumentException("Illegal Position: " + position);
    index = start + position;
    return current();
  }

  @Override
  public Object clone() {
    CharArrayIterator clone = new CharArrayIterator();
    clone.setText(array, start, length);
    clone.index = index;
    return clone;
  }
}
