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

package net.java.sen.util;


/**
 * Miscellaneous text utilities
 */
public class TextUtil {

	/**
	 * Swap hiragana and katakana
	 *
	 * @param result The string to convert
	 * @return The converted string
	 */
	public static String invertKanaCase(String result) {
	
		StringBuffer foldedStringBuffer = new StringBuffer(result);
		int length = foldedStringBuffer.length();
		for (int i = 0; i < length; i++) {
			char character = foldedStringBuffer.charAt(i);
			if ((character >= 0x30a1) && (character < 0x30f4)) {
				// Katakana -> hiragana
				foldedStringBuffer.setCharAt (i, (char)(character - 96));
			} else if ((character >= 0x3041) && (character < 0x3094)) {
				// Hiragana -> katakana
				foldedStringBuffer.setCharAt (i, (char)(character + 96));
			}
		}
		return foldedStringBuffer.toString();
	
	}

}
