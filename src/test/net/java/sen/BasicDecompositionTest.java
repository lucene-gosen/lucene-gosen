/*
 * Copyright (C) 2006-2007
 * Matt Francis <asbel@neosheffield.co.uk>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package net.java.sen;

import java.io.IOException;
import java.util.List;

import net.java.sen.StringTagger;
import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Token;

import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

import static net.java.sen.SenTestUtil.*;


/**
 * Tests basic string analysis
 */
public class BasicDecompositionTest extends LuceneTestCase {

	/**
	 * Tests string decomposition
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testBlankDecomposition() throws IOException {

		String testString = "";

		Token[] testTokens = new Token[] {};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Tests string decomposition
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testDecomposition1() throws IOException {

		String testString = "本来は、貧困層の女性や子供に医療保護を提供するために創設された制度である、アメリカ低所得者医療援助制度が、今日では、その予算の約３分の１を老人に費やしている。";

		Token[] testTokens = new Token[] {
				new Token ("本来", 3199, 0, 2, new Morpheme ("名詞-副詞可能", "*", "*", "*", new String[]{"ホンライ"}, new String[]{"ホンライ"}, null)),
				new Token ("は", 4128, 2, 1, new Morpheme ("助詞-係助詞", "*", "*", "*", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("、", 4680, 3, 1, new Morpheme ("記号-読点", "*", "*", "*", new String[]{"、"}, new String[]{"、"}, null)),
				new Token ("貧困", 7949, 4, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"ヒンコン"}, new String[]{"ヒンコン"}, null)),
				new Token ("層", 10893, 6, 1, new Morpheme ("名詞-接尾-一般", "*", "*", "*", new String[]{"ソウ"}, new String[]{"ソー"}, null)),
				new Token ("の", 11484, 7, 1, new Morpheme ("助詞-連体化", "*", "*", "*", new String[]{"ノ"}, new String[]{"ノ"}, null)),
				new Token ("女性", 13512, 8, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"ジョセイ"}, new String[]{"ジョセイ"}, null)),
				new Token ("や", 15070, 10, 1, new Morpheme ("助詞-並立助詞", "*", "*", "*", new String[]{"ヤ"}, new String[]{"ヤ"}, null)),
				new Token ("子供", 17180, 11, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"コドモ"}, new String[]{"コドモ"}, null)),
				new Token ("に", 18031, 13, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "*", new String[]{"ニ"}, new String[]{"ニ"}, null)),
				new Token ("医療", 20992, 14, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"イリョウ"}, new String[]{"イリョー"}, null)),
				new Token ("保護", 23895, 16, 2, new Morpheme ("名詞-サ変接続", "*", "*", "*", new String[]{"ホゴ"}, new String[]{"ホゴ"}, null)),
				new Token ("を", 24677, 18, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "*", new String[]{"ヲ"}, new String[]{"ヲ"}, null)),
				new Token ("提供", 27282, 19, 2, new Morpheme ("名詞-サ変接続", "*", "*", "*", new String[]{"テイキョウ"}, new String[]{"テイキョー"}, null)),
				new Token ("する", 27768, 21, 2, new Morpheme ("動詞-自立", "サ変・スル", "基本形", "*", new String[]{"スル"}, new String[]{"スル"}, null)),
				new Token ("ため", 29210, 23, 2, new Morpheme ("名詞-非自立-副詞可能", "*", "*", "*", new String[]{"タメ"}, new String[]{"タメ"}, null)),
				new Token ("に", 29770, 25, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "*", new String[]{"ニ"}, new String[]{"ニ"}, null)),
				new Token ("創設", 32892, 26, 2, new Morpheme ("名詞-サ変接続", "*", "*", "*", new String[]{"ソウセツ"}, new String[]{"ソーセツ"}, null)),
				new Token ("さ", 33378, 28, 1, new Morpheme ("動詞-自立", "サ変・スル", "未然レル接続", "する", new String[]{"サ"}, new String[]{"サ"}, null)),
				new Token ("れ", 33429, 29, 1, new Morpheme ("動詞-接尾", "一段", "連用形", "れる", new String[]{"レ"}, new String[]{"レ"}, null)),
				new Token ("た", 33678, 30, 1, new Morpheme ("助動詞", "特殊・タ", "基本形", "*", new String[]{"タ"}, new String[]{"タ"}, null)),
				new Token ("制度", 36383, 31, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"セイド"}, new String[]{"セイド"}, null)),
				new Token ("で", 37431, 33, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "*", new String[]{"デ"}, new String[]{"デ"}, null)),
				new Token ("ある", 38994, 34, 2, new Morpheme ("動詞-自立", "五段・ラ行", "基本形", "*", new String[]{"アル"}, new String[]{"アル"}, null)),
				new Token ("、", 40616, 36, 1, new Morpheme ("記号-読点", "*", "*", "*", new String[]{"、"}, new String[]{"、"}, null)),
				new Token ("アメリカ", 43078, 37, 4, new Morpheme ("名詞-固有名詞-地域-国", "*", "*", "*", new String[]{"アメリカ"}, new String[]{"アメリカ"}, null)),
				new Token ("低", 46278, 41, 1, new Morpheme ("接頭詞-名詞接続", "*", "*", "*", new String[]{"テイ"}, new String[]{"テイ"}, null)),
				new Token ("所得", 48703, 42, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"ショトク"}, new String[]{"ショトク"}, null)),
				new Token ("者", 50321, 44, 1, new Morpheme ("名詞-接尾-一般", "*", "*", "*", new String[]{"シャ", "モノ"}, new String[]{"シャ", "モノ"}, null)),
				new Token ("医療", 53433, 45, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"イリョウ"}, new String[]{"イリョー"}, null)),
				new Token ("援助", 56492, 47, 2, new Morpheme ("名詞-サ変接続", "*", "*", "*", new String[]{"エンジョ"}, new String[]{"エンジョ"}, null)),
				new Token ("制度", 59334, 49, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"セイド"}, new String[]{"セイド"}, null)),
				new Token ("が", 60226, 51, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "*", new String[]{"ガ"}, new String[]{"ガ"}, null)),
				new Token ("、", 61208, 52, 1, new Morpheme ("記号-読点", "*", "*", "*", new String[]{"、"}, new String[]{"、"}, null)),
				new Token ("今日", 63768, 53, 2, new Morpheme ("名詞-副詞可能", "*", "*", "*", new String[]{"キョウ", "コンニチ"}, new String[]{"キョー", "コンニチ"}, null)),
				new Token ("で", 65126, 55, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "*", new String[]{"デ"}, new String[]{"デ"}, null)),
				new Token ("は", 65801, 56, 1, new Morpheme ("助詞-係助詞", "*", "*", "*", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("、", 66353, 57, 1, new Morpheme ("記号-読点", "*", "*", "*", new String[]{"、"}, new String[]{"、"}, null)),
				new Token ("その", 67980, 58, 2, new Morpheme ("連体詞", "*", "*", "*", new String[]{"ソノ"}, new String[]{"ソノ"}, null)),
				new Token ("予算", 70263, 60, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"ヨサン"}, new String[]{"ヨサン"}, null)),
				new Token ("の", 70932, 62, 1, new Morpheme ("助詞-連体化", "*", "*", "*", new String[]{"ノ"}, new String[]{"ノ"}, null)),
				new Token ("約", 72930, 63, 1, new Morpheme ("接頭詞-数接続", "*", "*", "*", new String[]{"ヤク"}, new String[]{"ヤク"}, null)),
				new Token ("３", 73888, 64, 1, new Morpheme ("名詞-数", "*", "*", "*", new String[]{"サン"}, new String[]{"サン"}, null)),
				new Token ("分の", 76019, 65, 2, new Morpheme ("名詞-接尾-助数詞", "*", "*", "*", new String[]{"ブンノ"}, new String[]{"ブンノ"}, null)),
				new Token ("１", 77746, 67, 1, new Morpheme ("名詞-数", "*", "*", "*", new String[]{"イチ"}, new String[]{"イチ"}, null)),
				new Token ("を", 79950, 68, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "*", new String[]{"ヲ"}, new String[]{"ヲ"}, null)),
				new Token ("老人", 83094, 69, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"ロウジン"}, new String[]{"ロージン"}, null)),
				new Token ("に", 83945, 71, 1, new Morpheme ("助詞-格助詞-一般", "*", "*", "*", new String[]{"ニ"}, new String[]{"ニ"}, null)),
				new Token ("費やし", 87143, 72, 3, new Morpheme ("動詞-自立", "五段・サ行", "連用形", "費やす", new String[]{"ツイヤシ"}, new String[]{"ツイヤシ"}, null)),
				new Token ("て", 87489, 75, 1, new Morpheme ("助詞-接続助詞", "*", "*", "*", new String[]{"テ"}, new String[]{"テ"}, null)),
				new Token ("いる", 87732, 76, 2, new Morpheme ("動詞-非自立", "一段", "基本形", "*", new String[]{"イル"}, new String[]{"イル"}, null)),
				new Token ("。", 88146, 78, 1, new Morpheme ("記号-句点", "*", "*", "*", new String[]{"。"}, new String[]{"。"}, null))

		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Tests string decomposition
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testDecomposition2() throws IOException {

		String testString = "麻薬の密売は根こそぎ絶やさなければならない";

		Token[] testTokens = new Token[] {
				new Token ("麻薬", 3557, 0, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"マヤク"}, new String[]{"マヤク"}, null)),
				new Token ("の", 4226, 2, 1, new Morpheme ("助詞-連体化", "*", "*", "*", new String[]{"ノ"}, new String[]{"ノ"}, null)),
				new Token ("密売", 8127, 3, 2, new Morpheme ("名詞-サ変接続", "*", "*", "*", new String[]{"ミツバイ"}, new String[]{"ミツバイ"}, null)),
				new Token ("は", 9252, 5, 1, new Morpheme ("助詞-係助詞", "*", "*", "*", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("根こそぎ", 12949, 6, 4, new Morpheme ("副詞-一般", "*", "*", "*", new String[]{"ネコソギ"}, new String[]{"ネコソギ"}, null)),
				new Token ("絶やさ", 16815, 10, 3, new Morpheme ("動詞-自立", "五段・サ行", "未然形", "絶やす", new String[]{"タヤサ"}, new String[]{"タヤサ"}, null)),
				new Token ("な", 17310, 13, 1, new Morpheme ("助動詞", "特殊・ナイ", "ガル接続", "ない", new String[]{"ナ"}, new String[]{"ナ"}, null)),
				new Token ("けれ", 20406, 14, 2, new Morpheme ("動詞-自立", "五段・ラ行", "仮定形", "ける", new String[]{"ケレ"}, new String[]{"ケレ"}, null)),
				new Token ("ば", 20408, 16, 1, new Morpheme ("助詞-接続助詞", "*", "*", "*", new String[]{"バ"}, new String[]{"バ"}, null)),
				new Token ("なら", 21095, 17, 2, new Morpheme ("動詞-非自立", "五段・ラ行", "未然形", "なる", new String[]{"ナラ"}, new String[]{"ナラ"}, null)),
				new Token ("ない", 21189, 19, 2, new Morpheme ("助動詞", "特殊・ナイ", "基本形", "*", new String[]{"ナイ"}, new String[]{"ナイ"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Tests string decomposition
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testDecomposition3() throws IOException {

		String testString = "魔女狩大将マシュー・ホプキンス。";

		Token[] testTokens = new Token[] {
				new Token ("魔女", 3866, 0, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"マジョ"}, new String[]{"マジョ"}, null)),
				new Token ("狩", 8568, 2, 1, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"カリ"}, new String[]{"カリ"}, null)),
				new Token ("大将", 12919, 3, 2, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"タイショウ"}, new String[]{"タイショー"}, null)),
				new Token ("マシュー", 43724, 5, 4, new Morpheme ("未知語", null, null, "*", new String[]{}, new String[]{}, null)),
				new Token ("・", 45404, 9, 1, new Morpheme ("記号-一般", "*", "*", "*", new String[]{"・"}, new String[]{"・"}, null)),
				new Token ("ホプキンス", 76442, 10, 5, new Morpheme ("未知語", null, null, "*", new String[]{}, new String[]{}, null)),
				new Token ("。", 77500, 15, 1, new Morpheme ("記号-句点", "*", "*", "*", new String[]{"。"}, new String[]{"。"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * Tests string decomposition
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testDecomposition4() throws IOException {

		String testString = "これは本ではない";

		Token[] testTokens = new Token[] {
				new Token ("これ", 1848, 0, 2, new Morpheme ("名詞-代名詞-一般", "*", "*", "*", new String[]{"コレ"}, new String[]{"コレ"}, null)),
				new Token ("は", 2445, 2, 1, new Morpheme ("助詞-係助詞", "*", "*", "*", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("本", 5181, 3, 1, new Morpheme ("名詞-一般", "*", "*", "*", new String[]{"ホン", "モト"}, new String[]{"ホン", "モト"}, null)),
				new Token ("で", 6466, 4, 1, new Morpheme ("助動詞", "特殊・ダ", "連用形", "だ", new String[]{"デ"}, new String[]{"デ"}, null)),
				new Token ("は", 6978, 5, 1, new Morpheme ("助詞-係助詞", "*", "*", "*", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("ない", 7098, 6, 2, new Morpheme ("助動詞", "特殊・ナイ", "基本形", "*", new String[]{"ナイ"}, new String[]{"ナイ"}, null))
		};


		StringTagger tagger = getStringTagger();

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


}