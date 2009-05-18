package org.knopflerfish.bundle.command.commands;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.lang.reflect.*;
import org.osgi.framework.*;
import org.osgi.service.command.*;

public class CommandCommands {

  public static String SCOPE = "command";
  public static String[] FUNCTION = new String[] { "help" };

  BundleContext bc;

  public CommandCommands(BundleContext bc) {
    this.bc = bc;
  }

  public Object help(String scope) throws Exception {
    String filter = "(" + CommandProcessor.COMMAND_FUNCTION + "=*)";
    if(scope != null) {
      filter = 
        "(&" + 
        filter + 
        "(" + CommandProcessor.COMMAND_SCOPE + "=" + scope + ")" + 
        ")";
        
    }
    ServiceReference[] srl = bc.getServiceReferences(null, filter);
    for(int i = 0; srl != null && i < srl.length; i++) {
      Object obj = null;
      try {
        obj = bc.getService(srl[i]);
        System.out.println(obj.getClass().getName());
        System.out.println(" function: " + srl[i].getProperty(CommandProcessor.COMMAND_FUNCTION));
        System.out.println(" scope:    " + srl[i].getProperty(CommandProcessor.COMMAND_SCOPE));
        Method[] ml = obj.getClass().getMethods();
        for(int j = 0; ml != null && j < ml.length; j++) {
          System.out.println(" " + ml[j]);
        }
      } finally {
        bc.ungetService(srl[i]);
      }
    } 
    
    return "foo";
  }
}