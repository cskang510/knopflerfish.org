/*
 * Copyright (c) 2003, KNOPFLERFISH project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer. 
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution. 
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

package org.knopflerfish.ant.taskdefs.bundle;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;
import org.apache.tools.ant.util.JavaEnvUtils;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.BasicType;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

/**
 * <p>
 * Task that analyzes a set of bundle jar files and builds OBR XML 
 * documentation from these bundles. 
 * </p>
 *
 * <p>
 * Bundle jar files files are analyzed using the static manifest attributes.
 * </p>
 *
 * <h3>Parameters</h3>
 *
 * <table border=>
 *  <tr>
 *   <td valign=top><b>Attribute</b></td>
 *   <td valign=top><b>Description</b></td>
 *   <td valign=top><b>Required</b></td>
 *  </tr>
 *
 *  <tr>
 *   <td valign=top>baseDir</td>
 *   <td valign=top>
 *    Base directory for scanning for jar files.
 *   </td>
 *   <td valign=top>No.<br> Default value is "."</td>
 *  </tr>
 *
 *  <tr>
 *   <td valign=top>baseURL</td>
 *   <td valign=top>
 *    Base URL for generated bundleupdate locations.
 *   </td>
 *   <td valign=top>No.<br> Default value is ""</td>
 *  </tr>
 *
 *  <tr>
 *   <td valign=top>outFile</td>
 *   <td valign=top>
 *    File name of generated repository XML file
 *   </td>
 *   <td valign=top>No.<br> Default value is "repository.xml"</td>
 *  </tr>
 *
 *  <tr>
 *   <td valign=top>repoName</td>
 *   <td valign=top>
 *    Repository name
 *   </td>
 *   <td valign=top>No.<br> Default value is "Knopflerfish bundle repository"</td>
 *  </tr>
 *
 * <h3>Parameters specified as nested elements</h3>
 * <h4>fileset</h4>
 *
 * (required)<br>
 * <p>
 * All jar files must be specified as a fileset. If there exists
 * jar files 
 * <code>[prefix]_all-[suffix].jar</code> and 
 * <code>[prefix]-[suffix].jar</code>, 
 * <b>only</b> 
 * <code>[prefix]-_all-[suffix].jar</code> will be included.
 * </p>
 *
 */
public class OBRExtractorTask extends Task {
  
  private Vector    filesets = new Vector();
  private FileUtils fileUtils;
  
  private File   baseDir             = new File(".");
  private String baseURL             = "";
  private String outFile             = "repository.xml";
  private String repoName            = "Knopflerfish bundle repository";
  private String repoXSLURL          = "";
 
  public OBRExtractorTask() {

    fileUtils = FileUtils.newFileUtils();

  }

  public void setBaseDir(String s) {
    this.baseDir = new File((new File(s)).getAbsolutePath());
  }

  public void setBaseURL(String s) {
    this.baseURL = s;
  }

  public void setRepoXSLURL(String s) {
    this.repoXSLURL = s;
  }
  
  public void setOutFile(String s) {
    this.outFile = s;
  }

  public void setRepoName(String s) {
    this.repoName = s;
  }
  
  public void addFileset(FileSet set) {
    filesets.addElement(set);
  }

  // File -> BundleInfo
  Map jarMap = new HashMap();
  
  // Implements Task
  public void execute() throws BuildException {
    if (filesets.size() == 0) {
      throw new BuildException("No fileset specified");
    }
    
    jarMap = new HashMap();
    
    System.out.println("loading bundle info...");
    
    try {
      for (int i = 0; i < filesets.size(); i++) {
        FileSet          fs      = (FileSet) filesets.elementAt(i);
        DirectoryScanner ds      = fs.getDirectoryScanner(project);
        File             projDir = fs.getDir(project);
	
        String[] srcFiles = ds.getIncludedFiles();
        String[] srcDirs  = ds.getIncludedDirectories();
        
        for (int j = 0; j < srcFiles.length ; j++) {
          File file = new File(projDir, srcFiles[j]); 
          if(file.getName().endsWith(".jar")) {
            jarMap.put(file, new BundleInfo(file));
          }
        }
      }
      
      Set removeSet = new HashSet();
      
      for(Iterator it = jarMap.keySet().iterator(); it.hasNext();) {
        File       file = (File)it.next();
        String name = file.getAbsolutePath();
        if(-1 != name.indexOf("_all-")) {
          File f2 = new File(Util.replace(name, "_all-", "-"));
          removeSet.add(f2);
          System.out.println("skip " + f2);
        }
      }
      
      if(removeSet.size() > 0) {
        System.out.println("skipping " + removeSet.size() + " bundles");
      }
      
      for(Iterator it = removeSet.iterator(); it.hasNext();) {
        File f = (File)it.next();
        jarMap.remove(f);
      }
      
      System.out.println("analyzing " + jarMap.size() + " bundles");
      
      for(Iterator it = jarMap.keySet().iterator(); it.hasNext();) {
        File       file = (File)it.next();
        BundleInfo info = (BundleInfo)jarMap.get(file);
        
        info.load();
      }
      
      System.out.println("writing bundle OBR to " + outFile);      
      
      OutputStream out = null;
      try {
        out = new FileOutputStream(outFile);
        PrintStream ps = new PrintStream(out);
        
        ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        if(repoXSLURL != null && !"".equals(repoXSLURL)) {
          ps.println("<?xml-stylesheet type=\"text/xsl\" href=\"" + 
                     repoXSLURL + "\"?>");
        }
        ps.println("");
        ps.println("<!-- Generated by " + getClass().getName() + 
                   " -->");
        ps.println("");
        ps.println("<bundles>");
        ps.print(" <repository-bundle-name>");
        ps.print(repoName);
        ps.println("</repository-bundle-name>");
        ps.println(" <date>" + (new Date()) + "</date>");
        ps.println(" <dtd-version>1.0</dtd-version>");
        
        for(Iterator it = jarMap.keySet().iterator(); it.hasNext();) {
          File       file = (File)it.next();
          BundleInfo info = (BundleInfo)jarMap.get(file);
          
          info.writeBundleXML(ps);
        }
        ps.println("</bundles>");
        ps.close();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try { out.close(); } catch (Exception ignored) { }
      }
      
    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException("Failed to extract bundle info: " + e, e);
    }
  }
  
