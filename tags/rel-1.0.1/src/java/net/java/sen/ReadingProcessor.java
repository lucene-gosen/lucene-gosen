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
import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.sen.dictionary.Reading;
import net.java.sen.dictionary.Sentence;
import net.java.sen.dictionary.Token;
import net.java.sen.dictionary.Tokenizer;
import net.java.sen.dictionary.Viterbi;
import net.java.sen.filter.ReadingFilter;
import net.java.sen.filter.ReadingNode;
import net.java.sen.util.TextUtil;


/**
 * A text processor that builds reading data suitable for application as
 * furigana. {@link net.java.sen.filter.ReadingFilter}s can be used to refine
 * and customise the output
 * 
 * <p>See examples.ReadingProcessorDemo in the Sen source for an example of how
 * to use this class
 * 
 * <p><b>Thread Safety</b>: Objects of this class are <b>NOT</b> thread safe and
 * should not be accessed simultaneously by multiple threads. Note that creating
 * additional instances using {@link SenFactory} is relatively cheap in both
 * memory and time
 */
public class ReadingProcessor {

	/**
	 * A pattern used to ensure that text is treated as literal fixed strings
	 * within a Pattern
	 */
	private static Pattern patternSpecialCharsPattern = Pattern.compile("([^a-zA-z0-9])");

	/**
	 * A map of reading filters to be applied in order of their integer keys
	 */
	private Map<Integer,ReadingFilter> filters = new TreeMap<Integer,ReadingFilter>();

	/**
	 * The Viterbi used to analyse text
	 */
	private Viterbi viterbi;

	/**
	 * The sentence containing the text currently being analysed
	 */
	private Sentence sentence;

	/**
	 * <code>true</code> if the sentence has been updated and requires analysis,
	 * otherwise <code>false</code>
	 */
	private boolean needsAnalysis = true;

	/**
	 * The currently analysed tokens
	 */
	private List<Token> tokens;


	/**
	 * The result of reading processing. Once created, the contents are
	 * unaffected by any later changes applied to the reading processor
	 */
	public static class ReadingResult {

		/**
		 * The Viterbi used to analyse the sentence. Used here to find possible
		 * tokens for a given position on request
		 */
		private Viterbi viterbi;

		/**
		 * The analysed sentence. Used here to find possible tokens for a given
		 * position on request. Note that as this shadows the reading
		 * processor's {@link Sentence} it will actually change with any later
		 * reading constraint updates, but as the call to
		 * {@link Viterbi#getPossibleTokens(Sentence, int)}) ignores these
		 * constraints, no change will be visible in this context
		 */
		private Sentence sentence;

		/**
		 * The tokens resulting from sentence analysis
		 */
		private List<Token> tokens;

		/**
		 * The base readings resulting from reading processing, indexed by their
		 * starting character indices
		 */
		private SortedMap<Integer,Reading> baseReadings;

		/**
		 * The visible reading fragments resulting from reading processing,
		 * indexed by their starting character indices
		 */
		private SortedMap<Integer,Reading> displayReadings;

		/**
		 * The set of tokens containing at least one visible reading
		 */
		private BitSet visibleTokens;


		/**
		 * Gets the tokens resulting from analysis of the result's text.<br><br>
		 * 
		 * Note that although the returned data is isolated from any changes to
		 * the {@link ReadingProcessor}, it is not copied internally, and any
		 * changes you make to the returned data will be reflected by
		 * subsequent calls to this method on the same {@link ReadingResult} object
		 *
		 * @return The tokens resulting from analysis of the result's text
		 */
		public List<Token> getTokens() {

			return this.tokens;

		}


		/**
		 * Gets the base readings resulting from processing of the result's
		 * text.<br><br>
		 * 
		 * Note that although the returned data is isolated from any changes to
		 * the {@link ReadingProcessor}, it is not copied internally, and any
		 * changes you make to the returned data will be reflected by
		 * subsequent calls to this method on the same {@link ReadingResult} object
		 *
		 * @return The readings resulting from processing of the result's text
		 */
		public SortedMap<Integer,Reading> getBaseReadings() {
			
			return this.baseReadings;

		}


		/**
		 * Gets the visible reading fragments resulting from processing of the
		 * result's text.<br><br>
		 * 
		 * Note that although the returned data is isolated from any changes to
		 * the {@link ReadingProcessor}, it is not copied internally, and any
		 * changes you make to the returned data will be reflected by
		 * subsequent calls to this method on the same {@link ReadingResult} object
		 *
		 * @return The readings resulting from processing of the result's text
		 */
		public SortedMap<Integer,Reading> getDisplayReadings() {
			
			return this.displayReadings;

		}


