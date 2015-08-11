package ch.tweaklab.ip6.test.connector;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.tweaklab.ip6.connector.BrightSignSdCardConnector;
import ch.tweaklab.ip6.media.MediaFile;
import ch.tweaklab.ip6.media.XMLConfigCreator;
import ch.tweaklab.ip6.test.util.TestUtil;

public class BrightSignSdCardConnectorTest {

  BrightSignSdCardConnector sdConnector;
  boolean success = true;

  
  public static class AsNonApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // noop
    }
}

@BeforeClass
public static void initJFX() {
    Thread t = new Thread("JavaFX Init Thread") {
        public void run() {
            Application.launch(AsNonApp.class, new String[0]);
        }
    };
    t.setDaemon(true);
    t.start();
}
  
  
  @Before
  public void setUp() throws Exception {
    sdConnector = new BrightSignSdCardConnector();
    connect();

  }
  
  
  
  

  @Test
  public void connect() {
    String target;
    List<String> targetList = sdConnector.getPossibleTargets();
    if(targetList.size() > 0){
      target = targetList.get(0);
    }
    else{
      target = "D:\\";
    }
    Boolean connected = sdConnector.connect(target);
    assertTrue(connected);

  }

  @Test
  public void uploadFiles() {
    try {

      List<MediaFile> mediaFiles = TestUtil.getMediaFiles();
      File configFile = XMLConfigCreator.createPlayListXML(mediaFiles);
      Task<Boolean> uploadTask = sdConnector.uploadMediaFiles(mediaFiles,configFile);
      uploadTask.setOnSucceeded(event -> success = true);
      uploadTask.setOnCancelled(event -> success = false);
      uploadTask.setOnFailed(event -> success = false);
      Thread uploadThread = new Thread(uploadTask);
      uploadThread.setDaemon(false);
      uploadThread.start();
      assertTrue(uploadTask.get());

    } catch (Exception e) {
      e.printStackTrace();
      success = false;
    }
    assertTrue(success);
  }
  


}
