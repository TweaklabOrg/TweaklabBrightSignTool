package org.tweaklab.brightsigntool.connector;

import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.gui.controller.MainApp;
import org.tweaklab.brightsigntool.model.Keys;
import org.tweaklab.brightsigntool.model.MediaFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import org.tweaklab.brightsigntool.util.CommandlineTool;
import org.tweaklab.brightsigntool.util.OSValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

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
 *
 *
 */
public class BrightSignSdCardConnector extends Connector {
  private static final Logger LOGGER = Logger.getLogger(BrightSignSdCardConnector.class.getName());

  public static final String CLASS_DISPLAY_NAME = "BS SD-Card Connector";
  private static final int TIMEOUT_DISKUTIL_IN_MILLIS = 1000;


  private String mediaFolderPath;

  public BrightSignSdCardConnector() {
    mediaFolderPath = Keys.loadProperty("default_mediaFolder");
  }

  @Override
  public boolean connect(String path) {
    if (isPathValid(path)) {
    	this.name = path;
      this.target = path;
      this.isConnected = true;
    } else {
      this.isConnected = false;
    }

    LOGGER.info("SD Card " + path + " was connected.");

    return isConnected;
  }

  private Boolean isPathValid(String path) {
    // TODO: zzAlain check if path is valid

    return true;
  }



  @Override
  public boolean disconnect() {
    this.name = "";
    String target = this.target;
    this.target = "";
    this.isConnected = false;

    LOGGER.info("SD Card " + target + " was disconnected.");

    return !this.isConnected;
  }

  @Override
  public Task<Boolean> upload(MediaUploadData uploadData, List<UploadFile> systemFiles) {
    Task<Boolean> uploadTask = new Task<Boolean>() {
      private final Logger LOGGER = Logger.getLogger(getClass().getName());

      @Override
      public Boolean call() throws Exception{

        // writeSystemFiles
        for (UploadFile systemFile : systemFiles) {
          if (systemFile != null) {
            File targetFile = new File(target + "/" + systemFile.getFileName());
            // write file to root folder
            try {
              FileUtils.writeByteArrayToFile(targetFile, systemFile.getFileAsBytes());
            } catch (IOException e) {
              LOGGER.log(Level.WARNING, "Couldn't write to target!", e);
              return false;
            }
          }
        }

        // reset media folder on sd card
        if ((target.endsWith("/") || target.endsWith("\\")) == false) {
          target = target + "/";
        }
        File mediaFolder = new File(target + "/" + mediaFolderPath);
        if (mediaFolder.exists()) {
          try {
            FileUtils.deleteDirectory(mediaFolder);
          } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Couldn't delete mediaFolder!", e);
            return false;
          }
        }
        if (!mediaFolder.mkdir()) {
          LOGGER.warning("Wasn't able to create media folder.");
          MainApp.showInfoMessage("Wasn't able to create media folder.");
          return false;
        }

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
        LOGGER.info("Done uploading to SD.");
        return true;
      }

    };
    return uploadTask;
  }

  @Override
  public Task<List<String>> getPossibleTargets() {

    Task<List<String>> getTargetTask = new Task<List<String>>() {
      @Override
      public List<String> call() throws Exception{
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
    return getTargetTask;
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

    collectMode(result, builder);
    collectEntries("settings.xml", result, builder);
    collectEntries("display.xml", result, builder);
    // TODO: building filemanagement to make that possible. For ex. skip mediaupload of already existing files, but allow modifications on settings.
//    collectEntries("gpio.xml", result, builder);
//    collectPlaylist(result, builder);

    LOGGER.info("Collectable settings loaded from SD.");

    return result;
  }

  @Override
  public boolean isResolutionSupported(String brightSignResolutionString) {
    // not able to verify
    return true;
  }

  private void collectEntries(String file, Map<String, String> result, DocumentBuilder builder) {
    Document xml = getDocFromXML(file, builder);
    if (xml != null) {
      Element settings = xml.getDocumentElement();
      Node entry = settings.getChildNodes().item(0);
      while (entry != null) {
        if (entry.getNodeType() == Node.ELEMENT_NODE) {
          result.put(entry.getNodeName(), ((Element) entry).getTextContent());
        }
        entry = entry.getNextSibling();
      }
    }
  }

  private void collectMode(Map<String, String> result, DocumentBuilder builder) {
    Document xml = getDocFromXML("mode.xml", builder);
    if (xml != null) {
      Element mode = xml.getDocumentElement();
      result.put(mode.getTagName(), mode.getTextContent());
    }
  }

  private void collectPlaylist(Map<String, String> result, DocumentBuilder builder) {
    Document xml = getDocFromXML("playlist.xml", builder);
    if (xml != null) {
      Element root = xml.getDocumentElement();
      Node entry = root.getChildNodes().item(0);
      int i = 0;
      while (entry != null) {
        if (entry.getNodeType() == Node.ELEMENT_NODE) {
          result.put(entry.getNodeName() + i, ((Element) entry).getTextContent());
          i++;
        }
        entry = entry.getNextSibling();
      }
    }
  }

  private Document getDocFromXML(String file, DocumentBuilder builder) {
    Document xml = null;
    try {
      xml = builder.parse(new File(this.target + "/" + file));
    } catch (SAXException e) {
      LOGGER.log(Level.WARNING, "Couldn't parse " + file, e);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't find " + file + " on SD.", e);
    }
    return xml;
  }

  // TODO return success
  private void copyOrReplaceFile(File sourceFile, String destPath) {
    if (!destPath.endsWith("/")) {
      destPath = destPath + "/";
    }

    File destFile = new File(destPath + sourceFile.getName());
    if (destFile.exists()) {
      if (!destFile.delete()) {
        LOGGER.warning("" + destPath + " could not be deleted.");
        MainApp.showInfoMessage("" + destPath + " could not be deleted.");
      }
    }

    try {
      destFile.createNewFile();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't create " + destFile.getName(), e);
      MainApp.showInfoMessage("" + destPath + " could bot be created.");
      return;
    }

    FileChannel source = null;
    FileChannel destination = null;

    try {
      source = new FileInputStream(sourceFile).getChannel();
    } catch (FileNotFoundException e) {
      LOGGER.log(Level.WARNING, "Couldn't find " + sourceFile.getName(), e);
      return;
    }
    try {
      destination = new FileOutputStream(destFile).getChannel();
    } catch (FileNotFoundException e) {
      LOGGER.log(Level.WARNING, "Couldn't find "+ destFile.getName(), e);
      return;
    }
    try {
      destination.transferFrom(source, 0, source.size());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't write data to " + destFile.getName(), e);
      return;
    }

    if (source != null) {
      try {
        source.close();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Couldn't close " + sourceFile.getName(), e);
      }
    }
    if (destination != null) {
      try {
        destination.close();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Couldn't close " + destFile.getName(), e);
      }
    }
  }
  
  private void copyOrReplaceFile(UploadFile sourceFile, String destPath){
    if (!destPath.endsWith("/")) {
      destPath = destPath + "/";
    }
    File destFile = new File(destPath + sourceFile.getFileName());
    if (destFile.exists()) {
      if (!destFile.delete()) {
        LOGGER.warning("" + destPath + " could not be deleted.");
        MainApp.showInfoMessage("" + destPath + " could not be deleted.");
      }
    }
    try {
      FileUtils.writeByteArrayToFile(destFile, sourceFile.getFileAsBytes());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't write to " + destFile.getName(), e);
    }
  }
}