		/**
		 * Gets the set of tokens that contain at least one visible reading.
		 * Where a reading is present, the BitSet will contain <code>true</code>
		 * at the {@link Token}'s index.<br><br>
		 * 
		 * Note that although the returned data is isolated from any changes to
		 * the {@link ReadingProcessor}, it is not copied internally, and any
		 * changes you make to the returned data will be reflected by
		 * subsequent calls to this method on the same {@link ReadingResult} object
		 *
		 * @return The set of tokens containing at least one visible reading
		 */
		public BitSet getVisibleTokens() {

			return this.visibleTokens;
			
		}


		/**
		 * Searches for possible tokens starting at the given position within the
		 * result's text
		 *
		 * @param position The position to search at
		 * @return The possible tokens at the given position
		 */
		public List<Token> getPossibleTokens(int position) {

			try {
				return this.viterbi.getPossibleTokens(this.sentence, position);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		}


		/**
		 * @param viterbi The Viterbi used for sentence analysis
		 * @param sentence The analysed Sentence
		 * @param tokens The Tokens resulting from analysis
		 * @param baseReadings The readings resulting from reading processing (without compound reading processing)
		 * @param displayReadings The visible reading fragments resulting from reading processing 
		 * @param visibleTokens The set of tokens containing at least one
		 *                      visible Reading
		 */
		private ReadingResult(Viterbi viterbi, Sentence sentence, List<Token> tokens,
				SortedMap<Integer, Reading> baseReadings, SortedMap<Integer, Reading> displayReadings, BitSet visibleTokens)
		{

			this.viterbi = viterbi;
			this.sentence = sentence;
			this.tokens = tokens;
			this.baseReadings = baseReadings;
			this.displayReadings = displayReadings;
			this.visibleTokens = visibleTokens;

		}

	}


	/**
	 * Escapes text to ensure that it is treated as literal fixed strings
	 * within a Pattern
	 *
	 * @param input The text to escape
	 * @return The escaped text
	 */
	private String escapePatternSpecialChars(String input) {

		return patternSpecialCharsPattern.matcher(input).replaceAll("\\\\$1");

	}


