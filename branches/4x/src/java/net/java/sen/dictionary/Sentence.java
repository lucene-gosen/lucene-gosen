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

	/**
	 * The sentence's characters
	 */
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
			if ((i >= 0) && (Sentence.this.constraints[i] != null)) {
				i += Sentence.this.constraints[i].length;
			} else {
				i++;
			}

			// Find the next iterable position, skipping any ignored spans and space
			for (int j = i; j < Sentence.this.characters.length; ) {
				if (Sentence.this.breakingIgnoreSet.get(j)) {
					j = Sentence.this.breakingIgnoreSet.nextClearBit(j);
				} else if (
							   (Sentence.this.characters[j] == ' ')
							|| (Sentence.this.characters[j] == '\t')
							|| (Sentence.this.characters[j] == '\r')
							|| (Sentence.this.characters[j] == '\n')
				          )
				{
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
					&& (this.nextOrigin < Sentence.this.characters.length)
					&& (Sentence.this.constraints[this.nextOrigin] != null)
			   )
			{
				this.nextLimit = this.nextOrigin + Sentence.this.constraints[this.nextOrigin].length; 
			} else {
				this.nextLimit = Sentence.this.characters.length;
			}

		}


		/* SentenceIterator interface */

		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#current()
		 */
		public char current() {

			return Sentence.this.characters[this.nextIndex];
		}


		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#hasNextOrigin()
		 */
		public boolean hasNextOrigin() {

			if (this.nextOrigin == -1) {
				findNextOrigin();
			}

			return (this.nextOrigin != -1);

		}


		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#length()
		 */
		public int length() {

			return Sentence.this.characters.length;

		}


		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#nextOrigin()
		 */
		public int nextOrigin() {

			if (this.nextOrigin == -1) {
				findNextOrigin();
			}

			this.skipped = this.nextSkipped;
			this.origin = this.nextOrigin;
			this.limit = this.nextLimit;
			this.nextIndex = this.origin;
			findNextOrigin();

			return this.origin;

		}


		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#origin()
		 */
		public int origin() {

			return this.origin;

		}


		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#rewindToOrigin()
		 */
		public void rewindToOrigin() {

			this.nextIndex = this.origin;

		}


		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#skippedCharCount()
		 */
		public int skippedCharCount() {

			return this.skipped;

		}


		/* CharIterator interface */

		/* (non-Javadoc)
		 * @see net.java.sen.trie.CharIterator#hasNext()
		 */
		public boolean hasNext() {

			boolean nextIndexValid;

			if (this.nextIndex >= this.limit) {
				nextIndexValid = false;
			} else if (Sentence.this.breakingIgnoreSet.get(this.nextIndex)) {
				nextIndexValid = false;
			} else 	if ((this.nextIndex > this.origin) && (Sentence.this.constraints[this.nextIndex] != null)) {
				nextIndexValid = false;
			} else if (
						   (Sentence.this.characters[this.nextIndex] == ' ')
						|| (Sentence.this.characters[this.nextIndex] == '\t')
						|| (Sentence.this.characters[this.nextIndex] == '\r')
						|| (Sentence.this.characters[this.nextIndex] == '\n')
			          )
			{
				nextIndexValid = false;
			} else {
				nextIndexValid = true;
			}

			this.nextIndexValid = nextIndexValid;
			
			return nextIndexValid;

		}


		/* (non-Javadoc)
		 * @see net.java.sen.trie.CharIterator#next()
		 */
		public char next() throws NoSuchElementException {

			// Check validity of next index
			if (this.nextIndexValid == null) {
				hasNext();
			}
			if (!this.nextIndexValid) {
				throw new NoSuchElementException();
			}

			char nextCharacter = Sentence.this.characters[this.nextIndex];
			this.nextIndex++;
			this.nextIndexValid = null;
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

		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#current()
		 */
		public char current() {

			return Sentence.this.characters[this.nextIndex];
		}


		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#hasNextOrigin()
		 */
		public boolean hasNextOrigin() {

			return false;

		}


		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#length()
		 */
		public int length() {

			return Sentence.this.characters.length;

		}


		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#nextOrigin()
		 */
		public int nextOrigin() {

			throw new IllegalStateException();

		}


		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#origin()
		 */
		public int origin() {

			return this.origin;

		}


		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#rewindToOrigin()
		 */
		public void rewindToOrigin() {

			this.nextIndex = this.origin;

		}


		/* (non-Javadoc)
		 * @see net.java.sen.dictionary.SentenceIterator#skippedCharCount()
		 */
		public int skippedCharCount() {

			return 0;

		}


		/* CharIterator interface */

		/* (non-Javadoc)
		 * @see net.java.sen.trie.CharIterator#hasNext()
		 */
		public boolean hasNext() {

			boolean nextIndexValid;

			if (this.nextIndex >= Sentence.this.characters.length) {
				nextIndexValid = false;
			} else if (Sentence.this.breakingIgnoreSet.get(this.nextIndex)) {
				nextIndexValid = false;
			} else if (
					   (Sentence.this.characters[this.nextIndex] == ' ')
					|| (Sentence.this.characters[this.nextIndex] == '\t')
					|| (Sentence.this.characters[this.nextIndex] == '\r')
					|| (Sentence.this.characters[this.nextIndex] == '\n')
		          )
			{
				nextIndexValid = false;
			} else {
				nextIndexValid = true;
			}

			this.nextIndexValid = nextIndexValid;

			return nextIndexValid;

		}


		/* (non-Javadoc)
		 * @see net.java.sen.trie.CharIterator#next()
		 */
		public char next() throws NoSuchElementException {

			// Check validity of next index
			if (this.nextIndexValid == null) {
				hasNext();
			}
			if (!this.nextIndexValid) {
				throw new NoSuchElementException();
			}

			char nextCharacter = Sentence.this.characters[this.nextIndex];
			this.nextIndex++;
			this.nextIndexValid = null;
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

		this.breakingIgnoreSet.set(position, position + length);

	}


	/**
	 * Sets a reading constraint on the Sentence starting at <code>position<code>;
	 * any existing constraints that overlap the new constraint will be removed.
	 *
	 * @param constraint The constraint to set
	 */
	public void setReadingConstraint(Reading constraint) {

		// Check starting position
		if ((constraint.start < 0) || (constraint.start >= this.characters.length)) {
			throw new IllegalArgumentException("Invalid constraint starting position");
		}

		// Check length
		if ((constraint.length <= 0) || (constraint.start + constraint.length - 1) >= this.characters.length) {
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
			if (this.constraints[i] != null) {
				if ((i + this.constraints[i].length - 1) >= constraint.start) {
					this.constraints[i] = null;
				} else {
					done = true;
				}
			}
		}

		this.constraints[constraint.start] = constraint;

	}


	/**
	 * Gets the reading constraint at the given position, if any
	 *
	 * @param position The position to get the constraint at
	 * @return The constraint if present, or <code>null</code>
	 */
	public Reading getReadingConstraint(int position) {

		return this.constraints[position];

	}


	/**
	 * Removes the reading constraint at the given position, if any
	 *
	 * @param position The position to remove the constraint from
	 */
	public void removeReadingConstraint(int position) {

		this.constraints[position] = null;

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

		return this.characters;

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
