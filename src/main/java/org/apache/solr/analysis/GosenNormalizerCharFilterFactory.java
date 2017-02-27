/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.analysis;

import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.gosen.GosenNormalizerCharFilter;
import org.apache.lucene.analysis.util.CharFilterFactory;

import java.io.Reader;
import java.util.Map;

import static org.apache.lucene.analysis.gosen.GosenNormalizerCharFilter.DEFAULT_NORM_FORM;

/**
 * GosenNormalizerCharFilterFactory
 *
 * Factory for {@link GosenNormalizerCharFilter}.
 *
 * &lt;fieldType name="text_norm" class="solr.TextField" positionIncrementGap="100"&gt;
 *   &lt;analyzer&gt;
 *     &lt;charFilter class="solr.GosenNormalizerCharFilterFactory"
 *                            name="nfkc" mode="compose"/&gt;
 *
 *     &lt;tokenizer class="solr.GosenTokenizerFactory"
 *                            compositePOS="compositePOS.txt" dictionaryDir="dictionary/naist-chasen"/&gt;
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;</pre>
 *
 * name is specified normalize algorithm: you can specify nfc, nfkc, nfkd.
 * mode is specified either compose or decompose, which specify how to treat the tokens.
 *
 */
/**
 * Factory class for ICUCharFilter
 */
public class GosenNormalizerCharFilterFactory extends CharFilterFactory {

  private final String strNormForm;
  private final Normalizer2.Mode normMode;

  public GosenNormalizerCharFilterFactory(Map<String, String> args) {
    super(args);

    String strName = args.get("name");
    String strMode = args.get("mode");

    if (strName == null) {
      strNormForm = DEFAULT_NORM_FORM;
    } else {
      strNormForm = strName;
    }

    if (strMode == null) {
      strMode = "compose";
    }
    if (strMode.equals("compose")) {
      normMode = Normalizer2.Mode.COMPOSE;
    } else if (strMode.equals("decompose")) {
      normMode = Normalizer2.Mode.DECOMPOSE;
    } else {
      throw new IllegalArgumentException("Invalid mode: " + strMode);
    }
  }

  @Override
  public Reader create(Reader reader) {
    return new GosenNormalizerCharFilter(reader, strNormForm, normMode);
  }
}
