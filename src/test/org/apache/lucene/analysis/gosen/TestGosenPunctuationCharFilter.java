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
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.solr.analysis.GosenTokenizerFactory;

public class TestGosenPunctuationCharFilter extends BaseTokenStreamTestCase {

    CharArraySet protectedTokenSet;
    GosenTokenizerFactory jatok_factory;
            
    @Override
    public void setUp() throws Exception {
        super.setUp();

        protectedTokenSet = new CharArraySet(asSet("+", "-", "#", "【", "】", "℃", "＄", "＃", " "), false);

        jatok_factory = new GosenTokenizerFactory(new HashMap<String, String>());
    }
    
    //------------------------------------------------------------------------------------------------------------

    /**
     *
     */
    public void testEmptyTerm() throws IOException {
        Reader reader = new StringReader("");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = new MockTokenizer(cs, MockTokenizer.WHITESPACE, false);
        assertTokenStreamContents(ts, new String[] {});
    }

    //------------------------------------------------------------------------------------------------------------

    public void testFilter01() throws IOException {
        Reader reader = new StringReader("aa bb 【cc】");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = new MockTokenizer(cs, MockTokenizer.WHITESPACE, false);
        assertTokenStreamContents(ts, new String[] {"aa", "bb", "【cc】"});
    }

    public void testFilter02() throws Exception {
        Reader reader = new StringReader("日本語・英語");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts,
                new String[]{"日本語", "英語"},
                new int[]{0, 3},
                new int[]{3, 5});
    }

    public void testFilter03() throws Exception {
        Reader reader = new StringReader("楽★天");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts,
                new String[]{"楽天"},
                new int[]{0},
                new int[]{2});
    }

    public void testFilter04() throws Exception {
        Reader reader = new StringReader("#0001");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts,
                new String[]{"#", "0001"},
                new int[]{0,1},
                new int[]{1,5},
                5);
    }

    public void testFilter05() throws Exception {
        Reader reader = new StringReader("(0001)");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts, new String[]{"0001"});
    }

    public void testFilter06() throws Exception {
        Reader reader = new StringReader("C++Guide");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts,
                new String[]{"C", "++", "Guide"},
                new int[]{0,1,3},
                new int[]{1,3,8});
    }

    public void testFilter07() throws Exception {
        Reader reader = new StringReader("C#");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts, new String[]{"C","#"});
    }

    public void testFilter08() throws Exception {
        Reader reader = new StringReader("【Marketing-Keyword】");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts,
                new String[]{"【", "Marketing", "-", "Keyword", "】"},
                new int[]{0,1,10,11,18},
                new int[]{1,10,11,18,19});
    }

    public void testFilter09() throws Exception {
        Reader reader = new StringReader("【Marketing=Keyword】");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts, new String[]{"【", "MarketingKeyword", "】"});
    }

    public void testFilter10() throws Exception {
        Reader reader = new StringReader("$00&&||01");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts,
                new String[]{"0001"},
                new int[]{0},
                new int[]{4});
    }

    public void testFilter11() throws Exception {
        Reader reader = new StringReader("C**Guide");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts,
                new String[]{"CGuide"},
                new int[]{0},
                new int[]{6});
    }

    public void testFilter12() throws Exception {
        Reader reader = new StringReader("[Marketing=Keyword]");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts,
                new String[]{"MarketingKeyword"},
                new int[]{0},
                new int[]{16});
    }

    public void testFilter13() throws Exception {
        Reader reader = new StringReader("4℃"); //=> /4/℃/

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, false);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts,
                new String[]{"4", "℃"});
    }

    //------------------------------------------------------------------------------------------------------------

    public void testFilter14() throws Exception {
        Reader reader = new StringReader("日本語・英語");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, true);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts,
                new String[]{"日本語", "英語"},
                new int[]{0,4},
                new int[]{3,6});
    }

    public void testFilter15() throws Exception {
        Reader reader = new StringReader("$00&&||01");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, true);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts,
                new String[]{"00", "01"},
                new int[]{1,7},
                new int[]{3,9});
    }

    public void testFilter16() throws Exception {
        Reader reader = new StringReader("楽★天");

        CharFilter cs = new GosenPunctuationCharFilter(reader, protectedTokenSet, true);
        TokenStream ts = jatok_factory.create(cs);
        assertTokenStreamContents(ts,
                new String[]{"楽", "天"},
                new int[]{0,2},
                new int[]{1,3});
    }
}
