package org.tweaklab.brightsigntool.test.connector;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.configurator.XmlConfigCreator;
import org.tweaklab.brightsigntool.connector.BrightSignSdCardConnector;
import org.tweaklab.brightsigntool.gui.controller.ControllerMediator;
import org.tweaklab.brightsigntool.model.MediaFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import org.tweaklab.brightsigntool.model.ModeType;
import org.tweaklab.brightsigntool.test.util.TestUtil;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class BrightSignSdCardConnectorTest {

  BrightSignSdCardConnector sdConnector;
  boolean success = true;

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
    Task<List<String>> possibleTargetsTask = ControllerMediator.getInstance().getConnector().getPossibleTargets();
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
        target = "C:\\temp\\";
      }
      Boolean connected = sdConnector.connect(target);
      assertTrue(connected);
    } catch (Exception e) {
      assertTrue(e.getMessage(), false);
    }
  }

  @Test
  public void uploadFiles() {
    try {
      List<UploadFile> systemFiles = TestUtil.getSystemFiles();
      List<MediaFile> mediaFiles = TestUtil.getMediaFiles();

      UploadFile uploadFile = XmlConfigCreator.createPlayListXML(mediaFiles);
      MediaUploadData uploadData = new MediaUploadData(mediaFiles, uploadFile, ModeType.GPIO);
      Task<Boolean> uploadTask = sdConnector.upload(uploadData, systemFiles);
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

  public static class AsNonApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
      // noop
    }
  }


}
