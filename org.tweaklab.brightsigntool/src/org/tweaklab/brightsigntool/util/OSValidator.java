package org.tweaklab.brightsigntool.util;

public class OSValidator {
 
  private static String OS = System.getProperty("os.name").toLowerCase();
 
  public static boolean isWindows() {
    return (OS.indexOf("win") >= 0);
 
  }
 
  public static boolean isMac() {
    return (OS.indexOf("mac") >= 0);
 
  }
}