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

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;

import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

public class TestGosenTokenizerFactory extends LuceneTestCase {
  
  private File baseDir;
  private File dicDir;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    File testRoot = new File(System.getProperty("java.io.tmpdir")).getCanonicalFile();
    baseDir = new File(testRoot, "core-test");
    baseDir.mkdir();
    dicDir = new File(baseDir, "custom-dic");
    dicDir.mkdir();
  }
  
  @Override
  public void tearDown() throws Exception {
    dicDir.delete();
    baseDir.delete();
    super.tearDown();
  }

  @Test
  public void testDictionaryDir() throws Exception {

    ResourceLoader loader = new StringMockResourceLoader("");

    Map<String, String> args = new HashMap<String, String>();
    GosenTokenizerFactory factory = new GosenTokenizerFactory(args);
    factory.inform(loader);
    Field field = GosenTokenizerFactory.class.getDeclaredField("dictionaryDir");
    field.setAccessible(true);
    assertNull("dictionaryDir must be null.", field.get(factory));
    
    // relative path (from conf dir)
    args.put("dictionaryDir", dicDir.getName());
    factory = new GosenTokenizerFactory(args);
    factory.inform(loader);
    assertEquals("dictionaryDir is incorrect.", dicDir.getName(), field.get(factory));
    
    // absolute path
    args.put("dictionaryDir", dicDir.getAbsolutePath());
    factory = new GosenTokenizerFactory(args);
    factory.inform(loader);
    assertEquals("dictionaryDir is incorrect.", dicDir.getAbsolutePath(), field.get(factory));
    
    // not exists path
    String notExistsPath = dicDir.getAbsolutePath() + "/hogehoge";
    args.put("dictionaryDir", notExistsPath);
    factory = new GosenTokenizerFactory(args);
    factory.inform(loader);
    assertEquals("dictionaryDir is incorrect.", notExistsPath, field.get(factory));
    
    
  }

  @Test
  public void testBogusArgments() throws Exception{
    try{
      new GosenTokenizerFactory(new HashMap<String, String>() {{
        put("bogusArg", "bogusValue");
      }});
      fail();
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Unknown parameters"));
    }
  }
}
