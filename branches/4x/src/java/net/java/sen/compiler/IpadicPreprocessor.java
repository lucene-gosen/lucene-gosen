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

package net.java.sen.compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Preprocesses an unpacked Ipadic dictionary into the CSV form used for
 * compilation
 */
public class IpadicPreprocessor {

	/**
	 * Input dictionary CSV filename
	 */
	private static final String DICTIONARY_CSV_FILENAME = "dictionary.csv";

	/**
	 * Input connection CSV filename
	 */
	private static final String CONNECTION_CSV_FILENAME = "connection.csv";

	/**
	 * The charset used to read the dictionary
	 */
	private String charset;

	/**
	 * The directory of the unpacked dictionary
	 */
	private String inputDirectory;


	/**
	 * Builds a connection CSV file from an unpacked ipadic
	 * 
	 * @param outputFilename The output filename for the connection CSV file
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void buildConnectionCSV(String outputFilename)
			throws UnsupportedEncodingException, FileNotFoundException, IOException
	{
		String regexp =
			"^\\(" +
				"\\(" + 
					"(?:\\(\\(\\(([^ )]+)?(?: ([^ )]+))?(?: ([^ )]+))?(?: ([^ )]+))?\\)(?: ?([^ )]+)?(?: ([^ )]+))?(?: ([^ )]+))?)\\)\\) )?" +
					"\\(\\(\\(([^ )]+)?(?: ([^ )]+))?(?: ([^ )]+))?(?: ([^ )]+))?\\)(?: ?([^ )]+)?(?: ([^ )]+))?(?: ([^ )]+))?)\\)\\) " +
					"\\(\\(\\(([^ )]+)?(?: ([^ )]+))?(?: ([^ )]+))?(?: ([^ )]+))?\\)(?: ?([^ )]+)?(?: ([^ )]+))?(?: ([^ )]+))?)\\)\\)" +
				"\\)" +
				" (\\d+)" +
			"\\)$";

		Pattern pattern = Pattern.compile(regexp);

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.inputDirectory + "/connect.cha"), this.charset));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilename), "UTF-8"));

		String line = null;
		StringBuilder builder = new StringBuilder();


		while (((line = reader.readLine()) != null)) {

			Matcher matcher = pattern.matcher(line);
			matcher.find();

			builder.replace(0, builder.length(), "\"");
			int i;
			if (matcher.group(1) == null) {
				builder.append("*,*,*,*,*,*,*\",\"");
				i = 8;
			} else {
				i = 1;
			}

			for (; i <= 21; i++) {
				String group = matcher.group(i);
				if ((group == null) || group.equals("")) {
					builder.append("*");
				} else {
					builder.append(group);
				}
				if ((i == 7) || (i == 14)) {
					builder.append("\",\"");
				} else if (i != 21) {
					builder.append(",");				
				}
			}
			builder.append("\",");
			builder.append(matcher.group(22));
			builder.append("\n");
			writer.write(builder.toString());

		}
		writer.close();

	}


	/**
	 * Loads cforms.cha from an unpacked ipadic
	 * 
	 * @return The cforms data
	 * @throws IOException 
	 */
	private Map<String,List<String[]>> loadCForms() throws IOException {

		String headExpression = "^\\((\\S+)\\s*$";
		String entryExpression = "^\\s*\\(([^;\\s]+)\\s+(\\S+)\\s+([^)\\s]+)?\\s*([^)\\s]+)?\\s*\\)";

		Pattern headPattern = Pattern.compile(headExpression);
		Pattern entryPattern = Pattern.compile(entryExpression);

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.inputDirectory + "/cforms.cha"), this.charset));

		String line = null;

		Map<String,List<String[]>> cforms = new HashMap<String,List<String[]>>();
		String head = null;
		List<String[]> entries = null;

		while ((line = reader.readLine()) != null) {

			Matcher headMatcher = headPattern.matcher(line);
			if (headMatcher.find()) {
				if (head != null) {
					cforms.put(head, entries);
				}
				head = headMatcher.group(1);
				entries = new ArrayList<String[]>(); 
			} else {
				Matcher entryMatcher = entryPattern.matcher(line);
				if (entryMatcher.find()) {
					entries.add(new String[] {entryMatcher.group(1), entryMatcher.group(2), entryMatcher.group(3), entryMatcher.group(4)});
				}
			}
		}

		if (head != null) {
			cforms.put(head, entries);
		}

		return cforms;

	}


	/**
	 * Builds a dictionary CSV file from an unpacked ipadic
	 *
	 * @param outputFilename The filename to use for the dictionary CSV file
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	private void buildDictionaryCSV(String outputFilename) throws IOException, FileNotFoundException {

		Map<String,List<String[]>> cforms = loadCForms();

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilename), "UTF-8"));

		String expression =
			"^\\(" +
				"品詞 \\(([\\S]*)(?: ([\\S]*))?(?: ([\\S]*))?(?: ([\\S]*))?\\)\\) " +
				"\\(\\(見出し語 \\(\"?([^\" ]+)\"? (\\d+)\\)\\) " +
				"\\(読み \"?([^\" ]+)\"?\\) " +
				"\\(発音 \"?([^\" ]+)\"?\\) " +
				"(?:\\(活用型 ([^)]*)\\) )?" +
			"\\)$";

		Pattern linePattern = Pattern.compile(expression);

		File directory = new File(this.inputDirectory);
		File[] dictionaryFiles = directory.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.matches("^.*\\.dic$");
			}

		});

		for (File dictionaryFile : dictionaryFiles) {

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dictionaryFile), this.charset));

			String line = null;
			int lineNumber = 0;

			while ((line = reader.readLine()) != null) {

				lineNumber++;

				Matcher matcher = linePattern.matcher(line);
				boolean found = matcher.find();

				if (!found) {

					// Try to add up to two further lines
					// One line in ipadic 2.6.0 requires this
					for (int i = 0; !found && (i < 2); i++) {
						line = line.trim() + " " + reader.readLine().trim();
						lineNumber++;
						matcher = linePattern.matcher(line);
						found = matcher.find();
					}

				}

				if (!found) {
					throw new IOException("Parse error in file " + dictionaryFile.getName() + " line " + lineNumber);
				}

				String lex = matcher.group(5);
				String score = matcher.group(6);
				String pos1 = matcher.group(1);
				String pos2 = matcher.group(2);
				String pos3 = matcher.group(3);
				String pos4 = matcher.group(4);
				String ctype = matcher.group(9);
				String base = lex;
				String reading = matcher.group(7);
				String pronunciation = matcher.group(8);

				if (pos1 == null) pos1 = "*";
				if (pos2 == null) pos2 = "*";
				if (pos3 == null) pos3 = "*";
				if (pos4 == null) pos4 = "*";
				if (ctype == null) ctype = "*";

				String lexBase = lex;
				String readingBase = reading;
				String pronunciationBase = pronunciation;

				List<String[]> ctypeList = cforms.get(ctype);

				if (ctypeList == null) {

					String csvLine = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s,%9$s,%10$s,%11$s\n",
							lex, score, pos1, pos2, pos3, pos4, ctype, "*", base, reading, pronunciation);
					writer.write(csvLine);

				} else {

					String[] ctype0 = ctypeList.get(0); 

					if (!ctype0[1].equals("*")) {
						lexBase = lex.substring(0, lex.length() - ctype0[1].length());
						readingBase = reading.substring(0, reading.length() - ctype0[2].length());
						pronunciationBase = pronunciation.substring(0, pronunciation.length() - ctype0[2].length());
					}

					for (String[] ctypeArray : ctypeList) {

						String extension1 = ((ctypeArray[1] == null) || ctypeArray[1].equals("*")) ? "" : ctypeArray[1]; 
						String extension2 = ((ctypeArray[2] == null) || ctypeArray[2].equals("*")) ? "" : ctypeArray[2];

						String cform = ctypeArray[0];
						String composedLex = lexBase + extension1;
						String composedReading = readingBase + extension2;
						String composedPronunciation = pronunciationBase + extension2;

						if (composedLex.length() > 0) {
							String csvLine = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s,%9$s,%10$s,%11$s\n",
									composedLex, score, pos1, pos2, pos3, pos4, ctype, cform, base, composedReading, composedPronunciation);
							writer.write(csvLine);
						}

					}
				}
			}
		}

		writer.close();

	}


	/**
	 * Preprocesses the dictionary
	 *
	 * @param outputDirectory The directory to write the preprocessed dictionary to 
	 * @throws IOException
	 */
	public void build(String outputDirectory) throws IOException {

		buildConnectionCSV(outputDirectory + "/" + CONNECTION_CSV_FILENAME);

		buildDictionaryCSV(outputDirectory + "/" + DICTIONARY_CSV_FILENAME);

	}


	/**
	 * Creates a new preprocessor for the unpacked dictionary in the given
	 * directory
	 * 
	 * @param charset The charset used to read the dictionary 
	 * @param inputDirectory The directory of the unpacked dictionary
	 */
	public IpadicPreprocessor(String charset, String inputDirectory) {

		this.charset = charset;
		this.inputDirectory = inputDirectory;

	}

}
