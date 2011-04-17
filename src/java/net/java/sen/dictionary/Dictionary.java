/*
 * Copyright (C) 2002-2007
 * Taku Kudoh <taku-ku@is.aist-nara.ac.jp>
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

package net.java.sen.dictionary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import net.java.sen.SenFactory;
import net.java.sen.trie.CharIterator;
import net.java.sen.trie.TrieSearcher;

/**
 * The <code>Dictionary</code> class wraps access to a compiled Sen dictionary
 */
public class Dictionary {

	/**
	 * Mapped buffer of the token file (token.sen)
	 */
	private ByteBuffer tokenBuffer = null;

	/**
	 * Mapped buffer of the part-of-speech information file (partOfSpeech.sen)
	 */
	private CharBuffer partOfSpeechInfoBuffer = null;

	/**
	 * Mapped buffer of the Trie data (trie.sen)
	 */
	private IntBuffer trieBuffer = null;

	/**
	 * Mapped buffer of the connection cost matrix file (connectionCost.sen)
	 */
	private ShortBuffer connectionCostBuffer;

	/**
	 * Size of the first extent of the connection cost matrix
	 */
	private int connectionSize1;

	/**
	 * Size of the second extent of the connection cost matrix
	 */
	private int connectionSize2;

	/**
	 * Size of the third extent of the connection cost matrix
	 */
	private int connectionSize3;

	/**
	 * A CToken representing a beginning-of-string
	 */
	private CToken bosToken = new CToken();

	/**
	 * A CToken representing an end-of-string
	 */
	private CToken eosToken = new CToken();

	/**
	 * A CToken representing an unknown morpheme
	 */
	private CToken unknownToken = new CToken();

	/**
	 * A buffer used to store result indices from a Trie search. Reused on
	 * every call to the {@link #commonPrefixSearch(CharIterator)} method
	 */
	private int trieSearchResults[] = new int[256];

	/**
	 * A buffer used to store {@link CToken}s resulting from a search. Reused
	 * on every call to the {@link #commonPrefixSearch(CharIterator)} method
	 */
	private CToken results[] = new CToken[256];

	String posIndex[];
	String conjTypeIndex[];
	String conjFormIndex[];

	/**
	 * Map the connection cost matrix file (matrix.sen)
	 * 
	 * @throws IOException 
	 */
	private void loadConnectionCostFile() {

		ShortBuffer buffer = SenFactory.getConnectionCostBuffer();

		this.connectionSize1 = buffer.get();
		this.connectionSize2 = buffer.get();
		this.connectionSize3 = buffer.get();

		int expectedSize = 3 + (this.connectionSize1 * this.connectionSize2 * this.connectionSize3);
		if (expectedSize != buffer.limit()) {
			throw new RuntimeException("Expected connection cost file to be " + (2 * expectedSize) + " bytes, but was " + (2 * buffer.limit()));
		}

		this.connectionCostBuffer = buffer.slice();

	}


	/**
	 * Gets a unique beginning-of-string {@link CToken <code>CToken</code>}. The {@link CToken <code>CToken</code>} returned by this method is
	 * freshly cloned and not an alias of any other {@link CToken <code>CToken</code>}
	 *
	 * @return A beginning-of-string CToken
	 */
	public CToken getBOSToken() {

		return this.bosToken.clone();

	}


	/**
	 * Gets a unique end-of-string {@link CToken <code>CToken</code>}. The {@link CToken <code>CToken</code>} returned by this method is
	 * freshly cloned and not an alias of any other {@link CToken <code>CToken</code>}
	 *
	 * @return An end-of-string CToken
	 */
	public CToken getEOSToken() {

		return this.eosToken.clone();

	}


	/**
	 * Gets a unique unknown-morpheme {@link CToken <code>CToken</code>}. The {@link CToken <code>CToken</code>} returned by this method is
	 * freshly cloned and not an alias of any other {@link CToken <code>CToken</code>}
	 *
	 * @return A unknown-morpheme CToken
	 */
	public CToken getUnknownToken() {

		return this.unknownToken.clone();

	}


	/**
	 * Returns the part of speech info character buffer
	 *
	 * @return The character buffer
	 */
	CharBuffer getPartOfSpeechInfoBuffer() {

		return this.partOfSpeechInfoBuffer;

	}


	/**
	 * Retrieves the cost between three Nodes from the connection cost matrix
	 * 
	 * @param lNode2 The first Node
	 * @param lNode The second Node
	 * @param rNode The third Node
	 * @return The connection cost
	 */
	public int getCost(Node lNode2, Node lNode, Node rNode) {

		int position = this.connectionSize3 * (this.connectionSize2 * lNode2.ctoken.rcAttr2 + lNode.ctoken.rcAttr1) + rNode.ctoken.lcAttr;
		return this.connectionCostBuffer.get(position) + rNode.ctoken.cost;

	}


	/**
	 * Searches for possible morphemes starting at the current position of a
	 * CharIterator. The iterator is advanced by the length of the longest
	 * matching morpheme 
	 *
	 * @param iterator The iterator to search from 
	 * @return The possible morphemes found
	 */
	public CToken[] commonPrefixSearch(CharIterator iterator) {

		int size = 0;

		int n = TrieSearcher.commonPrefixSearch(trieBuffer, iterator, this.trieSearchResults);

		for (int i = 0; i < n; i++) {

			int k = 0xff & this.trieSearchResults[i];
			int p = this.trieSearchResults[i] >> 8;

			this.tokenBuffer.position((int) ((p + 3) * CToken.SIZE));
			for (int j = 0; j < k; j++) {
				this.results[size] = CToken.read(this.tokenBuffer);
				size++;
			}

		}

		// Null terminate
		this.results[size] = null;

		return this.results;

	}


	/**
	 * @throws IOException
	 */
	public Dictionary()
	{

		// Map connection cost file
		loadConnectionCostFile();

		// Map position infomation file.
		this.partOfSpeechInfoBuffer = SenFactory.getPOSBuffer();

		// Map token file
		this.tokenBuffer = SenFactory.getTokenBuffer();
		this.bosToken = CToken.read(this.tokenBuffer);
		this.eosToken = CToken.read(this.tokenBuffer);
		this.unknownToken = CToken.read(this.tokenBuffer);
		
		// Map double array trie dictionary
		this.trieBuffer = SenFactory.getTrieBuffer();

		// indexes (unique POS values, etc)
		this.posIndex = SenFactory.getPOSIndex();
		this.conjTypeIndex = SenFactory.getConjTypeIndex();
		this.conjFormIndex = SenFactory.getConjFormIndex();
	}


}
