package ch.tweaklab.ip6.gui.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import ch.tweaklab.ip6.connector.Connector;
import ch.tweaklab.ip6.gui.model.Context;
import ch.tweaklab.ip6.util.KeyValueData;

/**
 * Controller of RootPage.fxml Contains Connections Components and Tabview
 * 
 * @author Alf
 *
 */
public class RootPageController {

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

    KeyValueData sdConnector = new KeyValueData("BrightSign SD Card", "ch.tweaklab.ip6.connector.BrightSignSdCardConnector");
    KeyValueData webConnector = new KeyValueData("BrightSign Web", "ch.tweaklab.ip6.connector.BrightSignWebConnector");

    connectorComboBox.setItems(FXCollections.observableArrayList(webConnector, sdConnector));
    connectorComboBox.getSelectionModel().selectFirst();

    handleChangeConnector();

  }

  @FXML
  private void scanPossibleTargets(){
    MainApp.primaryStage.getScene().setCursor(Cursor.WAIT);
    Task<List<String>> possibleTargetsTask =  Context.getConnector().getPossibleTargets();   
    possibleTargetsTask.setOnSucceeded(event -> ScanTargetFinished(possibleTargetsTask));
    Thread uploadThread = new Thread(possibleTargetsTask);
    uploadThread.start();
  }

  private void ScanTargetFinished(Task<List<String>> possibleTargetsTask) {
    try {
      targetComboBox.getItems().setAll(possibleTargetsTask.get());
      targetComboBox.getSelectionModel().selectFirst();
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
      Context.setConnector(connector);
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

    if (target.length() < 1) {
      MainApp.showErrorMessage("Target not valid!", "Please enter a valid target address.");
      return;
    }
    Boolean isConnected = false;

    try {
      isConnected = Context.getConnector().connect(target);
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
    }
    if (isConnected) {
      hostNameLabel.setText(target);
      connectionStateLabel.setText("Connected");
      addTabs();
      showOrCloseConnectionMenu();
    } else {
      hostNameLabel.setText("none");
      MainApp.showErrorMessage("Connection failed!", "Please verify the target address.");
      connectionStateLabel.setText("Not connected");
    }
  }

  @FXML
  public void showOrCloseConnectionMenu() {
    if (connectMenuOpen) {
      splitPane.setDividerPosition(0, 0.06);
      connectionMenuButton.setText("more..");
      this.connectMenuOpen = false;
    } else {
      splitPane.setDividerPosition(0, 0.22);
      connectionMenuButton.setText("less..");
      this.connectMenuOpen = true;
    }

  }

  private void addTabs() {
    try {
      tabPane.getTabs().clear();
      // add playlist Tab
      Tab playlistTab = new Tab();
      playlistTab.setText("Playlist");
      tabPane.getTabs().add(playlistTab);
      playlistTab.setContent((Node) FXMLLoader.load(getClass().getResource("../view/PlaylistTab.fxml")));

      Tab buttonTab = new Tab();
      buttonTab.setText("Buttons");
      tabPane.getTabs().add(buttonTab);
      buttonTab.setContent((Node) FXMLLoader.load(getClass().getResource("../view/ButtonTab.fxml")));

    } catch (IOException e) {
      MainApp.showExceptionMessage(e);
    }

  }

  public void setHostNameLabelText(String hostNameLabelText) {
    this.hostNameLabel.setText(hostNameLabelText);
  }

}
