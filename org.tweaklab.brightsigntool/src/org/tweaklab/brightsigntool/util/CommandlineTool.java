package org.tweaklab.brightsigntool.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by Stephan on 17.10.15.
 */
public class CommandlineTool {

  /*
   * This method should only be used with self-terminating commands where no timeout is needed.
   */
  public static String executeCommand(List<String> command) {
    StringBuffer output = new StringBuffer();
    Process process = null;
    ProcessBuilder p = new ProcessBuilder(command);
    try {
      process = p.start();
    } catch (IOException e) {
      // TODO Stephan: handle exception -> error message?
      e.printStackTrace();
    }

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "US-ASCII"));
    } catch (UnsupportedEncodingException e){
      e.printStackTrace();
      return "";
    } catch (NullPointerException e) {
      e.printStackTrace();
      return "";
    }

    try {
      String line = reader.readLine();
      while (line != null) {
        output.append(line + "\n");
        line = reader.readLine();
      }
    } catch (IOException e) {
      // TODO Stephan: handle exception -> error message?
      e.printStackTrace();
    }

    try {
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return output.toString();
  }

  /*
   * This method can also be used by non-self-termination commands. Specify an appropriate timeout.
   */
  public static String executeCommand(String command, int timeoutInMillis) {
    StringBuffer output = new StringBuffer();

    Searcher searcher = new Searcher(command, output);
    searcher.start();
    try {
      Thread.sleep(timeoutInMillis);
    } catch (InterruptedException e) {
      // nothing to handle. If Thread intterupts, it might not have found any results, and test might be repeated.
    }
    searcher.stopSearching();

    return output.toString();
  }

  private static class Searcher extends Thread {
    boolean searching;
    String command;
    StringBuffer output;
    Process process = null;

    public Searcher(String command, StringBuffer output) {
      searching = true;
      this.command = command;
      this.output = output;
    }

    public void run() {
      searching = true;
      BufferedReader reader = null;
      try {
        process = Runtime.getRuntime().exec(command);
        reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "US-ASCII"));
        while (searching) {
          if (reader.ready()) {
            String line = reader.readLine();
            if (streamEndReached(line)) {
              searching = false;
            } else {
              output.append(line + "\n");
            }
          } else {
            Thread.sleep(100); // if buffer is empty, poll 10 times a second.
          }
        }
      } catch (Exception e) {
        // TODO Stephan: handle exception -> error message?
        e.printStackTrace();
      } finally {
        try {
          reader.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    public void stopSearching() {
      searching = false;
    }

    private boolean streamEndReached(String nextLine) {
      return nextLine == null;
    }
  }
}
