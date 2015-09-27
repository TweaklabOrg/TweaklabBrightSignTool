package ch.tweaklab.player.util;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

/**
 * Helper Class to discover Bonjour Devices in Network
 * @author Alain + Stephan
 *
 */
public class DiscoverServices {

  public static List<String> searchServices(String servicename) throws IOException {
    // TODO: Stephan: Only listens on one available network interface. If you have more than one, it chooses a random one.
    List<String> targets = new LinkedList<>();
    InetAddress addr = InetAddress.getLocalHost();
    String hostname = InetAddress.getByName(addr.getHostName()).toString();
    JmDNS jmdns = JmDNS.create(addr, hostname);
    jmdns.addServiceListener(servicename + "._tcp.local.", new SL(targets));
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    jmdns.close();
    return targets;
  }

  private static class SL implements ServiceListener {
    private List<String> targets;
    
    public SL(List<String> targets) {
      this.targets = targets;
    }
    
    @Override
    public void serviceAdded(ServiceEvent event) {
      targets.add(event.getName());
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
      targets.remove(event.getName());
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
      targets.add(event.getName());
    }
  }
}
