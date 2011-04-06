/*
 * Copyright (C) 2002-2007
 * Takashi Okamoto <tora@debian.org>
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
import java.util.ArrayList;
import java.util.List;

import net.java.sen.dictionary.Sentence;
import net.java.sen.dictionary.Token;
import net.java.sen.dictionary.Tokenizer;
import net.java.sen.dictionary.Viterbi;
import net.java.sen.filter.StreamFilter;


/**
 * Tokenizes strings
 * 
 * <p>See examples.StringTaggerDemo in the Sen source for an example of how to
 * use this class
 * 
 * <p><b>Thread Safety</b>: Objects of this class are <b>NOT</b> thread safe and
 * should not be accessed simultaneously by multiple threads. Note that creating
 * additional instances using {@link SenFactory} is relatively cheap in both
 * memory and time
 * 
 */
public class StringTagger {

	/**
	 * The Viterbi analyser used to decompose strings
	 */
	private Viterbi viterbi = null;

	/**
	 * {@link StreamFilter}s to apply during analysis
	 */
	private List<StreamFilter> filterList = new ArrayList<StreamFilter>();


	/**
	 * Apply the pre-processing phase of all attached {@link StreamFilter}s to
	 * the input sentence
	 *
	 * @param sentence The input sentence
	 */
	private void filterPreProcess(Sentence sentence) {

		for (StreamFilter filter : this.filterList) {
			filter.preProcess(sentence);
		}

	}


	/**
	 * Apply the post-processing phase of all attached {@link StreamFilter}s to
	 * the analysed {@link Token}s
	 *
	 * @param tokens The analysed {@link Token}s
	 * @return The filtered {@link Token}s
	 */
	private List<Token> filterPostProcess(List<Token> tokens) {

		for (int i = this.filterList.size() - 1; i >= 0; i--) {
			StreamFilter filter = this.filterList.get(i);
			tokens = filter.postProcess(tokens);
		}

		return tokens;

	}


	/**
	 * Add a {@link StreamFilter} to be applied during analysis
	 *
	 * @param filter The {@link StreamFilter} to add
	 */
	public void addFilter(StreamFilter filter) {

		this.filterList.add(filter);

	}


	/**
	 * Remove all current {@link StreamFilter}s
	 */
	public void removeFilters() {

		this.filterList.clear();

	}


	/**
	 * Decompose a string into its most likely constituent morphemes
	 * 
	 * @param surface The string to analyse
	 * @return An array of {@link Token}s representing the most likely morphemes
	 * @throws IOException 
	 */
	public List<Token> analyze(String surface) throws IOException {

		Sentence sentence = new Sentence(surface.toCharArray());
		filterPreProcess(sentence);

		List<Token> tokens = this.viterbi.getBestTokens(sentence);

		tokens = filterPostProcess(tokens);

		return tokens;

	}


	/**
	 * Decompose a string into its most likely constituent morphemes
	 * 
	 * @param surface The string to analyse
	 * @return An array of {@link Token}s representing the most likely morphemes
	 * @throws IOException 
	 */
	public List<Token> analyze(char[] surface) throws IOException {

		Sentence sentence = new Sentence(surface);
		filterPreProcess(sentence);

		List<Token> tokens = this.viterbi.getBestTokens(sentence);

		tokens = filterPostProcess(tokens);

		return tokens;

	}


	/**
	 * @param tokenizer The Tokenizer to use for analysis 
	 */
	public StringTagger(Tokenizer tokenizer)
	{

		this.viterbi = new Viterbi(tokenizer);

	}


}
