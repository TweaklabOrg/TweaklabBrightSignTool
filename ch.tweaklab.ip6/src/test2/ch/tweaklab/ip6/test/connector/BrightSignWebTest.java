package ch.tweaklab.ip6.test.connector;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.tweaklab.ip6.connector.BrightSignWebConnector;
import ch.tweaklab.ip6.connector.Connector;
import ch.tweaklab.ip6.gui.ApplicationData;
import ch.tweaklab.ip6.gui.MainApp;
import ch.tweaklab.ip6.media.MediaFile;
import ch.tweaklab.ip6.media.XMLConfigCreator;
import ch.tweaklab.ip6.test.util.TestUtil;
import ch.tweaklab.ip6.util.PortScanner;

public class BrightSignWebTest {

  BrightSignWebConnector webConnector;
  Properties configFile;
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
    webConnector = new BrightSignWebConnector();
    configFile = new Properties();
    configFile.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
    connect();

  }
  
  
  
  

  @Test
  public void connect() {
    String target;
    List<String> ipList = PortScanner.getAllIpWithOpenPortInLocalSubnet(80);
    if(ipList.size() > 0){
      target = ipList.get(0);
    }
    else{
      target = "192.168.0.66";
    }
    Boolean connected = webConnector.connect(target);
    assertTrue(connected);

  }

  @Test
  public void uploadFiles() {
    try {

      List<MediaFile> mediaFiles = TestUtil.getMediaFiles();
      File configFile = XMLConfigCreator.createPlayListXML(mediaFiles);
      Task<Boolean> uploadTask = webConnector.uploadMediaFiles(mediaFiles,configFile);
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
  


  @Test
  public void sendSSH() {
    String scriptName = configFile.getProperty("resetMediaFolderScriptName");
    webConnector.RunScriptOverSSH(scriptName);
  }

}
