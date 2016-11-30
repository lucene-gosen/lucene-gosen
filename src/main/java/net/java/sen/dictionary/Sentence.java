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

package net.java.sen.dictionary;

import java.util.BitSet;
import java.util.NoSuchElementException;

/**
 * A Sentence represents a character array to be morphologically analysed. It
 * supports breaking ignore spans, which prevent certain characters from being
 * considered for tokenisation, and reading constraints which restrict the
 * returned morphemes at a certain position to those with the given reading.
 */
public class Sentence {
  
  /** The sentence's characters */
  private char[] characters;
  
  /**
   * An BitSet of the same length as the <code>characters</code> array.
   * characters at indices that are <code>true</code> are both ignored
   * and treated as breaking points
   */
  private BitSet breakingIgnoreSet;
  
  /**
   * The reading constraints to apply. The constraints set will prevent other
   * spans from intersecting the indicated region, allowing the Viterbi
   * algorithm to ensure that a morpheme with the chosen length and reading
   * is picked in preference to any other possible sequence of morphemes
   * for the same characters
   */
  private Reading[] constraints;
  
  
  /**
   * A SentenceIterator that obeys the defined breaking ignore spans,
   * reading constraints, and skips space characters
   */
  private class ConstrainedIterator implements SentenceIterator {
    
    /**
     * The index of the next character to return, if valid; -1 otherwise
     */
    private int nextIndex = -1;
    
    /**
     * Validity of the next character index. Stored when <code>hasNext()</code>
     *  called to avoid recalculating during <code>next()</code>
     * <code>Boolean.TRUE</code> when next index is valid
     * <code>Boolean.FALSE</code> when next index is invalid
     * <code>null</code> when next index validity has not been calculated
     */
    private Boolean nextIndexValid = null;
    
    /**
     * The number of characters skipped between the current and next origins
     */
    private int nextSkipped = -1;
    
    /**
     * The next character cursor origin if it exists and has been calculated;
     * -1 otherwise
     */
    private int nextOrigin = -1;
    
    /**
     * The next character cursor limit
     */
    private int nextLimit = -1;
    
    /**
     * The number of characters skipped between the previous and current
     * origins
     */
    private int skipped = -1;
    
    /**
     * The current character cursor origin
     */
    private int origin = -1;
    
    /**
     * The current character cursor limit. Used when the current span is a
     * reading constraint; in other spans this will be set beyond the end of
     * the character array (which will not be the actual limit if there are
     * breaking ignore spans, reading constraints or space characters yet to
     * be reached) 
     */
    private int limit = -1;
    
    /**
     * Sets up the next subsequence of characters to iterate over
     *
     */
    private void findNextOrigin() {
      int nextOrigin = -1;
      int nextSkipped = 0;
      
      // If the current span has a reading constraint, skip to the end.
      // Otherwise start at the next character
      int i = this.origin;
      if ((i >= 0) && (constraints[i] != null)) {
        i += constraints[i].length;
      } else {
        i++;
      }
      
      // Find the next iterable position, skipping any ignored spans and space
      for (int j = i; j < characters.length; ) {
        if (breakingIgnoreSet.get(j)) {
          j = breakingIgnoreSet.nextClearBit(j);
        } else if (characters[j] == ' '  ||
            characters[j] == '\t' ||
            characters[j] == '\r' ||
            characters[j] == '\n') {
          j++;
        } else {
          nextOrigin = j;
          nextSkipped = j - i;
          break;
        }
      }
      
      // Set the iteration limit to account for a reading constraint at the current origin 
      this.nextOrigin = nextOrigin;
      this.nextSkipped = nextSkipped;
      if (
          (this.nextOrigin >= 0)
          && (this.nextOrigin < characters.length)
          && (Sentence.this.constraints[this.nextOrigin] != null)
      )
      {
        this.nextLimit = this.nextOrigin + constraints[this.nextOrigin].length; 
      } else {
        this.nextLimit = characters.length;
      }
    }
    
