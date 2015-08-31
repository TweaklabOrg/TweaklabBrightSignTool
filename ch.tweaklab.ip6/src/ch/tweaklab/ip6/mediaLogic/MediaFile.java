package ch.tweaklab.ip6.mediaLogic;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.regex.Pattern;

public class MediaFile {

  private File file;
  private int displayTime;
  private MediaType mediaType;
  private String fileSize;

  public MediaFile(File file) {
    this.file = file;
    displayTime = 0;
    calculateFileSize();
    generateMediaTypeSpecificValues();

  }

  private void calculateFileSize() {

    double size = file.length();
    if (size <= 1024) {
      fileSize = new DecimalFormat("#.##").format(size);
      fileSize = fileSize + " b";
      return;
    }
    size = size / 1024;
    if (size <= 1024) {
      fileSize = new DecimalFormat("#.##").format(size);
      fileSize = fileSize + " kb";
      return;
    }
    size = size / 1024;
    fileSize = new DecimalFormat("#.##").format(size);
    fileSize = fileSize + " mb";
  }

  private void generateMediaTypeSpecificValues() {

    Properties configFile = new Properties();
    try {
      configFile.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
      String image_regex_pattern = configFile.getProperty("image_regex");
      String audio_regex_pattern = configFile.getProperty("audio_regex");
      String video_regex_pattern = configFile.getProperty("video_regex");

      String fileName = file.getName().replace(" ", "+");
      if (Pattern.matches(image_regex_pattern, fileName)) {
        this.mediaType = MediaType.IMAGE;
      } else if (Pattern.matches(video_regex_pattern, fileName)) {
        this.mediaType = MediaType.VIDEO;
      } else if (Pattern.matches(audio_regex_pattern, fileName)) {
        this.mediaType = MediaType.AUDIO;
      } else {
        this.mediaType = MediaType.UNKNOWN;
      }

    } catch (IOException e) {
      this.mediaType = MediaType.UNKNOWN;
    }

  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public int getDisplayTime() {
    return displayTime;
  }

  public void setDisplayTime(int displayTime) {
    this.displayTime = displayTime;
  }

  public String getFileSize() {
    return fileSize;
  }

  public MediaType getMediaType() {
    return mediaType;
  }

  @Override
  public String toString() {
    return this.getFile().getName();
  }

}
