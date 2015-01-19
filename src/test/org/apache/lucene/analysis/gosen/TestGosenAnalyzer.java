package org.apache.lucene.analysis.gosen;

/**
 * Copyright 2004 The Apache Software Foundation
 *
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

import java.io.IOException;

import net.java.sen.SenTestUtil;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.gosen.GosenAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * Simple tests for {@link GosenAnalyzer} 
 */
public class TestGosenAnalyzer extends BaseTokenStreamTestCase {
  /** This test fails with NPE when the 
   * stopwords file is missing in classpath */
  public void testResourcesAvailable() {
    new GosenAnalyzer(SenTestUtil.IPADIC_DIR);
  }
  
  /**
   * An example sentence, test removal of particles, etc by POS,
   * lemmatization with the basic form, and that position increments
   * and offsets are correct.
   */
  public void testBasics() throws IOException {
    assertAnalyzesTo(new GosenAnalyzer(SenTestUtil.IPADIC_DIR), "多くの学生が試験に落ちた。",
      new String[] { "多く", "学生", "試験", "落ちる" },
      new int[] { 0, 3, 6,  9 },
      new int[] { 2, 5, 8, 11 },
      new int[] { 1, 2, 2,  2 }
    );
  }


  public void testSpecialSymbol() throws IOException {
    CharArraySet set = new CharArraySet(asSet("+", "-", "\\#", "【", "】", "℃"), false);
    assertAnalyzesTo(new GosenAnalyzer(SenTestUtil.IPADIC_DIR, set), "4℃",
            new String[] { "4", "℃" }
    );
  }

  /**
   * Analyzes random unicode strings, to ensure no exception
   * (results could be completely bogus, but makes sure we don't crash on some input)
   */
  public void testReliability() throws IOException {
    Analyzer analyzer = new GosenAnalyzer(SenTestUtil.IPADIC_DIR);
    checkRandomData(random(), analyzer, 10000*RANDOM_MULTIPLIER);
  }
}
