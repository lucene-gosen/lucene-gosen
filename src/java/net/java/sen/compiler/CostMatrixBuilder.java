/*
 * Copyright (C) 2001-2007
 * Taku Kudoh <taku-ku@is.aist-nara.ac.jp>
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

package net.java.sen.compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


/**
 * Builds an axis of the Connection Cost matrix from supplied part-of-speech /
 * conjugation data
 * 
 * TODO The workings of this class are relatively simple but somewhat magical.
 * It could use some explanation from someone who understands what exactly it's
 * doing.
 */
class CostMatrixBuilder {

	/**
	 * Set containing all unique values from one column of the Connection Cost CSV file
	 */
	private LinkedHashSet<String> ruleSet = new LinkedHashSet<String>();

	/**
	 * The input rules (from ruleSet) split into individual values
	 */
	private Vector<String[]> ruleList = new Vector<String[]>();

	
	/**
	 * TODO This is magic. How does this work?
	 */
	private Vector<Vector<Integer>> idList = new Vector<Vector<Integer>>();

	/**
	 * // dic2IdHash('word type')= id for word type
	 * TODO This is magic. How does this work?
	 */
	private Map<String, Integer> dicIndex = new HashMap<String, Integer>();

	
	/**
	 * A map containing a unique integer ID for each rule added
	 */
	private Map<String, Integer> ruleIndex = new HashMap<String, Integer>();

	/**
	 * Contains the set of the rules' last fields where the field is not equal to '*'
	 * TODO This is magic. How does this work?
	 */
	private Set<String> lexicalized = new HashSet<String>();


	/**
	 * Converts a list of part-of-speech / conjugation identifier strings to
	 * a vector of IDs unique to each string 
	 * TODO This is magic. How does this work?
	 *
	 * @param csv The part-of-speech / conjugation strings
	 * @param parent TODO How does this work?
	 * @return A vector of IDs for the strings
	 */
	private Vector<Integer> getIdList(String csv[], boolean parent) {

		Vector<Integer> results = new Vector<Integer>(this.ruleList.size());
		results.setSize(this.ruleList.size());

		for (int j = 0; j < this.ruleList.size(); j++) {
			results.set(j, j);
		}

		for (int j = 0; j < csv.length; j++) {
			int k = 0;
			for (int n = 0; n < results.size(); n++) {
				int i = results.get(n);
				String rl_ij = this.ruleList.get(i)[j];
				if (
						   ((!parent) && (csv[j].charAt(0) == '*'))
						|| ((parent) && (rl_ij.charAt(0) == '*'))
						|| rl_ij.equals(csv[j])
				   )
				{
					results.set(k++, results.get(n));
				}
			}
			results.setSize(k);
		}

		return results;

	}


	/**
	 * Calculates a unique(?) ID for a split rule
	 * TODO This is magic. How does this work?
	 *
	 * @param csv The split rule
	 * @return The calculated ID
	 */
	private int getDicIdNoCache(String csv[]) {

		Vector<Integer> results = getIdList(csv, true);

		if (results.size() == 0) {
			throw new IllegalArgumentException();
		}

		int priority[] = new int[results.size()];
		int max = 0;
		for (int i = 0; i < results.size(); i++) {
			String csvValues[] = this.ruleList.get(results.get(i));
			for (int j = 0; j < csvValues.length; j++) {
				if (csvValues[j].charAt(0) != '*') {
					priority[i]++;
				}
			}
			if (priority[max] < priority[i]) {
				max = i;
			}
		}

		return results.get(max);

	}


	/**
	 * Adds a Connection Cost CSV value to the builder
	 *
	 * @param rule The rule to add
	 */
	public void add(String rule) {

		this.ruleSet.add(rule);

	}


	/**
	 * Builds the matrix axis based on the data passed to {@link #add(String)}.
	 * It is an error to call {@link #add(String)} after calling
	 * {@link #build()}.
	 */
	public void build() {

		int i = 0;

		this.ruleList.setSize(this.ruleSet.size());
		for (Iterator<String> iterator = this.ruleSet.iterator(); iterator.hasNext();) {
			String str = iterator.next();
			this.ruleIndex.put(str, i);

			String tokenList[] = str.split(",");

			this.ruleList.set(i, tokenList);
			if (tokenList[tokenList.length - 1].charAt(0) != '*') {
				this.lexicalized.add(tokenList[tokenList.length - 1]);
			}
			i++;
		}

		this.ruleSet.clear();

		this.idList.setSize(this.ruleList.size());
		for (int j = 0; j < this.ruleList.size(); j++) {
			Vector<Integer> results = getIdList(this.ruleList.get(j), false);
			this.idList.set(j, results);
		}

	}


	/**
	 * Returns the size of the built matrix axis
	 *
	 * @return The size of the built matrix axis
	 */
	public int size() {

		return this.ruleList.size();

	}


	/**
	 * TODO This is magic. How does this work?
	 *
	 * @param rule The rule
	 * @return TODO how is this ID defined?
	 */
	public int getDicId(String rule) {

		String csv[] = rule.split(",");

		String lex = csv[csv.length - 1];

		if (this.lexicalized.contains(lex)) {

			return getDicIdNoCache(csv);

		}

		// Remove end field
		String partOfSpeech = rule.substring(0, rule.lastIndexOf(","));

		Integer r = this.dicIndex.get(partOfSpeech);
		if ((r != null) && (r != 0)) {

			// 0 if empty
			return r - 1;

		}

		int rg = getDicIdNoCache(csv);

		this.dicIndex.put(partOfSpeech, rg + 1);
		return rg;

	}


	/**
	 * Converts a rule to a vector of IDs unique to each component part
	 *
	 * @param rule The rule
	 * @return A vector of IDs for the component parts
	 */
	public Vector<Integer> getRuleIdList(String rule) {

		return this.idList.get(this.ruleIndex.get(rule));

	}


}
