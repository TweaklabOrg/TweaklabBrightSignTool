package ch.tweaklab.player.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaUploadData {

  
  private PlayModusType playModus;
  
  private List<MediaFile> uploadList;
  private File configFile;
  
  
  public List<MediaFile> getUploadList() {
    return uploadList;
  }

  public File getConfigFile() {
    return configFile;
  }

  public PlayModusType getPlayModus() {
    return playModus;
  }

  public MediaUploadData(PlayModusType playModus, List<MediaFile> uploadList, File configFile) {
    super();
    this.playModus = playModus;
    this.uploadList = uploadList;
    this.configFile = configFile;
  }

  
  
  
}
