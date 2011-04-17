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

package net.java.sen;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Reading;
import net.java.sen.dictionary.Sentence;
import net.java.sen.dictionary.SentenceIterator;
import net.java.sen.dictionary.Token;
import net.java.sen.dictionary.Viterbi;

import org.junit.Test;
import static org.junit.Assert.*;
import static net.java.sen.SenTestUtil.*;


/**
 * Tests the usage of Sentence
 */
public class SentenceTest {

	/**
	 * Test class representing an expected Sentence span
	 */
	private static class TestSpan {

		/**
		 * The span's expected origin
		 */
		public final int origin;

		/**
		 * The span's expected skipped character count
		 */
		public final int skipped;

		/**
		 * The span's expected substring
		 */
		public final String subString;

		/**
		 * The span's expected reading constraint
		 */
		public final String readingConstraint;

	
		/**
		 * @param origin The span's expected origin
		 * @param skipped The span's expected skipped character count
		 * @param subString The span's expected substring
		 * @param readingConstraint The span's expected reading constraint string
		 */
		public TestSpan(int origin, int skipped, String subString, String readingConstraint) {

			this.origin = origin;
			this.skipped = skipped;
			this.subString = subString;
			this.readingConstraint = readingConstraint;

		}

	}


	/**
	 * Test fixture for Sentence features
	 *
	 * @param sentence The sentence to test against
	 * @param expectedLength The expected reported length
	 * @param expectedSpans The expected substrings
	 */
	private void sentenceTestFixture (Sentence sentence, int expectedLength, TestSpan[] expectedSpans) {

		SentenceIterator iterator = sentence.iterator();

		assertEquals (expectedLength, iterator.length());

		assertEquals (-1, iterator.origin());

		for (int i = 0; i < expectedSpans.length; i++) {
			String subString = expectedSpans[i].subString;
			
			iterator.nextOrigin();
			assertEquals ("Unexpected origin:", expectedSpans[i].origin, iterator.origin());
			assertEquals ("Unexpected skip count:", expectedSpans[i].skipped, iterator.skippedCharCount());
			Reading reading = sentence.getReadingConstraint (iterator.origin());
			String constraintReading = (reading == null) ? null : reading.text;
			assertEquals ("Unexpected reading constraint:", expectedSpans[i].readingConstraint, constraintReading);

			for (int j = 0; j < subString.length(); j++) {
				assertTrue ("Expected more characters at span " + i + " (\"" + subString + "\")", iterator.hasNext());
				assertEquals ("Unexpected character at span " + i +  " (\"" + subString + "\"):", subString.charAt(j), iterator.next());
			}
			assertFalse ("More characters than expected at span " + i, iterator.hasNext());

			boolean caught = false;
			try {
				iterator.next();
			} catch (NoSuchElementException e) {
				caught = true;
			} catch (Exception e) {
				fail ("next() beyond end of sentence threw unexpected exception " + e);
			}
			assertTrue ("next() beyond end of sentence failed to throw exception", caught);

		}

		assertFalse ("Unexpected span at end", iterator.hasNextOrigin());

		
	}


	/**
	 * Test fixture for Sentence unconstrained iterator features
	 * The "skipped" and "readingConstraint" fields of the test spans are ignored
	 *
	 * @param sentence The sentence to test against
	 * @param expectedSpans The expected substrings
	 */
	private void sentenceUnconstrainedTestFixture (Sentence sentence, TestSpan[] expectedSpans) {

		for (int i = 0; i < expectedSpans.length; i++) {
			String subString = expectedSpans[i].subString;
			
			SentenceIterator iterator = sentence.unconstrainedIterator (expectedSpans[i].origin);

			assertEquals ("Unexpected origin:", expectedSpans[i].origin, iterator.origin());

			for (int k = 0; k < subString.length(); k++) {
				assertTrue ("Expected more characters at span " + i + " (\"" + subString + "\")", iterator.hasNext());
				assertEquals ("Unexpected character at span " + i +  " (\"" + subString + "\"):", subString.charAt(k), iterator.next());
			}
			assertFalse ("More characters than expected at span " + i, iterator.hasNext());

			boolean caught = false;
			try {
				iterator.next();
			} catch (NoSuchElementException e) {
				caught = true;
			} catch (Exception e) {
				fail ("next() beyond end of sentence threw unexpected exception " + e);
			}
			assertTrue ("next() beyond end of sentence failed to throw exception", caught);

			assertFalse ("Unexpected span at end", iterator.hasNextOrigin());

		}

	}


