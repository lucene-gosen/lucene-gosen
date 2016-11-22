/**
 * Unit tests for GosenNormalizerCharFilterFactory
 */
package org.apache.solr.analysis;

import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.apache.lucene.analysis.BaseTokenStreamTestCase.assertTokenStreamContents;


public class TestGosenNormalizerCharFilterFactory extends LuceneTestCase {

  private void checkToken(Map<String, String> args, String input, String expected) throws IOException {
    GosenNormalizerCharFilterFactory factory = new GosenNormalizerCharFilterFactory(args);

    StringReader tmpInput = new StringReader(input);
    Reader reader = factory.create(tmpInput);
    Tokenizer tokenizer = new MockTokenizer(MockTokenizer.KEYWORD, false);
    tokenizer.setReader(reader);

    assertTokenStreamContents(tokenizer, new String[]{expected});
  }

  /**
   * Test nfkc normalization that normalize characters to half-width latin
   * from full-width latin.
   *
   * @throws Exception
   */
  @Test
  public void testDefaults() throws Exception {
    Map<String, String> args = new HashMap<String, String>();
    args.put("name", "nfkc");

    checkToken(args, "This is a Ｔｅｓｔ", "This is a Test");
  }

  /**
   * Test nfkc normalization that normalize characters to half-width Katakana
   * from full-width Katakana.
   *
   * @throws IOException
   */
  @Test
  public void testNFKCKatakana() throws IOException {
    Map<String, String> args = new HashMap<String, String>();
    args.put("name", "nfkc");

    checkToken(args, "ﾊﾝｶｸｶﾅ", "ハンカクカナ");
  }

  /**
   * Text nfkc_cf normalization, which is applied Unicode Case-Folding
   * Algorithm to NKC normalization.
   *
   * @throws IOException
   */
  @Test
  public void testNFKCAlphaNumeric() throws IOException {
    Map<String, String> args = new HashMap<String, String>();
    args.put("name", "nfkc");

    checkToken(args, "半角ABCabcと１００万円", "半角ABCabcと100万円");
  }

  @Test
  public void testNFKDKatakana() throws IOException {
    Map<String, String> args = new HashMap<String, String>();
    args.put("name", "nfkc");
    args.put("mode", "decompose");

    //ボス
    checkToken(args, "\u30DC\u30B9", "\u30DB\u3099\u30B9");
  }

  @Test
  public void testNFKDHiragana() throws IOException {
    Map<String, String> args = new HashMap<String, String>();
    args.put("name", "nfkc");
    args.put("mode", "decompose");

    //ゔぁいおりん
    checkToken(args, "\u3094\u3043\u304A\u308A\u3093", "\u3046\u3099\u3043\u304A\u308A\u3093");
  }

  @Test
  public void testNFKCHiragana() throws IOException {
    Map<String, String> args = new HashMap<String, String>();
    args.put("name", "nfkc");

    checkToken(args, "か゛っきー", "がっきー");
    checkToken(args, "ほ゜っきー", "ぽっきー");
  }

  @Test
  public void testNFKCLongSentence() throws IOException {
    Map<String, String> args = new HashMap<String, String>();
    args.put("name", "nfkc");

    checkToken(args,
        "経営再建中のシャープは５日、２０１５年度に前年の３倍となる３００人の新卒社員を採用する計画を発表した。業績が回復傾向にあり、医療関連事業など、次の成長に必要な新事業を担う人員を増やす計画だ。１～２月に開いた会社説明会には昨年の２・５倍の数の学生が集まるなど、学生の反応も上々だという。 シャープの採用人数のピークは１９９１年度の２４４１人。最近の１０年間でみると、０９年度に１２５６人を採用した。しかし、１３年３月期には巨額の純損失を２年連続で計上。採用も１３年度は９１人、１４年度は９５人（見込み）と絞ってきた。　しかし、１５年度は大卒２００人、高卒１００人の計３００人を採用する予定で、１２年度（２７３人）を上回る規模まで戻す。大卒のうち１３０人が技術職で、７０人が営業などの事務職。技術では過去に多かった理工系だけでなく、医学部や農学部など、新事業に必要な技術と知識を持つ学生の採用を増やす。また、事務職は男女半々で採用するという。 採用を担当する深堀昭吾執行役員は「業績が上向いていることで、学生の反応もいい」と話す。",
        "経営再建中のシャープは5日、2015年度に前年の3倍となる300人の新卒社員を採用する計画を発表した。業績が回復傾向にあり、医療関連事業など、次の成長に必要な新事業を担う人員を増やす計画だ。1~2月に開いた会社説明会には昨年の2・5倍の数の学生が集まるなど、学生の反応も上々だという。 シャープの採用人数のピークは1991年度の2441人。最近の10年間でみると、09年度に1256人を採用した。しかし、13年3月期には巨額の純損失を2年連続で計上。採用も13年度は91人、14年度は95人(見込み)と絞ってきた。 しかし、15年度は大卒200人、高卒100人の計300人を採用する予定で、12年度(273人)を上回る規模まで戻す。大卒のうち130人が技術職で、70人が営業などの事務職。技術では過去に多かった理工系だけでなく、医学部や農学部など、新事業に必要な技術と知識を持つ学生の採用を増やす。また、事務職は男女半々で採用するという。 採用を担当する深堀昭吾執行役員は「業績が上向いていることで、学生の反応もいい」と話す。"
    );
  }

  @Test
  public void testNFKCWhiteSpace() throws IOException {
    Map<String, String> args = new HashMap<String, String>();
    args.put("name", "nfkc");

    checkToken(args, " ", " ");
    checkToken(args, "　", " "); // Fill-Width whitespace -> Half-Width whitespace
  }
}
