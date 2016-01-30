package org.tweaklab.brightsigntool.connector;

/**
 * Implementation of Connector.
 * Connects to a BrightSign Device via HTTP and SSH
 */
import javafx.concurrent.Task;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.gui.controller.MainApp;
import org.tweaklab.brightsigntool.model.Keys;
import org.tweaklab.brightsigntool.model.MediaFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import org.tweaklab.brightsigntool.util.DiscoverServices;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrightSignWebConnector extends Connector {

  private static final Logger LOGGER = Logger.getLogger(BrightSignSdCardConnector.class.getName());

  public static final String CLASS_DISPLAY_NAME = "BS Web Connector";

  private String uploadRootUrl;
  private String mediaFolder;

  private int tcpPort;

  public BrightSignWebConnector() {
    tcpPort = Integer.parseInt(Keys.loadProperty(Keys.DEFAULT_TCP_PORT_PROPS_KEY));
    mediaFolder = "/" + Keys.loadProperty(Keys.DEFAULT_MEDIA_FOLDER_PROPS_KEY);
  }

  public boolean connect(String host) {
    isConnected = false;
    try {
      this.target = host + ".local";
      this.name = host;
      Socket tcpSocket = new Socket(this.target, tcpPort);
      tcpSocket.close();
      uploadRootUrl = "http://" + this.target + "/upload.html?rp=sd";
      this.isConnected = sendGetRequest("http://" + this.target);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "There was an issue connection to " + host, e);
    }

    return this.isConnected;
  }

  @Override
  public boolean disconnect() {
    this.target = "";
    this.name = "";
    uploadRootUrl = "";
    isConnected = false;
    return !isConnected;
  }

  @Override
  public Task<Boolean> upload(MediaUploadData mediaUploadData, List<UploadFile> systemFiles) {
    Task<Boolean> uploadTask = new Task<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        Boolean success = true;

        // uploadSystemFiles
        for (UploadFile systemFile : systemFiles) {
          if (systemFile != null) {
            success = deleteFile("/", systemFile.getFileName());
            if (!success)
              return false;

            // upload new file system file to root folder
            success = uploadFile("", systemFile);
            if (!success)
              return false;
          }

        }

        //skip mediata upload if null
        if (mediaUploadData != null) {
          // run script to delete whole media folder
          String answer = sendTCPCommand("resetFilestructure");
          if (!answer.equals("OK")) {
            return false;
          }

          success = deleteFile("/", mediaUploadData.getConfigFile().getFileName());
          if (!success)
            return false;
          // upload media config file
          success = uploadFile("/", mediaUploadData.getConfigFile());
          if (!success) {
            return false;
          }

          // upload Media
          for (MediaFile mediaFile : mediaUploadData.getUploadList()) {
            if (this.isCancelled()) {
              return false;
            }

            if (mediaFile != null) {
              // TODO: zzAlain: just needed while uploading config File, Remove if finished
              // delete file in rootpath
              success = deleteFile(mediaFolder, mediaFile.getFile().getName());
              if (!success)
                return false;

              // upload new file to mediafolder
              success = uploadFile(mediaFolder, mediaFile.getFile());
              if (!success)
                return false;
            }
          }
        }
        
        
        String answer = sendTCPCommand("reboot");
        if (!answer.equals("OK")) {
          return false;
        }
        return success;
      }
    };
    return uploadTask;
  }

  private Boolean uploadFile(String destinationFolder, File file) {

    MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
    multiPartBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    FileBody fileBody = new FileBody(file);
    multiPartBuilder.addPart(file.getName(), fileBody);
    HttpPost request = new HttpPost(uploadRootUrl + destinationFolder);
    request.setEntity(multiPartBuilder.build());
    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = null;
    try {
      response = client.execute(request);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "There's a problem with the connection.", e);
    }
    if (!response.getStatusLine().toString().contains("200")) {
      LOGGER.warning("Upload Error: Error while upload. Status was:" + response.getStatusLine().toString());
      MainApp.showErrorMessage("Upload Error", "Error while upload. Status was:" + response.getStatusLine().toString());
      return false;
    }
    LOGGER.info("File successfully uploaded: " + file);
    return true;
  }

  private Boolean uploadFile(String destinationFolder, UploadFile uploadFile) {
    MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
    multiPartBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

    ContentBody mimePart = new ByteArrayBody(uploadFile.getFileAsBytes(), uploadFile.getFileName());
    multiPartBuilder.addPart(mimePart.getFilename(), mimePart);

    HttpPost request = new HttpPost(uploadRootUrl + destinationFolder);

    request.setEntity(multiPartBuilder.build());
    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = null;
    try {
      response = client.execute(request);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "There's a problem with the connection.", e);
    }
    if (!response.getStatusLine().toString().contains("200")) {
      LOGGER.warning("Upload Error: Error while upload. Status was: " + response.getStatusLine().toString());
      MainApp.showErrorMessage("Upload Error", "Error while upload. Status was: " + response.getStatusLine().toString());
      return false;
    }
    LOGGER.info("File successfully uploaded: " + uploadFile.getFileName());
    return true;
  }

  private Boolean deleteFile(String destinationFolder, String fileName) {
    String urlFileName = fileName.replace(" ", "+");
    String urlFilePath = destinationFolder.replace("/", "%2F") + "%2F" + urlFileName;
    String deleteUrl = "http://" + target + "/delete?filename=sd" + urlFilePath + "&delete=Delete";
    return sendGetRequest(deleteUrl);
  }

  private Boolean sendGetRequest(String url) {
    try {
      URL u = new URL(url);
      HttpURLConnection huc = (HttpURLConnection) u.openConnection();
      huc.setRequestMethod("GET");
      huc.setConnectTimeout(15 * 100);
      huc.connect();
      int returnCode = huc.getResponseCode();
      if (returnCode != 200) {
        LOGGER.warning("URL Error: Wrong return code for url " + url + ". Code was:" + returnCode);
        MainApp.showErrorMessage("URL Error", "Wrong return code for url " + url + ". Code was:" + returnCode);
        return false;
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't send GET request to " + url, e);
    }
    LOGGER.info("Successfully sent GET request to BS: " + url);
    return true;
  }

  public String sendTCPCommand(String command) {
    String answer = "";
    Socket tcpSocket = null;
    DataOutputStream outToTcpServer = null;
    BufferedReader inFromTcpServer = null;
    try {
      tcpSocket = new Socket(this.target, tcpPort);
      outToTcpServer = new DataOutputStream(tcpSocket.getOutputStream());
      inFromTcpServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream(), "US-ASCII"));
      outToTcpServer.writeBytes(command + '\n');
      LOGGER.info("Sent to BS: " + command);
      answer = inFromTcpServer.readLine();
      LOGGER.info("Received from BS: " + answer);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't send command: " + command, e);
    }

    try {
      if (tcpSocket != null) {
        tcpSocket.close();
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't close Socket.", e);
    }
    return answer;
  }

  @Override
  public Task<List<String>> getPossibleTargets() {
    // TODO: Stephan: reaction if no target found.
    // TODO: Stephan: Somehow the app reacts strange after first scan. Divices are not selectable.
    Task<List<String>> getTargetTask = new Task<List<String>>() {
      private final Logger LOGGER = Logger.getLogger(getClass().getName());
      @Override
      public List<String> call() throws Exception{
        List<String> result = DiscoverServices.searchServices("_tl");
        LOGGER.info("Done collecting Targets. " + result.size() + " found");
        return result;
      }
    };
    return getTargetTask;
  }

  @Override
  public Map<String, String> getSettingsOnDevice() {
    return new HashMap<>();
  }

}
