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
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.java.sen.util.IOUtils;

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
public class VirtualTupleList implements Closeable {
  
  /**
   * A RandomAccessFile used to create the memory mapped buffer during sorting
   */
  private RandomAccessFile file = null;
  
  private BufferedOutputStream bos = null; 
  
  private FileOutputStream fos = null;
  
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
  private final Comparator<Integer> comparator = new VirtualListComparator();
  
  /**
   * A Comparator class for indices within the list
   */
  private class VirtualListComparator implements Comparator<Integer> {
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
    int position = outputStream.size();
    
    CToken.write(outputStream, ctoken);
    outputStream.writeShort(string.length());
    outputStream.writeChars(string);
    
    indices.add(position);
  }
  
  /**
   * Retrieves an entry from the list. Only valid after the list has been
   * sorted
   *
   * @param index The index of the entry to retrieve
   * @return The list entry
   */
  public StringCTokenTuple get(int index) {
    int position = indices.get(index);
    
    mappedBuffer.position(position);
    CToken ctoken = new CToken();
    ctoken.read(mappedBuffer);
    short numChars = mappedBuffer.getShort();
    char stringChars[] = new char[numChars];
    for (int i = 0; i < numChars; i++) {
      stringChars[i] = mappedBuffer.getChar();
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
    mappedBuffer.position((int) (position + CToken.SIZE));
    short numChars = mappedBuffer.getShort();
    char stringChars[] = new char[numChars];
    for (int i = 0; i < numChars; i++) {
      stringChars[i] = mappedBuffer.getChar();
    }
    
    return new String(stringChars);
  }
  
  /**
   * Sorts the list
   * 
   * @throws IOException 
   */
  public void sort() throws IOException {
    outputStream.flush();
    mappedBuffer = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, file.length());
    Collections.sort(indices, comparator);
  }
  
  /**
   * Returns the number of entries in the list
   *
   * @return The number of entries in the list
   */
  public int size() {
    return indices.size();
  }
  
  /**
   * @throws IOException 
   */
  public VirtualTupleList() throws IOException {
    File tempFile;
    
    tempFile = Files.createTempFile("_tok", null).toFile();
    tempFile.deleteOnExit();
    this.file = new RandomAccessFile(tempFile, "rw");
    this.fos = new FileOutputStream(tempFile);
    this.bos = new BufferedOutputStream(fos);
    this.outputStream = new DataOutputStream(bos);
  }
  
  public void close() throws IOException {
    IOUtils.close(file, fos, bos, outputStream);
  }
}