  class BundleInfo {
    File       file;
    Attributes attribs;
    String path;
    Map pkgExportMap;
    Map pkgImportMap;

    public BundleInfo(File file) throws IOException  {
      this.file = file;
      path = Util.replace(file.getCanonicalPath().substring(1 + baseDir.getCanonicalPath().length()), "\\", "/");
    }
    
    public void load() throws Exception {
      JarFile    jarFile      = new JarFile(file);
      Manifest   mf           = jarFile.getManifest();
      attribs                 = mf.getMainAttributes();

      pkgExportMap     = parseNames(attribs.getValue("Export-Package"));
      pkgImportMap     = parseNames(attribs.getValue("Import-Package"));
      
    }

    
    Map parseNames(String s) {
      
      Map map = new TreeMap();

      //      System.out.println(file + ": " + s);
      if(s != null) {
        s = s.trim();
        String[] lines = Util.splitwords(s, ",", '\"');
        for(int i = 0; i < lines.length; i++) {
          String[] words = Util.splitwords(lines[i].trim(), ";", '\"');
          if(words.length < 1) {
            throw new RuntimeException("bad package spec '" + s + "'"); 
          }
          String spec = ArrayInt.UNDEF;
          String name = words[0].trim();
          
          for(int j = 1; j < words.length; j++) {
            String[] info = Util.splitwords(words[j], "=", '\"');
            
            if(info.length == 2) {
              if("specification-version".equals(info[0].trim())) {
                spec = info[1].trim();
              }
            }
          }
          
          ArrayInt version = new ArrayInt(spec);
          
          map.put(name, spec);
        }
      }
      
      return map;
    }
    
    public void writeBundleXML(PrintStream out) throws IOException {
      
      out.println("");
      out.println(" <!-- " + path + " -->");
      out.println(" <bundle>");

      printAttrib(out, "bundle-name",        "Bundle-Name");
      printAttrib(out, "bundle-version",     "Bundle-Version");
      printAttrib(out, "bundle-docurl",      "Bundle-DocURL");
      printAttrib(out, "bundle-category",    "Bundle-Category");
      printAttrib(out, "bundle-vendor",      "Bundle-Vendor");
      printAttrib(out, "bundle-description",    "Bundle-Description");
      printAttrib(out, "bundle-subversionurl",  "Bundle-SubversionURL");
      printAttrib(out, "bundle-apivendor",      "Bundle-APIVendor");
      printAttrib(out, "bundle-uuid",           "Bundle-UUID");
      printAttrib(out, "application-icon",      "Application-Icon");

      
      out.print("  <bundle-updatelocation>");
      out.print(baseURL + path);
      out.println("</bundle-updatelocation>");

      dumpPackages(out, "export-package", pkgExportMap);
      dumpPackages(out, "import-package", pkgImportMap);

      out.print("  <bundle-filesize>");
      out.print(Long.toString(file.length()));
      out.println("</bundle-filesize>");
      

      out.println(" </bundle>");

    }

    void dumpPackages(PrintStream out, String tag, Map map) {
      for(Iterator it = map.keySet().iterator(); it.hasNext();) {
        String pkg  = (String)it.next();
        String spec = (String)map.get(pkg);
        boolean bSkip = false;
        if("import-package".equals(tag)) {
          String exportSpec = (String)pkgExportMap.get(pkg);
          if(exportSpec != null && spec.equals(exportSpec)) {
            // Skip import of exported packages
            bSkip = true;
          }
        }
        if(bSkip) {
          out.println("  <!-- skip import " + pkg + "; " + spec + " -->");
        } else {
          out.print("  <" + tag + " package=\"" + pkg + "\"");
          if("".equals(spec)) {
            out.println("/>");
          } else {
            out.println("\n                  specification-version=\"" + spec + "\"/>");
          }
        }
      }
    }
    
    void printAttrib(PrintStream out, String tag, String key) {
      String val = attribs.getValue(key);
      if(val == null) {
        val = "";
      }
      out.println("  <" + tag + ">" +  val + "</" + tag + ">");
    }
  }
}


