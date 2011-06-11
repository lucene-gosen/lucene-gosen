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

import net.java.sen.ReadingProcessor;
import net.java.sen.dictionary.Reading;
import net.java.sen.filter.reading.NumberFilter;

import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

import static net.java.sen.SenTestUtil.*;


/**
 * Tests usage of NumberFilter in reading processing
 */
public class NumberFilterTest extends LuceneTestCase {


	/**
	 * Tests default skipping of number kanji
	 */
	@Test
	public void testNumbers() {

		String testString = "一二三四五六七八九十百千万億兆";

		Reading[] expectedReadings = new Reading[] {
				new Reading (13, 1, "おく"),
				new Reading (14, 1, "ちょう")				
		};

		ReadingProcessor processor = getReadingProcessor();
		processor.addFilter(0, new NumberFilter());

		processor.setText (testString);

		List<Reading> readings = processor.getDisplayReadings();

		compareReadings (expectedReadings, readings);

	}


	/**
	 * Tests correct reading behaviour of "ヵ月"
	 */
	@Test
	public void testKagetsu() {

		String[] testStrings = new String[] {
				"一ヵ月",
				"一ヶ月",
				"一カ月",
				"一ケ月",
				"一か月"
		};

		Reading[] expectedReadings = new Reading[] {
				new Reading (2, 1, "げつ")
		};

		for (String testString : testStrings) {

			ReadingProcessor processor = getReadingProcessor();
			processor.addFilter(0, new NumberFilter());

			processor.setText (testString);

			List<Reading> readings = processor.getDisplayReadings();

			compareReadings (expectedReadings, readings);

		}

	}


	/**
	 * Tests correct reading behaviour of "分" -> "ふん"
	 */
	@Test
	public void testFun() {

		String[] testStrings = new String[] {
				"二分ほど",
				"五分ほど",
				"七分ほど",
				"九分ほど",
		};

		Reading[] expectedReadings = new Reading[] {
				new Reading (1, 1, "ふん")
		};

		for (String testString : testStrings) {

			ReadingProcessor processor = getReadingProcessor();
			processor.addFilter(0, new NumberFilter());

			processor.setText (testString);

			List<Reading> readings = processor.getDisplayReadings();

			compareReadings (expectedReadings, readings);

		}

	}


	/**
	 * Tests correct reading behaviour of "分" -> "ふん" for "億"
	 */
	@Test
	public void testFunOku() {

		String testString = "億分ほど";

		Reading[] expectedReadings = new Reading[] {
				new Reading (0, 1, "おく"),
				new Reading (1, 1, "ふん")
		};

		ReadingProcessor processor = getReadingProcessor();

		processor.setText (testString);

		List<Reading> readings = processor.getDisplayReadings();

		compareReadings (expectedReadings, readings);

	}


	/**
	 * Tests correct reading behaviour of "分" -> "ふん" for "兆"
	 */
	@Test
	public void testFunChou() {

		String testString = "兆分ほど";

		Reading[] expectedReadings = new Reading[] {
				new Reading (0, 1, "ちょう"),
				new Reading (1, 1, "ふん")
		};

		ReadingProcessor processor = getReadingProcessor();

		processor.setText (testString);

		List<Reading> readings = processor.getDisplayReadings();

		compareReadings (expectedReadings, readings);

	}


	/**
	 * Tests correct reading behaviour of "分" -> "ぷん"
	 */
	@Test
	public void testPun() {

		String[] testStrings = new String[] {
				"一分ほど",
				"三分ほど",
				"四分ほど",
				"六分ほど",
				"八分ほど",
				"十分ほど",
				"百分ほど",
				"千分ほど",
				"万分ほど"
		};

		Reading[] expectedReadings = new Reading[] {
				new Reading (1, 1, "ぷん")
		};

		for (String testString : testStrings) {

			ReadingProcessor processor = getReadingProcessor();
			processor.addFilter(0, new NumberFilter());

			processor.setText (testString);

			List<Reading> readings = processor.getDisplayReadings();

			compareReadings (expectedReadings, readings);

		}

	}


}
