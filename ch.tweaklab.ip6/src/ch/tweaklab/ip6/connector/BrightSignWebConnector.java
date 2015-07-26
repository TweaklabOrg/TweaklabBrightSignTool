package ch.tweaklab.ip6.connector;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

public class BrightSignWebConnector extends Connector {

  private String uploadUrl;

  public boolean connect(String hostname) {
    isConnected = true;
    this.hostname = hostname;
    uploadUrl = "http://" + hostname + "/upload.html?rp=sd";
    try {
      URL u = new URL("http://" + hostname);
      HttpURLConnection huc = (HttpURLConnection) u.openConnection();
      huc.setRequestMethod("GET"); // OR huc.setRequestMethod ("HEAD");
      huc.connect();
      int returnCode = huc.getResponseCode();
      if(returnCode != 200){
        return false;
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public boolean uploadFiles(List<File> files) {
    try {
      files.forEach(file -> System.out.println(file.getName()));
      System.out.println("files uploaded");

      MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
      multiPartBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

      FileBody fileBody;
      for (File file : files) {
        fileBody = new FileBody(file);
        multiPartBuilder.addPart(file.getName(), fileBody);
      }

      HttpPost request = new HttpPost(uploadUrl);
      request.setEntity(multiPartBuilder.build());

      HttpClient client = new DefaultHttpClient();
      HttpResponse response = client.execute(request);
      System.out.println(response);

      return true;
    } catch (Exception e) {
      return false;
    }
  }

}
