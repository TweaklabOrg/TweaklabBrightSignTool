package ch.tweaklab.ip6.gui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ch.tweaklab.ip6.connector.BrightSignSdCardConnector;
import ch.tweaklab.ip6.connector.Connector;
import ch.tweaklab.ip6.util.KeyValueData;

public class DeviceSetupController {

  
  
  private Connector connector;
  private File targetDirectory;
  
  @FXML
  private TextField targetDirectoryTextField;
  
  
 
  
  
  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {

    connector = new BrightSignSdCardConnector();
    
  }
  
  
  
  
  
  
  
  
  
  
  
  @FXML
  private void handleGetTargetDirectory(){
  
  DirectoryChooser directoryChooser = new DirectoryChooser();
  targetDirectory = directoryChooser.showDialog(MainApp.primaryStage);
  if(targetDirectory != null){
    this.targetDirectoryTextField.setText(targetDirectory.getAbsolutePath());
  }
  else{
    System.out.println("blabla");
  }
  }
  
  
  
  @FXML
 private void handleSetupDevice(){
    connector.connect(targetDirectory.getAbsolutePath());
    if (connector.isConnected() == false){
      MainApp.showErrorMessage("wrong target", "this directory is not valid!");
    }
    try {
      FileUtils.cleanDirectory(targetDirectory);
      URL sourceUrl = this.getClass().getClassLoader().getResource("setup");
      File sourceDir = new File(sourceUrl.getPath()); 
      FileUtils.copyDirectory(sourceDir,targetDirectory);
      MainApp.showInfoMessage("BrightSign Device configurated. Please restart the device to activate the new configuration.");
    } catch (IOException e) {
      MainApp.showExceptionMessage(e);
    }
    
    
  }
  
}
