package ch.tweaklab.ip6.connector;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
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

  private String uploadRootUrl;

  public boolean connect(String hostname) {
    isConnected = true;
    this.hostname = hostname;
    uploadRootUrl = "http://" + hostname + "/upload.html?rp=sd";
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

  public boolean uploadMediaFiles(List<File> files) {
    try {
      for (File file : files) {
        deleteFile(file);
        uploadFile("/", file);
      }

      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  private Boolean uploadFile(String destinationFolder, File file) throws Exception{
    MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
    multiPartBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

    FileBody fileBody = new FileBody(file);
    multiPartBuilder.addPart(file.getName(), fileBody);

    HttpPost request = new HttpPost(uploadRootUrl + destinationFolder);
    
    request.setEntity(multiPartBuilder.build());

    HttpClient client = new DefaultHttpClient();
    HttpResponse response = client.execute(request);
    
    System.out.println(response.getStatusLine());
    return true;
  }
  
  private Boolean deleteFile(File file) throws Exception{
  String urlFileName = file.getName().replace(" ", "+");
  String deleteUrl =  "http://" + hostname + "/delete?filename=sd%2F"+ urlFileName + "&delete=Delete";
  
  URL u = new URL(deleteUrl);
  HttpURLConnection huc = (HttpURLConnection) u.openConnection();
  huc.setRequestMethod("GET"); // OR huc.setRequestMethod ("HEAD");
  huc.connect();
  System.out.println(huc.getResponseMessage());
  return true;
  }

}
