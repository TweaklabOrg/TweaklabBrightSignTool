package ch.tweaklab.player.configurator;

/**
 * Contains a file to be uploaded to the bright sign player
 * @author Alain
 *
 */
public class UploadFile {
  private final String fileName;
  private final byte[] fileAsBytes;

  public UploadFile(String fileName, byte[] fileAsBytes) {
    this.fileName = fileName;
    this.fileAsBytes = fileAsBytes.clone();
  }

  public String getFileName() {
    return fileName;
  }

  public byte[] getFileAsBytes() {
    return fileAsBytes.clone();
  }

}