	/**
	 * Analyse a token containing both kanji and non-kanji for the readings of
	 * its kanji parts
	 *
	 * @param text The source text 
	 * @param reading The reading to apply
	 * @param tokenStart The starting index of the token within the sentence
	 * @return A list of readings, or <code>null</code> if a match could not be found
	 */
	private List<Reading> splitComplexToken(String text, String reading, int tokenStart) {
	
		List<Reading> tokenReadings = new ArrayList<Reading>();

		// Split the source text on kanji/kana boundaries 
		List<String> fragments = new ArrayList<String>();
		boolean fragmentIsKanji = (Character.UnicodeBlock.of(text.charAt(0)) == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
		boolean firstFragmentIsKanji = fragmentIsKanji;
		int fragmentStart = 0;
		for (int i = 1; i < text.length(); i++) {
			boolean newIsKanji = (Character.UnicodeBlock.of(text.charAt(i)) == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
			if (fragmentIsKanji != newIsKanji) {
				fragments.add(text.substring(fragmentStart, i));
				fragmentStart = i;
				fragmentIsKanji = newIsKanji;
			}
		}
		fragments.add(text.substring(fragmentStart, text.length()));

		// Find a way to split the reading to match the source text
		String regexp = "^";
		fragmentIsKanji = firstFragmentIsKanji;
		for (String fragment : fragments) {
			if (fragmentIsKanji) {
				regexp += "(.*?)";
			} else {
				regexp += "(" + escapePatternSpecialChars(fragment) + ")";
			}
			fragmentIsKanji = !fragmentIsKanji;
		}
		regexp += "$";

		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(reading);
		if (!matcher.find()) {
			return null;
		}

		// Create reading nodes for the kanji fragments of the source text
		fragmentIsKanji = firstFragmentIsKanji;
		fragmentStart = 0;
		for (int i = 0; i < fragments.size(); i++) {
			String fragment = fragments.get(i);
			if (fragmentIsKanji) {
				String fragmentReading = matcher.group(i + 1);
				tokenReadings.add(new Reading(tokenStart + fragmentStart, fragment.length(), fragmentReading));
			}
			fragmentStart += fragment.length();
			fragmentIsKanji = !fragmentIsKanji;
		}

		return tokenReadings;

	}


	/**
	 * Gets the tokens resulting from analysis of the current text,
	 * re-performing the actual analysis if any change that would require it has
	 * occurred since the previous analysis
	 *
	 * @return The tokens resulting from analysis of the current text
	 */
	private List<Token> getTokens() {

		try {

			if (this.needsAnalysis) {
				this.tokens = this.viterbi.getBestTokens(this.sentence);
				this.needsAnalysis = false;
			}

			return this.tokens;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}


	/**
	 * Compiles <code>Token</code>s into a list of <code>ReadingNode</code>s
	 *
	 * <ul>
	 *   <li>A node is created for every token containing one or more kanji</li>
	 *   <li>For tokens containing both kanji and kana, a reading will be
	 *       assigned to each span of kanji</li>
	 *   <li>Where a token results from a reading constraint, the resulting
	 *       node will have exactly that reading. Tokens without constraints
	 *       will be assigned their Morpheme's first listed reading even if
	 *       alternatives are present</li> 
	 * </ul>
	 *
	 * @return The head of the compiled list of <code>ReadingNode</code>s
	 */
	private ReadingNode compileReadings() {

		ReadingNode headNode = null;
		ReadingNode prev = null;

		List<Token> tokens = getTokens();

		for (int i = 0; i < tokens.size(); i++) {

			Token token = tokens.get(i);

			boolean hasKanji = false;
			boolean hasNonKanji = false;
			for (char c : token.getSurface().toCharArray()) {
				if (Character.UnicodeBlock.of(c) == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
					hasKanji = true;
				} else {
					hasNonKanji = true;
				}
			}

			List<String> readings = token.getMorpheme().getReadings();
			Reading readingConstraint = this.sentence.getReadingConstraint(token.getStart());
			String constraintText = null;
			if (readingConstraint != null) {
				constraintText = TextUtil.invertKanaCase(readingConstraint.text);
			}


			ReadingNode node = new ReadingNode();
			node.firstToken = i;
			node.lastToken = i;
			node.visible = true;

			if (!("".equals(constraintText)) &&
					(
							   (hasKanji && (readings.size() > 0))
							|| (
									   (readingConstraint != null)
									&& (!token.getSurface().equals(constraintText) && (
											(readings.size() == 0) || !token.getSurface().equals(readings.get(0)))
									   )
							   )
					)
				)
			{

				String readingText;
				if (readingConstraint == null) {
					// No constraint set -> select first available reading
					readingText = TextUtil.invertKanaCase(readings.get(0));
				} else {
					// Constraint set -> Use exact constraint text
					readingText = TextUtil.invertKanaCase(readingConstraint.text); 
				}

				node.baseReadings.add(new Reading(token.getStart(), token.getLength(), readingText));

				if (hasKanji != hasNonKanji) {
					// Simple (no splitting required)
					node.displayReadings.add(new Reading(token.getStart(), token.getLength(), readingText));
				} else {
					// Complex (mixed kanji and non-kanji)
					String text = token.getSurface();
					List<Reading> tokenReadings = splitComplexToken(text, readingText, token.getStart());
					if (tokenReadings != null) {
						// Correctly matched reading fragments
						node.displayReadings.addAll(tokenReadings);
					} else {
						// No match - fall back to whole reading
						node.displayReadings.add(new Reading(token.getStart(), token.getLength(), readingText));
					}
				}

			}

			if (headNode == null) {
				headNode = node;
			}

			node.prev = prev;
			if (prev != null) {
				prev.next = node;
			}				
			prev = node;

		}

		return headNode;

	}


	/**
	 * Sets the currently analysed text. The <code>reset()</code> method will
	 * be invoked on any currently set filters in order to clear sentence
	 * specific state
	 *
	 * @param text The text to analyse
	 */
	public void setText(String text) {

		this.sentence = new Sentence(text);
		this.needsAnalysis = true;

		// Reset any sentence specific state in filters
		for (ReadingFilter filter : this.filters.values()) {
			filter.reset();
		}

	}


	/**
	 * Adds a reading filter to be applied during processing
	 *
	 * @param priority The precedence of the filter. Lower numbered filters are
	 *                 applied first. If a filter is added with the same
	 *                 priority as an existing filter, the existing filter will
	 *                 be overwritten
	 * @param filter The filter to add
	 */
	public void addFilter(int priority, ReadingFilter filter) {

		this.filters.put(priority, filter);

	}


	/**
	 * Removes the filter with the given priority, if it exists
	 *
	 * @param priority The priority of the filter to remove
	 */
	public void removeFilter(int priority) {

		this.filters.remove(priority);

	}


	/**
	 * Sets all reading filters to be applied during processing. Any existing
	 * filters are discarded
	 *
	 * @param filters The filters to use
	 */
	public void setFilters(Map<Integer,ReadingFilter> filters) {

		this.filters.clear();
		this.filters.putAll(filters);

	}


	/**
	 * Removes any previously set reading filters
	 */
	public void clearFilters() {

		this.filters.clear();

	}


	/**
	 * Returns the complete set of reading filters currently applied during
	 * processing
	 *
	 * @return The reading filters
	 */
	public Map<Integer,ReadingFilter> getFilters() {

		return new TreeMap<Integer, ReadingFilter>(this.filters);

	}


	/**
	 * Gets a reading constraint set on the currently analysed text
	 *
	 * @param position The index within the sentence to get the constraint at
	 * @return The constraint
	 */
	public Reading getReadingConstraint(int position) {

		Reading constraint = this.sentence.getReadingConstraint(position);
		
		if (constraint != null) {
			Reading invertedConstraint = new Reading(constraint.start, constraint.length, TextUtil.invertKanaCase(constraint.text));
			return invertedConstraint;
		}

		return null;

	}


	/**
	 * Sets a reading constraint on the currently analysed text.<br>
	 * Note: In contrast to constraints set directly on a Viterbi instance,
	 * there is no need to pass the constraint in katakana; the exact reading
	 * text supplied will appear in the analysed readings
	 *
	 * @param constraint
	 */
	public void setReadingConstraint(Reading constraint) {

		Reading invertedConstraint = new Reading(constraint.start, constraint.length, TextUtil.invertKanaCase(constraint.text));

		this.sentence.setReadingConstraint(invertedConstraint);
		this.needsAnalysis = true;

	}


	/**
	 * Remove the reading constraint at the given position
	 *
	 * @param position The position from which to remove the constraint
	 */
	public void removeReadingConstraint(int position) {

		this.sentence.removeReadingConstraint(position);
		this.needsAnalysis = true;

	}


	/**
	 * Returns a list of readings generated from the current text. If all you
	 * need is the readings, this will be marginally quicker than calling
	 * {@link #process()} and then {@link ReadingResult#getDisplayReadings()}
	 *
	 * @return The readings
	 */
	public List<Reading> getDisplayReadings() {

		// Compile the initial readings
		ReadingNode node = compileReadings();

		// Apply filters
		for (ReadingFilter filter : this.filters.values()) {
			filter.filterReadings(this.tokens, node);
		}

		// Create reading list to return
		List<Reading> readings = new ArrayList<Reading>();
		for (; node != null; node = node.next) {
			if (node.visible) {
				for (Reading reading : node.displayReadings) {
					readings.add(reading);
				}
			}
		}

		return readings;

	}


	/**
	 * Performs full reading processing and returns a {@link ReadingResult} object
	 * isolated from further changes to the reading processor
	 *
	 * @return An object containing the results of processing
	 */
	public ReadingResult process() {

		// Compile the initial readings
		ReadingNode node = compileReadings();

		// Apply filters
		for (ReadingFilter filter : this.filters.values()) {
			filter.filterReadings(this.tokens, node);
		}

		// Create reading list and visible token bit set
		SortedMap<Integer,Reading> baseReadings = new TreeMap<Integer,Reading>();
		SortedMap<Integer,Reading> displayReadings = new TreeMap<Integer,Reading>();
		BitSet visibleTokens = new BitSet();
		for (; node != null; node = node.next) {
			if (node.visible) {
				if (node.displayReadings.size() > 0) {
					for (int i = node.firstToken; i <= node.lastToken; i++) {
						visibleTokens.set(i);
					}
				}
				for (Reading reading : node.baseReadings) {
					baseReadings.put(reading.start, reading);
				}
				for (Reading reading : node.displayReadings) {
					displayReadings.put(reading.start, reading);
				}
			}
		}

		return new ReadingResult(this.viterbi, this.sentence, this.tokens, baseReadings, displayReadings, visibleTokens);

	}


	/**
	 * @param tokenizer The Tokenizer to use
	 */
	public ReadingProcessor(Tokenizer tokenizer)
	{

		this.viterbi = new Viterbi(tokenizer);

	}


}
