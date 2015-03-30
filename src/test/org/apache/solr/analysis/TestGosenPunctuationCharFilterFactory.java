package org.apache.solr.analysis;

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

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.CharFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArraySet;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class TestGosenPunctuationCharFilterFactory extends BaseTokenStreamTestCase {

    CharArraySet protectedTokenSet;
    GosenPunctuationCharFilterFactory filterFactory;
    GosenTokenizerFactory tokenizerFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        protectedTokenSet = new CharArraySet(asSet("+", "-", "#", "【", "】", "℃", "＄", "＃", " "), false);
        Map<String, String> filterArgs = new HashMap<String, String>();
        filterArgs.put("protectedTokens", "lang/ja/punctuation-protected.txt");
        filterArgs.put("paddingSpace", "true");

        filterFactory = new GosenPunctuationCharFilterFactory(filterArgs);
        filterFactory.setProtectedTokens(protectedTokenSet);

        tokenizerFactory = new GosenTokenizerFactory(new HashMap<String, String>());
    }

    //------------------------------------------------------------------------------------------------------------

    /** Test that bogus arguments result in exception */
    public void testBogusArguments() throws Exception {
        try {
            new GosenPunctuationCharFilterFactory(new HashMap<String,String>() {{
                put("bogusArg", "bogusValue");
            }});
            fail();
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Unknown parameters"));
        }
    }

    //------------------------------------------------------------------------------------------------------------

    public void testExecuter(final String in, final String[] out) throws IOException {

        CharFilter filter = filterFactory.create(new StringReader(in));
        TokenStream ts = tokenizerFactory.create(filter);

        assertTokenStreamContents(ts, out);
    }

    //------------------------------------------------------------------------------------------------------------

    public void testPunctuationFilterFactory01() throws IOException {
        testExecuter("C++", new String[]{"C", "++"});
    }

    public void testPunctuationFilterFactory02() throws IOException {
        testExecuter("C#", new String[]{"C", "#"});
    }

    public void testPunctuationFilterFactory03() throws IOException {
        testExecuter("C&", new String[]{"C"});
    }

    public void testPunctuationFilterFactory04() throws IOException {
        testExecuter("あいだ★みつを", new String[]{"あいだ", "みつを"});
    }

    public void testPunctuationFilterFactory05() throws IOException {
        testExecuter("【PHC-1000-11】", new String[]{"【", "PHC", "-", "1000", "-", "11", "】"});
    }

    public void testPunctuationFilterFactory06() throws IOException {
        testExecuter("PA++++", new String[]{"PA", "++++"});
    }

    public void testPunctuationFilterFactory07() throws IOException {
        testExecuter("-1000", new String[]{"-", "1000"});
    }

    public void testPunctuationFilterFactory08() throws IOException {
        testExecuter("1/2", new String[]{"1", "2"});
    }

    public void testPunctuationFilterFactory09() throws IOException {
        testExecuter("[PHC-1000-11]", new String[]{"PHC", "-", "1000", "-", "11"});
    }

    public void testPunctuationFilterFactory10() throws IOException {
        testExecuter("PHC_1000_11", new String[]{"PHC", "1000", "11"});
    }

}
