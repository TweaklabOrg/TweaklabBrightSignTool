package org.tweaklab.brightsigntool.connector;

/**
 * Implementation of Connector.
 * Connects to a BrightSign Device via HTTP and SSH
 */

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.model.Keys;
import org.tweaklab.brightsigntool.model.MediaFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import org.tweaklab.brightsigntool.util.DiscoverServices;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrightSignWebConnector extends Connector {

  public static final String CLASS_DISPLAY_NAME = "BS Web Connector";
  private static final Logger LOGGER = Logger.getLogger(BrightSignSdCardConnector.class.getName());
  private String uploadRootUrl;
  private String mediaFolder;

  private int tcpPort;

  public BrightSignWebConnector() {
    tcpPort = Integer.parseInt(Keys.loadProperty(Keys.DEFAULT_TCP_PORT_PROPS_KEY));
    mediaFolder = "/" + Keys.loadProperty(Keys.DEFAULT_MEDIA_FOLDER_PROPS_KEY);
  }

  public boolean connect(String host) {
    try {
      this.target = host + ".local";
      this.name = host;
      Socket tcpSocket = new Socket(this.target, tcpPort);
      tcpSocket.close();
      uploadRootUrl = "http://" + this.target + "/upload.html?rp=sd";
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "There was an issue connection to " + host, e);
    }

    return isConnected();
  }

  @Override
  public boolean disconnect() {
    this.target = "";
    this.name = "";
    uploadRootUrl = "";
    return !isConnected();
  }

  @Override
  public Task<Boolean> upload(MediaUploadData mediaUploadData, List<UploadFile> systemFiles) {
    // stop player and signal that data is comming
    sendTCPCommand("receiveData");

    return new Task<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        // uploadSystemFiles
        for (UploadFile systemFile : systemFiles) {
          if (systemFile != null) {
            boolean success = deleteFile("/", systemFile.getFileName());
            if (!success) {
              LOGGER.log(Level.SEVERE, "Can't delete " + systemFile.getFileName());
              updateMessage("Can't delete " + systemFile.getFileName());
              return false;
            } else {
              LOGGER.info(systemFile.getFileName() + " deleted.");
            }

            // upload new file system file to root folder
            success = uploadFile("", systemFile);
            if (!success) {
              updateMessage("Couldn't write " + systemFile.getFileName());
              return false;
            }
          }
        }

        //skip media upload if null
        if (mediaUploadData != null) {
          // run script to delete whole media folder
          String answer = sendTCPCommand("resetFilestructure");
          if (!answer.equals("OK")) {
            LOGGER.log(Level.SEVERE, "Can't reset filestrucutre.");
            updateMessage("Can't reset filestrucutre.");
            return false;
          } else {
            LOGGER.info("Filestructure resetted.");
          }

          // delete media config file
          boolean success = deleteFile("/", mediaUploadData.getConfigFile().getFileName());
          if (!success) {
            LOGGER.log(Level.SEVERE, "Can't delete file " + mediaUploadData.getConfigFile().getFileName());
            updateMessage("Can't delete file " + mediaUploadData.getConfigFile().getFileName());
            return false;
          } else {
            LOGGER.info(mediaUploadData.getConfigFile().getFileName() + "deleted.");
          }

          // upload media config file
          success = uploadFile("/", mediaUploadData.getConfigFile());
          if (!success) {
            updateMessage("Can't write " + mediaUploadData.getConfigFile().getFileName());
            return false;
          }

          // upload Media
          for (MediaFile mediaFile : mediaUploadData.getUploadList()) {
            if (this.isCancelled()) {
              LOGGER.info("Upload was cancelled.");
              return false;
            }

            if (mediaFile != null) {
              // upload new file to mediafolder
              success = uploadFile(mediaFolder, mediaFile.getFile());
              if (!success) {
                updateMessage("Can't write " + mediaFile.toString());
                return false;
              }
            }
          }
        }


        String answer = sendTCPCommand("reboot");
        if (!answer.equals("OK")) {
          LOGGER.severe("Can't reboot target.");
          updateMessage("Can't reboot target.");
          return false;
        } else {
          LOGGER.info("Rebooting target.");
        }
        return true;
      }
    };
  }

  @Override
  public Boolean isConnected() {
    return sendGetRequest("http://" + this.target);
  }

  private Boolean uploadFile(String destinationFolder, File file) {
    MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
    multiPartBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    FileBody fileBody = new FileBody(file);
    multiPartBuilder.addPart(file.getName(), fileBody);
    HttpPost request = new HttpPost(uploadRootUrl + destinationFolder);
    request.setEntity(multiPartBuilder.build());
    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response;
    try {
      response = client.execute(request);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "There's a problem with the connection.", e);
      return false;
    }
    if (!response.getStatusLine().toString().contains("200")) {
      LOGGER.warning("Upload Error: Error while uploading " + file.getName() + ". Status was:" + response.getStatusLine().toString());
      new Alert(Alert.AlertType.NONE,
              "Error while uploading " + file.getName() + ". Status was:" + response.getStatusLine().toString(),
              ButtonType.OK).showAndWait();
      return false;
    }
    LOGGER.info("File successfully uploaded: " + file.getName());
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
    HttpResponse response;
    try {
      response = client.execute(request);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "There's a problem with the connection.", e);
      return false;
    }
    if (!response.getStatusLine().toString().contains("200")) {
      LOGGER.warning("Upload Error: Error while uploading " + uploadFile.getFileName() + ". Status was: " + response.getStatusLine().toString());
      new Alert(Alert.AlertType.NONE,
              "Error while uploading " + uploadFile.getFileName() + ". Status was: " + response.getStatusLine().toString(),
              ButtonType.OK).showAndWait();
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
        new Alert(Alert.AlertType.NONE, "Error return code for url " + url + ". Code was:" + returnCode,
                ButtonType.OK).showAndWait();
        return false;
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't send GET request to " + url, e);
      return false;
    }
    LOGGER.info("Successfully sent GET request to BS: " + url);
    return true;
  }

  private String getResponseFromGetRequest(String url) {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(url);

    ResponseHandler<String> handler = httpResponse -> {
      int status = httpResponse.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        HttpEntity entity = httpResponse.getEntity();
        return entity != null ? EntityUtils.toString(entity) : null;
      } else {
        LOGGER.log(Level.WARNING, "BS returned a error: " + status);
      }
      return "";
    };

    String respond = "";
    try {
      respond = httpclient.execute(httpGet, handler);
      LOGGER.log(Level.INFO, "GET request sent: " + httpGet.toString());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't send GET request: " + httpGet.toString(), e);
    }

    try {
      httpclient.close();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't close tcp connection: " + httpclient.toString(), e);
    }
    return respond;
  }

  private String sendTCPCommand(String command) {
    String answer = "";
    Socket tcpSocket = null;
    DataOutputStream outToTcpServer;
    BufferedReader inFromTcpServer;
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
    return new Task<List<String>>() {
      private final Logger LOGGER1 = Logger.getLogger(getClass().getName());

      @Override
      public List<String> call() throws Exception {
        List<String> result = DiscoverServices.searchServices("_tl");
        LOGGER1.info("Done collecting Targets. " + result.size() + " found");
        return result;
      }
    };
  }

  @Override
  public Map<String, String> getSettingsOnDevice() {
    Map<String, String> result = new HashMap<>();

    String settingsXMLAsString = getResponseFromGetRequest("http://" + this.target + "/view?rp=sd/settings.xml");
    String modeXMLAsString = getResponseFromGetRequest("http://" + this.target + "/view?rp=sd/mode.xml");
    String displayXMLAsString = getResponseFromGetRequest("http://" + this.target + "/view?rp=sd/display.xml");

    InputStream settingsXMLAsFile = new ByteArrayInputStream(settingsXMLAsString.getBytes());
    InputStream modeXMLAsFile = new ByteArrayInputStream(modeXMLAsString.getBytes());
    InputStream displayXMLAsFile = new ByteArrayInputStream(displayXMLAsString.getBytes());

    DocumentBuilder builder = null;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      LOGGER.log(Level.SEVERE, "Not able to get DocumentBuilder?", e);
    }

    collectEntries(settingsXMLAsFile, result, builder);
    collectMode(modeXMLAsFile, result, builder);
    collectEntries(displayXMLAsFile, result, builder);
    // TODO: building filemanagement to make that possible. For ex. skip mediaupload of already existing files, but allow modifications on settings.
//    collectEntries("gpio.xml", result, builder);
//    collectPlaylist(result, builder);

    LOGGER.info("Collectable settings loaded from BS.");

    return result;
  }

  @Override
  public boolean isResolutionSupported(String brightSignResolutionString) {
    return sendTCPCommand(brightSignResolutionString).equals("supported");
  }
}
