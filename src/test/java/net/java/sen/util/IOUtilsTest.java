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

package net.java.sen.util;

import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link IOUtils} exception-safe close helpers.
 */
public class IOUtilsTest {

  // -----------------------------------------------------------------------
  // close() — normal paths
  // -----------------------------------------------------------------------

  @Test
  public void testCloseNull() throws IOException {
    IOUtils.close((Closeable) null);
  }

  @Test
  public void testCloseEmpty() throws IOException {
    IOUtils.close();
  }

  @Test
  public void testCloseWorking() throws IOException {
    boolean[] closed = {false};
    IOUtils.close(() -> closed[0] = true);
    assertTrue(closed[0]);
  }

  @Test
  public void testCloseMultipleAllClosed() throws IOException {
    boolean[] closed = {false, false};
    IOUtils.close(() -> closed[0] = true, () -> closed[1] = true);
    assertTrue(closed[0]);
    assertTrue(closed[1]);
  }

  @Test
  public void testCloseNullMixedWithWorking() throws IOException {
    boolean[] closed = {false};
    IOUtils.close(null, () -> closed[0] = true);
    assertTrue(closed[0]);
  }

  // -----------------------------------------------------------------------
  // close() — exception propagation
  // -----------------------------------------------------------------------

  @Test
  public void testCloseIOExceptionPropagated() {
    try {
      IOUtils.close(() -> { throw new IOException("disk error"); });
      fail("Expected IOException");
    } catch (IOException e) {
      assertEquals("disk error", e.getMessage());
    }
  }

  @Test
  public void testCloseRuntimeExceptionPropagated() {
    try {
      IOUtils.close(() -> { throw new RuntimeException("boom"); });
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertEquals("boom", e.getMessage());
    } catch (IOException e) {
      fail("should not be IOException");
    }
  }

  @Test
  public void testCloseErrorPropagated() {
    try {
      IOUtils.close(() -> { throw new Error("fatal"); });
      fail("Expected Error");
    } catch (Error e) {
      assertEquals("fatal", e.getMessage());
    } catch (IOException e) {
      fail("should not be IOException");
    }
  }

  @Test
  public void testCloseFirstThrowsSecondStillClosed() {
    boolean[] secondClosed = {false};
    try {
      IOUtils.close(
          () -> { throw new IOException("first"); },
          () -> secondClosed[0] = true
      );
      fail("Expected IOException");
    } catch (IOException e) {
      assertEquals("first", e.getMessage());
      assertTrue("second closeable must be closed even when first throws", secondClosed[0]);
    }
  }

  // -----------------------------------------------------------------------
  // closeWhileHandlingException()
  // -----------------------------------------------------------------------

  @Test
  public void testCloseWhileHandlingExceptionNull() throws IOException {
    IOUtils.closeWhileHandlingException((Closeable) null);
  }

  @Test
  public void testCloseWhileHandlingExceptionWorking() throws IOException {
    boolean[] closed = {false};
    IOUtils.closeWhileHandlingException(() -> closed[0] = true);
    assertTrue(closed[0]);
  }

  @Test
  public void testCloseWhileHandlingExceptionSuppressesThrow() throws IOException {
    IOUtils.closeWhileHandlingException(() -> { throw new IOException("suppressed"); });
  }

  @Test
  public void testCloseWhileHandlingExceptionMultiple() throws IOException {
    boolean[] closed = {false, false};
    IOUtils.closeWhileHandlingException(
        () -> { closed[0] = true; throw new IOException("ignored"); },
        () -> closed[1] = true
    );
    assertTrue(closed[0]);
    assertTrue(closed[1]);
  }

  @Test
  public void testCloseBothThrowFirstExceptionPropagated() {
    // Exercises the addSuppressed(non-null, non-null) path
    try {
      IOUtils.close(
          () -> { throw new IOException("first"); },
          () -> { throw new IOException("second"); }
      );
      fail("Expected IOException");
    } catch (IOException e) {
      assertEquals("first", e.getMessage());
    }
  }

  @Test
  public void testCloseWithNullBetweenThrows() {
    // null in the middle: first throws, second is null (no-op), third throws
    try {
      IOUtils.close(
          () -> { throw new IOException("e1"); },
          null,
          () -> { throw new IOException("e2"); }
      );
      fail("Expected IOException");
    } catch (IOException e) {
      assertEquals("e1", e.getMessage());
    }
  }
}
