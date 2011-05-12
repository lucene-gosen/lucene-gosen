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

package net.java.sen.tools;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.java.sen.compiler.IpadicPreprocessor;


/**
 * Preprocesses an input dictionary into the intermediate CSV format used by the
 * dictionary compiler. Currently assumes an ipadic dictionary
 */
public class DictionaryPreprocessor {

	/**
	 * Precompiles a dictionary into the intermediate form used by the
	 * dictionary compiler
	 *
	 * @param args The directory of the unpacked input dictionary
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {

		if (args.length != 3) {
			System.out.println("Syntax: java DictionaryPreprocessor <input charset> <dictionary directory> <output directory>");
			System.exit(1);
		}

		String inputCharset = args[0];
		String inputDirectory = args[1];
		String outputDirectory = args[2];

		new IpadicPreprocessor(inputCharset, inputDirectory).build(outputDirectory);

	}

}
