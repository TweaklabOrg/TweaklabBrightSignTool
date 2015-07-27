package ch.tweaklab.ip6.connector;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javafx.concurrent.Task;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;

import ch.tweaklab.ip6.model.MediaFile;

public class BrightSignWebConnector extends Connector {

  private String uploadRootUrl;

  public boolean connect(String hostname) throws Exception {
    this.hostname = hostname;
    uploadRootUrl = "http://" + hostname + "/upload.html?rp=sd";
    this.isConnected = sendGetRequest("http://" + hostname);
    return this.isConnected;
  }

  public Task<Boolean> getUploadMediaFilesTask(List<MediaFile> mediaFiles) throws Exception {
    Task<Boolean> uploadTask = new Task<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        Boolean success = true;
        for (MediaFile mediaFile : mediaFiles) {
          if (this.isCancelled()) return false;
          boolean return1 = deleteFile(mediaFile);
          boolean return2 = uploadFile("/", mediaFile);
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
    String deleteUrl = "http://" + hostname + "/delete?filename=sd%2F" + urlFileName + "&delete=Delete";
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


}
