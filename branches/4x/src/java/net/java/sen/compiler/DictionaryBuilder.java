/*
 * Copyright (C) 2002-2007
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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.java.sen.dictionary.CToken;
import net.java.sen.trie.TrieBuilder;
import net.java.sen.util.CSVData;
import net.java.sen.util.CSVParser;


/**
 * Compiles CSV source data into the data files used for analysis
 */
public class DictionaryBuilder {

	/**
	 * Input dictionary CSV filename
	 */
	private static final String DICTIONARY_CSV_FILENAME = "dictionary.csv";

	/**
	 * Input connection CSV filename
	 */
	private static final String CONNECTION_CSV_FILENAME = "connection.csv";

	/**
	 * Compiled connection cost data filename
	 */
	private static final String CONNECTION_COST_DATA_FILENAME = "connectionCost.sen";

	/**
	 * Compiled part of speech data filename
	 */
	private static final String PART_OF_SPEECH_DATA_FILENAME = "partOfSpeech.sen";

	/**
	 * Compiled token data filename
	 */
	private static final String TOKEN_DATA_FILENAME = "token.sen";

	/**
	 * Compiled trie data filename
	 */
	private static final String TRIE_DATA_FILENAME = "trie.sen";

	/**
	 * Default connection cost
	 */
	private static final short DEFAULT_CONNECTION_COST = 10000;

	/**
	 * Start of part-of-speech data within the dictionary CSV
	 */
	private static final int PART_OF_SPEECH_START = 2;

	/**
	 * Size of part-of-speech data within the dictionary CSV
	 */
	private static final int PART_OF_SPEECH_SIZE = 7;

	/**
	 * Beginning-of-string token part-of-speech
	 */
	private static final String BOS_PART_OF_SPEECH = "文頭,*,*,*,*,*,*";
	
	/**
	 * End-of-string token part-of-speech
	 */
	private static final String EOS_PART_OF_SPEECH = "文末,*,*,*,*,*,*";

	/**
	 * Unknown token part-of-speech
	 */
	private static final String UNKNOWN_PART_OF_SPEECH = "名詞,サ変接続,*,*,*,*,*";


	/**
	 * Precursor data for the Trie file
	 */
	private static class TrieData {

		/**
		 * Trie keys
		 */
		public String keys[];
	
		/**
		 * Trie values
		 */
		public int values[];
	
		/**
		 * The actual number of entries in the keys/values arrays
		 */
		public int size;

	}


	/**
	 * Increases the size of an array of <code>short</code>s
	 *
	 * @param current The existing array
	 * @return The resized array
	 */
	private static short[] resize(short current[]) {

		short tmp[] = new short[(int) (current.length * 1.5)];
		System.arraycopy(current, 0, tmp, 0, current.length);

		return tmp;

	}


	/**
	 * Splits a compound reading or pronunciation field into a list
	 * 
	 * Compound fields are of the form:
	 * 
	 *   "{head1/head2[/head3 ...]}tail"
	 * 
	 * The returned list will consist of:
	 * 
	 *   "head1tail",
	 *   "head2tail",
	 *   "head3tail",
	 *   ...
	 *
	 * @param compoundField The field to split
	 * @return The split list
	 */
	private List<String> splitCompoundField(String compoundField) {

		List<String> splitFieldList;

		if ((compoundField.length() == 0) || (compoundField.charAt(0) != '{')) {

			// No alternatives
			splitFieldList = new ArrayList<String>(1);
			splitFieldList.add(compoundField);

		} else {

			// 1 or more alternatives. No existing entry in Ipadic has more than 4
			splitFieldList = new ArrayList<String>(4);

			String[] parts = compoundField.split("[{}]");
			String tail = (parts.length == 3) ? parts[2] : "";
			String[] heads = parts[1].split("/");

			for (int i = 0; i < heads.length; i++) {
				splitFieldList.add(heads[i] + tail);
			}

		}

		return splitFieldList;

	}


