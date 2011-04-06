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

package net.java.sen.dictionary;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Represents an entry in the token file. A <code>CToken</code> contains
 * fixed-length data used in Viterbi path cost calculation, and a pointer to
 * its linked variable-length morpheme data in the part-of-speech information
 * file. <code>CToken</code>s are wrapped within <code>Node</code>s to form
 * the Viterbi lattice.
 */
final public class CToken implements Cloneable {

	/**
	 * The length in bytes of a stored CToken
	 */
	final public static long SIZE = 14;

	/**
	 * Used in Viterbi path cost calculation
	 */
	public short rcAttr2 = 0;

	/**
	 * Used in Viterbi path cost calculation
	 */
	public short rcAttr1 = 0;

	/**
	 * Used in Viterbi path cost calculation
	 */
	public short lcAttr = 0;

	/**
	 * The length of the morpheme this CToken wraps
	 */
	public short length = 0;

	/**
	 * The cost of this CToken
	 */
	public short cost = 0;

	/**
	 * The file index in the part-of-speech information file of the morpheme 
	 * data this CToken wraps
	 */
	public int partOfSpeechIndex = 0;


	/**
	 * Read a CToken from a ByteBuffer
	 *
	 * @param buffer The ByteBuffer to read from
	 * @return The CToken
	 */
	public static CToken read(ByteBuffer buffer) {

		CToken token = new CToken();

		token.rcAttr2 = buffer.getShort();
		token.rcAttr1 = buffer.getShort();
		token.lcAttr = buffer.getShort();
		token.length = buffer.getShort();
		token.cost = buffer.getShort();
		token.partOfSpeechIndex = buffer.getInt();

		return token;

	}


	/**
	 * Write a CToken to a DataOutput
	 *
	 * @param output The DataOutput to write to
	 * @param token The CToken to write
	 * @throws IOException
	 */
	public static void write(DataOutput output, CToken token) throws IOException {

		output.writeShort(token.rcAttr2);
		output.writeShort(token.rcAttr1);
		output.writeShort(token.lcAttr);
		output.writeShort(token.length);
		output.writeShort(token.cost);
		output.writeInt(token.partOfSpeechIndex);

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected CToken clone() {

		// Class contains only shorts and ints - a shallow copy is sufficient
		
		CToken cloneToken = null;
		try {
			cloneToken = (CToken) super.clone();
		} catch (CloneNotSupportedException e) {
			// Can't happen
			e.printStackTrace();
		}
		return cloneToken;

	}


}
