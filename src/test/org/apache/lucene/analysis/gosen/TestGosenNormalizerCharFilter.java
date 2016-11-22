/**
 * Unit tests for GosenNormalizerCharFilter
 */
package org.apache.lucene.analysis.gosen;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;


public class TestGosenNormalizerCharFilter extends BaseTokenStreamTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  private void checkToken(String input, String expected) throws IOException {
    Reader reader = new GosenNormalizerCharFilter(new StringReader(input));
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
  public void testNFKCNormLatin() throws Exception {
    checkToken("ＡＢＣＤ", "ABCD");
  }

  // decompose EAcute into E + combining Acute
  @Test
  public void testNFKCNormAcute() throws Exception {
    checkToken("\u0065\u0301", "\u00E9");
  }

  // Convert Full-width Latin to Half-width Latin
  @Test
  public void testNFKCNormLatinLetter() throws IOException {
    checkToken("Ｘ", "X");
  }

  // Normalize Half-width Katanaka to Full-width Katakana
  @Test
  public void testNFKCNormKatakana() throws IOException {
    checkToken("ﾆﾎﾝｺﾞﾄｴｲｺﾞ", "ニホンゴトエイゴ");
  }

  // Normalize Fulld-width Latin to Hlaf-width Latin
  @Test
  public void testNFKCLatinPunct() throws IOException {
    checkToken("Ｃ＋＋", "C++");
  }

  // normalization
  @Test
  public void testNFKCNormArabic() throws Exception {
    checkToken("ﴳﴺﰧ", "طمطمطم");
  }

  // normalization
  @Test
  public void testNFKCHiraganaLetter() throws Exception {
    checkToken("が", "が");
  }

  // U+3099: Combining KATAKANA-HIRAGANA Voiced Sound Mark
  @Test
  public void testNFKCComposeHiragana() throws Exception {
    checkToken("か" + '\u3099', "が");
  }

  // U+309B: KATAKANA-HIRAGANA Voiced Sound Mark
  // Standard ICU doesn't support this combination.
  @Test
  public void testNFKCComposeHiragaKatakanaMix() throws Exception {
    checkToken("か" + '\u309B', "が");
  }

  // The filter doesn't remove 'space'
  @Test
  public void testNFKCWhiteSpace() throws Exception {
    checkToken(" ", " ");
  }

  // The filter normalizes full-width space to half-width space
  @Test
  public void testLatinWithWS() throws Exception {
    checkToken("　Ａ", " A");
  }

  // Degree Celsius and Fahrenheit will be decomposed, which is different from
  // what we want to normalize and expected.
  @Test
  public void testUnexpectedBehavior() throws Exception {
    checkToken("℃", "°C"); // \u2103 => \u00B0 + \u0043
  }

  // This behavior might be tricky when someone intentionally use Acute Accent,
  // which will be normalized to Combined Acute Accent.
  @Test
  public void testTrickyNormalization() throws Exception {
    checkToken("´", " ́");  // \u00B4 => \u0301
  }
}
