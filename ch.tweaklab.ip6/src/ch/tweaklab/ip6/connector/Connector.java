package ch.tweaklab.ip6.connector;

import java.io.File;
import java.util.List;

public abstract class Connector {

 protected Boolean isConnected;
 protected String hostname;
  
  public abstract boolean connect(String hostname);
  
  public abstract boolean uploadMediaFiles(List<File> files);

  public Boolean getIsConnected() {
    return isConnected;
  }

  public void setIsConnected(Boolean isConnected) {
    this.isConnected = isConnected;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }
  

  
}
