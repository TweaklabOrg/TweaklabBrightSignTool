package ch.tweaklab.player.test.connector;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.tweaklab.player.configurator.PlayerGeneralSettings;
import ch.tweaklab.player.configurator.XMLConfigCreator;
import ch.tweaklab.player.connector.BrightSignSdCardConnector;
import ch.tweaklab.player.gui.controller.ControllerMediator;
import ch.tweaklab.player.gui.controller.MainApp;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.model.PlayModusType;
import ch.tweaklab.player.test.util.TestUtil;

public class BrightSignSdCardConnectorTest {

  BrightSignSdCardConnector sdConnector;
  boolean success = true;
  List<String> targetList;
  
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
    Task<List<String>> possibleTargetsTask =  ControllerMediator.getInstance().getConnector().getPossibleTargets();   
    possibleTargetsTask.setOnSucceeded(event -> ScanTargetFinished(possibleTargetsTask));
    Thread uploadThread = new Thread(possibleTargetsTask);
    uploadThread.start();
  }
  
  
  private void ScanTargetFinished(Task<List<String>> possibleTargetsTask) {
    try {
       
      String target;
      List<String> targetList = possibleTargetsTask.get();
      if(targetList.size() > 0){
        target = targetList.get(0);
      }
      else{
        target = "C:\\temp\\";
      }
      Boolean connected = sdConnector.connect(target);
      assertTrue(connected);
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
    }
  }
  
  
  
  

  @Test
  public void uploadFiles() {
    try {
      List<File> systemFiles = TestUtil.getSystemFiles();
      List<MediaFile> mediaFiles = TestUtil.getMediaFiles();
 
      File configFile = XMLConfigCreator.createPlayListXML(mediaFiles);
      MediaUploadData uploadData = new MediaUploadData(PlayModusType.PLAYLIST, mediaFiles, configFile);
      Task<Boolean> uploadTask = sdConnector.upload(uploadData,systemFiles);
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
