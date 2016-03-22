package org.tweaklab.brightsigntool.connector;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import org.tweaklab.brightsigntool.util.CommandlineTool;
import org.tweaklab.brightsigntool.util.OSValidator;

import javax.swing.filechooser.FileSystemView;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Connects to a SD Card of Bright Sign Device
 *
 * @author Alain + Stephan
 */
public class BrightSignSdCardConnector extends Connector {
  public static final String CLASS_DISPLAY_NAME = "BS SD-Card Connector";
  private static final Logger LOGGER = Logger.getLogger(BrightSignSdCardConnector.class.getName());
  private static final int TIMEOUT_DISKUTIL_IN_MILLIS = 1000;
  private static final Pattern FAT32_PATTERN = Pattern.compile("[\\s\\S]*File System Personality:( *)MS-DOS FAT32[\\s\\S]*");
  private static final Pattern HFS_PATTERN = Pattern.compile("[\\s\\S]*File System Personality:( *)HFS+[\\s\\S]*");;
  private static final Pattern J_HFS_PATTERN = Pattern.compile("[\\s\\S]*File System Personality:( *)Journaled HFS+[\\s\\S]*");;
  private static final Pattern CS_J_HFS_PATTERN = Pattern.compile("[\\s\\S]*File System Personality:( *)Case-sensitive Journaled HFS+[\\s\\S]*");;
  private static final Pattern NTFS_PATTERN = Pattern.compile("[\\s\\S]*File System Personality:( *)NTFS[\\s\\S]*");;

  private static final String HFS_MESSAGE = "SD is formatted to HFS+. " +
          "This is recommended if you work with files bigger than 4GB but programming " +
          "device remotely is no supported by BrightSign manufacture.";
  private static final String NTFS_MESSAGE = "SD is formatted to NTFS and can't be used on a Mac. " +
          "Recommended format is FAT32. If you use files bigger than 4GB, use HFS+ (Mac only) or NTFS (Windows only).";
  private static final String UNSUPPORTED_FS_MESSAGE = "SD file system is not supported. " +
          "Recommended format is FAT32. If you use files bigger than 4GB, use HFS+ (Mac only) or NTFS (Windows only).";

  private FileSystemFormat fileSystemFormat;

  public BrightSignSdCardConnector() {
    fileSystemFormat = FileSystemFormat.UNKNOWN;
  }

  @Override
  public boolean connect(String path) {
    // Find out if file system has a valid format concerning platform and store format if valid.
    if (OSValidator.isMac()) {
      final List<String> command = new LinkedList<>();
      command.add("diskutil");
      command.add("info");
      command.add(path);
      final String result = CommandlineTool.executeCommand(command, 1000);

      if (FAT32_PATTERN.matcher(result).matches()) {
        fileSystemFormat = FileSystemFormat.FAT_32;
      } else if (J_HFS_PATTERN.matcher(result).matches() || CS_J_HFS_PATTERN.matcher(result).matches() ||
              HFS_PATTERN.matcher(result).matches()) {
        fileSystemFormat = FileSystemFormat.HFS_PLUS;
        new Alert(Alert.AlertType.NONE, HFS_MESSAGE, ButtonType.OK).showAndWait();
      } else if (NTFS_PATTERN.matcher(result).matches()) {
        fileSystemFormat = FileSystemFormat.NTFS;
        new Alert(Alert.AlertType.NONE, NTFS_MESSAGE, ButtonType.OK).showAndWait();
        name = "";
        target = "";
        return false;
      } else {
        new Alert(Alert.AlertType.NONE, UNSUPPORTED_FS_MESSAGE, ButtonType.OK).showAndWait();
        name = "";
        target = "";
        return false;
      }
    } else if (OSValidator.isWindows()){
      // TODO Windows support of SD card format evaluation.
    }
    
    // set path first, as we need it to be set for the isConnected method.
    name = path;
    target = path;
    if (isConnected()) {
      LOGGER.info("SD Card " + path + " was connected.");
      return true;
    } else {
      name = "";
      target = "";
      return false;
    }
  }

