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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.java.sen.dictionary.CToken;


/**
 * A file-mapped list of {@link StringCTokenTuple <code>StringCTokenTuple</code>}s.
 * Slightly slower than a simple in-memory sort, but capable of storing and
 * sorting very long lists without using large quantities of heap memory.<br>
 * The index of entry positions in the list's file is stored in memory, leading
 * to a usage of one Integer's worth of memory for each entry.
 * 
 * <p> Usage:
 * <p>  - Call {@link #add} one or more times
 * <p>  - Call {@link #sort} once. Once the list has been sorted, it is no longer
 *          valid to add new entries
 * <p>  - Call {@link #get} to retrieve entries from the sorted list
 */
public class VirtualTupleList {

	/**
	 * A RandomAccessFile used to create the memory mapped buffer during sorting
	 */
	private RandomAccessFile file;

	/**
	 * A memory mapped buffer used to retrieve list entries. Created when the
	 * buffer is sorted
	 */
	private MappedByteBuffer mappedBuffer = null;

	/**
	 * An OutputStream to the temporary file used to store entries in the list
	 */
	private DataOutputStream outputStream;

	/**
	 * An index of entry positions within the temporary file
	 */
	private List<Integer> indices = new ArrayList<Integer>();

	/**
	 * A Comparator for indices within the list
	 */
	private Comparator<Integer> comparator = new VirtualListComparator();


	/**
	 * A Comparator class for indices within the list
	 */
	private class VirtualListComparator implements Comparator<Integer> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Integer o1, Integer o2) {

			String first = getString(o1);
			String second = getString(o2);
			return first.compareTo(second);

		}
		
	}


	/**
	 * Adds a StringCTokenTuple to the list. Passed in as the Tuple's
	 * constituent parts to avoid creating an object that we immediately
	 * serialise and throw away
	 *
	 * @param string The string
	 * @param ctoken The CToken
	 * @throws IOException 
	 */
	public void add(String string, CToken ctoken) throws IOException {

		int position = this.outputStream.size();

		CToken.write(this.outputStream, ctoken);
		this.outputStream.writeShort(string.length());
		this.outputStream.writeChars(string);

		this.indices.add(position);
			
	}


	/**
	 * Retrieves an entry from the list. Only valid after the list has been
	 * sorted
	 *
	 * @param index The index of the entry to retrieve
	 * @return The list entry
	 */
	public StringCTokenTuple get(int index) {

		int position = this.indices.get(index);

		this.mappedBuffer.position(position);
		CToken ctoken = CToken.read(this.mappedBuffer);
		short numChars = this.mappedBuffer.getShort();
		char stringChars[] = new char[numChars];
		for (int i = 0; i < numChars; i++) {
			stringChars[i] = this.mappedBuffer.getChar();
		}
		String string = new String(stringChars);

		return new StringCTokenTuple(string, ctoken);

	}


	/**
	 * Retrieves only the String portion of a list entry. Used in sorting
	 * (where the CToken is not relevant)
	 *
	 * @param position The file position of the list entry
	 * @return The entry's String component
	 */
	private String getString(int position) {

		this.mappedBuffer.position((int) (position + CToken.SIZE));
		short numChars = this.mappedBuffer.getShort();
		char stringChars[] = new char[numChars];
		for (int i = 0; i < numChars; i++) {
			stringChars[i] = this.mappedBuffer.getChar();
		}

		return new String(stringChars);

	}

	/**
	 * Sorts the list
	 * 
	 * @throws IOException 
	 */
	public void sort() throws IOException {

		this.outputStream.flush();
		this.outputStream.close();
		this.mappedBuffer = this.file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, this.file.length());
		Collections.sort(this.indices, this.comparator);

	}


	/**
	 * Returns the number of entries in the list
	 *
	 * @return The number of entries in the list
	 */
	public int size() {

		return this.indices.size();

	}

	/**
	 * @throws IOException 
	 */
	public VirtualTupleList() throws IOException {

		File tempFile;

		tempFile = File.createTempFile("_tok", null);
		tempFile.deleteOnExit();
		this.file = new RandomAccessFile(tempFile, "rw");
		this.outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));

	}

}
