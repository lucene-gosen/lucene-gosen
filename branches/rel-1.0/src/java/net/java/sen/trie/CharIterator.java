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

package net.java.sen.trie;

import java.util.NoSuchElementException;


/**
 * An iterator interface for <code>char</code>s
 */
public interface CharIterator {

	/**
	 * Reports whether more characters are available
	 *
	 * @return <code>true</code> if more characters are available, otherwise
	 *         <code>false</code>
	 */
	public boolean hasNext();


	/**
	 * Returns the next available character
	 *
	 * @return The next available character
	 * @throws NoSuchElementException - if no more elements are available
	 */
	public char next() throws NoSuchElementException;


}
