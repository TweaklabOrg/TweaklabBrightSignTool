package ch.tweaklab.player.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the media Files and the associated XML config file
 * @author Alain
 *
 */
public class MediaUploadData {

  private List<MediaFile> uploadList;
  private File configFile;

  public List<MediaFile> getUploadList() {
    return uploadList;
  }

  public File getConfigFile() {
    return configFile;
  }

  public MediaUploadData(List<MediaFile> uploadList, File configFile) {
    super();
    this.uploadList = uploadList;
    this.configFile = configFile;
  }

}
