/*
 * Copyright (C) 2002-2007
 * Tsuyoshi Fukui <fukui556@oki.com>
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

package net.java.sen.filter.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Sentence;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.StreamFilter;


/**
 * A filter to ignore delimited comments in the input sentence
 */
public class CommentFilter implements StreamFilter {


	/**
	 * The list of rules defining the start and end of comments, and the
	 * part-of-speech code to be used in the <code>Token</code>s
	 * used to replace them
	 */
	protected List<Rule> ruleList = new ArrayList<Rule>();

	/**
	 * The comment <code>Token</code>s generated from the current sentence
	 */
	private List<Token> commentTokens = new ArrayList<Token>();


	/**
	 * A rule defining the start and end of a comment, and the
	 * part-of-speech code to be used in the <code>Token</code>
	 * used to replace it
	 */
	class Rule {

		/**
		 * The text marking the starting boundary of the comment
		 */
		public String start;

		/**
		 * The text marking the ending boundary of the comment
		 */
		public String end;

		/**
		 * The part-of-speech code used in the Token that replaces the comment
		 */
		public String partOfSpeech;

	}


	/**
	 * Reads the rules to apply as space-delimited text
	 *
	 * @param reader The reader from which to read the rules
	 * @throws IOException
	 */
	public void readRules(BufferedReader reader) throws IOException {

		String line = null;

		while ((line = reader.readLine()) != null) {
			StringTokenizer tokenizer = new StringTokenizer(line);
			if (!tokenizer.hasMoreTokens()) {
				continue;
			}

			Rule rule = new Rule();

			if (tokenizer.countTokens() == 2) {
				rule.start = tokenizer.nextToken();
				rule.end = "";
				rule.partOfSpeech = tokenizer.nextToken();
			} else {
				rule.start = tokenizer.nextToken();
				rule.end = tokenizer.nextToken();
				rule.partOfSpeech = tokenizer.nextToken();
			}

			this.ruleList.add(rule);
		}

	}



	/* (non-Javadoc)
	 * @see net.java.sen.dictionary.Filter#preProcess(net.java.sen.dictionary.Sentence)
	 */
	public void preProcess(Sentence sentence) {

		Iterator itr = this.ruleList.iterator();
		this.commentTokens.clear();

		String surface = new String(sentence.getCharacters());
		while (itr.hasNext()) {
			int count = 0;
			Rule rule = (Rule) itr.next();

			while (count < surface.length()) {
				int start = -1;
				int end = -1;
				String tokenStr;

				start = surface.indexOf(rule.start, count);
				if (start >= 0) {
					count = start + rule.start.length();
					if (rule.end.equals("")) {
						end = start;
					} else {
						end = surface.indexOf(rule.end, count);
					}
					if (end >= 0) {
						if (rule.end.equals("")) {
							end += rule.start.length();
						} else {
							end += rule.end.length();
						}
						count = end;
						tokenStr = surface.substring(start, end);

						Morpheme morpheme = new Morpheme (rule.partOfSpeech, "*", "*", tokenStr, new String[]{tokenStr}, new String[]{tokenStr}, null);
						
                        Token token = new Token(
                        		tokenStr,
                        		0,
                        		start,
                        		end - start,
                        		morpheme
                        );

						this.commentTokens.add(token);

						sentence.setBreakingIgnoreSpan(start, (short)(end - start));

					} else {
						count = surface.length();
					}
				} else {
					count = surface.length();
				}
			}
		}

	}
	

	/* (non-Javadoc)
	 * @see net.java.sen.dictionary.Filter#postProcess(net.java.sen.dictionary.Token[])
	 */
	public List<Token> postProcess(List<Token> tokens) {

		if ((this.commentTokens.size() == 0) || (tokens.size() == 0)) {
			return tokens;
		}

		if (tokens.size() == 0) {
			return new ArrayList<Token>(this.commentTokens);
		}

		List<Token> newTokens = new ArrayList<Token>(tokens.size() + this.commentTokens.size());

		Iterator<Token> iterator = this.commentTokens.iterator();
		Token commentToken = iterator.next();
		for (int i = 0; i < tokens.size(); i++) {
			while (commentToken != null && tokens.get(i).getStart() >= commentToken.getStart()) {
				newTokens.add(commentToken);
				if (!iterator.hasNext()) {
					commentToken = null;
					break;
				}
				commentToken = iterator.next();
			}
			newTokens.add(tokens.get(i));
		}

		if (commentToken != null) {
			newTokens.add(commentToken);
			while (iterator.hasNext()) {
				newTokens.add(iterator.next());
			}
		}

		return newTokens;

	}


}
