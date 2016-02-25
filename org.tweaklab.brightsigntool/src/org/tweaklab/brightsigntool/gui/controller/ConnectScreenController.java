package org.tweaklab.brightsigntool.gui.controller;

import org.tweaklab.brightsigntool.connector.BrightSignSdCardConnector;
import org.tweaklab.brightsigntool.connector.BrightSignWebConnector;
import org.tweaklab.brightsigntool.connector.Connector;
import org.tweaklab.brightsigntool.model.Keys;
import org.tweaklab.brightsigntool.util.KeyValueData;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller of ConnectScreen.fxml
 * 
 * @author Alain
 *
 */
public class ConnectScreenController {
  private static final Logger LOGGER = Logger.getLogger(ConnectScreenController.class.getName());

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
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Not able to collect results.", e);
    }
    targetComboBox.getSelectionModel().selectFirst();
    MainApp.primaryStage.getScene().setCursor(Cursor.DEFAULT);
  }

  @FXML
  private void handleChangeConnector() {
    targetComboBox.getItems().clear();
    KeyValueData keyValuedata = connectorComboBox.getSelectionModel().getSelectedItem();

    String className = keyValuedata.value;
    Class<?> clazz;
    Connector connector = null;
    try {
      clazz = Class.forName(className);
      connector = (Connector) clazz.newInstance();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "The " + className + "connector class couldn't be constructed.", e);
    }
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
  }

  /**
   * Parse Class Name and invoke connect-method of choosen connector class
   */
  @FXML
  private void handleConnect() {
    String target = targetComboBox.getSelectionModel().getSelectedItem();

    if (target == null || target.length() < 1) {
      MainApp.showErrorMessage("Target not valid!", "Please enter a valid target address.");
      LOGGER.log(Level.INFO, target + " not valid!");
      return;
    }
    ControllerMediator.getInstance().connectToDevice(target);
  }
}