	/**
	 * Creates the part-of-speech data file
	 * 
	 * @param dictionaryCSVFilenames The filenames of the dictionary CSV data file and any additional dictionaries 
	 * @param partOfSpeechDataFilename The filename for the part-of-speech data file
	 * @param matrixBuilders The three <code>CostMatrixBuilder</code>s
	 * @param partOfSpeechStart The starting index of the part-of-speech data within a CSV line
	 * @param partOfSpeechSize The number of part-of-speech values within a CSV line
	 * @param charset The charset of the CSV data
	 * @param bosPartOfSpeech The beginning-of-string part-of-speech code
	 * @param eosPartOfSpeech The end-of-string part-of-speech code 
	 * @param unknownPartOfSpeech  The beginning-of-string part-of-speech code
	 * @param dictionaryList Populated by this method with the String/CToken tuples that will be used to create the Token file
	 * @param standardCTokens Populated by this method with the three standard CTokens ("bos", "eos" and "unknown")
	 *
	 * @throws IOException 
	 */
	private void createPartOfSpeechDataFile(List<String> dictionaryCSVFilenames, String partOfSpeechDataFilename,
			CostMatrixBuilder[] matrixBuilders, int partOfSpeechStart, int partOfSpeechSize, String charset,
			String bosPartOfSpeech, String eosPartOfSpeech, String unknownPartOfSpeech, VirtualTupleList dictionaryList, CToken[] standardCTokens) throws IOException
	{

		String[] csvValues = null;

		CSVData key_b = new CSVData();
		CSVData pos_b = new CSVData();

		DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(partOfSpeechDataFilename)));

		for (String dictionaryCSVFilename : dictionaryCSVFilenames) {
			
			CSVParser parser = new CSVParser(new FileInputStream(dictionaryCSVFilename), charset);

			while ((csvValues = parser.nextTokens()) != null) {

				if (csvValues.length < (partOfSpeechSize + partOfSpeechStart)) {
					throw new RuntimeException("format error:" + parser.currentLine());
				}

				key_b.clear();
				pos_b.clear();
				for (int i = partOfSpeechStart; i < (partOfSpeechStart + partOfSpeechSize); i++) {
					key_b.append(csvValues[i]);
					pos_b.append(csvValues[i]);
				}

				for (int i = partOfSpeechStart + partOfSpeechSize; i < csvValues.length; i++) {
					pos_b.append(csvValues[i]);
				}

				CToken ctoken = new CToken();

				ctoken.rcAttr2 = (short) matrixBuilders[0].getDicId(key_b.toString());
				ctoken.rcAttr1 = (short) matrixBuilders[1].getDicId(key_b.toString());
				ctoken.lcAttr = (short) matrixBuilders[2].getDicId(key_b.toString());
				ctoken.partOfSpeechIndex = outputStream.size() >> 1;
				ctoken.length = (short) csvValues[0].length();
				ctoken.cost = (short) Integer.parseInt(csvValues[1]);

				dictionaryList.add(csvValues[0], ctoken);


				// Write to part of speech data file

				StringBuilder partOfSpeechBuilder = new StringBuilder();
				for (int i = partOfSpeechStart; i < (partOfSpeechStart + 4); i++) {
					if (!csvValues[i].equals("*")) {
						partOfSpeechBuilder.append(csvValues[i]);
						partOfSpeechBuilder.append("-");
					}
				}
				String partOfSpeech = partOfSpeechBuilder.substring(0, partOfSpeechBuilder.length() - 1);
				String conjugationalType = csvValues[partOfSpeechStart + 4];
				String conjugationalForm = csvValues[partOfSpeechStart + 5];
				String basicForm = csvValues[partOfSpeechStart + 6];
				List<String> readings = splitCompoundField(csvValues[partOfSpeechStart + 7]);
				List<String> pronunciations = splitCompoundField(csvValues[partOfSpeechStart + 8]);

				outputStream.writeChar(partOfSpeech.length());
				outputStream.writeChars(partOfSpeech);

				outputStream.writeChar(conjugationalType.length());
				outputStream.writeChars(conjugationalType);

				outputStream.writeChar(conjugationalForm.length());
				outputStream.writeChars(conjugationalForm);

				outputStream.writeChar(basicForm.length());
				outputStream.writeChars(basicForm);

				outputStream.writeChar(readings.size());

				for (String reading : readings) {
					outputStream.writeChar(reading.length());
					outputStream.writeChars(reading);
				}

				for (String pronunciation : pronunciations) {
					outputStream.writeChar(pronunciation.length());
					outputStream.writeChars(pronunciation);
				}

			}

		}

		outputStream.close();

		dictionaryList.sort();

		CToken bosCToken = new CToken();
		bosCToken.rcAttr2 = (short) matrixBuilders[0].getDicId(bosPartOfSpeech);
		bosCToken.rcAttr1 = (short) matrixBuilders[1].getDicId(bosPartOfSpeech);
		bosCToken.lcAttr = (short) matrixBuilders[2].getDicId(bosPartOfSpeech);
		standardCTokens[0] = bosCToken;

		CToken eosCToken = new CToken();
		eosCToken.rcAttr2 = (short) matrixBuilders[0].getDicId(eosPartOfSpeech);
		eosCToken.rcAttr1 = (short) matrixBuilders[1].getDicId(eosPartOfSpeech);
		eosCToken.lcAttr = (short) matrixBuilders[2].getDicId(eosPartOfSpeech);
		standardCTokens[1] = eosCToken;

		CToken unknownCToken = new CToken();
		unknownCToken.rcAttr2 = (short) matrixBuilders[0].getDicId(unknownPartOfSpeech);
		unknownCToken.rcAttr1 = (short) matrixBuilders[1].getDicId(unknownPartOfSpeech);
		unknownCToken.lcAttr = (short) matrixBuilders[2].getDicId(unknownPartOfSpeech);
		unknownCToken.partOfSpeechIndex = -1;
		standardCTokens[2] = unknownCToken;

	}


	/**
	 * Creates the connection cost matrix file
	 * 
	 * @param connectionCSVFilename The filename of the connection CSV data
	 * @param connectionCostDataFilename The filename for the connection cost matrix
	 * @param defaultCost The default connection cost
	 * @param charset The charset of the connection CSV data
	 * @return An array of three <code>CostMatrixBuilder</code>s
	 * @throws IOException 
	 */
	private CostMatrixBuilder[] createConnectionCostFile(String connectionCSVFilename, String connectionCostDataFilename, short defaultCost, String charset) throws IOException {

		CostMatrixBuilder[] matrixBuilders = new CostMatrixBuilder[3];

		matrixBuilders[0] = new CostMatrixBuilder();
		matrixBuilders[1] = new CostMatrixBuilder();
		matrixBuilders[2] = new CostMatrixBuilder();
		Vector<String> rule1 = new Vector<String>();
		Vector<String> rule2 = new Vector<String>();
		Vector<String> rule3 = new Vector<String>();

		// The approximate length of the file, plus a bit. If we're wrong it'll be expanded during processing
		short[] scores = new short[30000];

		// Read connection cost CSV data
		CSVParser parser = new CSVParser(new FileInputStream(connectionCSVFilename), charset);
		String t[];
		int line = 0;
		while ((t = parser.nextTokens()) != null) {
			if (t.length < 4) {
				throw new IOException("Connection cost CSV format error");
			}
			matrixBuilders[0].add(t[0]);
			rule1.add(t[0]);

			matrixBuilders[1].add(t[1]);
			rule2.add(t[1]);

			matrixBuilders[2].add(t[2]);
			rule3.add(t[2]);

			if (line == scores.length) {
				scores = resize(scores);
			}

			scores[line++] = (short) Integer.parseInt(t[3]);
		}

		// Compile CostMatrixBuilders
		matrixBuilders[0].build();
		matrixBuilders[1].build();
		matrixBuilders[2].build();

		int size1 = matrixBuilders[0].size();
		int size2 = matrixBuilders[1].size();
		int size3 = matrixBuilders[2].size();
		int ruleSize = rule1.size();


		// Write connection cost data
		MappedByteBuffer buffer = null;
		ShortBuffer shortBuffer = null;
		int matrixSizeBytes = (size1 * size2 * size3 * 2);
		int headerSizeBytes = (3 * 2);

		RandomAccessFile file = new RandomAccessFile(connectionCostDataFilename, "rw");
		file.setLength(0);
		file.writeShort(size1);
		file.writeShort(size2);
		file.writeShort(size3);
		file.setLength(headerSizeBytes + matrixSizeBytes);
		FileChannel indexChannel = file.getChannel();
		buffer = indexChannel.map(FileChannel.MapMode.READ_WRITE, headerSizeBytes, matrixSizeBytes);
		shortBuffer = buffer.asShortBuffer();
		indexChannel.close();

		for (int i = 0; i < (size1 * size2 * size3); i++) {
			shortBuffer.put(i, defaultCost);
		}

		for (int i = 0; i < ruleSize; i++) {
			Vector<Integer> r1 = matrixBuilders[0].getRuleIdList(rule1.get(i));
			Vector<Integer> r2 = matrixBuilders[1].getRuleIdList(rule2.get(i));
			Vector<Integer> r3 = matrixBuilders[2].getRuleIdList(rule3.get(i));

			for (Iterator<Integer> i1 = r1.iterator(); i1.hasNext();) {
				int ii1 = i1.next();
				for (Iterator<Integer> i2 = r2.iterator(); i2.hasNext();) {
					int ii2 = i2.next();
					for (Iterator<Integer> i3 = r3.iterator(); i3.hasNext();) {
						int ii3 = i3.next();
						int position = size3 * (size2 * ii1 + ii2) + ii3;
						shortBuffer.put(position, scores[i]);
					}
				}
			}
		}

		buffer.force();

		return matrixBuilders;

	}


	/**
	 * Create the token data file
	 * 
	 * @param tokenDataFilename The filename for the token data file 
	 * @param standardCTokens The beginning-of-string, end-of-string, and unknown-morpheme CTokens  
	 * @param tupleList The (String,CToken) tuples
	 *
	 * @return The Trie precursor data
	 * @throws IOException 
	 */
	private TrieData createTokenFile(String tokenDataFilename, CToken[] standardCTokens, VirtualTupleList tupleList)
			throws IOException
	{

		TrieData trieData = new TrieData();
		
		trieData.values = new int[tupleList.size()];
		trieData.keys = new String[tupleList.size()];
		trieData.size = 0;
		int spos = 0;
		int bsize = 0;
		String prev = "";

		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tokenDataFilename)));

		// Write beginning-of-string, end-of-string, unknown-morpheme tokens
		CToken.write(out, standardCTokens[0]);
		CToken.write(out, standardCTokens[1]);
		CToken.write(out, standardCTokens[2]);

		// Write token data
		for (int i = 0; i < trieData.keys.length; i++) {
			StringCTokenTuple tuple = tupleList.get(i); 
			String k = tuple.key;
			if (!prev.equals(k) && i != 0) {
				trieData.keys[trieData.size] = tupleList.get(spos).key;
				trieData.values[trieData.size] = bsize + (spos << 8);
				trieData.size++;
				bsize = 1;
				spos = i;
			} else {
				bsize++;
			}
			prev = tuple.key;
			CToken.write(out, tuple.value);
		}
		out.flush();
		out.close();

		trieData.keys[trieData.size] = tupleList.get(spos).key;
		trieData.values[trieData.size] = bsize + (spos << 8);
		trieData.size++;


		return trieData;

	}


	/**
	 * Create Trie file
	 * 
	 * @param trieDataFilename The filename for the Trie file 
	 * @param trieData The Trie precursor data
	 * @throws IOException 
	 */
	private void createTrieFile(String trieDataFilename, TrieData trieData) throws IOException {

		TrieBuilder builder = new TrieBuilder(trieData.keys, trieData.values, trieData.size);
		builder.build(trieDataFilename);

	}


	/**
	 * Compiles CSV source data into the data files used for analysis
	 * 
	 * @param customDictionaryCSVFilenames The filenames of custom dictionaries, or <code>null</code>
	 * @throws IOException 
	 */
	public DictionaryBuilder(String[] customDictionaryCSVFilenames) throws IOException {

		List<String> dictionaryCSVFilenames = new ArrayList<String>();
		dictionaryCSVFilenames.add(DICTIONARY_CSV_FILENAME);
		dictionaryCSVFilenames.addAll(Arrays.asList(customDictionaryCSVFilenames));

		String charset = "UTF-8";


		// Create connection cost file (matrix.sen)
		CostMatrixBuilder[] matrixBuilders = createConnectionCostFile(
				CONNECTION_CSV_FILENAME,
				CONNECTION_COST_DATA_FILENAME,
				DEFAULT_CONNECTION_COST,
				charset
		);


		// Create part-of-speech data file (posInfo.sen)
		VirtualTupleList dictionaryList = new VirtualTupleList();
		CToken[] standardCTokens = new CToken[3];

		createPartOfSpeechDataFile(
				dictionaryCSVFilenames,
				PART_OF_SPEECH_DATA_FILENAME,
				matrixBuilders,
				PART_OF_SPEECH_START,
				PART_OF_SPEECH_SIZE,
				charset,
				BOS_PART_OF_SPEECH,
				EOS_PART_OF_SPEECH,
				UNKNOWN_PART_OF_SPEECH,
				dictionaryList,
				standardCTokens
		);

		// Free temporary object for GC
		matrixBuilders = null;


		// Create Token file (token.sen)
		TrieData trieData = createTokenFile(
				TOKEN_DATA_FILENAME,
				standardCTokens,
				dictionaryList
		);

		// Free temporary object for GC
		dictionaryList = null;


		// Create Trie file (da.sen)
		createTrieFile(TRIE_DATA_FILENAME, trieData);

	}


}
