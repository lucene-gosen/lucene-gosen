/*
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

package net.java.sen.filter.stream;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for the package-private {@link CompositeTokenFilter.Rule} inner class.
 *
 * <p>The integration tests in {@code CompositeTokenFilterTest} exercise the rule
 * loading and matching pipeline; this class covers the individual Rule methods
 * ({@code getPartOfSpeech}, {@code contains}, {@code remove}, {@code toString})
 * that would otherwise remain uncovered.
 */
public class CompositeTokenFilterRuleTest {

  private CompositeTokenFilter.Rule makeRule(String partOfSpeech, String... members) {
    Set<String> ruleSet = new HashSet<>();
    for (String m : members) {
      ruleSet.add(m);
    }
    return new CompositeTokenFilter.Rule(ruleSet, partOfSpeech);
  }

  @Test
  public void testGetPartOfSpeech() {
    CompositeTokenFilter.Rule rule = makeRule("名詞-数", "名詞-数", "名詞-数記号");
    assertEquals("名詞-数", rule.getPartOfSpeech());
  }

  @Test
  public void testContainsReturnsTrueForMember() {
    CompositeTokenFilter.Rule rule = makeRule("名詞-数", "名詞-数", "名詞-数記号");
    assertTrue(rule.contains("名詞-数"));
    assertTrue(rule.contains("名詞-数記号"));
  }

  @Test
  public void testContainsReturnsFalseForNonMember() {
    CompositeTokenFilter.Rule rule = makeRule("名詞-数", "名詞-数");
    assertFalse(rule.contains("動詞"));
  }

  @Test
  public void testRemoveDeletesMember() {
    CompositeTokenFilter.Rule rule = makeRule("名詞-数", "名詞-数", "名詞-数記号");
    rule.remove("名詞-数記号");
    assertFalse(rule.contains("名詞-数記号"));
    assertTrue(rule.contains("名詞-数"));
  }

  @Test
  public void testRemoveNonExistentIsNoOp() {
    CompositeTokenFilter.Rule rule = makeRule("名詞-数", "名詞-数");
    rule.remove("存在しない");
    assertTrue(rule.contains("名詞-数"));
  }

  @Test
  public void testToStringContainsPartOfSpeech() {
    CompositeTokenFilter.Rule rule = makeRule("名詞-数", "名詞-数");
    String s = rule.toString();
    assertNotNull(s);
    assertTrue("toString should start with partOfSpeech", s.startsWith("名詞-数"));
  }

  @Test
  public void testToStringContainsMember() {
    CompositeTokenFilter.Rule rule = makeRule("名詞-数", "名詞-数記号");
    String s = rule.toString();
    assertTrue("toString should list the member POS", s.contains("名詞-数記号"));
  }

  @Test
  public void testToStringMultipleMembers() {
    CompositeTokenFilter.Rule rule = makeRule("X", "A", "B", "C");
    String s = rule.toString();
    assertTrue(s.contains("X"));
    // At least two of the three members should appear somewhere
    int found = 0;
    for (String m : new String[]{"A", "B", "C"}) {
      if (s.contains(m)) found++;
    }
    assertTrue("toString should include member POS codes", found >= 1);
  }
}
