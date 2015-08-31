package ch.tweaklab.ip6.test.connector;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.tweaklab.ip6.connector.BrightSignWebConnector;
import ch.tweaklab.ip6.gui.controller.MainApp;
import ch.tweaklab.ip6.gui.model.Context;
import ch.tweaklab.ip6.mediaLogic.MediaFile;
import ch.tweaklab.ip6.mediaLogic.XMLConfigCreator;
import ch.tweaklab.ip6.test.util.TestUtil;

public class BrightSignWebConnectorTest {

  BrightSignWebConnector webConnector;
  Properties configFile;
  boolean success = true;

  
  //used to crate state for task
  public static class AsNonApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
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
    Task<List<String>> possibleTargetsTask =  Context.getConnector().getPossibleTargets();   
    possibleTargetsTask.setOnSucceeded(event -> ScanTargetFinished(possibleTargetsTask));
    Thread uploadThread = new Thread(possibleTargetsTask);
    uploadThread.start();
  }
  private void ScanTargetFinished(Task<List<String>> possibleTargetsTask) {
    try {
       
      String target;
      List<String> targetList = possibleTargetsTask.get();
      if (targetList.size() > 0) {
        target = targetList.get(0);
      } else {
        target = "192.168.0.66";
      }
      Boolean connected = webConnector.connect(target);
      assertTrue(connected);
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
    }
  }
  
  
  
  
  
  
  

  @Test
  public void uploadFiles() {
    try {

      List<MediaFile> mediaFiles = TestUtil.getMediaFiles();
      File configFile = XMLConfigCreator.createPlayListXML(mediaFiles);
      Task<Boolean> uploadTask = webConnector.uploadMediaFiles(mediaFiles, configFile);
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
