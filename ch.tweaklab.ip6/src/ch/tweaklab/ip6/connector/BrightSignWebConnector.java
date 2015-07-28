package ch.tweaklab.ip6.connector;

/**
 * Implementation of Connector.
 * Connects to a BrightSign Device via HTTP and SSH
 */
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
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

import ch.tweaklab.ip6.gui.MainApp;
import ch.tweaklab.ip6.model.MediaFile;
import ch.tweaklab.ip6.util.PortScanner;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class BrightSignWebConnector extends Connector {

  private String uploadRootUrl;
  Properties configFile;
  private final String mediaFolder = "/media";
  private final String resetMediaFolderScriptName = "resetMediaFolder.brs";

  public BrightSignWebConnector() {

    configFile = new Properties();
    try {
      configFile.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
    } catch (IOException e) {
      MainApp.showExceptionMessage(e);
    }

  }

  public boolean connect(String host) {
    try {
      this.target = host;
      uploadRootUrl = "http://" + host + "/upload.html?rp=sd";
      this.isConnected = sendGetRequest("http://" + host);
    } catch (Exception e) {
      this.isConnected = false;
    }
    return this.isConnected;
  }

  /**
   * Creates a task which upload the specified media files. the current files on the device will be
   * deleted.
   */
  public Task<Boolean> uploadMediaFiles(List<MediaFile> mediaFiles) throws Exception {
    Task<Boolean> uploadTask = new Task<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        Boolean success = true;
        for (MediaFile mediaFile : mediaFiles) {
          if (this.isCancelled()) {
            return false;
          }
          boolean return1 = RunScriptOverSSH(resetMediaFolderScriptName);
          boolean return2 = deleteFile(mediaFile);
          boolean return3 = uploadFile(mediaFolder, mediaFile);
          if (return1 == false || return2 == false || return3== false) {
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
    String urlFilePath = mediaFolder.replace("/","%2F") + "%2F" + urlFileName;
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

  /**
   * Start a Bright Sign Script over SSH
   * 
   * @param scriptName --> full path to script
   * @throws Exception
   */
  private Boolean RunScriptOverSSH(String scriptName) {

    try {
      String user = configFile.getProperty("ssh_user");
      String password = configFile.getProperty("ssh_password");
      int port = Integer.parseInt(configFile.getProperty("ssh_port"));

      JSch jsch = new JSch();
      Session session = jsch.getSession(user, target, port);
      session.setPassword(password);

      session.setConfig("StrictHostKeyChecking", "no");

      session.connect(); // making a connection with timeout.
      Channel channel = session.openChannel("shell");

      PipedInputStream pip = new PipedInputStream(40);
      channel.setInputStream(pip);

      PipedOutputStream pop = new PipedOutputStream(pip);
      PrintStream print = new PrintStream(pop);
      channel.setOutputStream(System.out);

      channel.connect();
      print.println("script " + scriptName);
      Thread.sleep(1000);
      channel.disconnect();
      session.disconnect();
    } catch (Exception e) {
      System.out.println(e);
    }

    return true;
  }

  @Override
  public List<String> getPossibleTargets() {
    PortScanner portScanner = new PortScanner();
    return portScanner.getAllIpWithOpenPortInLocalSubnet(80);
  }

}
