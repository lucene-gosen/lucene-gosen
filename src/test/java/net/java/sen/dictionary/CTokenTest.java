/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.java.sen.dictionary;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link CToken} binary serialization and cloning.
 *
 * <p>{@code CToken.write()} is called only from the dictionary compiler
 * (excluded from JaCoCo), so it requires a direct test to achieve coverage.
 */
public class CTokenTest {

  private CToken makeToken(short rcAttr2, short rcAttr1, short lcAttr,
                           short length, short cost, int posIndex) {
    CToken t = new CToken();
    t.rcAttr2 = rcAttr2;
    t.rcAttr1 = rcAttr1;
    t.lcAttr = lcAttr;
    t.length = length;
    t.cost = cost;
    t.partOfSpeechIndex = posIndex;
    return t;
  }

  @Test
  public void testWriteReadRoundTrip() throws IOException {
    CToken original = makeToken((short) 10, (short) 20, (short) 30,
                                (short) 4, (short) 1234, 999);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    CToken.write(new DataOutputStream(baos), original);
    assertEquals("write should emit exactly CToken.SIZE bytes", CToken.SIZE, baos.size());

    CToken read = new CToken();
    read.read(ByteBuffer.wrap(baos.toByteArray()));

    assertEquals(original.rcAttr2, read.rcAttr2);
    assertEquals(original.rcAttr1, read.rcAttr1);
    assertEquals(original.lcAttr, read.lcAttr);
    assertEquals(original.length, read.length);
    assertEquals(original.cost, read.cost);
    assertEquals(original.partOfSpeechIndex, read.partOfSpeechIndex);
    assertFalse("read() must set terminator=false", read.terminator);
  }

  @Test
  public void testReadSetsTerminatorFalse() throws IOException {
    CToken t = makeToken((short) 1, (short) 2, (short) 3, (short) 1, (short) 0, 0);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    CToken.write(new DataOutputStream(baos), t);

    CToken read = new CToken();
    read.terminator = true;
    read.read(ByteBuffer.wrap(baos.toByteArray()));
    assertFalse(read.terminator);
  }

  @Test
  public void testCloneIsIndependent() {
    CToken original = makeToken((short) 5, (short) 6, (short) 7,
                                (short) 2, (short) 100, 42);
    CToken copy = original.clone();

    assertNotSame(original, copy);
    assertEquals(original.rcAttr2, copy.rcAttr2);
    assertEquals(original.rcAttr1, copy.rcAttr1);
    assertEquals(original.lcAttr, copy.lcAttr);
    assertEquals(original.length, copy.length);
    assertEquals(original.cost, copy.cost);
    assertEquals(original.partOfSpeechIndex, copy.partOfSpeechIndex);

    // Mutating the copy must not affect the original
    copy.cost = (short) 999;
    assertEquals((short) 100, original.cost);
  }

  @Test
  public void testNegativeCostRoundTrip() throws IOException {
    CToken t = makeToken((short) 0, (short) 0, (short) 0, (short) 1, (short) -500, 0);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    CToken.write(new DataOutputStream(baos), t);

    CToken read = new CToken();
    read.read(ByteBuffer.wrap(baos.toByteArray()));
    assertEquals((short) -500, read.cost);
  }
}
