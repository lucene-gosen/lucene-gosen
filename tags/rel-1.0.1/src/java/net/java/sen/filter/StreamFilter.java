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

import net.java.sen.dictionary.Sentence;
import net.java.sen.dictionary.Token;


/**
 * Represents a Node filter capable of both pre- and post-processing. Filters
 * are applied as follows:
 * 
 * <p>
 * 
 * <ul>
 * <li> Each filter is called to pre-process the sentence in the same order they
 *      were added
 * <li> Viterbi analysis is performed on the pre-processed sentence
 * <li> Each filter is called, in reverse order, to post-process the sentence
 * </ul>
 */
public interface StreamFilter {

	/**
	 * Pre-processes a sentence
	 *
	 * @param sentence The sentence be pre-processed
	 */
	public void preProcess(Sentence sentence);


	/**
	 * Post-processes analysed tokens
	 * 
	 * @param tokens The analysed tokens
	 * @return The post-processed tokens
	 */
	public List<Token> postProcess(List<Token> tokens);


}
