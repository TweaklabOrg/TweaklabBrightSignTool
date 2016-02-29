package org.tweaklab.brightsigntool.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.configurator.XmlConfigCreator;
import org.tweaklab.brightsigntool.model.MediaFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import org.tweaklab.brightsigntool.model.ModeType;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Controller Class for ContentManagerTab.fxml Manages Upload of a playlist to device
 *
 * @author Alain + Stephan
 */
public class PlaylistTabController extends TabController {
  private static final Logger LOGGER = Logger.getLogger(PlaylistTabController.class.getName());

  @FXML
  private ListView<MediaFile> listView;

  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
  }

  @Override
  public void setContent(Map<String, String> content) {
    // TODO: building filemanagement to make that possible. For ex. skip mediaupload of already existing files, but allow modifications on settings.
//    listView.getItems().clear();
//    String searchString = "task0";
//    int i = 0;
//    while (content.containsKey(searchString)) {
//      listView.getItems().add(new MediaFile(new File(content.get(searchString))));
//      i++;
//      searchString = "task" + i;
//    }
//    listView.getSelectionModel().selectLast();
  }

  @FXML
  private void handleAddFileToListView() {
    final FileChooser fileChooser = new FileChooser();
    List<File> choosenFiles = fileChooser.showOpenMultipleDialog(MainApp.primaryStage);
    choosenFiles = (choosenFiles == null ? new LinkedList<>() : choosenFiles);
    for (File choosenFile : choosenFiles) {
      if (choosenFile != null) {
        if (!this.validateFileFormat(choosenFile.getName())) {
          LOGGER.warning("Filetype not supported: " + choosenFile.getName());
          new Alert(Alert.AlertType.NONE, "Filetype not supported: " + choosenFile.getName(), ButtonType.OK).showAndWait();
          return;
        }
        MediaFile mediaFile = new MediaFile(choosenFile);
        listView.getItems().add(mediaFile);
        listView.getSelectionModel().selectLast();
        LOGGER.info(choosenFile.getName() + " added to playlist.");
      }
    }
  }

  @FXML
  private void handleRemove() {
    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
    listView.getItems().remove(selectedIndex);
    LOGGER.info(listView.getSelectionModel().getSelectedItem() + "removed.");
  }

  @FXML
  private void moveItemUp() {
    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
    if (selectedIndex > 0) {
      Collections.swap(listView.getItems(), selectedIndex, selectedIndex - 1);
      listView.getSelectionModel().select(selectedIndex - 1);
    }
  }

  @FXML
  private void moveItemDown() {
    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
    if (selectedIndex < listView.getItems().size() - 1)
      Collections.swap(listView.getItems(), selectedIndex, selectedIndex + 1);
    listView.getSelectionModel().select(selectedIndex + 1);
  }

  @Override
  public MediaUploadData getMediaUploadData() {
    UploadFile configFile = XmlConfigCreator.createPlayListXML(listView.getItems());
    MediaUploadData mediaUploadData = new MediaUploadData(listView.getItems(), configFile, ModeType.PLAYLIST);
    LOGGER.info(mediaUploadData.getUploadList().size() + " files collected to upload.");
    return mediaUploadData;
  }

}
