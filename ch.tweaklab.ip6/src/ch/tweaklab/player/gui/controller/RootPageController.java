package ch.tweaklab.player.gui.controller;

import ch.tweaklab.player.model.Keys;
import ch.tweaklab.player.model.MediaUploadData;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller of RootPage.fxml Contains Connections Components and Tabview
 * 
 * @author Alain
 *
 */
public class RootPageController  {

  @FXML
  BorderPane rootBorderPane;

  @FXML
  private SplitPane splitPane;

  @FXML
  private TabPane tabPane;

  private List<TabController> tabControllers = new ArrayList<TabController>();
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
    try {
      if (splitPane.getItems().size() > 1) {
        splitPane.getItems().remove(1);
      }
      // Load root layout from fxml file.
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(this.getClass().getResource(Keys.UPLOAD_SCREEN_FXML_PATH));
      AnchorPane connectLayout = (AnchorPane) loader.load();

      UploadScreenController controller = loader.<UploadScreenController>getController();
      controller.initData(settingsOnDevice);

      splitPane.getItems().add(connectLayout);
      splitPane.setDividerPosition(0, 0.7);

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
      FXMLLoader loader = new FXMLLoader(getClass().getResource(Keys.CONNECT_SCREEN_FXML_PATH));
      AnchorPane connectLayout = (AnchorPane) loader.load();

      splitPane.setDividerPosition(0, 0.7);
      splitPane.getItems().add(connectLayout);

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void addTabs() {
    try {
      FXMLLoader playlistLoader = new FXMLLoader(getClass().getResource(Keys.PLAYLIST_TAB_FXML_PATH));
      Node node = (Node) playlistLoader.load();
      PlaylistTabController playlistController = playlistLoader.<PlaylistTabController> getController();
      tabControllers.add(playlistController);
      Tab playlistTab = new Tab();
      playlistTab.setText("Playlist Config");
      tabPane.getTabs().add(playlistTab);
      playlistTab.setContent(node);
      playlistTab.setOnSelectionChanged(new EventHandler<Event>() {
        @Override
        public void handle(Event t) {
          handleTabChange();
        }
      });

      FXMLLoader gpioLoader = new FXMLLoader(getClass().getResource(Keys.GPIO_TAB_FXML_PATH));
      Node gpioTabContent = (Node) gpioLoader.load();
      GpioTabController gpioTabController = gpioLoader.<GpioTabController> getController();
      tabControllers.add(gpioTabController);
      Tab gpioTab = new Tab();
      gpioTab.setText("GPIO Config");
      tabPane.getTabs().add(gpioTab);
      gpioTab.setContent(gpioTabContent);
      gpioTab.setOnSelectionChanged(new EventHandler<Event>() {
        @Override
        public void handle(Event t) {
          handleTabChange();
        }
      });

      handleTabChange();
    } catch (IOException e) {
      MainApp.showExceptionMessage(e);
    }

  }

  private void handleTabChange() {
    currentTabController = tabControllers.get(tabPane.getSelectionModel().getSelectedIndex());
  }

  public MediaUploadData getMediaUploadData() {
    return currentTabController.getMediaUploadData();
  }

}
