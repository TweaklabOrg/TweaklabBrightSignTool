package ch.tweaklab.ip6.connector;

import java.util.List;

import javafx.concurrent.Task;
import ch.tweaklab.ip6.model.MediaFile;

public abstract class Connector {

 protected Boolean isConnected = false;
 protected String hostname = "";
  
  public abstract boolean connect(String hostname) throws Exception; 
  
  public abstract Task<Boolean> getUploadMediaFilesTask(List<MediaFile> mediaFiles) throws Exception;

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
