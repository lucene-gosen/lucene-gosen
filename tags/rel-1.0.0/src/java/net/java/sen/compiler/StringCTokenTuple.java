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

package net.java.sen.compiler;

import net.java.sen.dictionary.CToken;

/**
 * A tuple comprising a String and a CToken
 */
public class StringCTokenTuple implements Comparable<StringCTokenTuple> {

	/**
	 * The tuple's String
	 */
	public String key = null;

	/**
	 * The tuple's CToken
	 */
	public CToken value = null;


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(StringCTokenTuple o) {
		return this.key.compareTo(o.key);
	}


	/**
	 * @param key The tuple's String
	 * @param value The tuple's CToken
	 */
	public StringCTokenTuple(String key, CToken value) {
		this.key = key;
		this.value = value;
	}

}