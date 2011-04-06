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

import static net.java.sen.SenTestUtil.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.java.sen.StringTagger;
import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.stream.CommentFilter;

import org.junit.Test;


/**
 * Test comment filter
 */
public class CommentFilterTest {

	/**
	 * HTML removal
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testCommentFilter1a() throws IOException {

		String testString = "これは<a href=\"#test\">テスト</a>だ";

		Token[] testTokens = new Token[] {
				new Token ("これ", 1848, 0, 2, new Morpheme ("名詞-代名詞-一般", "*", "*", "これ", new String[]{"コレ"}, new String[]{"コレ"}, null)),
				new Token ("は", 2445, 2, 1, new Morpheme ("助詞-係助詞", "*", "*", "は", new String[]{"ハ"}, new String[]{"ワ"}, null)),
				new Token ("<a href=\"#test\">", 0, 3, 16, new Morpheme ("記号-注釈", "*", "*", "<a href=\"#test\">", new String[]{"<a href=\"#test\">"}, new String[]{"<a href=\"#test\">"}, null)),
				new Token ("テスト", 5785, 19, 3, new Morpheme ("名詞-サ変接続", "*", "*", "テスト", new String[]{"テスト"}, new String[]{"テスト"}, null)),
				new Token ("</a>", 0, 22, 4, new Morpheme ("記号-注釈", "*", "*", "</a>", new String[]{"</a>"}, new String[]{"</a>"}, null)),
				new Token ("だ", 7298, 26, 1, new Morpheme ("助動詞", "特殊・ダ", "基本形", "だ", new String[]{"ダ"}, new String[]{"ダ"}, null))
		};


		StringTagger tagger = getStringTagger();
		CommentFilter filter = new CommentFilter();
		filter.readRules (new BufferedReader (new StringReader ("< > 記号-注釈")));
		tagger.addFilter (filter);

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * HTML removal
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testCommentFilter1b() throws IOException {

		String testString = "<p>test";

		Token[] testTokens = new Token[] {
				new Token ("<p>", 0, 0, 3, new Morpheme ("記号-注釈", "*", "*", "<p>", new String[]{"<p>"}, new String[]{"<p>"}, null)),
				new Token ("test", 31059, 3, 4, new Morpheme ("未知語", null, null, "test", new String[]{}, new String[]{}, null))
		};


		StringTagger tagger = getStringTagger();
		CommentFilter filter = new CommentFilter();
		filter.readRules (new BufferedReader (new StringReader ("< > 記号-注釈")));
		tagger.addFilter (filter);

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * HTML removal
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testCommentFilter1c() throws IOException {

		String testString = "test<p>";

		Token[] testTokens = new Token[] {
				new Token ("test", 31059, 0, 4, new Morpheme ("未知語", null, null, "test", new String[]{}, new String[]{}, null)),
				new Token ("<p>", 0, 4, 3, new Morpheme ("記号-注釈", "*", "*", "<p>", new String[]{"<p>"}, new String[]{"<p>"}, null))
		};


		StringTagger tagger = getStringTagger();
		CommentFilter filter = new CommentFilter();
		filter.readRules (new BufferedReader (new StringReader ("< > 記号-注釈")));
		tagger.addFilter (filter);

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * HTML removal
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testCommentFilter1d() throws IOException {

		String testString = "test<br><p>";

		Token[] testTokens = new Token[] {
				new Token ("test", 31059, 0, 4, new Morpheme ("未知語", null, null, "test", new String[]{}, new String[]{}, null)),
				new Token ("<br>", 0, 4, 4, new Morpheme ("記号-注釈", "*", "*", "<br>", new String[]{"<br>"}, new String[]{"<br>"}, null)),
				new Token ("<p>", 0, 8, 3, new Morpheme ("記号-注釈", "*", "*", "<p>", new String[]{"<p>"}, new String[]{"<p>"}, null))
		};


		StringTagger tagger = getStringTagger();
		CommentFilter filter = new CommentFilter();
		filter.readRules (new BufferedReader (new StringReader ("< > 記号-注釈")));
		tagger.addFilter (filter);

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * HTML removal
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testCommentFilter1e() throws IOException {

		String testString = "<br><p>test";

		Token[] testTokens = new Token[] {
				new Token ("<br>", 0, 0, 4, new Morpheme ("記号-注釈", "*", "*", "<br>", new String[]{"<br>"}, new String[]{"<br>"}, null)),
				new Token ("<p>", 0, 4, 3, new Morpheme ("記号-注釈", "*", "*", "<p>", new String[]{"<p>"}, new String[]{"<p>"}, null)),
				new Token ("test", 31059, 7, 4, new Morpheme ("未知語", null, null, "test", new String[]{}, new String[]{}, null))
		};


		StringTagger tagger = getStringTagger();
		CommentFilter filter = new CommentFilter();
		filter.readRules (new BufferedReader (new StringReader ("< > 記号-注釈")));
		tagger.addFilter (filter);

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * HTML removal
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testCommentFilter1f() throws IOException {

		String testString = "test<br><p>end";

		Token[] testTokens = new Token[] {
				new Token ("test", 31059, 0, 4, new Morpheme ("未知語", null, null, "test", new String[]{}, new String[]{}, null)),
				new Token ("<br>", 0, 4, 4, new Morpheme ("記号-注釈", "*", "*", "<br>", new String[]{"<br>"}, new String[]{"<br>"}, null)),
				new Token ("<p>", 0, 8, 3, new Morpheme ("記号-注釈", "*", "*", "<p>", new String[]{"<p>"}, new String[]{"<p>"}, null)),
				new Token ("end", 61942, 11, 3, new Morpheme ("未知語", null, null, "end", new String[]{}, new String[]{}, null))
		};


		StringTagger tagger = getStringTagger();
		CommentFilter filter = new CommentFilter();
		filter.readRules (new BufferedReader (new StringReader ("< > 記号-注釈")));
		tagger.addFilter (filter);

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


	/**
	 * HTML removal
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testCommentFilter2a() throws IOException {

		String testString = "ランダムな《《意味のない》》テキスト";

		Token[] testTokens = new Token[] {
				new Token ("ランダム", 4666, 0, 4, new Morpheme ("名詞-形容動詞語幹", "*", "*", "ランダム", new String[]{"ランダム"}, new String[]{"ランダム"}, null)),
				new Token ("な", 5027, 4, 1, new Morpheme ("助動詞", "特殊・ダ", "体言接続", "だ", new String[]{"ナ"}, new String[]{"ナ"}, null)),
				new Token ("《《意味のない》》", 0, 5, 9, new Morpheme ("記号-注釈", "*", "*", "《《意味のない》》", new String[]{"《《意味のない》》"}, new String[]{"《《意味のない》》"}, null)),
				new Token ("テキスト", 8488, 14, 4, new Morpheme ("名詞-一般", "*", "*", "テキスト", new String[]{"テキスト"}, new String[]{"テキスト"}, null))
		};


		StringTagger tagger = getStringTagger();
		CommentFilter filter = new CommentFilter();
		filter.readRules (new BufferedReader (new StringReader ("《《 》》 記号-注釈")));
		tagger.addFilter (filter);

		List<Token> tokens = tagger.analyze(testString);

		compareTokens (testTokens, tokens);

	}


}
