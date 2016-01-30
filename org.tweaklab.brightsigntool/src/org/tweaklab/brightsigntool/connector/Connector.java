package org.tweaklab.brightsigntool.connector;

/**
 * Absctract Class for Connector Classes.
 * This methods are called in the GUI via reflection.
 * the current used connector class is stored in ApplicationData.java
 */

import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Map;

public abstract class Connector {

 public static final String CLASS_DISPLAY_NAME = "Abstract Connector (Field not overwritten)"; 
 protected Boolean isConnected = false;
 protected String target = "";
 protected String name = "";
 
 

 /**
  * connect to a device
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
  public abstract Task<Boolean> upload(MediaUploadData uploadData, List<UploadFile> systemFiles);


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
  

  public String getName() {
	return name;
}

  public abstract Task<List<String>> getPossibleTargets();

  public abstract Map<String, String> getSettingsOnDevice();
}
