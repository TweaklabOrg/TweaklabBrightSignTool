package org.tweaklab.brightsigntool.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper Class to discover Bonjour Devices in Network
 * @author Alain + Stephan
 *
 */
public class DiscoverServices {
  private static final int TIMEOUT = 2000;

  public static List<String> searchServices(String servicename) {
    // this solution uses the native terminal command. Luckily the command on windows AND Mac is the same.
    List<String> command = new LinkedList<>();
    command.add("dns-sd");
    command.add("-B");
    command.add(servicename);
    command.add("local");
    String result = CommandlineTool.executeCommand(command, TIMEOUT);

    // Collect Service instance names.
    String[] splittedOutput = result.toString().split("( +)" + servicename + "._tcp.( +)");
    List<String> withoutFirst = Arrays.asList(splittedOutput).subList(1, splittedOutput.length); // first String would only be the introduction of the terminal output: Browsing for ...
    List<String> withTrimmedTail = withoutFirst.stream().map(e -> e.split("\n")[0]).collect(Collectors.toList());
    return withTrimmedTail;
  }
}
