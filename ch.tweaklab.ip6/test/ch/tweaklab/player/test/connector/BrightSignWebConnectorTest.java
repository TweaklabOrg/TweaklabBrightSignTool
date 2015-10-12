package ch.tweaklab.player.test.connector;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.tweaklab.player.configurator.UploadFile;
import ch.tweaklab.player.configurator.XmlConfigCreator;
import ch.tweaklab.player.connector.BrightSignWebConnector;
import ch.tweaklab.player.gui.controller.ControllerMediator;
import ch.tweaklab.player.gui.controller.MainApp;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.model.ModeType;
import ch.tweaklab.player.test.util.TestUtil;

public class BrightSignWebConnectorTest {

  BrightSignWebConnector webConnector;
  Properties configFile;
  boolean success = true;

  // used to crate state for task
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
    webConnector.connect("tl-player");

  }

  @Test
  public void ScanTargets() {
    Task<List<String>> possibleTargetsTask = webConnector.getPossibleTargets();
    possibleTargetsTask.setOnSucceeded(event -> {
      List<String> targetList = null;
      try {
        targetList = possibleTargetsTask.get();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      assertTrue(targetList.size() > 1);
    });
    Thread uploadThread = new Thread(possibleTargetsTask);
    uploadThread.start();
  }



  @Test
  public void uploadFiles() {
    try {

      List<UploadFile> systemFiles = TestUtil.getSystemFiles();
      List<MediaFile> mediaFiles = TestUtil.getMediaFiles();
      UploadFile configFile = XmlConfigCreator.createPlayListXML(mediaFiles);
      MediaUploadData uploadData = new MediaUploadData(mediaFiles, configFile,ModeType.PLAYLIST);
      Task<Boolean> uploadTask = webConnector.upload(uploadData, systemFiles);
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
