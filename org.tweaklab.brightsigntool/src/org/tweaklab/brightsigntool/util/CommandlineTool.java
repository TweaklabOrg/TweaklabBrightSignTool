package org.tweaklab.brightsigntool.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Stephan on 17.10.15.
 */
public class CommandlineTool {
  private static final Logger LOGGER = Logger.getLogger(CommandlineTool.class.getName());

  /*
  * This method can also be used by non-self-termination commands. Specify an appropriate timeout.
  * It's ugly code as InputStreams of executing processes are blocking read() and readline()
  * calls when no data is available AND the process is still alive. I'm not even completely
  * sure if the process is allways killed, or if we actually create zombies. Anyway, it's the
  * only solution I found for now.
  */
  public static String executeCommand(List<String> command, int timeoutInMillis) {
    StringBuffer output = new StringBuffer();

    Searcher searcher = new Searcher(command, output);
    searcher.start();
    try {
      Thread.sleep(timeoutInMillis);
    } catch (InterruptedException e) {
    }

    searcher.stopSearching();

    return output.toString();
  }


  private static class Searcher extends Thread {
    boolean searching;
    List<String> command;
    StringBuffer output;
    Process process = null;

    public Searcher(List<String> command, StringBuffer output) {
      searching = true;
      this.command = command;
      this.output = output;
    }

    public void run() {
      searching = true;
      BufferedReader reader = null;
      ProcessBuilder p = new ProcessBuilder(command);
      try {
        process = p.start();
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
        process.destroyForcibly();
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

