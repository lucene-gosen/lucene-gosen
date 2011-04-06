/*
 * Copyright (C) 2004-2007
 * Takashi Okamoto <tora@debian.org>
 * Tsuyoshi Fukui <fukui556@oki.com>
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

package net.java.sen.util;

import java.util.*;


/**
 * A class used to build a line of CSV data
 */
public class CSVData {

	/**
	 * The values comprising the line
	 */
	protected LinkedList<String> elements = new LinkedList<String>();


	/**
	 * Appends a value to the line
	 * 
	 * @param element The element to be appended.
	 */
	public void append(String element) {

		this.elements.add(element);

	}


	/**
	 * Inserts a value into the line at a given index
	 * 
	 * @param index The index at which the value is to be inserted
	 * @param element The value to be inserted
	 */
	public void insert(int index, String element) {

		this.elements.add(index, element);

	}


	/**
	 * Removes the value at the given index of the line
	 * 
	 * @param index The index from which to remove a value
	 */
	public void remove(int index) {

		this.elements.remove(index);

	}


	/**
	 * Replaces the value at the index of the line with a new
	 * value
	 * 
	 * @param index The index at which to replace a value
	 * @param element The value with which to replace
	 */
	public void set(int index, String element) {

		this.elements.set(index, element);

	}


	/**
	 * Removes all values from the line
	 */
	public void clear() {

		this.elements.clear();

	}


	/**
	 * Returns the line of CSV data represented by this class
	 * 
	 * @return The line of CSV data
	 */
	@Override
	public String toString() {

		StringBuffer buffer = new StringBuffer();
		Iterator iterator = this.elements.iterator();

		boolean isFirst = true;
		while (iterator.hasNext()) {
			String element = enquote((String) iterator.next());
			if (isFirst) {
				isFirst = false;
			} else {
				buffer.append(',');
			}
			buffer.append(element);
		}

		return new String(buffer);

	}


	/**
	 * Surrounds a string with double quotes if it contains either a double
	 * quote or a comma; replaces double quotes with a pair of double quotes
	 *
	 * @param string The string to quote
	 * @return The quoted string
	 */
	protected String enquote(String string) {

		if (string.length() == 0) {
			return string;
		}

		if (string.indexOf('"') == -1 && string.indexOf(',') == -1) {
			return string;
		}

		int size = string.length();
		StringBuffer buffer = new StringBuffer(size * 2);
		buffer.append('"');
		for (int i = 0; i < size; i++) {
			char c = string.charAt(i);
			if (c == '"') {
				buffer.append('"');
			}
			buffer.append(c);
		}
		buffer.append('"');

		return new String(buffer);

	}


}
