package org.tweaklab.brightsigntool.connector;

import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.model.Keys;
import org.tweaklab.brightsigntool.model.MediaFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Stephan on 22.03.16.
 */
public class SdUploadTask extends Task<Boolean> {
  private final Logger LOGGER = Logger.getLogger(SdUploadTask.class.getName());
  private static final long FAT_32_MAX_FILESIZE = 0x100000000L;

  private String mediaFolderPath;
  private FileSystemFormat fileSystemFormat;
  private MediaUploadData uploadData;
  private List<UploadFile> systemFiles;
  protected String target;

  public SdUploadTask(MediaUploadData uploadData, List<UploadFile> systemFiles, String target, FileSystemFormat fileSystemFormat) {
    mediaFolderPath = Keys.loadProperty("default_mediaFolder");
    this.fileSystemFormat = fileSystemFormat;
    this.uploadData = uploadData;
    this.systemFiles = systemFiles;
    this.target = target;
  }

  @Override
  public Boolean call() {
    // check, if there is enough space on target
    if (uploadData != null) {
      File targetRoot = new File(target);
      long totalSize = 0;
      for (MediaFile m : uploadData.getUploadList()) {
        if (m != null) {
          totalSize += m.getFileSizeAsNumber();
        }
      }
      if (targetRoot.getTotalSpace() < totalSize) {
        LOGGER.log(Level.WARNING, "Files are too big to fit on " + target + ". Nothing is copied.");
        updateMessage("Files are too big to fit on " + target + ". Nothing is copied.");
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
          LOGGER.info(systemFile.getFileName() + " written.");
        } catch (IOException e) {
          LOGGER.log(Level.WARNING, "Can't write to target!", e);
          updateMessage("Can't write to target!");
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
        LOGGER.info("Mediafolder deleted.");
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Can't delete mediaFolder!", e);
        updateMessage("Can't delete mediaFolder!");
        return false;
      }
    }
    if (!mediaFolder.mkdir()) {
      LOGGER.warning("Wasn't able to create media folder.");
      updateMessage("Wasn't able to create media folder.");
      return false;
    } else {
      LOGGER.info("Mediafolder created.");
    }

    // TODO Some strange behaviour here. If for ex. display changes are made, the xml is handled via systemFiles. Is that part really needed?
    // copy xml config file
    if (uploadData != null) {
      String destPath = target;
      if (!destPath.endsWith(File.separator)) {
        destPath += File.separator;
      }

      final File destFile = new File(destPath + uploadData.getConfigFile().getFileName());

      if (destFile.exists()) {
        if (!destFile.delete()) {
          LOGGER.warning(destPath + " could not have been deleted.");
          updateMessage(destPath + " could not have been deleted.");
          return false;
        }
      }

      try {
        FileUtils.writeByteArrayToFile(destFile, uploadData.getConfigFile().getFileAsBytes());
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Can't write to " + destFile.getName(), e);
        updateMessage("Can't write to " + destFile.getName());
        return false;
      }

      LOGGER.info(uploadData.getConfigFile().getFileName() + " written.");
    }

    // copy each mediafile
    if (uploadData != null) {
      for (MediaFile mediaFile : uploadData.getUploadList()) {
        if (this.isCancelled()) {
          LOGGER.info("Upload cancelled.");
          return false;
        }

        if (mediaFile != null) {
          String destPath = mediaFolder.getPath();
          if (!destPath.endsWith("/")) {
            destPath = destPath + "/";
          }

          File destFile = new File(destPath + mediaFile.getFile().getName());

          if (fileSystemFormat == FileSystemFormat.FAT_32 && mediaFile.getFile().length() > FAT_32_MAX_FILESIZE) {
            updateMessage("File to big for FAT_32 format. Format SD to HFS+ (Mac OSX Extended).");
            return false;
          }

          if (destFile.exists()) {
            if (!destFile.delete()) {
              LOGGER.warning("" + destPath + " could not be deleted.");
              updateMessage(destPath + " could not be deleted.");
              return false;
            }
          }

          try {
            if (!destFile.createNewFile()) {
              LOGGER.log(Level.WARNING, "Can't create " + destFile.getName());
              updateMessage("Can't create " + destFile.getName());
              return false;
            }
          } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can't create " + destFile.getName(), e);
            updateMessage("Can't create " + destFile.getName());
            return false;
          }

          FileChannel source;
          FileChannel destination;

          try {
            source = new FileInputStream(mediaFile.getFile()).getChannel();
          } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "Can't find " + mediaFile.getFile().getName(), e);
            updateMessage("Can't find " + mediaFile.getFile().getPath());
            return false;
          }

          try {
            destination = new FileOutputStream(destFile).getChannel();
          } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "Can't find " + destFile.getName(), e);
            updateMessage("Can't find " + destFile.getPath());
            return false;
          }

          try {
            destination.transferFrom(source, 0, source.size());
          } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can't write data to " + destFile.getPath(), e);
            updateMessage("Can't write data to " + destFile.getPath());
            return false;
          }

          try {
            source.close();
          } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can't close " + mediaFile.getFile().getName(), e);
            updateMessage("Can't close " + mediaFile.getFile().getName());
          }

          try {
            destination.close();
          } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can't close " + destFile.getName(), e);
            updateMessage("Can't close " + destFile.getName());
          }

          LOGGER.info(mediaFile + " written to " + destFile.getPath());
          return true;
        }
      }
    }

    LOGGER.info("Done uploading to SD.");
    return true;
  }
}
