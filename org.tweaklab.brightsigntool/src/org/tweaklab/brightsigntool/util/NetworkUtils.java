package org.tweaklab.brightsigntool.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * Contains some helper methods for network scanning
 * @author Alain + Stephan
 *
 */
public class NetworkUtils {
  private static final Logger LOGGER = Logger.getLogger(NetworkUtils.class.getName());
  /**
   * resolve a hostname to an ip adress. 
   * @param hostname
   * @return ip adress or null
   */
  public static String resolveHostName(String hostname) {
    String ip = "";
    InetAddress address = null;
    try {
      address = InetAddress.getByName(hostname);
    } catch (UnknownHostException e) {
      LOGGER.warning("Unknown host: " + hostname);
    }
    ip = address.getHostAddress();
    return ip;
  }
}
