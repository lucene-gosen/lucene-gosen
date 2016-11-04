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

package net.java.sen;

import java.util.List;

import net.java.sen.dictionary.Reading;
import net.java.sen.filter.reading.NumberFilter;
import net.java.sen.filter.reading.OverrideFilter;

import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

import static net.java.sen.SenTestUtil.*;

/**
 * Tests usage of OverrideFilter in reading processing
 */
public class OverrideFilterTest extends LuceneTestCase {
	/**
	 * Tests override of default visibility
	 */
	@Test
	public void testHide() {
		String testString = "３週間寒さが続いた";

		Reading[] expectedReadings = new Reading[] {
				new Reading (1, 2, "しゅうかん"),
				new Reading (6, 1, "つづ"),
		};

		OverrideFilter overrideFilter = new OverrideFilter();

		ReadingProcessor processor = getReadingProcessor();
		processor.addFilter (0, new NumberFilter());
		processor.addFilter (1, overrideFilter);

		processor.setText (testString);
		overrideFilter.setVisible (3, false);

		List<Reading> readings = processor.getDisplayReadings();

		compareReadings (expectedReadings, readings);
	}

	/**
	 * Tests override of default skipping of number kanji
	 */
	@Test
	public void testNumbers() {
		String testString = "一億三千";

		Reading[] expectedReadings = new Reading[] {
				new Reading (1, 1, "おく"),
		};

		OverrideFilter overrideFilter = new OverrideFilter();

		ReadingProcessor processor = getReadingProcessor();
		processor.addFilter (0, new NumberFilter());
		processor.addFilter (1, overrideFilter);

		processor.setText (testString);
		overrideFilter.setVisible (1, true);

		List<Reading> readings = processor.getDisplayReadings();

		compareReadings (expectedReadings, readings);
	}
}
