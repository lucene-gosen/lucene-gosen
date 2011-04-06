/*
 * Copyright (C) 2004-2007
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

package net.java.sen.trie;

import java.nio.IntBuffer;


/**
 * Searches a Trie data file
 */
public class TrieSearcher {

	/**
	 * The Trie data to search
	 */
	private IntBuffer trieData;


	/**
	 * Searches for Trie keys forming a complete substring of the given
	 * sentence, starting at the given position within the sentence
	 * 
	 * @param iterator The character iterator to read search characters from
	 * @param results An array used to return the values of the found keys
	 * @return The number of results found
	 * 
	 * @throws ArrayIndexOutOfBoundsException if results[] is too small
	 */
	public int commonPrefixSearch(CharIterator iterator, int results[]) {

		int b = this.trieData.get(0 << 1);
		int num = 0;
		int n;
		int p;

		while (iterator.hasNext()) {

			p = b;
			n = this.trieData.get(p << 1);
			if (b == this.trieData.get((p << 1) + 1) && n < 0) {
				// Will throw ArrayIndexOutOfBoundsException if results[] is too small
				results[num] = -n - 1;
				num++;
			}

			p = b + iterator.next() + 1;


			if (((p << 1) + 1) >= this.trieData.limit()) {
				// We fell off the end of the Trie data
				return num;
			}


			if (b == this.trieData.get((p << 1) + 1)) {
				b = this.trieData.get(p << 1);
			} else {
				return num;
			}
		}

		p = b;
		n = this.trieData.get(p << 1);
		if (b == this.trieData.get((p << 1) + 1) && (n < 0)) {
			// Will throw ArrayIndexOutOfBoundsException if results[] is too small
			results[num] = -n - 1;
			num++;
		}

		return num;

	}


	/**
	 * Creates a TrieSearcher for the given Trie data
	 * 
	 * @param trieData The data to search within 
	 */
	public TrieSearcher(IntBuffer trieData) {

		this.trieData = trieData;

	}


}
