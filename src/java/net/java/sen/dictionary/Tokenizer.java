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


/**
 * A String Tokenizer
 * 
 * <p> The Tokenizer uses a {@link Dictionary} to assist the decomposition of
 * strings into potential morphemes
 */
public abstract class Tokenizer {

	/**
	 * The {@link Dictionary}  used to find possible morphemes
	 */
	protected Dictionary dictionary;

	/**
	 * A {@link CToken} representing an unknown morpheme
	 */
	protected CToken unknownCToken;

	/**
	 * A {@link Node} representing a beginning-of-string
	 */
	protected Node bosNode;

	/**
	 * A {@link Node} representing an end-of-string
	 */
	protected Node eosNode;

	/**
	 * The part-of-speech code to use for unknown tokens
	 */
	protected String unknownPartOfSpeechDescription;


	/**
	 * @return Returns the dictionary used to find possible morphemes
	 */
	public Dictionary getDictionary() {
		return this.dictionary;
	}


	/**
	 * Creates a unique beginning-of-string {@link Node}. The {@link Node}
	 * returned by this method is freshly cloned and not an alias of any
	 * other {@link Node}
	 *
	 * @return A beginning-of-string {@link Node}
	 */
	public Node getBOSNode() {

		Node bosNode = this.bosNode.clone();
		bosNode.prev = this.bosNode.clone();

		return bosNode;

	}


	/**
	 * Creates a unique end-of-string {@link Node}. The {@link Node} returned by
	 * this method is freshly cloned and not an alias of any other {@link Node}
	 *
	 * @return An end-of-string Node
	 */
	public Node getEOSNode() {

		return this.eosNode.clone();

	}


	/**
	 * Creates an "unknown morpheme" {@link Node} with the specified
	 * characteristics.  The {@link Node} returned by this method is freshly
	 * cloned and not an alias of any other {@link Node}
	 *
	 * @param surface The underlying surface of which the {@link Node} is part
	 * @param start The index of the first character of the surface within the
	 *              {@link Node}
	 * @param length The length of the {@link Node}
	 * @param span The span of the {@link Node}
	 * @return The new "unknown morpheme" {@link Node}
	 */
	public Node getUnknownNode(char[] surface, int start, int length, int span) {

		Node unknownNode = new Node();

		unknownNode.ctoken = this.unknownCToken;
		unknownNode.start = start;
		unknownNode.length = length;
		unknownNode.span = span;
		unknownNode.morpheme = new Morpheme();
		unknownNode.morpheme.setBasicForm(new String(surface, start, length));
		unknownNode.morpheme.setPartOfSpeech(this.unknownPartOfSpeechDescription);

		return unknownNode;

	}


	/**
	 * Searches for possible morphemes from the given SentenceIterator. The
	 * {@link Node} that is returned links through
	 * <code>Node.rnext</code> to a list of matches which may be of varying
	 * lengths  
	 *
	 * @param iterator The iterator to search from
	 * @param surface The underlying character surface
	 * @return The head of a chain of {@link Node}s representing the possible
	 *         morphemes beginning at the given index
	 * @throws IOException
	 */
	public abstract Node lookup(SentenceIterator iterator, char[] surface) throws IOException;


	/**
	 * Constructs a new {@link Tokenizer} that uses the specified
	 * {@link Dictionary} to find possible morphemes within a given string
	 * 
	 * @param dictionary The {@link Dictionary} to search within
	 * @param unknownPartOfSpeechDescription The part-of-speech code to use for
	 *        unknown tokens
	 */
	public Tokenizer(Dictionary dictionary, String unknownPartOfSpeechDescription) {

		this.dictionary = dictionary;
		this.unknownPartOfSpeechDescription = unknownPartOfSpeechDescription;

		this.bosNode = new Node();
		this.bosNode.ctoken = this.dictionary.getBOSToken();

		this.eosNode = new Node();
		this.eosNode.ctoken = this.dictionary.getEOSToken();

		this.unknownCToken = this.dictionary.getUnknownToken();
		this.unknownCToken.cost = 30000;

	}


}
