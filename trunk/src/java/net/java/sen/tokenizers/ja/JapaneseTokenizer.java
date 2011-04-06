/*
 * Copyright (C) 2001-2007
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

package net.java.sen.tokenizers.ja;

import net.java.sen.dictionary.CToken;
import net.java.sen.dictionary.Dictionary;
import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Node;
import net.java.sen.dictionary.SentenceIterator;
import net.java.sen.dictionary.Tokenizer;
import net.java.sen.trie.CharIterator;


/**
 * A Tokenizer for Japanese text
 */
public class JapaneseTokenizer extends Tokenizer {

	/**
	 * Character class for non-Japanese text
	 */
	static final int OTHER = 0x80;

	/**
	 * Character class for whitespace
	 */
	static final int SPACE = 0x81;

	/**
	 * Character class for Kanji
	 */
	static final int KANJI = 0x82;

	/**
	 * Character class for Katakana
	 */
	static final int KATAKANA = 0x83;

	/**
	 * Character class for Hiragana
	 */
	static final int HIRAGANA = 0x84;

	/**
	 * Character class for half-width forms
	 */
	static final int HALF_WIDTH = 0x85;


	/**
	 * Gets the character class of the given character
	 *
	 * @param c The character
	 * @return The character class
	 */
	private int getCharClass(char c) {

		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		int type = Character.getType(Character.toLowerCase(c));

		if (ub == Character.UnicodeBlock.BASIC_LATIN) {
			if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
				return SPACE;
			}
			return type;
		} else if (ub == Character.UnicodeBlock.HIRAGANA) {
			return HIRAGANA;
		} else if (ub == Character.UnicodeBlock.KATAKANA && type != Character.CONNECTOR_PUNCTUATION) {
			return KATAKANA;
		} else if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
			return KANJI;
		} else if (ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return HALF_WIDTH;
		}
		return OTHER;

	}


	/**
	 * Find the length to use for an unknown token
	 *
	 * @param iterator The iterator to read from
	 * @return The length
	 */
	private int findUnknownToken(CharIterator iterator) {

		int length = 0;

		if (iterator.hasNext()) {

			int charClass = getCharClass(iterator.next());
			switch (charClass) {
				case HIRAGANA:
				case KANJI:
				case OTHER:
					length = 1;
					break;

				default:
					length = 1;
					while (iterator.hasNext() && (getCharClass(iterator.next()) == charClass)) {
						length++;
					}
					break;
			}

		}

		return length;

	}


	/* (non-Javadoc)
	 * @see net.java.sen.dictionary.Tokenizer#lookup(net.java.sen.dictionary.SentenceIterator, char[])
	 */
	@Override
	public Node lookup(SentenceIterator iterator, char[] surface) {

		Node resultNode = null;

		int charClass = getCharClass(iterator.current());

		int skipped = iterator.skippedCharCount();
		CToken t[] = getDictionary().commonPrefixSearch(iterator);
		for (int i = 0; t[i] != null; i++) {
			Node newNode = new Node();
			newNode.ctoken = t[i];
			newNode.length = t[i].length;
			newNode.start = iterator.origin();
			newNode.span = t[i].length + skipped; 
			newNode.rnext = resultNode;
			newNode.morpheme = new Morpheme(getDictionary(), t[i].partOfSpeechIndex);

			resultNode = newNode;
		}

		if ((resultNode != null) && (charClass == HIRAGANA || charClass == KANJI)) {
			return resultNode;
		}

		// Synthesize token for longest consecutive run of same character class
		iterator.rewindToOrigin();
		int unknownTokenLength = findUnknownToken(iterator);

		Node unknownNode = getUnknownNode(surface, iterator.origin(), unknownTokenLength, skipped + unknownTokenLength);
		unknownNode.rnext = resultNode;

		return unknownNode;

	}


	/**
	 * Creates a JapaneseTokenizer with the given Dictionary
	 * 
	 * @param dictionary The Dictionary in which to search for possible morphemes
	 * @param unknownPartOfSpeechDescription The part-of-speech code to use for unknown tokens
	 */
	public JapaneseTokenizer(Dictionary dictionary, String unknownPartOfSpeechDescription) {

		super(dictionary, unknownPartOfSpeechDescription);

	}


}
