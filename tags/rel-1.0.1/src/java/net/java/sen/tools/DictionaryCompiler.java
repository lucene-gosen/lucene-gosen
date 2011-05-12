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

package net.java.sen.tools;

import java.io.IOException;

import net.java.sen.compiler.DictionaryBuilder;


/**
 * Compiles source CSV data into the dictionary data files used for analysis
 */
public class DictionaryCompiler {

	/**
	 * Main method
	 * 
	 * @param args &lt;Custom dictionary file&gt; (optional)
	 * @throws IOException 
	 */
	public static void main(String args[]) throws IOException {

		String[] customDictionaries = args;
		new DictionaryBuilder(customDictionaries);

	}


}
