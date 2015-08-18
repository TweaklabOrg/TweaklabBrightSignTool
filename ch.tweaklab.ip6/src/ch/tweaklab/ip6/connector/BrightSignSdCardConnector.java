package ch.tweaklab.ip6.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import javafx.concurrent.Task;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;

import ch.tweaklab.ip6.gui.controller.MainApp;
import ch.tweaklab.ip6.media.MediaFile;
import ch.tweaklab.ip6.media.XMLConfigCreator;
import ch.tweaklab.ip6.util.OSValidator;
import ch.tweaklab.ip6.util.PortScanner;

/**
 * Connects to a SD Card of Bright Sign Device
 * 
 * @author Alf
 *
 */
public class BrightSignSdCardConnector extends Connector {

  private String mediaFolderPath;
  private Properties configFile;

  public BrightSignSdCardConnector() {

    configFile = new Properties();
    try {
      configFile.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
      mediaFolderPath = configFile.getProperty("mediaFolder");

    } catch (IOException e) {
      MainApp.showExceptionMessage(e);
    }

  }

  @Override
  public boolean connect(String path) {
    this.target = path;
    this.isConnected = true;
    return isConnected;
  }

  @Override
  public Task<Boolean> uploadMediaFiles(List<MediaFile> mediaFiles, File configFile) throws Exception {
 //   if (OSValidator.isWindows()) {
      Task<Boolean> uploadTask = new Task<Boolean>() {
        Boolean success;

        @Override
        public Boolean call() throws Exception {
          success = true;

          // reset media folder on sd card
          if ((target.endsWith("/") || target.endsWith("\\")) == false) {
            target = target + "/";
          }
          File mediaFolder = new File(target + mediaFolderPath);
          if (mediaFolder.exists()) {
            FileUtils.deleteDirectory(mediaFolder);
          }
          mediaFolder.mkdir();

          // copy xml config file
          copyOrReplaceFile(configFile, mediaFolder.getPath());

          // copy each mediafile
          for (MediaFile mediaFile : mediaFiles) {
            if (this.isCancelled()) {
              return false;
            }
            if (mediaFile != null) {
              copyOrReplaceFile(mediaFile.getFile(), mediaFolder.getPath());
            }
          }
          return success;
        }
      };
      return uploadTask;
//    } else {
//      return null;
//    }
  }

  @Override
  public Task<List<String>> getPossibleTargets() {

    Task<List<String>> getTargetTask = new Task<List<String>>() {
      @Override
      public List<String> call() throws Exception {
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
    };
    return getTargetTask;
  }

  private void copyOrReplaceFile(File sourceFile, String destPath) throws Exception {
    if (!destPath.endsWith("/")) {
      destPath = destPath + "/";
    }
    File destFile = new File(destPath + sourceFile.getName());
    if (destFile.exists()) {
      destFile.delete();
    }
    destFile.createNewFile();

    FileChannel source = null;
    FileChannel destination = null;

    source = new FileInputStream(sourceFile).getChannel();
    destination = new FileOutputStream(destFile).getChannel();
    destination.transferFrom(source, 0, source.size());

    if (source != null) {
      source.close();
    }
    if (destination != null) {
      destination.close();
    }
  }

}
