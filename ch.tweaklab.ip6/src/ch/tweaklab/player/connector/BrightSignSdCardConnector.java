package ch.tweaklab.player.connector;

import ch.tweaklab.player.configurator.UploadFile;
import ch.tweaklab.player.model.Keys;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.util.OSValidator;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Connects to a SD Card of Bright Sign Device
 * 
 * @author Alain
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
    // if (OSValidator.isWindows()) {
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
          mediaFolder.mkdir();

          // copy xml config file
          copyOrReplaceFile(uploadData.getConfigFile(), target + "/" + mediaFolderPath);

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
    // } else {
    // return null;
    // }
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
            // TODO: enable after problem sovled with ant
            // if (description.equals("Removable Disk") ||
            // description.equals("Wechseldatentraeger")) {
            if (description.equals("Removable Disk")) {
              targetList.add(path.getAbsolutePath());
            }
          }

        } else if (OSValidator.isMac()) {
          File volumes = new File("/Volumes");
          File files[] = volumes.listFiles();
          for (File f : files) {
            // TODO: Stephan: only add removeable disks
            if (f.getName().charAt(0) != '.') {
              String volumeInfo = executeCommand("diskutil info " + f);
              // [\\s\\S] matches all chars, even \n, ...
              if (volumeInfo.matches("[\\s\\S]*Protocol:( *)Secure Digital[\\s\\S]*")) {
                targetList.add(f.getAbsolutePath());
              }
            }
          }
        }
        return targetList;
      }
    };
    return getTargetTask;
  }

  private String executeCommand(String command) {
    StringBuffer output = new StringBuffer();

    Process p;
    try {
      p = Runtime.getRuntime().exec(command);
      p.waitFor();
      BufferedReader reader =
              new BufferedReader(new InputStreamReader(p.getInputStream()));

      String line = "";
      while ((line = reader.readLine())!= null) {
        output.append(line + "\n");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return output.toString();
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
  
  private void copyOrReplaceFile(UploadFile sourceFile, String destPath) throws Exception {
	    if (!destPath.endsWith("/")) {
	      destPath = destPath + "/";
	    }
	    File destFile = new File(destPath + sourceFile.getFileName());
	    if (destFile.exists()) {
	      destFile.delete();
	    }
	    FileUtils.writeByteArrayToFile(destFile, sourceFile.getFileAsBytes());
	  
	  }
}
