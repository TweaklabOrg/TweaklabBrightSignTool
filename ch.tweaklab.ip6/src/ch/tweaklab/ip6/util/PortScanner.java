package ch.tweaklab.ip6.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
 * Scans the local net for a open specified port
 * @author Alf
 *
 */
public class PortScanner {

  final int timeout = 200;
  public PortScanner() {

  }

 public List<String> getAllIpWithOpenPortInLocalSubnet(int port){
   List<String> foundIps = new ArrayList<>();
   try{

   //get prefix of local subnet
   InetAddress localHost = Inet4Address.getLocalHost();
   NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
   InetAddress inetAdress = networkInterface.getInterfaceAddresses().get(0).getAddress();
   String localIpStart = inetAdress.getHostAddress().split("\\.([^.]*)$")[0] + ".";
   
   String targetIp = null;
   
   //Create Threads
   final ExecutorService es = Executors.newFixedThreadPool(200);
   final List<Future<String>> taskList = new ArrayList<>();
   for (int ipEnd = 2; ipEnd <= 254; ipEnd++) {
     targetIp = localIpStart + ipEnd;
     taskList.add(checkPortTask(es, targetIp, port, timeout));
   }
   es.shutdown();
   
   //Get found IP's
   for (final Future<String> f : taskList) {
     String foundIp = f.get();
     if (foundIp != "") {
       foundIps.add(foundIp);
     }
   }
   }
   catch(InterruptedException | ExecutionException | UnknownHostException | SocketException e){
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
