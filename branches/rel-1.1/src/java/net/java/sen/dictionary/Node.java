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

package net.java.sen.dictionary;

/**
 * A node within the {@link Viterbi} cost lattice
 */
final public class Node implements Cloneable {
  
  /**
   * The <code>CToken</code> the <code>Dictionary</code> returned for the
   * <code>Morpheme</code> within this <code>Node</code>
   */
  public CToken ctoken;
  
  /**
   * The previous node on the best path through the <code>Node</code> lattice
   */
  public Node prev;
  
  /**
   * The next node on the best path through the <code>Node</code> lattice
   */
  public Node next;
  
  /**
   * The next <code>Node</code> returned for the same ending position
   * within the sentence by the <code>Dictionary</code>
   */
  public Node lnext;
  
  /**
   * The next <code>Node</code> returned for the same starting position
   * within the sentence by the <code>Dictionary</code>
   */
  public Node rnext;
  
  /**
   * The <code>Morpheme</code> that is contained within this <code>Node</code>
   */
  public Morpheme morpheme;
  
  /**
   * The index of the first character of this <code>Node</code> within the
   * surface
   */
  public int start;
  
  /**
   * The number of characters this <code>Node</code> covers
   */
  public int length;
  
  /**
   * The number of characters between the end of the previous <code>Node</code>
   * and the end of this one, including any ignored characters that do not
   * form part of the Morpheme
   */
  public int span;
  
  /**
   * The cost of the best path through this <code>Node</code>, comprising
   * <code>this.prev</code>, this <code>Node</code>, and
   * <code>this.next</code>. Lower cost is more likely, higher cost less
   * likely
   */
  public int cost; 
  
  @Override
  public Node clone() {
    // Nodes form a lattice, and must be copied shallowly
    try {
      return (Node) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e); // Can't happen
    }
  }
}
