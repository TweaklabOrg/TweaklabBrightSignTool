package ch.tweaklab.ip6.gui.controller;

import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import ch.tweaklab.ip6.connector.Connector;
import ch.tweaklab.ip6.gui.MainApp;
import ch.tweaklab.ip6.model.ApplicationData;
import ch.tweaklab.ip6.util.KeyValueData;

/**
 * Controler of RootPage.fxml Contains Connections Components and Tabview
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
  private ComboBox<String> targetComboBox;

  private Boolean connectMenuOpen = true;

  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
    KeyValueData webConnector = new KeyValueData("Bright Sign Web", "ch.tweaklab.ip6.connector.BrightSignWebConnector");
    KeyValueData sdConnector = new KeyValueData("Bright Sign SD Card", "ch.tweaklab.ip6.connector.BrightSignSdCardConnector");

    connectorComboBox.setItems(FXCollections.observableArrayList(webConnector, sdConnector));
    connectorComboBox.getSelectionModel().selectFirst();

    handleChangeConnector();
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
      ApplicationData.setConnector(connector);
      List<String> possibleTargets = connector.getPossibleTargets();
      targetComboBox.getItems().setAll(possibleTargets);
      targetComboBox.getSelectionModel().selectFirst();
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
      isConnected = ApplicationData.getConnector().connect(target);
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
      // add MediaContent Tab
      Tab contentTab = new Tab();
      contentTab.setText("Content Manager");
      tabPane.getTabs().add(contentTab);
      contentTab.setContent((Node) FXMLLoader.load(getClass().getResource("../view/PlaylistTab.fxml")));

    } catch (IOException e) {
      MainApp.showExceptionMessage(e);
    }

  }

  public void setHostNameLabelText(String hostNameLabelText) {
    this.hostNameLabel.setText(hostNameLabelText);
  }

}
