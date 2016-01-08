package org.tweaklab.brightsigntool.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper Class to discover Bonjour Devices in Network
 * @author Alain + Stephan
 *
 */
public class DiscoverServices {

  public static List<String> searchServices(String servicename) throws IOException {
    // this solution uses the native terminal command. Luckily the command on windows AND Mac is the same.
    int timeout = 2000;
    String result = CommandlineTool.executeCommand("dns-sd -B " + servicename + " local", timeout);

    // Collect Service instance names.
    String[] splittedOutput = result.toString().split("( +)" + servicename + "._tcp.( +)");
    List<String> withoutFirst = Arrays.asList(splittedOutput).subList(1, splittedOutput.length); // first String would only be the introduction of the terminal output: Browsing for ...
    List<String> withTrimmedTail = withoutFirst.stream().map(e -> e.split("\n")[0]).collect(Collectors.toList());
    return withTrimmedTail;
  }
}
