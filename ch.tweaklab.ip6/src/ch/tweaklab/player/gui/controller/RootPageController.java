package ch.tweaklab.player.gui.controller;

import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import ch.tweaklab.player.connector.Connector;
import ch.tweaklab.player.model.Mediator;
import ch.tweaklab.player.util.KeyValueData;

/**
 * Controller of RootPage.fxml Contains Connections Components and Tabview
 * 
 * @author Alf
 *
 */
public class RootPageController {



  
  @FXML
  BorderPane rootBorderPane;
  
  @FXML
  private SplitPane splitPane;

  @FXML
  private Button connectionMenuButton;

  @FXML
  private Label hostNameLabel;

  @FXML
  private ComboBox<KeyValueData> connectorComboBox;

  @FXML
  private Label connectionStateLabel;

  @FXML
  private TabPane tabPane;

  @FXML
  private Button getTargetButton;

  @FXML
  private ComboBox<String> targetComboBox;

  private Boolean connectMenuOpen = true;

  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
    Mediator.getInstance().setRootController(this);
  
    loadConnectItems();

    
  }

  
  public void loadUploadItems(){
    try {
      if(splitPane.getItems().size() > 1){
      splitPane.getItems().remove(1);
      }
      // Load root layout from fxml file.
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(this.getClass().getResource("../view/UploadScreen.fxml"));
      AnchorPane connectLayout = (AnchorPane) loader.load();
      
      splitPane.getItems().add(connectLayout);
      splitPane.setDividerPosition(0, 0.8);

    } catch (IOException e) {
      e.printStackTrace();
    }

  }
  
  
 public void loadConnectItems(){
   try {
     if(splitPane.getItems().size() > 1){
       splitPane.getItems().remove(1);
       }
     // Load root layout from fxml file.
     FXMLLoader loader = new FXMLLoader();
     loader.setLocation(this.getClass().getResource("../view/ConnectScreen.fxml"));
     AnchorPane connectLayout = (AnchorPane) loader.load();

     splitPane.setDividerPosition(0, 0.8);
     splitPane.getItems().add(connectLayout);
     

   } catch (IOException e) {
     e.printStackTrace();
   }

 }
  
 
 public void addTabs() {
   try {
     tabPane.getTabs().remove(0);
     // add playlist Tab
     Tab playlistTab = new Tab();
     playlistTab.setText("Playlist Config");
     tabPane.getTabs().add(playlistTab);
     playlistTab.setContent((Node) FXMLLoader.load(getClass().getResource("../view/PlaylistTab.fxml")));

     Tab buttonTab = new Tab();
     buttonTab.setText("GPIO Config");
     tabPane.getTabs().add(buttonTab);
     buttonTab.setContent((Node) FXMLLoader.load(getClass().getResource("../view/GpioTab.fxml")));
     

   } catch (IOException e) {
     MainApp.showExceptionMessage(e);
   }

 }
 
 
 
 
//  @FXML
//  private void scanPossibleTargets(){
//    MainApp.primaryStage.getScene().setCursor(Cursor.WAIT);
//    Task<List<String>> possibleTargetsTask =  Mediator.getConnector().getPossibleTargets();   
//    possibleTargetsTask.setOnSucceeded(event -> ScanTargetFinished(possibleTargetsTask));
//    Thread uploadThread = new Thread(possibleTargetsTask);
//    uploadThread.start();
//  }
//
//  private void ScanTargetFinished(Task<List<String>> possibleTargetsTask) {
//    try {
//      targetComboBox.getItems().setAll(possibleTargetsTask.get());
//      targetComboBox.getSelectionModel().selectFirst();
//      MainApp.primaryStage.getScene().setCursor(Cursor.DEFAULT);
//    } catch (Exception e) {
//      MainApp.showExceptionMessage(e);
//    }
//  }
//
//  @FXML
//  private void handleChangeConnector() {
//    targetComboBox.getItems().clear();
//    KeyValueData keyValuedata = connectorComboBox.getSelectionModel().getSelectedItem();
//
//    String className = keyValuedata.value;
//    Class<?> clazz;
//    try {
//      clazz = Class.forName(className);
//      Connector connector = (Connector) clazz.newInstance();
//      Mediator.setConnector(connector);
//    } catch (Exception e) {
//      MainApp.showExceptionMessage(e);
//    }
//
//  }
//
//  /**
//   * Parse Class Name and invoke connect-method of choosen connector class
//   */
//  @FXML
//  private void handleConnect() {
//
//    String target = targetComboBox.getSelectionModel().getSelectedItem();
//
//    if (target == null || target.length() < 1) {
//      MainApp.showErrorMessage("Target not valid!", "Please enter a valid target address.");
//      return;
//    }
//    Boolean isConnected = false;
//
//    try {
//      isConnected = Mediator.getConnector().connect(target);
//    } catch (Exception e) {
//      MainApp.showExceptionMessage(e);
//    }
//    if (isConnected) {
//      hostNameLabel.setText(target);
//      connectionStateLabel.setText("Connected");
//      addTabs();
//      showOrCloseConnectionMenu();
//    } else {
//      hostNameLabel.setText("none");
//      MainApp.showErrorMessage("Connection failed!", "Please verify the target address.");
//      connectionStateLabel.setText("Not connected");
//    }
//  }
//
//  @FXML
//  public void showOrCloseConnectionMenu() {
//    if (connectMenuOpen) {
//      rootBorderPane.setPrefHeight(830);
//      MainApp.primaryStage.setMinHeight(830);
//      MainApp.primaryStage.setMaxHeight(830);
//      splitPane.setDividerPosition(0, 0.06);
//      connectionMenuButton.setText("more..");
//      this.connectMenuOpen = false;
//    } else {
//      MainApp.primaryStage.setMinHeight(980);
//      MainApp.primaryStage.setMaxHeight(980);
//      splitPane.setDividerPosition(0, 0.22);
//      connectionMenuButton.setText("less..");
//      this.connectMenuOpen = true;
//    }
//
//  }
// public void setHostNameLabelText(String hostNameLabelText) {
//   this.hostNameLabel.setText(hostNameLabelText);
// }
  



}
