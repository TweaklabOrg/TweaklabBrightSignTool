package ch.tweaklab.player.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.tweaklab.player.configurator.UploadFile;

/**
 * Contains the media Files and the associated XML config file
 * @author Alain
 *
 */
public class MediaUploadData {

  private List<MediaFile> uploadList;
  private UploadFile configFile;
  private ModeType mode;


  public MediaUploadData(List<MediaFile> uploadList, UploadFile configFile, ModeType mode) {
    super();
    this.uploadList = uploadList;
    this.configFile = configFile;
    this.mode = mode;
  }
  
  public List<MediaFile> getUploadList() {
    return uploadList;
  }

  public UploadFile getConfigFile() {
    return configFile;
  }

  /**
   * @return the mode
   */
  public ModeType getMode() {
    return mode;
  }
  
  

}
