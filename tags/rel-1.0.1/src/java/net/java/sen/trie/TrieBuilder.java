/*
 * Copyright (C) 2004-2007
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

package net.java.sen.trie;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.BitSet;
import java.util.Vector;


/**
 * Constructs a Trie from the supplied sorted key and value arrays
 */
public class TrieBuilder {

	/**
	 * The file the Trie data is written to
	 */
	private RandomAccessFile trieFile;

	/**
	 * An expanding MappedByteBuffer used to write the Trie data without holding
	 * the entire Trie in heap memory
	 */
	private MappedByteBuffer byteBuffer = null;

	/**
	 * The Trie MappedByteBuffer represented as an IntBuffer
	 */
	private IntBuffer trieDataBuffer = null;

	/**
	 * A bit field used to track the occupied regions of the Trie data file
	 */
	private BitSet used = new BitSet();

	/**
	 * The next starting position to use in adding to the Trie
	 */
	private int nextCheckPosition = 0;

	/**
	 * The keys comprising the Trie
	 */
	private String keys[];

	/**
	 * The values for each key
	 */
	private int values[];

	/**
	 * The actual number of entries in the keys/values arrays
	 */
	private int size;


	/**
	 * A Trie node used in constructing the Trie data file
	 */
	private class TrieNode {

		/**
		 * The character at this TrieNode, as an int
		 */
		int code;

		/**
		 * The depth of the TrieNode
		 */
		int depth;

		/**
		 * The left extent of the TrieNode within the key array
		 */
		int left;

		/**
		 * The right extent of the TrieNode within the key array
		 */
		int right;

		/**
		 * Constructs a TrieNode with the given parameters
		 * 
		 * @param code The character at this TrieNode, as an int
		 * @param depth The depth of the TrieNode
		 * @param left The left extent of the TrieNode within the key array
		 * @param right The right extent of the TrieNode within the key array
		 */
		public TrieNode(int code, int depth, int left, int right) {

			this.code = code;
			this.depth = depth;
			this.left = left;
			this.right = right;

		}

	};


	/**
	 * Increases the size of the Trie data file
	 * 
	 * @param newSize The new size of the file
	 * @throws IOException 
	 */
	private void resize(int newSize) throws IOException {

		if (this.byteBuffer != null) {
			this.byteBuffer.force();
		}

		this.trieFile.setLength(newSize * 8);
		FileChannel indexChannel = this.trieFile.getChannel();
		this.byteBuffer = indexChannel.map(FileChannel.MapMode.READ_WRITE, 0, newSize * 8);
		this.trieDataBuffer = this.byteBuffer.asIntBuffer();

	}


	/**
	 * Builds a vector containing the children of the given node
	 * 
	 * @param parent The parent TrieNode
	 * @return The vector of child TrieNodes
	 */
	private Vector<TrieNode> fetch(TrieNode parent) {

		int prev = 0;
		Vector<TrieNode> siblings = new Vector<TrieNode>();

		for (int i = parent.left; i < parent.right; i++) {

			if (this.keys[i].length() < parent.depth) {
				continue;
			}

			String tmp = this.keys[i];

			int cur = 0;
			if (this.keys[i].length() != parent.depth) {
				cur = tmp.charAt(parent.depth) + 1;
			}

			if (prev > cur) {
				throw new RuntimeException("Fatal: Keys are not sorted");
			}

			if (cur != prev || siblings.size() == 0) {
				TrieNode tempNode = new TrieNode(cur, parent.depth + 1, i, 0);
				if (siblings.size() != 0) {
					TrieNode lastSibling = siblings.lastElement();
					lastSibling.right = i;
				}

				siblings.add(tempNode);
			}

			prev = cur;

		}

		if (siblings.size() != 0) {
			TrieNode lastSibling = siblings.lastElement();
			lastSibling.right = parent.right;
		}

		return siblings;

	}


	/**
	 * Find a position with the Trie data file where the given vector of TrieNodes
	 * may be written, resizing the data file if necessary
	 *
	 * @param siblings The TrieNodes to find a position for
	 * @return The position to write to
	 * @throws IOException 
	 */
	private int findInsertionPoint(Vector<TrieNode> siblings) throws IOException {

		int begin = 0;
		int nonZeroNum = 0;
		int first = 0;
		int position;

		if ((siblings.get(0).code + 1) > (this.nextCheckPosition)) {
			position = siblings.get(0).code;
		} else {
			position = this.nextCheckPosition - 1;
		}


		while (true) {

			position++;
			if (position > (this.trieDataBuffer.limit() >> 1)) {
				resize((int)(position * 1.05));
			}

			if (this.trieDataBuffer.get((position << 1) + 1) != 0) {
				nonZeroNum++;
				continue;
			} else if (first == 0) {
				this.nextCheckPosition = position;
				first = 1;
			}

			begin = position - siblings.get(0).code;

			int t = begin + siblings.get(siblings.size() - 1).code;
			if (t > (this.trieDataBuffer.limit() >> 1)) {
				resize((int) (t * 1.05));
			}

			if (this.used.get(begin) == true) {
				continue;
			}

			boolean flag = false;

			for (int i = 1; i < siblings.size(); i++) {
				if (this.trieDataBuffer.get(((begin + siblings.get(i).code) << 1) + 1) != 0) {
					flag = true;
					break;
				}
			}

			if (!flag) {
				break;
			}

		}


		if (1.0 * nonZeroNum / (position - this.nextCheckPosition + 1) >= 0.95) {
			this.nextCheckPosition = position;
		}

		this.used.set(begin);
		return begin;

	}


	/**
	 * Inserts a vector of TrieNodes into the Trie data file
	 * 
	 * @param siblings The vector of nodes to insert
	 * @return The position at which the nodes were inserted
	 * @throws IOException 
	 */
	private int insert(Vector<TrieNode> siblings) throws IOException {

		int begin = findInsertionPoint(siblings);

		
		for (int i = 0; i < siblings.size(); i++) {
			this.trieDataBuffer.put(((begin + siblings.get(i).code) << 1) + 1, begin);
		}

		for (int i = 0; i < siblings.size(); i++) {

			int position = (begin + siblings.get(i).code) << 1;
			int value;

			Vector<TrieNode> newSiblings = fetch(siblings.get(i));
			if (newSiblings.size() == 0) {

				if (this.values == null) {

					value = (-siblings.get(i).left - 1);

				} else {

					value = -this.values[siblings.get(i).left] - 1;

					if (value >= 0) {
						throw new RuntimeException("Fatal: Negative value assigned");
					}

				}

			} else {

				value = insert(newSiblings);

			}

			this.trieDataBuffer.put(position,value);
		}

		return begin;

	}


	/**
	 * Builds The trie data file
	 * 
	 * @param filename The filename for the Trie data file 
	 * @throws IOException 
	 */
	public void build(String filename) throws IOException {

		this.trieFile = new RandomAccessFile(filename, "rw");
		this.trieFile.setLength(0);

		resize(1024 * 10);
		this.trieDataBuffer.put(0, 1);

		TrieNode rootNode = new TrieNode(0, 0, 0, this.size);

		Vector<TrieNode> siblings = fetch(rootNode);
		insert(siblings);

		this.byteBuffer.force();
		this.trieFile.close();

	}


	/**
	 * Creates a TrieBuilder to build the given data
	 * 
	 * @param keys The sorted Trie keys
	 * @param values The values for each key
	 * @param size The actual number of entries in the key/value arrays
	 */
	public TrieBuilder(String keys[], int values[], int size) {

		this.keys = keys;
		this.values = values;
		this.size = size;

	}


}
