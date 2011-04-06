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


/**
 * A single token from an analysed sentence
 * 
 *  <p><b>Thread Safety</b>: Objects of this class are <b>NOT</b> thread safe and
 * should not be accessed simultaneously by multiple threads.
 * 
 *  <p><b>CAUTION</b>: {@link Morpheme}s are implemented as lazy proxies onto a
 *  {@link Dictionary}, and care should be taken not to access the same
 *  {@link Dictionary} from multiple threads. Once any member of a
 *  {@link Morpheme} has been read, its link to the {@link Dictionary} is broken
 *  and this restriction is relaxed
 */
public class Token {

	/**
	 * The character range of this Token within the underlying sentence
	 */
	private String surface = null;

	/**
	 * The Viterbi cost of this Token
	 */
	private int cost = -1;

	/**
	 * The start of the character range of this Token within the underlying
	 * sentence
	 */
	private int start = -1;

	/**
	 * The length of the character range of this Token within the underlying
	 * sentence
	 */
	private int length = -1;

	/**
	 * The morpheme data represented by this Token
	 */
	private Morpheme morpheme = new Morpheme();


	/**
	 * Gets the start of the character range of this Token within the
	 * underlying sentence
	 *
	 * @return The start of the character range of this Token within the
	 *         underlying sentence
	 */
	public int getStart() {

		return this.start;

	}


	/**
	 * Sets the start of the character range of this Token within the
	 * underlying sentence
	 * 
	 * @param start The start of the character range of this Token within the
	 *              underlying sentence
	 */
	public void setStart(int start) {

		this.start = start;

	}


	/**
	 * Gets the length of the character range of this Token within the
	 * underlying sentence
	 * 
	 * @return The length of the character range of this Token within the
	 *         underlying sentence
	 */
	public int getLength() {
		return this.length;
	}


	/**
	 * Sets the length of the character range of this Token within the
	 * underlying sentence
	 * 
	 * @param length The length of the character range of this Token within the
	 *               underlying sentence
	 */
	public void setLength(int length) {
		this.length = length;
	}


	/**
	 * Gets the character range of this Token within the underlying sentence
	 *
	 * @return The character range of this Token within the underlying sentence
	 */
	public String getSurface() {

		return this.surface;

	}


	/**
	 * Sets the character range of this Token within the underlying sentence
	 * 
	 * @param surface The character range of this Token within the underlying
	 *                sentence 
	 */
	public void setSurface(String surface) {

		this.surface = surface;

	}


	/**
	 * Gets the Viterbi cost of this Token
	 *
	 * @return The Viterbi cost of this Token
	 */
	public int getCost() {

		return this.cost;

	}


	/**
	 * Sets the Viterbi cost of this Token
	 * 
	 * @param cost The Viterbi cost of this Token
	 */
	public void setCost(int cost) {

		this.cost = cost;

	}


	/**
	 * Gets the morpheme data for this Token
	 *
	 * @return The morpheme data for this Token
	 */
	public Morpheme getMorpheme() {

		return this.morpheme;

	}


	/**
	 * Gets the end of the character range of this Token within the underlying
	 * sentence
	 *
	 * @return The end of the character range of this Token within the underlying
	 * sentence
	 */
	public int end() {
		return getStart() + getLength();
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {

		if (object instanceof Token) {

			Token token = ((Token) object);

			if (
					   ((this.surface == token.surface) || (this.surface != null && this.surface.equals(token.surface)))
					&& (this.cost == token.cost)
					&& (this.start == token.start)
					&& (this.length == token.length)
					&& ((this.morpheme == token.morpheme) || (this.morpheme != null && this.morpheme.equals(token.morpheme)))
			   )
			{
				return true;
			}

		}

		return false;

	}


	/**
	 * Returns the character range of this Token within the underlying sentence
	 * 
	 * @return The character range of this Token within the underlying sentence
	 */
	@Override
	public String toString() {

		return getSurface();

	}


	/**
	 * Creates a Token from a Node
	 * 
	 * @param surface The underlying sentence string 
	 * @param node The Node to create from
	 */
	public Token(String surface, Node node) {

		this.morpheme = node.morpheme;
		this.cost = node.cost;
		this.surface = surface.substring(node.start, node.start + node.length);
		this.start = node.start;
		this.length = node.length;

	}


	/**
	 * Creates a Token with explicit parameters
	 * 
	 * @param surface The character range within the underlying sentence
	 * @param cost The Viterbi cost
	 * @param start The start of the character range within the underlying
	 *              sentence
	 * @param length The length of the character range within the underlying
	 *               sentence
	 * @param morpheme The morpheme data
	 */
	public Token(String surface, int cost, int start, int length, Morpheme morpheme) {

		this.surface = surface;
		this.cost = cost;
		this.start = start;
		this.length = length;
		this.morpheme = morpheme;

	}


	/**
	 * Creates a blank Token
	 */
	public Token() {

	}


}
