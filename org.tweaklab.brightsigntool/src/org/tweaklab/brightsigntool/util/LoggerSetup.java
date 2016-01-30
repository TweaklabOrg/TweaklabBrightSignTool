package org.tweaklab.brightsigntool.util;

import java.io.IOException;
import java.util.logging.*;

/**
 * Created by Stephan on 19.01.16.
 */
public class LoggerSetup {
  private static final int MAX_SIZE = 100000;
  private static final int MAX_FILES = 20;

  public static void setup() {
    Logger root = Logger.getLogger("");

    // remove default handlers
    Handler[] handlers = root.getHandlers();
    for(Handler handler : handlers) {
      root.removeHandler(handler);
    }

    // log to console
    root.addHandler(new ConsoleHandler());

    // log to log files in systems temporary folder
    FileHandler fh = null;
    try {
      fh = new FileHandler("%t/tweaklabBrightSignTool_log_%g_%u.txt", MAX_SIZE, MAX_FILES);
    } catch (IOException e) {
      e.printStackTrace();
    }
    fh.setFormatter(new SimpleFormatter());
    root.addHandler(fh);

    // set default level
    root.setLevel(Level.INFO);
  }
}
