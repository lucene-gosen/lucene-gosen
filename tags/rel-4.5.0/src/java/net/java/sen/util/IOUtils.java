package net.java.sen.util;

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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * a subset of org.apache.lucene.util.IOUtils.
 *
 */
public class IOUtils {
  /** This reflected {@link Method} is {@code null} before Java 7 */
  private static final Method SUPPRESS_METHOD;
  static {
    Method m;
    try {
      m = Throwable.class.getMethod("addSuppressed", Throwable.class);
    } catch (Exception e) {
      m = null;
    }
    SUPPRESS_METHOD = m;
  }

  /**
   * Closes all given <tt>Closeable</tt>s.  Some of the
   * <tt>Closeable</tt>s may be null; they are
   * ignored.  After everything is closed, the method either
   * throws the first exception it hit while closing, or
   * completes normally if there were no exceptions.
   * 
   * @param objects
   *          objects to call <tt>close()</tt> on
   */
  public static void close(Closeable... objects) throws IOException {
    Throwable th = null;

    for (Closeable object : objects) {
      try {
        if (object != null) {
          object.close();
        }
      } catch (Throwable t) {
        addSuppressed(th, t);
        if (th == null) {
          th = t;
        }
      }
    }

    if (th != null) {
      if (th instanceof IOException) throw (IOException) th;
      if (th instanceof RuntimeException) throw (RuntimeException) th;
      if (th instanceof Error) throw (Error) th;
      throw new RuntimeException(th);
    }
  }
  /**
   * Closes all given <tt>Closeable</tt>s, suppressing all thrown exceptions.
   * Some of the <tt>Closeable</tt>s may be null, they are ignored.
   * 
   * @param objects
   *          objects to call <tt>close()</tt> on
   */
  public static void closeWhileHandlingException(Closeable... objects) throws IOException {
    for (Closeable object : objects) {
      try {
        if (object != null) {
          object.close();
        }
      } catch (Throwable t) {
      }
    }
  }
  /** adds a Throwable to the list of suppressed Exceptions of the first Throwable (if Java 7 is detected)
   * @param exception this exception should get the suppressed one added
   * @param suppressed the suppressed exception
   */
  private static final void addSuppressed(Throwable exception, Throwable suppressed) {
    if (SUPPRESS_METHOD != null && exception != null && suppressed != null) {
      try {
        SUPPRESS_METHOD.invoke(exception, suppressed);
      } catch (Exception e) {
        // ignore any exceptions caused by invoking (e.g. security constraints)
      }
    }
  }
  
}
