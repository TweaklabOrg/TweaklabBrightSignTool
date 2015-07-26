package ch.tweaklab.ip6.gui.view;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ch.tweaklab.ip6.application.model.ApplicationData;
import ch.tweaklab.ip6.connector.Connector;
import ch.tweaklab.ip6.util.KeyValueData;

public class RootPageController {

  @FXML
  private TextField hostnameField;

  @FXML
  private SplitPane splitPane;
  
  @FXML
  private Button connectionMenuButton;

  @FXML
  private Label hostNameLabel;

  @FXML
  private ChoiceBox<KeyValueData> connectorChoice = new ChoiceBox<KeyValueData>();

  @FXML
  private Label connectionStateLabel;

  @FXML
  private TabPane tabPane;
  
  private Boolean connectMenuOpen = true;
  private Stage dialogStage;

  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
    KeyValueData webConnector = new KeyValueData("Bright Sign Web", "ch.tweaklab.ip6.connector.BrightSignWebConnector");

    connectorChoice.setItems(FXCollections.observableArrayList(webConnector));
    connectorChoice.getSelectionModel().selectFirst();
  }


  /**
   * Parse Class Name and invoke connect-method of choosen connector class
   */
  @FXML
  private void handleConnect() {

    String hostname = hostnameField.getText();

    if (hostname.length() < 1) {
      showErrorMessage("Hostname not valid!");
      return;
    }

    KeyValueData keyValuedata = connectorChoice.getSelectionModel().getSelectedItem();

    String className = keyValuedata.value;
    Class<?> clazz;
    try {
      clazz = Class.forName(className);
      if (Connector.class.isAssignableFrom(clazz)) {

        Connector connector = (Connector) clazz.newInstance();
        ApplicationData.setConnector(connector);
        ApplicationData.getConnector().connect(hostname);
        hostNameLabel.setText(hostname);

        addTabs();
        showOrCloseConnectionMenu();
      } else {
        showErrorMessage("Connector Class " + className + " not valid. Class must implement Connector Interface");
      }
    } catch (Exception e) {
      showErrorMessage(e.getMessage());
      e.printStackTrace();
    }
    if (ApplicationData.getConnector().getIsConnected()) {
      connectionStateLabel.setText("Connected");
    } else {
      connectionStateLabel.setText("Not connected");
    }
  }

  @FXML
  public void showOrCloseConnectionMenu() {
    if (connectMenuOpen) {
      splitPane.setDividerPosition(0, 0.07);
      connectionMenuButton.setText("more..");
      this.connectMenuOpen = false;
    } else {
      splitPane.setDividerPosition(0, 0.35);
      connectionMenuButton.setText("less..");
      this.connectMenuOpen = true;
    }

  }

  private void addTabs() {
    try {
      tabPane.getTabs().clear();
      
       FXMLLoader loader = new FXMLLoader(this.getClass().getResource("ContentManagerTab.fxml"));
      //add MediaContent Tab
      Tab contentTab = new Tab();
      contentTab.setText("Content Manager");
      tabPane.getTabs().add(contentTab);
      contentTab.setContent((Node) loader.load());
      ContentManagerTabController contentTabController = loader.getController();
      contentTabController.setRootPageController(this);
      
      
    } catch (IOException e) {
      showErrorMessage(e.getMessage());
      e.printStackTrace();
    }

  }
  
  public void showErrorMessage(String errorMessage){
    Alert alert = new Alert(AlertType.WARNING);
    alert.initOwner(dialogStage);
    alert.setTitle("Error!");
    alert.setHeaderText("An Error Occured");
    alert.setContentText(errorMessage);
    alert.showAndWait();
  
  }
  
  
  public Stage getDialogStage() {
    return dialogStage;
  }


  public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }

  public void setHostNameLabelText(String hostNameLabelText) {
    this.hostNameLabel.setText(hostNameLabelText);
  }

}
