package org.tweaklab.brightsigntool.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Contains some helper methods for network scanning
 * @author Alain
 *
 */
public class NetworkUtils {

  public static void main(String[] args) {

  }

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

      ip = address.getHostAddress();
    } catch (UnknownHostException e) {

    }
    return ip;
  }

  /**
   * Scan all ip adresses in same subnet as network adapter for the given port
   * @param port
   * @return
   */
  public static List<String> getAllIpWithOpenPortInLocalSubnet(int port) {
    final int timeout = 200;
    List<String> foundIps = new ArrayList<>();
    try {

      // get prefix of local subnet
      InetAddress localHost = Inet4Address.getLocalHost();
      NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);

      for (InterfaceAddress interFaceAdress : networkInterface.getInterfaceAddresses()) {
        InetAddress inetAdress = interFaceAdress.getAddress();
        String localIpStart = inetAdress.getHostAddress().split("\\.([^.]*)$")[0] + ".";

        String targetIp = null;

        // Create Threads
        final ExecutorService es = Executors.newFixedThreadPool(200);
        final List<Future<String>> taskList = new ArrayList<>();
        for (int ipEnd = 2; ipEnd <= 254; ipEnd++) {
          targetIp = localIpStart + ipEnd;
          taskList.add(checkPortTask(es, targetIp, port, timeout));
        }
        es.shutdown();

        // Get found IP's
        for (final Future<String> f : taskList) {
          String foundIp = f.get();
          if (!foundIp.equals("")) {
            foundIps.add(foundIp);
          }
        }
      }
    } catch (InterruptedException | ExecutionException | UnknownHostException | SocketException e) {
      e.printStackTrace();
    }
    return foundIps;
  }

  public static Future<String> checkPortTask(final ExecutorService es, final String ip, final int port, final int timeout) {
    return es.submit(new Callable<String>() {
      @Override
      public String call() {
        try {
          Socket socket = new Socket();
          socket.connect(new InetSocketAddress(ip, port), timeout);
          socket.close();
          return ip;
        } catch (Exception ex) {
          return "";
        }
      }
    });
  }
}
