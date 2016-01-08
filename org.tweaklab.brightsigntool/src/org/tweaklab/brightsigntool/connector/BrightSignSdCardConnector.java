package org.tweaklab.brightsigntool.connector;

import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.gui.controller.MainApp;
import org.tweaklab.brightsigntool.model.Keys;
import org.tweaklab.brightsigntool.model.MediaFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import org.tweaklab.brightsigntool.util.CommandlineTool;
import org.tweaklab.brightsigntool.util.OSValidator;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.swing.filechooser.FileSystemView;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * Connects to a SD Card of Bright Sign Device
 * 
 * @author Alain
 *
 *
 */
public class BrightSignSdCardConnector extends Connector {

  public static final String CLASS_DISPLAY_NAME = "BS SD-Card Connector";

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

    return isConnected;
  }

  private Boolean isPathValid(String path) {
    // TODO: zzAlain check if path is valid

    return true;
  }



  @Override
  public boolean disconnect() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Task<Boolean> upload(MediaUploadData uploadData, List<UploadFile> systemFiles) throws Exception {
    Task<Boolean> uploadTask = new Task<Boolean>() {
      Boolean success;

      @Override
      public Boolean call() throws Exception {
        success = true;

        // writeSystemFiles
        for (UploadFile systemFile : systemFiles) {
          if (systemFile != null) {
            File targetFile = new File(target + "/" + systemFile.getFileName());
            // write file to root folder
            FileUtils.writeByteArrayToFile(targetFile, systemFile.getFileAsBytes());
          }

        }

        if (uploadData != null) {
          // reset media folder on sd card
          if ((target.endsWith("/") || target.endsWith("\\")) == false) {
            target = target + "/";
          }
          File mediaFolder = new File(target + "/" + mediaFolderPath);
          if (mediaFolder.exists()) {
            FileUtils.deleteDirectory(mediaFolder);
          }
          if (!mediaFolder.mkdir()) {
            MainApp.showInfoMessage("Wasn't able to create media folder.");
            success = false;
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
        }
        return success;
      }

    };
    return uploadTask;
  }

  @Override
  public Task<List<String>> getPossibleTargets() {

    Task<List<String>> getTargetTask = new Task<List<String>>() {
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
                String path = f.getCanonicalPath();//.replace(" ", "\\ ");
                List<String> command = new LinkedList<>();
                command.add("diskutil");
                command.add("info");
                command.add(path);
                String volumeInfo = CommandlineTool.executeCommand(command);
                // [\\s\\S] matches all chars, even \n, ...
                if (volumeInfo.matches("[\\s\\S]*Ejectable:( *)Yes[\\s\\S]*")
                        || volumeInfo.matches("[\\s\\S]*Removable Media:( *)Yes[\\s\\S]*")) {
                  targetList.add(f.getAbsolutePath());
                }
              }
            }
          } else {
            MainApp.showInfoMessage("Folder /Volumes not found or I/O error occured.");
          }
        }
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
      e.printStackTrace();
    }

    collectMode(result, builder);
    collectEntries("settings.xml", result, builder);
    collectEntries("display.xml", result, builder);
    // TODO: building filemanagement to make that possible. For ex. skip mediaupload of already existing files, but allow modifications on settings.
//    collectEntries("gpio.xml", result, builder);
//    collectPlaylist(result, builder);

    return result;
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
      // nothing to do, as we don't absolutely need the data;
    } catch (IOException e) {
      // nothing to do, as we don't absolutely need the data;
    }
    return xml;
  }

  // TODO return success
  private void copyOrReplaceFile(File sourceFile, String destPath) throws Exception {
	    if (!destPath.endsWith("/")) {
	      destPath = destPath + "/";
	    }
	    File destFile = new File(destPath + sourceFile.getName());
	    if (destFile.exists()) {
	      if (!destFile.delete()) {
          MainApp.showInfoMessage("" + destPath + " could not be deleted.");
        }
	    }
	    if (!destFile.createNewFile()) {
        MainApp.showInfoMessage("" + destPath + " could bot be created.");
      }

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
  
  private void copyOrReplaceFile(UploadFile sourceFile, String destPath) throws Exception {
	    if (!destPath.endsWith("/")) {
	      destPath = destPath + "/";
	    }
	    File destFile = new File(destPath + sourceFile.getFileName());
	    if (destFile.exists()) {
	      if (!destFile.delete()) {
          MainApp.showInfoMessage("" + destPath + " could not be deleted.");
        }
	    }
	    FileUtils.writeByteArrayToFile(destFile, sourceFile.getFileAsBytes());
	  
	  }
}
