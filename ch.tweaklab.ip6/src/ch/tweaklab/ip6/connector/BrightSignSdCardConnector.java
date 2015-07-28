package ch.tweaklab.ip6.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;

import javax.swing.filechooser.FileSystemView;

import ch.tweaklab.ip6.model.MediaFile;
import ch.tweaklab.ip6.util.OSValidator;

/**
 * Connects to a SD Card of Bright Sign Device
 * 
 * @author Alf
 *
 */
public class BrightSignSdCardConnector extends Connector {

  public BrightSignSdCardConnector() {

  }

  @Override
  public boolean connect(String path) {
    this.target = path;
    this.isConnected = true;
    return isConnected;
  }

  @Override
  public Task<Boolean> uploadMediaFiles(List<MediaFile> mediaFiles) throws Exception {
    Task<Boolean> uploadTask = new Task<Boolean>() {
      Boolean success;

      @Override
      public Boolean call() throws Exception {
        success = true;
        Boolean returnValue;

        for (MediaFile m : mediaFiles) {
          if (this.isCancelled()) {
            return false;
          }
          try {
            returnValue = replaceFile(m.getFile(), target + "/");
            if (returnValue == false) {
              success = false;
            }
          } catch (Exception e) {
            success = false;
          }
        }
        return success;
      }
    };
    return uploadTask;
  }

  @Override
  public List<String> getPossibleTargets() {
    List<String> targetList = new ArrayList<String>();

    if (OSValidator.isWindows()) {
      File[] paths;
      FileSystemView fsv = FileSystemView.getFileSystemView();

      paths = File.listRoots();
      for (File path : paths) {
        String description = fsv.getSystemTypeDescription(path);
        if (description.equals("Removable Disk")) {
          targetList.add(path.getAbsolutePath());
        }
      }

    } else if (OSValidator.isMac()) {
      File volumes = new File("/Volumes");
      File files[] = volumes.listFiles();
      for (File f : files) {
        targetList.add(f.getAbsolutePath());
      }
    }

    return targetList;
  }

  private Boolean replaceFile(File sourceFile, String destPath) throws IOException {
    if (!destPath.endsWith("/")) {
      destPath = destPath + "/";
    }
    File destFile = new File(destPath + sourceFile.getName());
    if (!destFile.exists()) {
      destFile.delete();
      destFile.createNewFile();
    }

    FileChannel source = null;
    FileChannel destination = null;

    try {
      source = new FileInputStream(sourceFile).getChannel();
      destination = new FileOutputStream(destFile).getChannel();
      destination.transferFrom(source, 0, source.size());
    } catch (Exception e) {
      return false;
    } finally {
    }
    if (source != null) {
      source.close();
    }
    if (destination != null) {
      destination.close();
    }

    return true;
  }

}
