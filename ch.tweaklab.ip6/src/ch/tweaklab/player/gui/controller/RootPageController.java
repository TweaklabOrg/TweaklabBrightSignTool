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
import ch.tweaklab.player.model.Keys;
import ch.tweaklab.player.util.KeyValueData;

/**
 * Controller of RootPage.fxml Contains Connections Components and Tabview
 * 
 * @author Alain
 *
 */
public class RootPageController {

  @FXML
  BorderPane rootBorderPane;

  @FXML
  private SplitPane splitPane;

  @FXML
  private TabPane tabPane;

  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
    ControllerMediator.getInstance().setRootController(this);

    loadConnectItems();

  }
  
  
  public void disconnectFromDevice() {
    tabPane.getTabs().clear();
    this.loadConnectItems();
    
  }

  public void connectToDevice() {
    this.addTabs();
    this.loadUploadItems();
    
  }

  private void loadUploadItems() {
    try {
      if (splitPane.getItems().size() > 1) {
        splitPane.getItems().remove(1);
      }
      // Load root layout from fxml file.
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(this.getClass().getResource(Keys.UPLOAD_SCREEN_FXML_PATH));
      AnchorPane connectLayout = (AnchorPane) loader.load();

      splitPane.getItems().add(connectLayout);
      splitPane.setDividerPosition(0, 0.75);

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void loadConnectItems() {
    try {
      if (splitPane.getItems().size() > 1) {
        splitPane.getItems().remove(1);
      }
      // Load root layout from fxml file.
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(this.getClass().getResource(Keys.CONNECT_SCREEN_FXML_PATH));
      AnchorPane connectLayout = (AnchorPane) loader.load();

      splitPane.setDividerPosition(0, 0.75);
      splitPane.getItems().add(connectLayout);

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void addTabs() {
    try {
      tabPane.getTabs().clear();
      // add playlist Tab
      Tab playlistTab = new Tab();
      playlistTab.setText("Playlist Config");
      tabPane.getTabs().add(playlistTab);
      playlistTab.setContent((Node) FXMLLoader.load(getClass().getResource(Keys.PLAYLIST_TAB_FXML_PATH)));

      Tab buttonTab = new Tab();
      buttonTab.setText("GPIO Config");
      tabPane.getTabs().add(buttonTab);
      buttonTab.setContent((Node) FXMLLoader.load(getClass().getResource(Keys.GPIO_TAB_FXML_PATH)));

    } catch (IOException e) {
      MainApp.showExceptionMessage(e);
    }

  }



}
