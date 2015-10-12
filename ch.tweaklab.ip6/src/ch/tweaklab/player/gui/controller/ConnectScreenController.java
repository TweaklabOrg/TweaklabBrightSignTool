package ch.tweaklab.player.gui.controller;

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
import ch.tweaklab.player.connector.BrightSignSdCardConnector;
import ch.tweaklab.player.connector.BrightSignWebConnector;
import ch.tweaklab.player.connector.Connector;
import ch.tweaklab.player.model.Keys;
import ch.tweaklab.player.util.KeyValueData;

/**
 * Controller of ConnectScreen.fxml
 * 
 * @author Alain
 *
 */
public class ConnectScreenController {

  

 

  @FXML
  private Label targetDescriptionLabel;
  
  
  @FXML
  private ComboBox<KeyValueData> connectorComboBox;



  @FXML
  private Button getTargetButton;

  @FXML
  private ComboBox<String> targetComboBox;



  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {

    KeyValueData sdConnector = new KeyValueData(BrightSignSdCardConnector.CLASS_DISPLAY_NAME, BrightSignSdCardConnector.class.getName());
    KeyValueData webConnector = new KeyValueData(BrightSignWebConnector.CLASS_DISPLAY_NAME, BrightSignWebConnector.class.getName());
    connectorComboBox.setItems(FXCollections.observableArrayList(webConnector, sdConnector));
    connectorComboBox.getSelectionModel().selectFirst();
    handleChangeConnector();
    
  }

  @FXML
  private void scanPossibleTargets(){
    MainApp.primaryStage.getScene().setCursor(Cursor.WAIT);
    Task<List<String>> possibleTargetsTask =  ControllerMediator.getInstance().getConnector().getPossibleTargets();   
    possibleTargetsTask.setOnSucceeded(event -> ScanTargetFinished(possibleTargetsTask));
    Thread uploadThread = new Thread(possibleTargetsTask);
    uploadThread.start();
  }

  private void ScanTargetFinished(Task<List<String>> possibleTargetsTask) {
    try {
      targetComboBox.getItems().setAll(possibleTargetsTask.get());
      targetComboBox.getSelectionModel().selectLast();
      MainApp.primaryStage.getScene().setCursor(Cursor.DEFAULT);
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
    }
  }

  @FXML
  private void handleChangeConnector() {
    targetComboBox.getItems().clear();
    KeyValueData keyValuedata = connectorComboBox.getSelectionModel().getSelectedItem();

    String className = keyValuedata.value;
    Class<?> clazz;
    try {
      clazz = Class.forName(className);
      Connector connector = (Connector) clazz.newInstance();
      ControllerMediator.getInstance().setConnector(connector);
      if(connector instanceof BrightSignWebConnector){
        this.targetDescriptionLabel.setText("Name:");
        targetComboBox.getItems().add(Keys.loadProperty(Keys.DEFAULT_HOSTNAME_PROPS_KEY));
        targetComboBox.getSelectionModel().selectLast();
        targetComboBox.setEditable(true);
      }
      else if(connector instanceof BrightSignSdCardConnector){
        this.targetDescriptionLabel.setText("Path to SD-Card:");
        targetComboBox.setEditable(false);
      }
        
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
    }

  }

  /**
   * Parse Class Name and invoke connect-method of choosen connector class
   */
  @FXML
  private void handleConnect() {

    String target = targetComboBox.getSelectionModel().getSelectedItem();

    if (target == null || target.length() < 1) {
      MainApp.showErrorMessage("Target not valid!", "Please enter a valid target address.");
      return;
    }
    ControllerMediator mediator = ControllerMediator.getInstance();
  ControllerMediator.getInstance().connectToDevice(target);
  }



}