    /* SentenceIterator interface */
    
    public char current() {
      return characters[nextIndex];
    }
    
    public boolean hasNextOrigin() {
      if (nextOrigin == -1) {
        findNextOrigin();
      }
      
      return (nextOrigin != -1);
    }
    
    public int length() {
      return characters.length;
    }
    
    public int nextOrigin() {
      if (nextOrigin == -1) {
        findNextOrigin();
      }
      
      skipped = nextSkipped;
      origin = nextOrigin;
      limit = nextLimit;
      nextIndex = origin;
      findNextOrigin();
      
      return origin;
    }
    
    public int origin() {
      return origin;
    }
    
    public void rewindToOrigin() {
      nextIndex = origin;
    }
    
    public int skippedCharCount() {
      return skipped;
    }
    
    /* CharIterator interface */
    
    public boolean hasNext() {
      boolean nextIndexValid;
      
      if (nextIndex >= limit) {
        nextIndexValid = false;
      } else if (breakingIgnoreSet.get(nextIndex)) {
        nextIndexValid = false;
      } else 	if ((nextIndex > origin) && (constraints[nextIndex] != null)) {
        nextIndexValid = false;
      } else if (characters[nextIndex] == ' '  ||
          characters[nextIndex] == '\t' ||
          characters[nextIndex] == '\r' ||
          characters[nextIndex] == '\n') {
        nextIndexValid = false;
      } else {
        nextIndexValid = true;
      }
      
      this.nextIndexValid = nextIndexValid;
      
      return nextIndexValid;
    }
    
    public char next() throws NoSuchElementException {
      // Check validity of next index
      if (nextIndexValid == null) {
        hasNext();
      }
      if (!nextIndexValid) {
        throw new NoSuchElementException();
      }
      
      char nextCharacter = characters[nextIndex];
      nextIndex++;
      nextIndexValid = null;
      return nextCharacter;
    }
  }
  
  /**
   * A special-purpose iterator that ignores reading constraints. Unlike
   * ConstrainedIterator, the origin of UnconstrainedIterator cannot be
   * advanced.
   */
  private class UnconstrainedIterator implements SentenceIterator {
    
    /**
     * The current character cursor origin
     */
    private int origin = -1;
    
    /**
     * The index of the next character to return, if valid; -1 otherwise
     */
    private int nextIndex = -1;
    
    /**
     * Validity of the next character index. Stored when <code>hasNext()</code>
     *  called to avoid recalculating during <code>next()</code>
     * <code>Boolean.TRUE</code> when next index is valid
     * <code>Boolean.FALSE</code> when next index is invalid
     * <code>null</code> when next index validity has not been calculated
     */
    private Boolean nextIndexValid = null;
    
    /* SentenceIterator interface */
    
    public char current() {
      return characters[nextIndex];
    }
    
    public boolean hasNextOrigin() {
      return false;
    }
    
    public int length() {
      return characters.length;
    }
    
    public int nextOrigin() {
      throw new IllegalStateException();
    }
    
    public int origin() {
      return origin;
    }
    
    public void rewindToOrigin() {
      nextIndex = origin;
    }
    
    public int skippedCharCount() {
      return 0;
    }
    
    /* CharIterator interface */
    
    public boolean hasNext() {
      boolean nextIndexValid;
      
      if (nextIndex >= characters.length) {
        nextIndexValid = false;
      } else if (breakingIgnoreSet.get(nextIndex)) {
        nextIndexValid = false;
      } else if (characters[nextIndex] == ' '  ||
          characters[nextIndex] == '\t' ||
          characters[nextIndex] == '\r' ||
          characters[nextIndex] == '\n') {
        nextIndexValid = false;
      } else {
        nextIndexValid = true;
      }
      
      this.nextIndexValid = nextIndexValid;
      
      return nextIndexValid;
    }
    
