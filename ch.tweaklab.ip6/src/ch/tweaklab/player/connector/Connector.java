package ch.tweaklab.player.connector;

/**
 * Absctract Class for Connector Classes.
 * This methods are called in the GUI via reflection.
 * the current used connector class is stored in ApplicationData.java
 */
import java.io.File;
import java.util.List;

import javafx.concurrent.Task;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.model.MediaUploadData;

public abstract class Connector {

 public static final String CLASS_DISPLAY_NAME = "Abstract Connector (Field not overwritten)"; 
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
   * disconnect from current device
   * @return
   */
  public abstract boolean disconnect();
  
 /**
  * 
  * 
  * @param uploadData
  * @return
  * @throws Exception
  */
  public abstract Task<Boolean> upload(MediaUploadData uploadData, List<File> systemFiles) throws Exception;


  /**
   * Check if Device is currently connected
   * @return
   */
  public Boolean isConnected() {
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
