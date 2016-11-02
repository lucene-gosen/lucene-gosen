/*
 * GosenCharacterNormalizeFilterFactory
 *
 * Factory for {@link ICUCharFilter}.
 *
 * &lt;fieldType name="text_norm" class="solr.TextField" positionIncrementGap="100"&gt;
 *   &lt;analyzer&gt;
 *     &lt;charFilter class="solr.GosenCharacterNormalizeFilterFactory"
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

package org.apache.solr.analysis;

import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.gosen.GosenCharacterNormalizeFilter;
import org.apache.lucene.analysis.util.CharFilterFactory;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;

import java.io.Reader;
import java.util.Map;

import static org.apache.lucene.analysis.gosen.GosenCharacterNormalizeFilter.DEFAULT_NORM_FORM;

/**
 * Factory class for ICUCharFilter
 */
public class GosenCharacterNormalizeFilterFactory extends CharFilterFactory {

  private final String strNormForm;
  private final Normalizer2.Mode normMode;

  public GosenCharacterNormalizeFilterFactory(Map<String, String> args) {
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
      throw new SolrException(ErrorCode.SERVER_ERROR, "Invalid normMode: " + strMode);
    }
  }

  @Override
  public Reader create(Reader reader) {
    return new GosenCharacterNormalizeFilter(reader, strNormForm, normMode);
  }
}