    public char next() throws NoSuchElementException {
      // Check validity of next index
      if (nextIndexValid == null) {
        hasNext();
      }
      if (!nextIndexValid) {
        throw new NoSuchElementException();
      }
      
      char nextCharacter = characters[nextIndex];
      nextIndex++;
      nextIndexValid = null;
      return nextCharacter;
    }
    
    /**
     * Creates an UnconstrainedIterator starting from the given index
     * 
     * @param origin The index to start iterating from
     */
    public UnconstrainedIterator(int origin) {
      this.origin = origin;
      this.nextIndex = origin;
    }
  }
  
  /**
   * Sets a breaking ignore span. <code>length</code> characters starting at
   * <code>position</code> will be ignored during iteration; no iterated
   * subsequence of characters will cross the ignored span.<br>
   *
   * @param position The position of the ignore span to set
   * @param length The length of the ignore span to set
   */
  public void setBreakingIgnoreSpan(int position, short length) {
    breakingIgnoreSet.set(position, position + length);
  }
  
  /**
   * Sets a reading constraint on the Sentence starting at <code>position</code>;
   * any existing constraints that overlap the new constraint will be removed.
   *
   * @param constraint The constraint to set
   */
  public void setReadingConstraint(Reading constraint) {
    // Check starting position
    if ((constraint.start < 0) || (constraint.start >= characters.length)) {
      throw new IllegalArgumentException("Invalid constraint starting position");
    }
    
    // Check length
    if ((constraint.length <= 0) || (constraint.start + constraint.length - 1) >= characters.length) {
      throw new IllegalArgumentException("Invalid constraint length");
    }
    
    // Check constraint reading
    if (constraint.text == null) {
      throw new IllegalArgumentException("Invalid constraint reading");
    }
    
    // Remove any existing constraints that overlap the new constraint
    
    // Starting at the end of the constraint, work backwards until we hit
    // zero or find a constraint that doesn't overlap
    boolean done = false;
    for (int i = constraint.start + constraint.length - 1; (i >= 0) && !done; i--) {
      if (constraints[i] != null) {
        if ((i + constraints[i].length - 1) >= constraint.start) {
          constraints[i] = null;
        } else {
          done = true;
        }
      }
    }
    
    constraints[constraint.start] = constraint;
  }
  
  /**
   * Gets the reading constraint at the given position, if any
   *
   * @param position The position to get the constraint at
   * @return The constraint if present, or <code>null</code>
   */
  public Reading getReadingConstraint(int position) {
    return constraints[position];
  }
  
  /**
   * Removes the reading constraint at the given position, if any
   *
   * @param position The position to remove the constraint from
   */
  public void removeReadingConstraint(int position) {
    constraints[position] = null;
  }
  
  /**
   * Returns a SentenceIterator that obeys the defined breaking ignore spans,
   * reading constraints, and skips space characters
   *
   * @return The iterator
   */
  public SentenceIterator iterator() {
    return new ConstrainedIterator();
  }
  
  /**
   * Returns a SentenceIterator that obeys the defined breaking ignore spans,
   * skips space characters, but ignores reading constraints
   *
   * @param position The position to start iterating from
   * @return The iterator
   */
  public SentenceIterator unconstrainedIterator(int position) {
    return new UnconstrainedIterator(position);
  }
  
  /**
   * Returns the underlying characters of this Sentence
   *
   * @return The underlying characters
   */
  public char[] getCharacters() {
    return characters;
  }
  
  /**
   * Creates a sentence with the given characters
   *  
   * @param characters The sentence's characters 
   */
  public Sentence(char[] characters) {
    this.characters = characters;
    this.breakingIgnoreSet = new BitSet(characters.length);
    this.constraints = new Reading[characters.length];
  }
  
  /**
   * Creates a sentence with the given string
   *
   * @param text The string containing the sentence's characters
   */
  public Sentence(String text) {
    this(text.toCharArray());
  }
}
