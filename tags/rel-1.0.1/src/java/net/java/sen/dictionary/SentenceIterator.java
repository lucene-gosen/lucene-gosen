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

import net.java.sen.trie.CharIterator;


/**
 * An iterator over a sequence of characters, consisting of subsequences that
 * may overlap, and that do not necessarily cover every character in the
 * underlying sequence. The iterator has both a current character position,
 * from which characters are read, and an origin position that the character
 * position may be returned to. Initially, both the origin and current
 * character positions are invalid (as there may be no valid origins present);
 * a call to {@link #hasNextOrigin hasNextOrigin} should first be made to
 * determine if an origin is present, followed by a call to {@link #nextOrigin}
 * to move to the first available origin.
 */
public interface SentenceIterator extends CharIterator {

	/**
	 * Returns the length of the underlying character range being iterated
	 * over, including any ignored characters
	 *
	 * @return The length of the sentence being iterated over
	 */
	public int length();


	/**
	 * Returns the current origin position. The origin is the position starting
	 * from which characters are read. {link returnToOrigin returnToOrigin}
	 * moves back to the current origin; {link nextOrigin nextOrigin} moves to
	 * the next available origin, if any.
	 *
	 * @return The current origin position
	 */
	public int origin();


	/**
	 * Returns the character at the current character cursor position
	 *
	 * @return The character at the current character cursor position
	 */
	public char current();


	/**
	 * Reports whether the sentence has any more origins
	 *
	 * @return <code>true</code> if the sentence has more origins remaining
	 */
	public boolean hasNextOrigin();


	/**
	 * Moves the origin forward to the next available position. Subsequent
	 * characters returned by {@link CharIterator#next next} will start at the
	 * new origin position
	 *
	 * @return The new origin 
	 */
	public int nextOrigin();


	/**
	 * Returns to the current origin position. Subsequent characters returned
	 * by {@link CharIterator#next next} will start at the origin position
	 *
	 */
	public void rewindToOrigin();


	/**
	 * Returns the number of characters skipped between the previous and
	 * current character spans
	 *
	 * @return The number of characters skipped
	 */
	public int skippedCharCount();

}
