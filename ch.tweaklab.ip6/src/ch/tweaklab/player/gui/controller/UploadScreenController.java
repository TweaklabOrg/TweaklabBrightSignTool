package ch.tweaklab.player.gui.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import ch.tweaklab.player.configurator.XMLConfigCreator;
import ch.tweaklab.player.connector.Connector;
import ch.tweaklab.player.gui.view.WaitScreen;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.util.KeyValueData;

/**
 /*
 * 
 * @author Alf
 *
 */
public class UploadScreenController {


  WaitScreen waitScreen;
  Task<Boolean> uploadTask;
  private Thread uploadThread;



  @FXML
  private Label hostNameLabel;

@FXML
private Label currentUploadSetLabel;


  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
this.hostNameLabel.setText(ControllerMediator.getInstance().getConnector().getTarget());
    ControllerMediator.getInstance().setUploadController(this);
  }

  @FXML
  private void handleDisconnect(){
    
    ControllerMediator.getInstance().disconnectFromDevice();
  }

  public void updateCurrentUploadSetLabel(String playType){
    this.currentUploadSetLabel.setText(playType);
  }
  @FXML
  private void handleUpload() {
  

    try {
      // Show waitscreen
      waitScreen = new WaitScreen();
      waitScreen.setOnCancel(event -> uploadTask.cancel());
      waitScreen.setOnClose(event -> uploadTask.cancel());

      // Create Upload Task and add Events
      Connector connector = ControllerMediator.getInstance().getConnector();
     MediaUploadData uploadData = ControllerMediator.getInstance().getUploadData();
     
      uploadTask = connector.upload(uploadData);

      uploadTask.setOnSucceeded(event -> uploadTaskSucceedFinish());
      uploadTask.setOnCancelled(event -> uploadTaskAbortFinish());
      uploadTask.setOnFailed(event -> uploadTaskAbortFinish());

      uploadThread = new Thread(uploadTask);
      uploadThread.start();
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
    }
  }

  private void uploadTaskSucceedFinish() {
    try {
      if (uploadTask.get()) {
        waitScreen.closeScreen();
        MainApp.showInfoMessage("Upload finished!");
      } else {
        uploadTaskAbortFinish();
      }
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
    }
  }

  private void uploadTaskAbortFinish() {
    waitScreen.closeScreen();
    MainApp.showErrorMessage("Upload Failed", "An error occured during upload. Some files are not uploaded!");
  }



}
