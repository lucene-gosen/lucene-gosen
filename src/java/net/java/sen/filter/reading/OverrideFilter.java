/*
 * Copyright (C) 2007
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

package net.java.sen.filter.reading;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.java.sen.dictionary.Token;
import net.java.sen.filter.ReadingFilter;
import net.java.sen.filter.ReadingNode;


/**
 * A reading filter that overrides decisions on reading visibility made by
 * earlier filters. Typically this filter will be set as the last in a
 * chain of filters to allow changes to be made interactively
 */
public class OverrideFilter implements ReadingFilter {

	/**
	 * A map of visibility override settings. Where an entry exists for a given
	 * integer index, any <code>ReadingNode</code> starting at that index will
	 * have its visibility set to the stored value
	 */
	private Map<Integer,Boolean> visibility = new HashMap<Integer,Boolean>();


	/* (non-Javadoc)
	 * @see net.java.sen.filter.ReadingFilter#filterReadings(java.util.List, net.java.sen.filter.ReadingNode)
	 */
	public void filterReadings(List<Token> tokens, ReadingNode readingNode) {

		for (ReadingNode node = readingNode; node != null; node = node.next) {

			Boolean visible = this.visibility.get(tokens.get(node.firstToken).getStart());
			if (visible != null) {
				node.visible = visible;
			}

		}

	}


	/* (non-Javadoc)
	 * @see net.java.sen.filter.ReadingFilter#reset()
	 */
	public void reset() {

		this.visibility.clear();

	}


	/**
	 * Sets a visibility override at a given character index
	 *
	 * @param position The position to set the override at
	 * @param visible The visibility to set. <code>null</code> removes the
	 *                override
	 */
	public void setVisible(int position, Boolean visible) {

		this.visibility.put(position,visible);

	}


}
