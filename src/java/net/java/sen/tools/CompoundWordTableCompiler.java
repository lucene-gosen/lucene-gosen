/*
 * Copyright (C) 2004-2007
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

package net.java.sen.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import net.java.sen.util.CSVParser;


/**
 * Compiles a table for the CompoundWordFilter
 */
public class CompoundWordTableCompiler {

	/**
	 * Input compound CSV filename
	 */
	private static final String COMPOUND_CSV_FILENAME = "compound.csv";

	/**
	 * Output compound table filename
	 */
	private static final String COMPOUND_TABLE_FILENAME = "compound.sen";

	/**
	 * Start of part-of-speech data within the dictionary CSV
	 */
	private static final int PART_OF_SPEECH_START = 2;

	/**
	 * Size of part-of-speech data within the dictionary CSV
	 */
	private static final int PART_OF_SPEECH_SIZE = 7;


	/**
	 * Builds a compound word table
	 *
	 * @param reader The input compound word data
	 * @param partOfSpeechStart The start of the part-of-speech data within
	 *                          the CSV
	 * @param partOfSpeechSize The number of elements of part-of-speech data
	 *                         within the CSV
	 * @param tableFilename The filename for the compiled table
	 * @throws IOException
	 */
	public static void buildTable(BufferedReader reader, int partOfSpeechStart, int partOfSpeechSize, String tableFilename)
			throws IOException
	{
		String t;
		int line = 0;

		HashMap<String, String> compoundTable = new HashMap<String, String>();
		StringBuffer buffer = new StringBuffer();
		while ((t = reader.readLine()) != null) {

			CSVParser parser = new CSVParser(t);
			String csv[] = parser.nextTokens();
			if (csv.length < (partOfSpeechSize + partOfSpeechStart)) {
				throw new RuntimeException("format error:" + line);
			}

			buffer.setLength(0);
			for (int i = partOfSpeechStart; i < (partOfSpeechStart + partOfSpeechSize - 1); i++) {
				buffer.append(csv[i]);
				buffer.append(',');
			}

			buffer.append(csv[partOfSpeechStart + partOfSpeechSize - 1]);
			buffer.append(',');

			for (int i = partOfSpeechStart + partOfSpeechSize; i < (csv.length - 2); i++) {
				buffer.append(csv[i]);
				buffer.append(',');
			}
			buffer.append(csv[csv.length - 2]);

			compoundTable.put(buffer.toString(), csv[csv.length - 1]);

		}

		reader.close();
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(tableFilename));
		os.writeObject(compoundTable);
		os.close();

	}


	/**
	 * Main method
	 * 
	 * @param args Ignored 
	 */
	public static void main(String args[]) {

		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(COMPOUND_CSV_FILENAME),
					"UTF-8"
			));

			buildTable(reader, PART_OF_SPEECH_START, PART_OF_SPEECH_SIZE, COMPOUND_TABLE_FILENAME);

		} catch (Exception e) {

			e.printStackTrace();
			System.exit(1);

		}

	}


}
