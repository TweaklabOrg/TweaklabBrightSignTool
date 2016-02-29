package org.tweaklab.brightsigntool.connector;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.FileUtils;
import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.gui.controller.MainApp;
import org.tweaklab.brightsigntool.model.Keys;
import org.tweaklab.brightsigntool.model.MediaFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import org.tweaklab.brightsigntool.util.CommandlineTool;
import org.tweaklab.brightsigntool.util.OSValidator;

import javax.swing.filechooser.FileSystemView;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connects to a SD Card of Bright Sign Device
 *
 * @author Alain
 */
public class BrightSignSdCardConnector extends Connector {
  public static final String CLASS_DISPLAY_NAME = "BS SD-Card Connector";
  private static final Logger LOGGER = Logger.getLogger(BrightSignSdCardConnector.class.getName());
  private static final int TIMEOUT_DISKUTIL_IN_MILLIS = 1000;


  private String mediaFolderPath;

  public BrightSignSdCardConnector() {
    mediaFolderPath = Keys.loadProperty("default_mediaFolder");
  }

  @Override
  public boolean connect(String path) {
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

    return !isConnected();
  }

  @Override
  public Task<Boolean> upload(MediaUploadData uploadData, List<UploadFile> systemFiles) {
    return new Task<Boolean>() {
      private final Logger LOGGER = Logger.getLogger(getClass().getName());

      @Override
      public Boolean call() {
        // check, if there is enough space on target
        if (uploadData != null) {
          File targetRoot = new File(target);
          long totalSize = 0;
          for (MediaFile m : uploadData.getUploadList()) {
            totalSize += m.getFileSizeAsNumber();
          }
          if (targetRoot.getTotalSpace() < totalSize) {
            LOGGER.log(Level.WARNING, "Files are too big to fit on " + target + ". Nothing is copied.");
            updateMessage("Files are too big to fit on " + target + ". Nothing is copied.");
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "blabla", new ButtonType("bu"));
            alert.showAndWait().ifPresent(response -> {
              if (response == ButtonType.OK) {
//                formatSystem();
              }
            });
            return false;
          }
        }

        // writeSystemFiles
        for (UploadFile systemFile : systemFiles) {
          if (systemFile != null) {
            File targetFile = new File(target + "/" + systemFile.getFileName());
            // write file to root folder
            try {
              FileUtils.writeByteArrayToFile(targetFile, systemFile.getFileAsBytes());
            } catch (IOException e) {
              LOGGER.log(Level.WARNING, "Couldn't write to target!", e);
              updateMessage("Couldn't write to target!");
              return false;
            }
          }
        }

        // reset media folder on sd card
        if (!(target.endsWith("/") || target.endsWith("\\"))) {
          target = target + "/";
        }
        File mediaFolder = new File(target + "/" + mediaFolderPath);
        if (mediaFolder.exists()) {
          try {
            FileUtils.deleteDirectory(mediaFolder);
          } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Couldn't delete mediaFolder!", e);
            updateMessage("Couldn't delete mediaFolder!");
            return false;
          }
        }
        if (!mediaFolder.mkdir()) {
          LOGGER.warning("Wasn't able to create media folder.");
          updateMessage("Wasn't able to create media folder.");
          return false;
        }

        // TODO Some strange behaviour here. If for ex. display changes are made, the xml is handled via systemFiles. Is that part really needed?
        // copy xml config file
        if (uploadData != null && !copyOrReplaceFile(uploadData.getConfigFile(), target)) {
          return false;
        }

        // copy each mediafile
        if (uploadData != null) {
          for (MediaFile mediaFile : uploadData.getUploadList()) {
            if (this.isCancelled()) {
              return false;
            }
            if (mediaFile != null) {
              if (!copyOrReplaceFile(mediaFile.getFile(), mediaFolder.getPath())) {
                return false;
              }
            }
          }
        }

        LOGGER.info("Done uploading to SD.");
        return true;
      }
    };
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
                // TODO maybe not the same format on PCs?
                String path = null;//.replace(" ", "\\ ");
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
            LOGGER.info("Folder /Volumes not found.");
            MainApp.showInfoMessage("Folder /Volumes not found.");
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

  // TODO return success
  private boolean copyOrReplaceFile(File sourceFile, String destPath) {
    if (!destPath.endsWith("/")) {
      destPath = destPath + "/";
    }

    File destFile = new File(destPath + sourceFile.getName());

    if (destFile.exists()) {
      if (!destFile.delete()) {
        LOGGER.warning("" + destPath + " could not be deleted.");
        MainApp.showInfoMessage("" + destPath + " could not be deleted.");
        return false;
      }
    }

    try {
      if (!destFile.createNewFile()) {
        LOGGER.log(Level.WARNING, "Couldn't create " + destFile.getName());
        MainApp.showInfoMessage("" + destPath + " could bot be created.");
        return false;
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't create " + destFile.getName(), e);
      MainApp.showInfoMessage("" + destPath + " could bot be created.");
      return false;
    }

    FileChannel source;
    FileChannel destination;

    try {
      source = new FileInputStream(sourceFile).getChannel();
    } catch (FileNotFoundException e) {
      LOGGER.log(Level.WARNING, "Couldn't find " + sourceFile.getName(), e);
      return false;
    }
    try {
      destination = new FileOutputStream(destFile).getChannel();
    } catch (FileNotFoundException e) {
      LOGGER.log(Level.WARNING, "Couldn't find " + destFile.getName(), e);
      return false;
    }
    try {
      destination.transferFrom(source, 0, source.size());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't write data to " + destFile.getName(), e);
      return false;
    }

    try {
      source.close();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't close " + sourceFile.getName(), e);
    }

    try {
      destination.close();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't close " + destFile.getName(), e);
    }
    return true;
  }

  private boolean copyOrReplaceFile(UploadFile sourceFile, String destPath) {
    if (!destPath.endsWith("/")) {
      destPath = destPath + "/";
    }
    File destFile = new File(destPath + sourceFile.getFileName());
    if (destFile.exists()) {
      if (!destFile.delete()) {
        LOGGER.warning("" + destPath + " could not have been deleted.");
        MainApp.showInfoMessage("" + destPath + " could not have been deleted.");
        return false;
      }
    }
    try {
      FileUtils.writeByteArrayToFile(destFile, sourceFile.getFileAsBytes());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't write to " + destFile.getName(), e);
      return false;
    }
    return true;
  }
}
