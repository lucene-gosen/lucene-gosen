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

import org.apache.lucene.analysis.charfilter.BaseCharFilter;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * Removes punctuation from input stream
 */
public final class GosenPunctuationCharFilter extends BaseCharFilter {
    private final CharArraySet protectedSet;
    private final boolean paddingSpace;
    private Reader transformedInput;


    /**
     */
    public GosenPunctuationCharFilter(Reader in, CharArraySet protectedSet, boolean paddingSpace) {
        super(in);
        this.protectedSet = protectedSet;
        this.paddingSpace = paddingSpace;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        // Buffer all input on the first call.
        if (transformedInput == null) {
            fill();
        }

        return transformedInput.read(cbuf, off, len);
    }

    @Override
    public int read() throws IOException {
        if (transformedInput == null) {
            fill();
        }

        return transformedInput.read();
    }

    @Override
    protected int correct(int currentOff) {
        return Math.max(0, super.correct(currentOff));
    }

    /**
     */
    private void fill() throws IOException {
        StringBuilder buffered = new StringBuilder();
        char[] temp = new char[1024];
        for (int cnt = input.read(temp); cnt > 0; cnt = input.read(temp)) {
            buffered.append(temp, 0, cnt);
        }
        transformedInput = new StringReader(processPunctuation(buffered).toString());
    }
    
    static final boolean isPunctuation(char ch) {
        switch (Character.getType(ch)) {
            case Character.SPACE_SEPARATOR:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.CONTROL:
            case Character.FORMAT:
            case Character.DASH_PUNCTUATION:
            case Character.START_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.CONNECTOR_PUNCTUATION:
            case Character.OTHER_PUNCTUATION:
            case Character.MATH_SYMBOL:
            case Character.CURRENCY_SYMBOL:
            case Character.MODIFIER_SYMBOL:
            case Character.OTHER_SYMBOL:
            case Character.INITIAL_QUOTE_PUNCTUATION:
            case Character.FINAL_QUOTE_PUNCTUATION:
                return true;
            default:
                return false;
        }
    }

    /**
     */
    CharSequence processPunctuation(CharSequence input) {

        final StringBuffer cumulativeOutput = new StringBuffer();
        int originalLength = input.length();

        for (int i = 0; i < originalLength; i++) {
            char ch = input.charAt(i);

            if (isPunctuation(ch)) {
                if (protectedSet != null && !protectedSet.contains(ch)) {
                    if (!paddingSpace) {
                        continue;
                    } else {
                        ch = '\u0020';
                    }
                } else if (protectedSet == null || protectedSet.size() <= 0) {
                    if (!paddingSpace) {
                        continue;
                    } else {
                        ch = '\u0020';
                    }
                }
            }
            
            cumulativeOutput.append(ch);
        }

        int diffOffset = cumulativeOutput.length() - originalLength;
        addOffCorrectMap(originalLength, diffOffset);

        return cumulativeOutput;
    }

}
