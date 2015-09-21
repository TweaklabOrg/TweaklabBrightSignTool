package ch.tweaklab.player.connector;

/**
 * Implementation of Connector.
 * Connects to a BrightSign Device via HTTP and SSH
 */
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javafx.concurrent.Task;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;

import ch.tweaklab.player.gui.controller.MainApp;
import ch.tweaklab.player.model.Keys;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.util.DiscoverServices;

public class BrightSignWebConnector extends Connector {

  public static final String CLASS_DISPLAY_NAME = "BS Web Connector";

  private String uploadRootUrl;
  Properties configFile;
  private String mediaFolder;

  private int tcpPort;

  private Socket tcpSocket;
  private DataOutputStream outToTcpServer;
  private BufferedReader inFromTcpServer;

  public BrightSignWebConnector() {
    tcpPort = Integer.parseInt(Keys.loadProperty(Keys.DEFAULT_TCP_PORT_PROPS_KEY));
    mediaFolder = "/" + Keys.loadProperty(Keys.DEFAULT_MEDIA_FOLDER_PROPS_KEY);
  }

  public boolean connect(String host) {

    try {
      this.target = host + ".local";

      tcpSocket = new Socket(this.target, tcpPort);
      outToTcpServer = new DataOutputStream(tcpSocket.getOutputStream());
      inFromTcpServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
      uploadRootUrl = "http://" + this.target + "/upload.html?rp=sd";
      this.isConnected = sendGetRequest("http://" + this.target);
    } catch (Exception e) {
      this.isConnected = false;
    }
    return this.isConnected;
  }

  @Override
  public boolean disconnect() {
    try {
      tcpSocket.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return true;
  }

  @Override
  public Task<Boolean> upload(MediaUploadData uploadData, List<File> systemFiles) throws Exception {
    Task<Boolean> uploadTask = new Task<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        Boolean success = true;

        // uploadSystemFiles
        for (File systemFile : systemFiles) {
          if (systemFile != null) {
            success = deleteFile("/", systemFile);
            if (!success)
              return false;

            // upload new file to mediafolder
            success = uploadFile("/", systemFile);
            if (!success)
              return false;
          }

        }

        // run script to delete whole media folder
        String answer = sendTCPCommand("resetFilestructure");
        if (!answer.equals("OK")) {
          return false;
        }

        // upload config file
        success = deleteFile("/", uploadData.getConfigFile());
        if (!success)
          return false;
        success = uploadFile("/", uploadData.getConfigFile());
        if (!success)
          return false;

        // upload Media
        for (MediaFile mediaFile : uploadData.getUploadList()) {
          if (this.isCancelled()) {
            return false;
          }

          if (mediaFile != null) {
            // TODO: zzAlain: just needed while uploading config File, Remove if finished
            // delete file in rootpath
            success = deleteFile(mediaFolder,mediaFile.getFile());
            if (!success)
              return false;

            // upload new file to mediafolder
            success = uploadFile(mediaFolder, mediaFile.getFile());
            if (!success)
              return false;
          }
        }
        return success;
      }
    };
    return uploadTask;
  }

  private Boolean uploadFile(String destinationFolder, File file) throws Exception {

    MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
    multiPartBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    FileBody fileBody = new FileBody(file);
    multiPartBuilder.addPart(file.getName(), fileBody);
    HttpPost request = new HttpPost(uploadRootUrl + destinationFolder);
    request.setEntity(multiPartBuilder.build());
    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = client.execute(request);
    if (!response.getStatusLine().toString().contains("200")) {
      return false;
    }
    return true;
  }

  private Boolean deleteFile(String destinationFolder, File file) throws Exception {
    String urlFileName = file.getName().replace(" ", "+");
    String urlFilePath = destinationFolder.replace("/", "%2F") + "%2F" + urlFileName;
    String deleteUrl = "http://" + target + "/delete?filename=sd" + urlFilePath + "&delete=Delete";
    return sendGetRequest(deleteUrl);
  }

  private Boolean sendGetRequest(String url) throws Exception {
    URL u = new URL(url);
    HttpURLConnection huc = (HttpURLConnection) u.openConnection();
    huc.setRequestMethod("GET");
    huc.setConnectTimeout(15 * 100);
    huc.connect();
    int returnCode = huc.getResponseCode();
    if (returnCode != 200) {
      return false;
    }
    return true;
  }

  public String sendTCPCommand(String command) {
    String answer = "";
    try {
      outToTcpServer.writeBytes(command + '\n');
      answer = inFromTcpServer.readLine();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return answer;

  }

  @Override
  public Task<List<String>> getPossibleTargets() {
    // TODO: Stephan: reaction if no target found.
    Task<List<String>> getTargetTask = new Task<List<String>>() {
      @Override
      public List<String> call() throws Exception {
        List<String> result = DiscoverServices.searchServices("_tl");
        return result;
      }
    };
    return getTargetTask;
  }

}
