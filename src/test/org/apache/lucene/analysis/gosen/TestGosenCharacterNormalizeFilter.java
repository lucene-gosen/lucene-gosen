/**
 * Unit tests for GosenCharacterNormalizeFilter
 */
package org.apache.lucene.analysis.gosen;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;


public class TestGosenCharacterNormalizeFilter extends BaseTokenStreamTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  private void checkToken(String input, String expected) throws IOException {
    Reader reader = new GosenCharacterNormalizeFilter(new StringReader(input));
    Tokenizer tokenizer = new MockTokenizer(MockTokenizer.KEYWORD, false);
    tokenizer.setReader(reader);
    assertTokenStreamContents(tokenizer, new String[]{expected});
  }

  // Nothing to change - Just make sure normalizer is working.
  @Test
  public void testNothingChange() throws Exception {
    checkToken("x", "x");
  }

  // Normalize Full-width Latin to Half-width Latin
  @Test
  public void test01() throws Exception {
    checkToken("ＡＢＣＤ", "ABCD");
  }

  // decompose EAcute into E + combining Acute
  @Test
  public void test02() throws Exception {
    checkToken("\u0065\u0301", "\u00E9");
  }

  // Convert Full-width Latin to Half-width Latin
  @Test
  public void test03() throws IOException {
    checkToken("Ｘ", "X");
  }

  // Normalize Half-width Katanaka to Full-width Katakana
  @Test
  public void test04() throws IOException {
    checkToken("ﾆﾎﾝｺﾞﾄｴｲｺﾞ", "ニホンゴトエイゴ");
  }

  // Normalize Fulld-width Latin to Hlaf-width Latin
  @Test
  public void test05() throws IOException {
    checkToken("Ｃ＋＋", "C++");
  }

  // normalization
  @Test
  public void test06() throws Exception {
    checkToken("ﴳﴺﰧ", "طمطمطم");
  }

  // normalization
  @Test
  public void test07() throws Exception {
    checkToken("が", "が");
  }

  //U+3099: Combining KATAKANA-HIRAGANA Voiced Sound Mark
  @Test
  public void test08() throws Exception {
    checkToken("か" + '\u3099', "が");
  }

  //U+309B: KATAKANA-HIRAGANA Voiced Sound Mark
  //Standard ICU doesn't support this combination.
  @Test
  public void test09() throws Exception {
    checkToken("か" + '\u309B', "が");
  }

  // the filter doesn't remove 'space'
  @Test
  public void test10() throws Exception {
    checkToken(" ", " ");
  }

  // the filter normalizes full-width space to half-width space
  @Test
  public void test11() throws Exception {
    checkToken("　Ａ", " A");
  }

  @Test
  public void test12() throws Exception {
    checkToken("℃", "°C"); // \u2103 => \u00B0 + \u0043
  }

  @Test
  public void test13() throws Exception {
    checkToken("´", " ́");  // \u00B4 => \u0301
  }
}
