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

package net.java.sen.dictionary;

import java.util.Objects;

/**
 * A class representing a reading applied to a set of characters within a
 * sentence
 */
public class Reading {
  
  /** The starting point within the sentence */
  public final int start;
  
  /** The number of characters of the sentence covered by the reading */
  public final int length;
  
  /** The reading text applied to the covered span */
  public final String text;
  
  @Override
  public boolean equals(Object object) {
    if (object instanceof Reading) {
      Reading other = (Reading) object;
      return (start == other.start && length == other.length && text.equals(other.text));
    }
    
    return false;
  }
  
  @Override
  public String toString() {
    return "Reading:{" + start + ":" + length + ":" + text + "}";
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, length, text);
  }

  /**
   * @param start The starting point within the sentence
   * @param length The number of characters of the sentence covered by the
   *               reading
   * @param text The reading text applied to the covered span
   */
  public Reading(int start, int length, String text) {
    this.start = start;
    this.length = length;
    this.text = text;
  }
}
