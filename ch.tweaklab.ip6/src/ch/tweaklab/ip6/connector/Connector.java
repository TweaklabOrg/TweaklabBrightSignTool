package ch.tweaklab.ip6.connector;

/**
 * Absctract Class for Connector Classes.
 * This methods are called in the GUI via reflection.
 * the current used connector class is stored in ApplicationData.java
 */
import java.io.File;
import java.util.List;

import javafx.concurrent.Task;
import ch.tweaklab.ip6.mediaLogic.MediaFile;

public abstract class Connector {

 protected Boolean isConnected = false;
 protected String target = "";

 /**
  * connect to a device
  * @param host
  * @return
  * @throws Exception
  */
  public abstract boolean connect(String target); 
  
 /**
  * create a Task which handles upload of files and doesnt block the GUI
  * @param mediaFiles --> a list of files to upload
  * @param configFile --> an xml config file which contains the logig for the player
  * @return
  * @throws Exception
  */
  public abstract Task<Boolean> uploadMediaFiles(List<MediaFile> mediaFiles, File configFile) throws Exception;

  /**
   * Check if Device is currently connected
   * @return
   */
  public Boolean getIsConnected() {
    return isConnected;
  }

  /**
   * get the current host
   * @return
   */
  public String getTarget() {
    return target;
  }

  public abstract Task<List<String>> getPossibleTargets();


  

  
}
