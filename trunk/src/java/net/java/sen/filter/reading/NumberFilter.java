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

import java.lang.Character.UnicodeBlock;
import java.util.List;

import net.java.sen.dictionary.Reading;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.ReadingFilter;
import net.java.sen.filter.ReadingNode;


/**
 * A ReadingFilter that adapts the basic dictionary-based reading output to
 * account for the reading behaviour of number kanji and numeric suffixes
 */
public class NumberFilter implements ReadingFilter {

	/* (non-Javadoc)
	 * @see net.java.sen.filter.ReadingFilter#filterReadings(java.util.List, net.java.sen.filter.ReadingNode)
	 */
	public void filterReadings(List<Token> tokens, ReadingNode readingNode) {

		for (ReadingNode node = readingNode; node != null; node = node.next) {

			// Only operate on regular nodes - one token, one reading
			if (
					   (node.firstToken == node.lastToken)
					&& (node.displayReadings.size() == 1)
			   )
			{

				Token token = tokens.get(node.firstToken);
				Reading reading = node.displayReadings.get(0);
				String surface = token.getSurface();

				// Default basic number processing. Treat numbers 1-10,000 like kana and
				// don't show readings for them
				if (
						   (token.getMorpheme().getPartOfSpeech().equals("名詞-数"))
						&& ("一二三四五六七八九十百千万".contains(token.getSurface()))
				) {
					node.visible = false;
				}

				// "ヶ月/ヵ月/カ月/ケ月" processing - "か" is stripped from the reading
				// "か月" is taken care of by standard compound token processing
				// "箇月/個月" we leave with full reading
				if (
						   (surface.length() >= 2)
						&& (
								   surface.startsWith("ヵ")
								|| surface.startsWith("ヶ")
								|| surface.startsWith("カ")
								|| surface.startsWith("ケ")
						   )
						&& (Character.UnicodeBlock.of(surface.charAt(1)) == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
						&& (reading.text.charAt(0) == 'か')
				   )
				{
					Reading newReading = new Reading(reading.start + 1, reading.length - 1, reading.text.substring(1));
					node.displayReadings.set(0, newReading);
					continue;
				}

				// "一分" processing - becomes "ぷん" where appropriate
				if (
						token.getMorpheme().getPartOfSpeech().equals("名詞-接尾-助数詞")
						&& (node.prev != null)
						&& (tokens.get(node.prev.lastToken).getMorpheme().getPartOfSpeech().equals("名詞-数"))
						&& ("分歩本品辺杯報版泊派波羽".contains(surface))
				   )
				{
					char firstReadingChar = reading.text.charAt(0);
					String numberKanji = tokens.get(node.prev.lastToken).getSurface();
					if (
							"一三四六八十百千万".contains(numberKanji)
							&& ("はひふへほ".indexOf(firstReadingChar) != -1)
					   )
					{
						String newText = (char)(firstReadingChar + 0x02) + reading.text.substring(1, reading.text.length());
						Reading newReading = new Reading(reading.start, reading.length, newText);
						node.baseReadings.set(0, newReading);
						node.displayReadings.set(0, newReading);
						continue;
					}
				}
						
			}
		}

	}


	/* (non-Javadoc)
	 * @see net.java.sen.filter.ReadingFilter#reset()
	 */
	public void reset() {

		// Nothing to do at present

	}


}
