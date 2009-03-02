/*
 * Copyright (c) 2003-2009, KNOPFLERFISH project
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

package org.knopflerfish.framework.permissions;

import java.net.*;
import java.security.*;

import org.knopflerfish.framework.BundleURLStreamHandler;



/**
 * Implementation of a Permission Policy for Framework.
 *
 * Special handling for all protection domains that corresponds to a
 * bundle, for all other delegate to given default policy (normally
 * the policy file based policy implementation).
 *
 * @see java.security.Policy
 * @author Jan Stein, Philippe Laporte, Gunnar Ekolin
 */

class FrameworkPolicy extends Policy {

  /** The policy to delegate non-bundle permission requests to. */
  private final Policy defaultPolicy;

  private PermissionsHandle ph;


  /**
   */
  FrameworkPolicy(Policy policy, PermissionsHandle ph) {
    this.defaultPolicy = policy;
    this.ph = ph;
  }


  // Delegate to the wrapped defaultPolicy for all non-bundle domains.
  public PermissionCollection getPermissions(ProtectionDomain pd) {
    if (null==pd)
      return defaultPolicy.getPermissions(pd);

    CodeSource cs = pd.getCodeSource();
    if (null==cs)
      return defaultPolicy.getPermissions(pd);

    URL u = cs.getLocation();
    if (u != null && BundleURLStreamHandler.PROTOCOL.equals(u.getProtocol())) {
      return getPermissions(cs);
    } else {
      return defaultPolicy.getPermissions(pd);
    }
  }


  /**
   */
  public PermissionCollection getPermissions(CodeSource cs) {
    if (null==cs) {
      // Not a code source for a bundle, delegate to the default policy
      return defaultPolicy.getPermissions(cs);
    }

    URL u = cs.getLocation();
    if (u != null && BundleURLStreamHandler.PROTOCOL.equals(u.getProtocol())) {
      try {
        Long id = new Long(u.getHost());
        //return getPermissions(id);
        return ph.getPermissionCollection(id);
      } catch (NumberFormatException ignore) {
        return null;
      }
    } else {
      return defaultPolicy.getPermissions(cs);
    }
  }


  /**
   */
  public boolean implies(final ProtectionDomain pd, final Permission p) {
    // NYI! Optimize here for framework.jar + bootclasses?
    CodeSource cs = null == pd ? pd.getCodeSource() : null;
    URL u = null != cs ? cs.getLocation() : null;
    if (u != null && BundleURLStreamHandler.PROTOCOL.equals(u.getProtocol())) {
      PermissionCollection pc = getPermissions(cs);
      return (pc == null) ? false : pc.implies(p);
    } else {
      Boolean res = (Boolean)AccessController.doPrivileged(new PrivilegedAction() {
	  public Object run() {
	    return new Boolean(defaultPolicy.implies(pd, p));
	  }
	});
      return res.booleanValue();
    }
  }


  /**
   */
  public void refresh() {
    // A bundle permissions is allways up to date, but we must
    // propagate to the wrapped defaultPolicy.
    defaultPolicy.refresh();
  }

}
