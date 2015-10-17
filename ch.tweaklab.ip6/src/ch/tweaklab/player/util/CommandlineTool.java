package ch.tweaklab.player.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by Stephan on 17.10.15.
 */
public class CommandlineTool {

  /*
   * This method should only be used with self-terminating commands where no timeout is needed.
   */
  public static String executeCommand(String command) {
    return executeCommand(command, 0);
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
      e.printStackTrace();
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
      while(searching) {
        try {
          process = Runtime.getRuntime().exec(command);
          BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
          String line = "";
          while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    public void stopSearching() {
      // Destroy must be called, as Searcher might be blocked waiting for more output from the InputStream.
      // Destroying the process, interrupts this blockage.
      // TODO Stephan: unfortunaltely I couldn't find a way to interrupt reader.readLine() method. Right now the App crashes when it should be shut down, and each search ceates a new thread-zombee
      process.destroyForcibly();
      searching = false;
    }
  }
}
