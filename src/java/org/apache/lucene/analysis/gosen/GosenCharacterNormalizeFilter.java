/*
 * GosenCharacterNormalizeFilter
 *
 * A CharacterFilter that uses ICU4J to apply NFKC, NFKD and other character normalization that is controlled by
 * a given parameter.
 */

package org.apache.lucene.analysis.gosen;

import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.charfilter.BaseCharFilter;
import org.apache.lucene.analysis.util.RollingCharBuffer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;


public class GosenCharacterNormalizeFilter extends BaseCharFilter {

  private static final int VOICED_SOUND_MARK_CONVERT_OFFSET = 2;
  private static final int MAX_BUFFER_SIZE = 512;
  private static char[] specialCaseChars = new char[10];

  static {
    specialCaseChars[0] = '\u309B'; // Katakana-Hiragana Voiced Sound Mark
    specialCaseChars[1] = '\u309C'; // Katakana-Hiragana Semi-Voiced Sound Mark
  }

  public static String DEFAULT_NORM_FORM = "nfkc";

  private final Normalizer2 normalizer;
  private final RollingCharBuffer buffer = new RollingCharBuffer();
  private Reader normalizedInput;

  /**
   * Constructor that takes {@link Reader}
   *
   * @param reader
   */
  public GosenCharacterNormalizeFilter(Reader reader) {
    this(reader, DEFAULT_NORM_FORM, Normalizer2.Mode.COMPOSE);
  }

  /**
   * Constructor that takes {@link Reader} and {@link Normalizer2}.
   *
   * @param reader
   * @param name
   * @param mode
   */
  public GosenCharacterNormalizeFilter(Reader reader, String name, Normalizer2.Mode mode) {
    super(reader);
    this.normalizer = Normalizer2.getInstance(null, name, mode);
    buffer.reset(input);
  }

  private static char[] replaceSpecialCharacters(char[] inBuffer, int len) {
    //TODO: Prevent NFKC normalization if the input is Degree Celsius and Fahrenheit
    char[] outBuffer = new char[MAX_BUFFER_SIZE];
    for (int i = 0; i < len; i++) {
      char c = inBuffer[i];
      if (c == specialCaseChars[0] || c == specialCaseChars[1]) {
        // Replace the old-style voiced sound mark with new one
        outBuffer[i] = (char) (c - VOICED_SOUND_MARK_CONVERT_OFFSET);
      } else {
        outBuffer[i] = inBuffer[i];
      }
    }
    return outBuffer;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    // Buffer all input on the first call.
    if (normalizedInput == null) {
      StringBuilder sbBuffer = new StringBuilder();
      char[] temp = new char[MAX_BUFFER_SIZE];
      for (int cnt = input.read(temp); cnt > 0; cnt = input.read(temp)) {
        sbBuffer.append(replaceSpecialCharacters(temp, cnt), 0, cnt);
      }
      normalizedInput = new StringReader(normalizer.normalize(sbBuffer.toString()));
    }
    return normalizedInput.read(cbuf, off, len);
  }

  @Override
  protected int correct(int correctOffset) {
    return (Math.max(0, super.correct(correctOffset)));
  }
}
