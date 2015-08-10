package ch.tweaklab.ip6.connector;

/**
 * Implementation of Connector.
 * Connects to a BrightSign Device via HTTP and SSH
 */
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import ch.tweaklab.ip6.media.MediaFile;
import ch.tweaklab.ip6.util.PortScanner;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class BrightSignWebConnector extends Connector {

  private String uploadRootUrl;
  Properties configFile;
  private String mediaFolder;
  private String resetMediaFolderScriptName;

  public BrightSignWebConnector() {

    configFile = new Properties();
    try {
      configFile.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
      resetMediaFolderScriptName = configFile.getProperty("resetMediaFolderScriptName");
      mediaFolder = configFile.getProperty("mediaFolder");

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
   * Creates a task which deletes the mediafolder and upload the new specified media files to the
   * specified mediaFolder.
   */
  public Task<Boolean> uploadMediaFiles(List<MediaFile> mediaFiles, File configFile) throws Exception {
    Task<Boolean> uploadTask = new Task<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        Boolean success = true;
        // run script to delete whole media folder
        success = RunScriptOverSSH(resetMediaFolderScriptName);
        if (!success)
          return false;

     // upload config file
        success = uploadFile(mediaFolder, configFile);
        if (!success)
          return false;
        for (MediaFile mediaFile : mediaFiles) {
          if (this.isCancelled()) {
            return false;
          }

          // TODO: zzAlain: just needed while uploading config File, Remove if finished
          // delete file in rootpath
          success = deleteFile(mediaFile);
          if (!success)
            return false;

          // upload new file to mediafolder
          success = uploadFile(mediaFolder, mediaFile.getFile());
          if (!success)
            return false;
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

  private Boolean deleteFile(MediaFile mediaFile) throws Exception {
    String urlFileName = mediaFile.getFile().getName().replace(" ", "+");
    String urlFilePath = mediaFolder.replace("/", "%2F") + "%2F" + urlFileName;
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
   */
  public Boolean RunScriptOverSSH(String scriptName) {
    // TODO: zzAlain: change to private
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
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

      PipedInputStream pip = new PipedInputStream(100);
      channel.setInputStream(pip);

      PipedOutputStream pop = new PipedOutputStream(pip);
      PrintStream print = new PrintStream(pop);

      channel.setOutputStream(baos);

      channel.connect();
      int ctrlC = 3;
      print.println((char) ctrlC);
      Thread.sleep(1000);
      print.println("script " + scriptName);
      Thread.sleep(1000);
      print.println("run(\"" + scriptName + "\")");
      Thread.sleep(1000);

      print.close();
      channel.disconnect();
      session.disconnect();

    } catch (Exception e) {
      String output = new String(baos.toByteArray());
      System.out.println(output);
      e.printStackTrace();
      return false;
    }

    return true;
  }

  @Override
  public List<String> getPossibleTargets() {
    PortScanner portScanner = new PortScanner();
    return portScanner.getAllIpWithOpenPortInLocalSubnet(80);
  }

}
