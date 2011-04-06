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

import java.util.ArrayList;
import java.util.List;

import net.java.sen.dictionary.Reading;


/**
 * A class used by reading filters during reading processing
 */
public class ReadingNode {

	/**
	 * The previous node in the list
	 */
	public ReadingNode prev;

	/**
	 * The next node in the list
	 */
	public ReadingNode next;

	/**
	 * The index of the first token covered by this node
	 */
	public int firstToken;

	/**
	 * The index of the last token covered by this node
	 */
	public int lastToken;

	/**
	 * <code>true</code> if the stored readings, if any, are to be shown,
	 * otherwise <code>false</code>
	 */
	public boolean visible;


	/**
	 * A sorted list of readings within the covered range of morphemes.
	 * Compound reading processing is not applied to readings within this list,
	 * making them suitable for presentation where the "whole" reading at a given
	 * position is desired
	 */
	public List<Reading> baseReadings = new ArrayList<Reading>();


	/**
	 * A sorted list of visible reading fragments within the covered range of morphemes
	 */
	public List<Reading> displayReadings = new ArrayList<Reading>();

}
