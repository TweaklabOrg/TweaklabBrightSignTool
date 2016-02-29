package org.tweaklab.brightsigntool.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.tweaklab.brightsigntool.model.Keys;
import org.tweaklab.brightsigntool.model.MediaUploadData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Controller of RootPage.fxml Contains Connections Components and Tabview
 *
 * @author Alain + Stephan
 */
public class RootPageController {
  private static final Logger LOGGER = Logger.getLogger(RootPageController.class.getName());

  @FXML
  BorderPane rootBorderPane;

  @FXML
  private SplitPane splitPane;

  @FXML
  private TabPane tabPane;

  private List<TabController> tabControllers = new ArrayList<>();
  private TabController currentTabController;

  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
    ControllerMediator.getInstance().setRootController(this);
    loadConnectItems();
    addTabs();
  }

  public void disconnectFromDevice() {
    this.loadConnectItems();
  }

  public void connectToDevice(Map<String, String> settingsOnDevice) {
    this.loadUploadItems(settingsOnDevice);
    // TODO load content of settingsOnDevice into gpio and playlist tab
  }

  private void loadUploadItems(Map<String, String> settingsOnDevice) {
    if (splitPane.getItems().size() > 1) {
      splitPane.getItems().remove(1);
    }
    // Load root layout from fxml file.
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(this.getClass().getResource(Keys.UPLOAD_SCREEN_FXML_PATH));
    AnchorPane connectLayout = null;
    try {
      connectLayout = loader.load();
    } catch (IOException e) {
      LOGGER.severe("FXMLLoader can't load UploadScreen.fxml.");
    }
    UploadScreenController controller = loader.<UploadScreenController>getController();
    controller.initData(settingsOnDevice);
    splitPane.getItems().add(connectLayout);
    splitPane.setDividerPosition(0, 0.7);
  }

  private void loadConnectItems() {
    if (splitPane.getItems().size() > 1) {
      splitPane.getItems().remove(1);
    }
    // Load root layout from fxml file.
    FXMLLoader loader = new FXMLLoader(getClass().getResource(Keys.CONNECT_SCREEN_FXML_PATH));
    AnchorPane connectLayout = null;
    try {
      connectLayout = loader.load();
    } catch (IOException e) {
      LOGGER.severe("FXMLLoader can't load ConnectScreen.fxml.");
    }
    splitPane.setDividerPosition(0, 0.7);
    splitPane.getItems().add(connectLayout);

  }

  private void addTabs() {
    FXMLLoader playlistLoader = new FXMLLoader(getClass().getResource(Keys.PLAYLIST_TAB_FXML_PATH));
    Node node = null;
    try {
      node = playlistLoader.load();
    } catch (IOException e) {
      LOGGER.severe("FXMLLoader can't load PlaylistTab.fxml.");
    }
    PlaylistTabController playlistController = playlistLoader.<PlaylistTabController>getController();
    tabControllers.add(playlistController);
    Tab playlistTab = new Tab();
    playlistTab.setText("Playlist Config");
    tabPane.getTabs().add(playlistTab);
    playlistTab.setContent(node);
    playlistTab.setOnSelectionChanged(t -> handleTabChange());
    FXMLLoader gpioLoader = new FXMLLoader(getClass().getResource(Keys.GPIO_TAB_FXML_PATH));
    Node gpioTabContent = null;
    try {
      gpioTabContent = gpioLoader.load();
    } catch (IOException e) {
      LOGGER.severe("FXMLLoader can't load GpioTab.fxml.");
    }
    GpioTabController gpioTabController = gpioLoader.<GpioTabController>getController();
    tabControllers.add(gpioTabController);
    Tab gpioTab = new Tab();
    gpioTab.setText("GPIO Config");
    tabPane.getTabs().add(gpioTab);
    gpioTab.setContent(gpioTabContent);
    gpioTab.setOnSelectionChanged(t -> handleTabChange());
    handleTabChange();
  }

  private void handleTabChange() {
    currentTabController = tabControllers.get(tabPane.getSelectionModel().getSelectedIndex());
    LOGGER.info("Tab changes to " + currentTabController.getClass().getName());
  }

  public MediaUploadData getMediaUploadData() {
    return currentTabController.getMediaUploadData();
  }

}
