package ch.tweaklab.player.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Stephan on 17.10.15.
 */
public class CommandlineTool {

  /*
   * This method should only be used with self-terminating commands where no timeout is needed.
   */
  public static String executeCommand(String command) {
    StringBuffer output = new StringBuffer();
    Process process = null;
    try {
      process = Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      // TODO Stephan: handle exception -> error message?
      e.printStackTrace();
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
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
      try {
        process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
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