  @Override
  public boolean disconnect() {
    this.name = "";
    String target = this.target;
    this.target = "";

    if (OSValidator.isMac()) {
      List<String> command = new LinkedList<>();
      command.add("diskutil");
      command.add("unmount");
      command.add(target);
      CommandlineTool.executeCommand(command, TIMEOUT_DISKUTIL_IN_MILLIS);
    }

    LOGGER.info("SD Card " + target + " was disconnected.");

    fileSystemFormat = FileSystemFormat.UNKNOWN;

    return !isConnected();
  }

  @Override
  public Task<Boolean> upload(MediaUploadData uploadData, List<UploadFile> systemFiles) {
    return new SdUploadTask(uploadData, systemFiles, target, fileSystemFormat);
  }

  @Override
  public Boolean isConnected() {
    File target = new File(this.target);
    return target.exists();
  }

  @Override
  public Task<List<String>> getPossibleTargets() {
    return new Task<List<String>>() {
      @Override
      public List<String> call() throws Exception {
        List<String> targetList = new ArrayList<>();

        if (OSValidator.isWindows()) {
          File[] paths;
          FileSystemView fsv = FileSystemView.getFileSystemView();

          paths = File.listRoots();
          for (File path : paths) {
            String description = fsv.getSystemTypeDescription(path);
            if (description.equals("Removable Disk") || description.equals("Wechseldatenträger")) {
              targetList.add(path.getAbsolutePath());
            }
          }

        } else if (OSValidator.isMac()) {
          File volumes = new File("/Volumes");
          File files[] = volumes.listFiles();
          if (files != null) {
            for (File f : files) {
              if (f.getName().charAt(0) != '.') {
                String path = null;
                try {
                  path = f.getCanonicalPath();
                } catch (IOException e) {
                  LOGGER.log(Level.WARNING, "Couldn't get path of " + f.getName(), e);
                }

                List<String> command = new LinkedList<>();
                command.add("diskutil");
                command.add("info");
                command.add(path);

                String volumeInfo = CommandlineTool.executeCommand(command, TIMEOUT_DISKUTIL_IN_MILLIS);
                // [\\s\\S] matches all chars, even \n, ...
                if (volumeInfo.matches("[\\s\\S]*Ejectable:( *)Yes[\\s\\S]*")
                        || volumeInfo.matches("[\\s\\S]*Removable Media:( *)Yes[\\s\\S]*")) {
                  targetList.add(f.getAbsolutePath());
                }
              }
            }
          } else {
            LOGGER.info("Folder " + volumes.getPath() + " not found.(?!)");
            updateMessage("Folder " + volumes.getPath() + " not found.(?!)");
          }
        }

        LOGGER.info("Done searching for targets with " + targetList.size() + " results.");

        return targetList;
      }
    };
  }

  @Override
  public Map<String, String> getSettingsOnDevice() {
    HashMap<String, String> result = new HashMap<>();

    // get a document builder
    DocumentBuilder builder = null;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      LOGGER.log(Level.SEVERE, "Not able to get DocumentBuilder?", e);
    }

    InputStream settingsFile = null;
    InputStream displayFile = null;
    try {
      settingsFile = new FileInputStream(new File(this.target + "/settings.xml"));
    } catch (IOException e) {
      LOGGER.log(Level.INFO, "No settings.xml on SD.");
    }
    try {
      displayFile = new FileInputStream(new File(this.target + "/display.xml"));
    } catch (IOException e) {
      LOGGER.log(Level.INFO, "No display.xml on SD.");
    }

//    collectMode(modeFile, result, builder);
    if (settingsFile != null) {
      collectEntries(settingsFile, result, builder);
    }
    if (displayFile != null) {
      collectEntries(displayFile, result, builder);
    }
    // TODO: building filemanagement to make that possible. For ex. skip mediaupload of already existing files, but allow modifications on settings.
//    collectEntries("gpio.xml", result, builder);
//    collectPlaylist(result, builder);
    if (settingsFile != null || displayFile != null) {
      LOGGER.info("Collectable settings loaded from SD.");
    } else {
      LOGGER.info("No collectabel settings found on SD.");
    }

    return result;
  }

  @Override
  public boolean isResolutionSupported(String brightSignResolutionString) {
    // not able to verify
    return true;
  }


}
