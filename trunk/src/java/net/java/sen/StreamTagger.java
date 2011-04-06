/*
 * Copyright (C) 2002-2007
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

package net.java.sen;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import net.java.sen.dictionary.Token;
import net.java.sen.filter.StreamFilter;


/**
 * Tokenizes text read from a {@link java.io.Reader <code>java.io.Reader</code>}
 * 
 * <p>See examples.StreamTaggerDemo in the Sen source for an example of how to
 * use this class
 * 
 * <p><b>Thread Safety</b>: Objects of this class are <b>NOT</b> thread safe and
 * should not be accessed simultaneously by multiple threads. Note that creating
 * additional instances using {@link SenFactory} is relatively cheap in both
 * memory and time
 * 
 */
public class StreamTagger {

	/**
	 * The underlying StringTagger used to tokenise text
	 */
	private StringTagger stringTagger = null;

	/**
	 * The maximum amount of text to read at a time
	 */
	private static final int BUFFER_SIZE = 256;

	/**
	 * The buffer used to contain the current text line
	 */
	private final char[] buffer = new char[BUFFER_SIZE];

	/**
	 * The index of the next token to return
	 */
	private int currentTokenIndex = 0;

	/**
	 * The tokens of the current line of text
	 */
	private List<Token> tokens;

	/**
	 * <code>true</code> if the end of the stream has been reached
	 */
	private boolean complete = false;

	/**
	 * The Reader from which lines of text are read
	 */
	private Reader reader;

	/**
	 * The offset within the stream of the current line
	 */
	private int lastReadOffset;


	/**
	 * Reads the next line of text
	 *
	 * @return The number of characters read, or -1 if the end of the stream has
	 *     been reached with no characters read
	 * @throws IOException
	 */
	private int readToBuffer() throws IOException {

		int position = 0;
		int charactersRead = 0;

		while ((position < BUFFER_SIZE) && (!this.complete) && ((charactersRead = this.reader.read(this.buffer, position, 1)) != -1)) {
			if ((Character.getType(this.buffer[position]) == Character.OTHER_PUNCTUATION) && (position > 0)) {
				return position + 1;
			}
			position++;
		}

		if (charactersRead == -1) {
			this.complete = true;
		}

		if (this.complete && position == 0) {
			return -1;
		}

		return position;

	}


	/**
	 * Tests if more {@link Token}s are available
	 * 
	 * @return <code>true</code> if more {@link Token}s are available, otherwise
	 *         <code>false</code>
	 * @throws IOException 
	 */
	public boolean hasNext() throws IOException {

		if ((this.tokens == null) || (this.currentTokenIndex == this.tokens.size())) {

			int i;
			do {
				i = readToBuffer();
				if (i == -1) {
					return false;
				}
				this.tokens = this.stringTagger.analyze(new String(this.buffer, 0, i));
			} while (this.tokens == null);
			this.currentTokenIndex = 0;

			// Set the token starts to their position within the stream
			if (this.tokens != null) {
				for (int n = 0; n < this.tokens.size(); n++) {
					Token token = this.tokens.get(n);
					token.setStart(token.getStart() + this.lastReadOffset);
				}
			}
			this.lastReadOffset += i;

		}

		// In case of a stream containing only newlines
		if (this.tokens.size() == 0) {
			return false;
		}

		return true;

	}


	/**
	 * Returns the next available token
	 * 
	 * @return The next available token
	 * @throws IOException
	 */
	public Token next() throws IOException {

		if ((this.tokens == null) || (this.tokens.size() == this.currentTokenIndex)) {
			int i;

			do {
				i = readToBuffer();
				if (i == -1) {
					return null;
				}
				this.tokens = this.stringTagger.analyze(new String(this.buffer, 0, i));
			} while (this.tokens == null);
			this.currentTokenIndex = 0;

			// Set the token starts to their position within the stream
			if (this.tokens != null) {
				for (int n = 0; n < this.tokens.size(); n++) {
					Token token = this.tokens.get(n);
					token.setStart(token.getStart() + this.lastReadOffset);
				}
			}
			this.lastReadOffset += i;

		}

		return this.tokens.get(this.currentTokenIndex++);

	}


	/**
	 * Adds a {@link StreamFilter}
	 *
	 * @param filter The {@link StreamFilter} to add
	 */
	public void addFilter(StreamFilter filter) {

		this.stringTagger.addFilter(filter);

	}


	/**
	 * @param stringTagger The StringTagger to use to tokenise the read text 
	 * @param reader The Reader to read text from
	 */
	public StreamTagger(StringTagger stringTagger, Reader reader) {

		this.stringTagger = stringTagger;
		this.reader = reader;
		this.lastReadOffset = 0;

	}


}
