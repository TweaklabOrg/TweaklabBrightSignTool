package ch.tweaklab.player.util;

import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceInfo;
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

  public static List<String> searchServices(String servicename, JmmDNS jmmdns) throws IOException {
    ServiceInfo[] services = jmmdns.list(servicename + "._tcp.local.");
    return Arrays.stream(services).map(e -> e.getName()).collect(Collectors.toList());
  }
}
