package ch.tweaklab.player.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static java.nio.file.StandardCopyOption.*;
import javafx.concurrent.Task;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;

import ch.tweaklab.player.gui.controller.MainApp;
import ch.tweaklab.player.model.Keys;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.util.OSValidator;

/**
 * Connects to a SD Card of Bright Sign Device
 * 
 * @author Alain
 *
 */
public class BrightSignSdCardConnector extends Connector {

  public static final String CLASS_DISPLAY_NAME = "BS SC-Card Connector";

  private String mediaFolderPath;

  public BrightSignSdCardConnector() {

    mediaFolderPath = Keys.loadProperty("default_mediaFolder");

  }

  @Override
  public boolean connect(String path) {
    if (isPathValid(path)) {
      this.target = path;
      this.isConnected = true;
    } else {
      this.isConnected = false;
    }

    return isConnected;
  }

  private Boolean isPathValid(String path) {
    // TODO: zzAlain check if path is valid

    return true;
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
            // TODO: Stephan: parse .-files
            // TODO: Stephan: only add removeable disks
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

  @Override
  public boolean disconnect() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Task<Boolean> upload(MediaUploadData uploadData,List<File> systemFiles) throws Exception {
    // if (OSValidator.isWindows()) {
    Task<Boolean> uploadTask = new Task<Boolean>() {
      Boolean success;

      @Override
      public Boolean call() throws Exception {
        success = true;
        
        // writeSystemFiles
        for (File systemFile : systemFiles) {
          if (systemFile != null) {
            File targetFile = new File(target + "/" + systemFile.getName());
            // write file to root folder
            FileUtils.copyFile(systemFile, targetFile);
          }

        }
        
        // reset media folder on sd card
        if ((target.endsWith("/") || target.endsWith("\\")) == false) {
          target = target + "/";
        }
        File mediaFolder = new File(target + "/" + mediaFolderPath);
        if (mediaFolder.exists()) {
          FileUtils.deleteDirectory(mediaFolder);
        }
        mediaFolder.mkdir();

        // copy xml config file
        copyOrReplaceFile(uploadData.getConfigFile(), target);

        // copy each mediafile
        for (MediaFile mediaFile : uploadData.getUploadList()) {
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
    // } else {
    // return null;
    // }
  }



}
