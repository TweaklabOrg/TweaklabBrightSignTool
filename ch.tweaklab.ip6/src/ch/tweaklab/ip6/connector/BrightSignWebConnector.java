package ch.tweaklab.ip6.connector;
/**
 * Implementation of Connector.
 * Connects to a BrightSign Device via HTTP and SSH
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javafx.concurrent.Task;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import ch.tweaklab.ip6.gui.MainApp;
import ch.tweaklab.ip6.model.MediaFile;

public class BrightSignWebConnector extends Connector {

  private String uploadRootUrl;
  Properties configFile;

  
  public BrightSignWebConnector(){
    configFile = new Properties();
    try {
      configFile.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
    } catch (IOException e) {
      MainApp.showExceptionMessage(e);
    }
      
  }
  public boolean connect(String host) throws Exception {
    this.host = host;
    uploadRootUrl = "http://" + host + "/upload.html?rp=sd";
    this.isConnected = sendGetRequest("http://" + host);
    return this.isConnected;
  }

  /**
   * Creates a task which upload the specified media files. the current files on the device will be deleted.
   */
  public Task<Boolean> getUploadMediaFilesTask(List<MediaFile> mediaFiles) throws Exception {
    Task<Boolean> uploadTask = new Task<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        Boolean success = true;
        for (MediaFile mediaFile : mediaFiles) {
          if (this.isCancelled()) return false;
          boolean return1 = deleteFile(mediaFile);
          boolean return2 = uploadFile("/media", mediaFile);
          if (return1 == false || return2 == false) {
            success = false;
          }
        }
        return success;
      }
    };
    return uploadTask;

  }

  
  private Boolean uploadFile(String destinationFolder, MediaFile mediaFile) throws Exception {

    MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
    multiPartBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    FileBody fileBody = new FileBody(mediaFile.getFile());
    multiPartBuilder.addPart(mediaFile.getFile().getName(), fileBody);
    HttpPost request = new HttpPost(uploadRootUrl + destinationFolder);
    request.setEntity(multiPartBuilder.build());
    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = client.execute(request);
    if (!response.getStatusLine().toString().contains("200")) {
      return false;
    }
    return true;
  }

  private Boolean deleteFile(MediaFile mediaFile) throws Exception {
    String urlFileName = mediaFile.getFile().getName().replace(" ", "+");
    String deleteUrl = "http://" + host + "/delete?filename=sd%2F" + urlFileName + "&delete=Delete";
    return sendGetRequest(deleteUrl);
  }

  private Boolean sendGetRequest(String url) throws Exception {
    URL u = new URL(url);
    HttpURLConnection huc = (HttpURLConnection) u.openConnection();
    huc.setRequestMethod("GET"); // OR huc.setRequestMethod ("HEAD");
    huc.connect();
    int returnCode = huc.getResponseCode();
    if (returnCode != 200) {
      return false;
    }
    return true;
  }
  
  
  /**
   * Start a Bright Sign Script over SSH
   * 
   * @param scriptName --> full path to script
   * @throws Exception
   */
  private void runScriptOnDevice(String scriptName) throws Exception{

    String user = configFile.getProperty("ssh_user");
    String password = configFile.getProperty("ssh_password");
    int port=Integer.parseInt(configFile.getProperty("ssh_port"));

        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
        System.out.println("Establishing Connection...");
        session.connect();
        System.out.println("Connection established.");
        ChannelExec channelExec = (ChannelExec)session.openChannel("exec");

        InputStream in = channelExec.getInputStream();

        channelExec.setCommand("script " + scriptName);
        channelExec.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        int index = 0;

        while ((line = reader.readLine()) != null)
        {
            System.out.println(++index + " : " + line);
        }

        channelExec.disconnect();
        session.disconnect();

        System.out.println("Done!");
  
  }


}
