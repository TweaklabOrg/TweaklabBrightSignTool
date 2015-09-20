package ch.tweaklab.player.util;




import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

/**
 * Helper Class to discover Bonjour Devices in Network
 * @author Alain
 *
 */
public class DiscoverServices {

    static class SampleListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
            System.out.println("Service added   : " + event.getName() + "." + event.getType());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            System.out.println("Service removed : " + event.getName() + "." + event.getType());
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            System.out.println("Service resolved: " + event.getInfo());
        }
    }

    /**
     * the a bonjour service with a given service name
     * @param servicename
     * @throws InterruptedException 
     */
    
    public static void main(String[] args) throws InterruptedException{
      
      String type = "_bsp._tcp.local.";
    
  
          try {
            JmDNS  jmdns = JmDNS.create();
          
          ServiceListener listener;
          jmdns.addServiceListener(type, listener = new ServiceListener() {
            @Override
              public void serviceResolved(ServiceEvent ev) {
                  System.out.println("Service resolved: "
                           + ev.getInfo().getQualifiedName()
                           + " port:" + ev.getInfo().getPort());
              }
            @Override
              public void serviceRemoved(ServiceEvent ev) {
                System.out.println("Service removed: " + ev.getName());
              }
            @Override
              public void serviceAdded(ServiceEvent event) {
                  // Required to force serviceResolved to be called again
                  // (after the first search)
                System.out.println("added");
                  jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
              }
          });

          
          Thread.sleep(5000);
          System.out.println("finish");
          jmdns.removeServiceListener(type, listener);
      
            jmdns.close();
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
      
    }
    
    public void searchServices(String servicename){
        try {
          InetAddress addr = InetAddress.getLocalHost();
          String hostname = InetAddress.getByName(addr.getHostName()).toString();
          JmDNS jmdns = JmDNS.create(addr, hostname);
            jmdns.addServiceListener("_tl._tcp.local.", new SampleListener());

         
             System.out.println("Press q and Enter, to quit");
             int b;
             while ((b = System.in.read()) != -1 && (char) b != 'q') {
                 /* Stub */
             }
           
        
            jmdns.close();
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
