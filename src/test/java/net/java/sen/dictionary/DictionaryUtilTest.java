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
 * Unit tests for {@link DictionaryUtil} encoding helpers.
 *
 * <p>{@code readVInt}/{@code writeVInt} use a variable-length encoding where
 * each byte holds 7 payload bits; the high bit signals that more bytes follow.
 * The thresholds below exercise each branch of the unwound loop in
 * {@code readVInt}.
 */
public class DictionaryUtilTest {

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------

  private int vIntRoundTrip(int value) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DictionaryUtil.writeVInt(new DataOutputStream(baos), value);
    return DictionaryUtil.readVInt(ByteBuffer.wrap(baos.toByteArray()));
  }

  private int vIntEncodedLength(int value) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DictionaryUtil.writeVInt(new DataOutputStream(baos), value);
    return baos.size();
  }

  // -----------------------------------------------------------------------
  // readVInt / writeVInt
  // -----------------------------------------------------------------------

  @Test
  public void testVIntOneByte() throws IOException {
    assertEquals(0, vIntRoundTrip(0));
    assertEquals(1, vIntRoundTrip(1));
    assertEquals(127, vIntRoundTrip(127));
    assertEquals(1, vIntEncodedLength(127));
  }

  @Test
  public void testVIntTwoBytes() throws IOException {
    assertEquals(128, vIntRoundTrip(128));
    assertEquals(16383, vIntRoundTrip(16383));
    assertEquals(2, vIntEncodedLength(128));
    assertEquals(2, vIntEncodedLength(16383));
  }

  @Test
  public void testVIntThreeBytes() throws IOException {
    assertEquals(16384, vIntRoundTrip(16384));
    assertEquals(2097151, vIntRoundTrip(2097151));
    assertEquals(3, vIntEncodedLength(16384));
    assertEquals(3, vIntEncodedLength(2097151));
  }

  @Test
  public void testVIntFourBytes() throws IOException {
    assertEquals(2097152, vIntRoundTrip(2097152));
    assertEquals(268435455, vIntRoundTrip(268435455));
    assertEquals(4, vIntEncodedLength(2097152));
    assertEquals(4, vIntEncodedLength(268435455));
  }

  @Test
  public void testVIntFiveBytes() throws IOException {
    assertEquals(268435456, vIntRoundTrip(268435456));
    assertEquals(Integer.MAX_VALUE, vIntRoundTrip(Integer.MAX_VALUE));
    assertEquals(5, vIntEncodedLength(268435456));
    assertEquals(5, vIntEncodedLength(Integer.MAX_VALUE));
  }

  // -----------------------------------------------------------------------
  // writeKatakana / readKatakana
  // -----------------------------------------------------------------------

  @Test
  public void testKatakanaRoundTrip() throws IOException {
    String katakana = "アイウエオカキクケコ";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DictionaryUtil.writeKatakana(new DataOutputStream(baos), katakana);

    char[] result = new char[katakana.length()];
    DictionaryUtil.readKatakana(ByteBuffer.wrap(baos.toByteArray()), result, 0, katakana.length());
    assertEquals(katakana, new String(result));
  }

  @Test
  public void testKatakanaRoundTripSingleChar() throws IOException {
    String katakana = "ン";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DictionaryUtil.writeKatakana(new DataOutputStream(baos), katakana);

    char[] result = new char[1];
    DictionaryUtil.readKatakana(ByteBuffer.wrap(baos.toByteArray()), result, 0, 1);
    assertEquals(katakana, new String(result));
  }

  @Test
  public void testKatakanaEncodingIsSingleByte() throws IOException {
    String katakana = "テスト";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DictionaryUtil.writeKatakana(new DataOutputStream(baos), katakana);
    assertEquals("each katakana char encodes to 1 byte", katakana.length(), baos.size());
  }

  // -----------------------------------------------------------------------
  // readString
  // -----------------------------------------------------------------------

  @Test
  public void testReadStringRoundTrip() throws IOException {
    String s = "テスト文字列";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    for (char c : s.toCharArray()) {
      dos.writeChar(c);
    }

    char[] result = new char[s.length()];
    DictionaryUtil.readString(ByteBuffer.wrap(baos.toByteArray()), result, 0, s.length());
    assertEquals(s, new String(result));
  }

  @Test
  public void testReadStringWithOffset() throws IOException {
    String s = "AB";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    for (char c : s.toCharArray()) {
      dos.writeChar(c);
    }

    char[] result = new char[4];
    result[0] = 'X';
    DictionaryUtil.readString(ByteBuffer.wrap(baos.toByteArray()), result, 1, 3);
    assertEquals('X', result[0]);
    assertEquals('A', result[1]);
    assertEquals('B', result[2]);
  }
}
