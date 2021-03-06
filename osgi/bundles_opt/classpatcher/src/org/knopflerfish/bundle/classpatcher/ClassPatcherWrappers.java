/*
 * Copyright (c) 2003-2011, KNOPFLERFISH project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the KNOPFLERFISH project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.knopflerfish.bundle.classpatcher;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

/**
 * Class containing static wrapper methods used by ClassPatcher. These methods
 * are typically referenced from the patcher.conf file.
 * 
 * <p>
 * Each wrapper must have the exact same signature as the original method, plus
 * one extra long argument representing the bundle id and one object argument
 * representing the caller's context (this) The context object may be null when
 * called from a static context
 * </p>
 * 
 * @see ClassPatcher
 * @author Erik Wistrand
 */
public class ClassPatcherWrappers {

  protected static Bundle getBundle(long bid) {
    return Activator.bc.getBundle(bid);
  }

  public static void systemExitWrapper(int code, long bid, Object context) {
    Activator.println("CP.systemExit code=" + code + ", bid=" + bid + ", context=" + context);
    try {
      // NYI, we only handle framework start via Main.
      Bundle b = getBundle(bid);
      Activator.println("stopping " + b);
      b.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Get bundle class loader at Class.getSystemClassLoader() calls.
   */
  public static ClassLoader getSystemClassLoaderWrapper(long bid, Object context) {
    Bundle b = getBundle(bid);
    if (b == null) {
      throw new IllegalStateException("Undefined bid=" + bid);
    }
    BundleWiring bw = b.adapt(BundleWiring.class);
    if (bw == null) {
      throw new RuntimeException("Unable to adapt Bundle to a BundleWiring" + bid);
    }
    return bw.getClassLoader();
  }

  /**
   * Use bundle class loader at Class.forName(String) calls
   * 
   * <p>
   * First, try Class.forName(String). If this fails, try the context's class
   * loader. If this fails too, explicitly try the bundle class loader.
   * </p>
   */
  public static Class forName1Wrapper(String name, long bid, Object context)
      throws ClassNotFoundException {
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException e) {
      try {
        if (context != null) {
          return Class.forName(name, true, context.getClass().getClassLoader());
        }
      } catch (ClassNotFoundException keeptrying) {
        // noop
      }
      Bundle b = getBundle(bid);
      if (b == null) {
        throw new ClassNotFoundException("Undefined class '" + name + "' since bid=" + bid
            + " is undefined");
      }
      return b.loadClass(name);
    }
  }

  /**
   * Use bundle class loader at Class.forName(String, boolean, ClassLoader)
   * calls.
   * 
   * <p>
   * First, try the supplied class loader. If this fails, try the bundle class
   * loader instead.
   * </p>
   */
  public static Class forName3Wrapper(String name, boolean initialize, ClassLoader cl,
      long bid, Object context) throws ClassNotFoundException {
    try {
      return Class.forName(name, initialize, cl);
    } catch (ClassNotFoundException e) {
      // keep trying with the context's class loader
      try {
        if (context != null) {
          return Class.forName(name, initialize, context.getClass().getClassLoader());
        }
      } catch (ClassNotFoundException keeptrying) {
        // keep trying with the bundle class loader
      }
      Bundle b = getBundle(bid);
      if (b == null) {
        throw new ClassNotFoundException("Undefined class '" + name + "' since bid=" + bid
            + " is undefined");
      }
      return b.loadClass(name);
    }
  }
}
