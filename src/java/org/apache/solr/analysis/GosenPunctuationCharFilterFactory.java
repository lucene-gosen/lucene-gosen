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

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.apache.lucene.analysis.CharFilter;
import org.apache.lucene.analysis.gosen.GosenPunctuationCharFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.CharFilterFactory;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Factory for {@link org.apache.lucene.analysis.gosen.GosenPunctuationCharFilter}.
 * <pre class="prettyprint" >
 * &lt;fieldType name="text_ja" class="solr.TextField"&gt;
 *   &lt;analyzer&gt;
 *
 *     &lt;charFilter class="org.apache.solr.analysis.gosen.GosenPunctuationCharFilterFactory"
 *                    protectedTokens="lang/ja/punctuation-protected.txt"
 *                    paddingSpace="false"/&gt;
 *   
 *     &lt;tokenizer class="solr.GosenTokenizerFactory"/&gt;
 *
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;</pre>
 */
public class GosenPunctuationCharFilterFactory extends CharFilterFactory implements ResourceLoaderAware {

    private final String protectedFile;
    private final boolean paddingSpace;
    private CharArraySet protectedSet;

    /**
     */
    public GosenPunctuationCharFilterFactory(Map<String, String> args) {
        super(args);

        this.protectedFile = get(args, "protectedTokens");
        this.paddingSpace = getBoolean(args, "paddingSpace", false);

        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    @Override
    public CharFilter create(Reader reader) {
        return new GosenPunctuationCharFilter(reader, this.protectedSet, this.paddingSpace);
    }

    @Override
    public void inform(ResourceLoader resourceLoader) throws IOException {
        if (protectedFile != null) {
            loadProtectedTokens(resourceLoader);
        }
    }

    public void loadProtectedTokens(ResourceLoader loader) throws IOException {
        CharArraySet casTriggerWords = getWordSet(loader, protectedFile, false);
        protectedSet = new CharArraySet(casTriggerWords.size(), false);
        for (Object elem : casTriggerWords) {
            char chars[] = (char[]) elem;
            if (chars.length > 1 && (chars[0] == '\\' && chars[1] == '#')) {
                // Special treatment for '#' that is processed as a comment symbol in getWordSet method.
                protectedSet.add('#');
            } else {
                protectedSet.add(elem);
            }
        }
    }

    public void setProtectedTokens(CharArraySet protectedSet) {
        this.protectedSet = protectedSet;
    }

}
