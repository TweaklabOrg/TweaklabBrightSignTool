package ch.tweaklab.player.util;

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
    // this solution uses the native terminal command. Luckily it works on windows AND Mac.
    String result = CommandlineTool.executeCommand("dns-sd -B " + "_ssh" + " local", 2000);

    // Collect Service instance names.
    String[] splittedOutput = result.toString().split("( +)" + "_ssh" + "._tcp.( +)");
    List<String> withoutFirst = Arrays.asList(splittedOutput).subList(1, splittedOutput.length);
    return withoutFirst.stream().map(e -> e.split("\n")[0]).collect(Collectors.toList());
  }
}