	/**
	 * Test hasNext() / next()
	 */
	@Test
	public void testBasicIteration() {

		String testString = "abcde";

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 0, "e", null)
		};

		Sentence sentence = new Sentence (testString.toCharArray());
		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedSpans);

	}


	/**
	 * Test hasNext() / next() with ignored range
	 */
	@Test
	public void testIterationWithIgnoredRange() {

		String testString = "abcde";

		TestSpan[] expectedSpans = {
				new TestSpan (3, 3, "de", null),
				new TestSpan (4, 0, "e", null)
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "", null),
				new TestSpan (1, 0, "", null),
				new TestSpan (2, 0, "", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 0, "e", null)
		};

		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setBreakingIgnoreSpan (0, (short)3);

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test hasNext() / next() with ignored range
	 */
	@Test
	public void testIterationWithIgnoredRange2() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setBreakingIgnoreSpan (2, (short)3);

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "ab", null),
				new TestSpan (1, 0, "b", null)
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "ab", null),
				new TestSpan (1, 0, "b", null),
				new TestSpan (2, 0, "", null),
				new TestSpan (3, 0, "", null),
				new TestSpan (4, 0, "", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test hasNext() / next() with ignored range
	 */
	@Test
	public void testIterationWithIgnoredRange3() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setBreakingIgnoreSpan (1, (short)3);

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "a", null),
				new TestSpan (4, 3, "e", null)
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "a", null),
				new TestSpan (1, 0, "", null),
				new TestSpan (2, 0, "", null),
				new TestSpan (3, 0, "", null),
				new TestSpan (4, 3, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test compounding of ignored ranges
	 */
	@Test
	public void testIterationWithIgnoredRange4() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setBreakingIgnoreSpan (1, (short)2);
		sentence.setBreakingIgnoreSpan (2, (short)2);

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "a", null),
				new TestSpan (4, 3, "e", null)
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "a", null),
				new TestSpan (1, 0, "", null),
				new TestSpan (2, 0, "", null),
				new TestSpan (3, 0, "", null),
				new TestSpan (4, 3, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test one character reading constraint at start of sentence
	 */
	@Test
	public void testReadingConstraint1() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (0, 1, "q"));

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "a", "q"),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 0, "e", null),
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 3, "e", null)
		};
		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);
	
	}


	/**
	 * Test multi character reading constraint at start of sentence
	 */
	@Test
	public void testReadingConstraint2() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (1, 3, "qwe"));

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "a", null),
				new TestSpan (1, 0, "bcd", "qwe"),
				new TestSpan (4, 0, "e", null)
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 3, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test multi character reading constraint of different length to source text
	 */
	@Test
	public void testReadingConstraint3() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (1, 3, "q"));

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "a", null),
				new TestSpan (1, 0, "bcd", "q"),
				new TestSpan (4, 0, "e", null)
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 3, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test multiple reading constraints
	 */
	@Test
	public void testReadingConstraint4() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (1, 2, "q"));
		sentence.setReadingConstraint (new Reading (3, 2, "w"));

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "a", null),
				new TestSpan (1, 0, "bc", "q"),
				new TestSpan (3, 0, "de", "w")
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 3, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test reading constraint at end of sentence
	 */
	@Test
	public void testReadingConstraint5() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (4, 1, "q"));

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "abcd", null),
				new TestSpan (1, 0, "bcd", null),
				new TestSpan (2, 0, "cd", null),
				new TestSpan (3, 0, "d", null),
				new TestSpan (4, 0, "e", "q")
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 3, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test overlapping reading constraints with last character overlap
	 */
	@Test
	public void testOverlappingReadingConstraint1() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (0, 3, "qwe"));
		sentence.setReadingConstraint (new Reading (2, 3, "rty"));

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "ab", null),
				new TestSpan (1, 0, "b", null),
				new TestSpan (2, 0, "cde", "rty")
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 3, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test overlapping reading constraints with mid range overlap
	 */
	@Test
	public void testOverlappingReadingConstraint2() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (1, 3, "qwe"));
		sentence.setReadingConstraint (new Reading (2, 1, "r"));

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "ab", null),
				new TestSpan (1, 0, "b", null),
				new TestSpan (2, 0, "c", "r"),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 0, "e", null),
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 3, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test overlapping reading constraints with overlap at start of constraint
	 */
	@Test
	public void testOverlappingReadingConstraint3() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (2, 3, "qwe"));
		sentence.setReadingConstraint (new Reading (1, 2, "rt"));

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "a", null),
				new TestSpan (1, 0, "bc", "rt"),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 0, "e", null),
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 3, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test non-overlapping constraint immediately before existing constraint
	 */
	@Test
	public void testOverlappingReadingConstraint4() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (2, 3, "qwe"));
		sentence.setReadingConstraint (new Reading (0, 2, "rt"));

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "ab", "rt"),
				new TestSpan (2, 0, "cde", "qwe")
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 3, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test hasNext() / next()
	 */
	@Test
	public void testRemovedReadingConstraint() {

		String testString = "abcde";

		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (2, 3, "qwe"));
		sentence.removeReadingConstraint (2);

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 0, "e", null)
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 3, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test reading constraint before start of sentence
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidReadingConstraint1() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (4, -1, "q"));
		
	}


	/**
	 * Test reading constraint after end of sentence
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidReadingConstraint2() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (4, 5, "q"));
		
	}


	/**
	 * Test zero length reading constraint
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidReadingConstraint3() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (1, 0, "q"));
		
	}


	/**
	 * Test negative length reading constraint
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidReadingConstraint4() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (1, -1, "q"));
		
	}


	/**
	 * Test reading constraint with length beyond end
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidReadingConstraint5() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (4, 2, "q"));
		
	}


	/**
	 * Test reading constraint with null reading
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidReadingConstraint6() {

		String testString = "abcde";
		Sentence sentence = new Sentence (testString.toCharArray());
		sentence.setReadingConstraint (new Reading (1, 1, null));
		
	}


	/**
	 * Test space skipping at start of sentence
	 */
	@Test
	public void testSpaceSkipping() {

		String testString = " abcde";
		Sentence sentence = new Sentence (testString.toCharArray());

		TestSpan[] expectedSpans = {
				new TestSpan (1, 1, "abcde", null),
				new TestSpan (2, 0, "bcde", null),
				new TestSpan (3, 0, "cde", null),
				new TestSpan (4, 0, "de", null),
				new TestSpan (5, 0, "e", null)
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "", null),
				new TestSpan (1, 0, "abcde", null),
				new TestSpan (2, 0, "bcde", null),
				new TestSpan (3, 0, "cde", null),
				new TestSpan (4, 0, "de", null),
				new TestSpan (5, 3, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test space skipping in middle of sentence
	 */
	@Test
	public void testSpaceSkipping2() {

		String testString = "abc de";
		Sentence sentence = new Sentence (testString.toCharArray());

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "abc", null),
				new TestSpan (1, 0, "bc", null),
				new TestSpan (2, 0, "c", null),
				new TestSpan (4, 1, "de", null),
				new TestSpan (5, 0, "e", null)
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "abc", null),
				new TestSpan (1, 0, "bc", null),
				new TestSpan (2, 0, "c", null),
				new TestSpan (3, 0, "", null),
				new TestSpan (4, 1, "de", null),
				new TestSpan (5, 0, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test space skipping at end of sentence
	 */
	@Test
	public void testSpaceSkipping3() {

		String testString = "abcde ";
		Sentence sentence = new Sentence (testString.toCharArray());

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 0, "e", null)
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "abcde", null),
				new TestSpan (1, 0, "bcde", null),
				new TestSpan (2, 0, "cde", null),
				new TestSpan (3, 0, "de", null),
				new TestSpan (4, 0, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test multiple space/tab/linefeed skipping
	 */
	@Test
	public void testSpaceSkipping4() {

		String testString = "a  bc \t\rd\r\ne";
		Sentence sentence = new Sentence (testString.toCharArray());

		TestSpan[] expectedSpans = {
				new TestSpan (0, 0, "a", null),
				new TestSpan (3, 2, "bc", null),
				new TestSpan (4, 0, "c", null),
				new TestSpan (8, 3, "d", null),
				new TestSpan (11, 2, "e", null)
		};

		TestSpan[] expectedUnconstrainedSpans = {
				new TestSpan (0, 0, "a", null),
				new TestSpan (1, 0, "", null),
				new TestSpan (2, 0, "", null),
				new TestSpan (3, 2, "bc", null),
				new TestSpan (4, 0, "c", null),
				new TestSpan (5, 0, "", null),
				new TestSpan (6, 0, "", null),
				new TestSpan (7, 0, "", null),
				new TestSpan (8, 0, "d", null),
				new TestSpan (9, 0, "", null),
				new TestSpan (10, 0, "", null),
				new TestSpan (11, 0, "e", null)
		};

		sentenceTestFixture (sentence, testString.length(), expectedSpans);
		sentenceUnconstrainedTestFixture (sentence, expectedUnconstrainedSpans);

	}


	/**
	 * Test reading constraint
	 *
	 * @throws IOException
	 */
	@Test
	public void testReadingConstraintBestTokens() throws IOException {

		Sentence testSentence = new Sentence ("今日は".toCharArray());
		testSentence.setReadingConstraint (new Reading(0, 1, "イマ"));
		testSentence.setReadingConstraint (new Reading(1, 1, "ヒ"));

		Token[] testTokens = new Token[] {
				new Token ("今", 2989, 0, 1, new Morpheme ("接頭詞-名詞接続", "*", "*", "*", new String[]{"コン", "イマ"}, new String[]{"コン", "イマ"}, null)),
				new Token ("日", 5551, 1, 1, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"ヒ", "ニチ"}, new String[]{"ヒ", "ニチ"}, null)),
				new Token ("は", 6470, 2, 1, new Morpheme ("助詞-係助詞", "*", "*", "*", new String[]{"ハ"}, new String[]{"ワ"}, null))
		};

		Viterbi viterbi = getViterbi();

		List<Token> tokens = viterbi.getBestTokens (testSentence);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Test getting possible tokens at a position (with overlapping reading constraints - should be ignored)
	 *
	 * @throws IOException
	 */
	@Test
	public void testReadingConstraintPossibleTokens() throws IOException {

		Sentence testSentence = new Sentence ("今日は".toCharArray());
		testSentence.setReadingConstraint (new Reading(0, 1, "イマ"));
		testSentence.setReadingConstraint (new Reading(1, 1, "ヒ"));

		Token[] testTokens = new Token[] {
				new Token ("日", 0, 1, 1, new Morpheme ("名詞-接尾-一般", "*", "*", "*", new String[]{"ビ", "ニチ"}, new String[]{"ビ", "ニチ"}, null)),
				new Token ("日", 0, 1, 1, new Morpheme ("名詞-接尾-助数詞", "*", "*", "*", new String[]{"ニチ"}, new String[]{"ニチ"}, null)),
				new Token ("日", 0, 1, 1, new Morpheme ("名詞-固有名詞-地域-国", "*", "*", "*", new String[]{"ニチ", "ニッ"}, new String[]{"ニチ", "ニッ"}, null)),
				new Token ("日", 0, 1, 1, new Morpheme ("名詞-固有名詞-地域-一般", "*", "*", "*", new String[]{"ヒ"}, new String[]{"ヒ"}, null)),
				new Token ("日", 0, 1, 1, new Morpheme ("名詞-非自立-副詞可能", "*", "*", "*", new String[]{"ヒ"}, new String[]{"ヒ"}, null)),
				new Token ("日", 0, 1, 1, new Morpheme ("名詞-非自立-一般", "*", "*", "*", new String[]{"ヒ"}, new String[]{"ヒ"}, null)),
				new Token ("日", 0, 1, 1, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"ヒ", "ニチ"}, new String[]{"ヒ", "ニチ"}, null)),
				new Token ("日", 0, 1, 1, new Morpheme ("名詞-副詞可能", "*", "*", "*", new String[]{"ヒ"}, new String[]{"ヒ"}, null))
		};
	
		Viterbi viterbi = getViterbi();

		List<Token> tokens = viterbi.getPossibleTokens (testSentence, 1);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Test getting possible tokens at a position (without overlapping reading constraints)
	 *
	 * @throws IOException
	 */
	@Test
	public void testPossibleTokens() throws IOException {

		Sentence testSentence = new Sentence ("買い被る".toCharArray());

		Token[] testTokens = new Token[] {
				new Token ("買い被る", 0, 0, 4, new Morpheme ("動詞-自立", "五段・ラ行", "基本形", "*", new String[]{"カイカブル"}, new String[]{"カイカブル"}, null)),
				new Token ("買い被", 0, 0, 3, new Morpheme ("動詞-自立", "五段・ラ行", "体言接続特殊２", "買い被る", new String[]{"カイカブ"}, new String[]{"カイカブ"}, null)),
				new Token ("買い", 0, 0, 2, new Morpheme ("動詞-自立", "五段・ワ行促音便", "連用形", "買う", new String[]{"カイ"}, new String[]{"カイ"}, null)),
				new Token ("買い", 0, 0, 2, new Morpheme ("名詞-接尾-一般", "*", "*", "*", new String[]{"カイ", "ガイ"}, new String[]{"カイ", "ガイ"}, null)),
				new Token ("買い", 0, 0, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"カイ"}, new String[]{"カイ"}, null)),
				new Token ("買", 0, 0, 1, new Morpheme ("名詞-接尾-一般", "*", "*", "*", new String[]{"カイ", "ガイ"}, new String[]{"カイ", "ガイ"}, null))
		};
	
		Viterbi viterbi = getViterbi();

		List<Token> tokens = viterbi.getPossibleTokens (testSentence, 0);

		compareTokens (testTokens, tokens);

	}


}
