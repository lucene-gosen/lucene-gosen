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

package net.java.sen.filter;

import java.util.List;

import net.java.sen.dictionary.Token;


/**
 * An interface to filters used during reading processing
 */
public interface ReadingFilter {

	/**
	 * Filters readings
	 *
	 * @param tokens The tokens to which the readings are applied
	 * @param readingNode The head of a list of <code>ReadingNode</code>s
	 */
	public void filterReadings(List<Token> tokens, ReadingNode readingNode);

	/**
	 * Resets any sentence specific state held by the filter. The implementing
	 * filter decides exactly what state is affected, and may choose, for
	 * instance, to exclude state relating to global or persistant settings
	 */
	public void reset();

}
