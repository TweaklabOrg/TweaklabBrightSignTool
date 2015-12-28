package ch.tweaklab.player.gui.controller;

import ch.tweaklab.player.configurator.UploadFile;
import ch.tweaklab.player.configurator.XmlConfigCreator;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.model.ModeType;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Controller Class for ContentManagerTab.fxml Manages Upload of a playlist to device
 * 
 * @author Alf
 *
 */
public class PlaylistTabController extends TabController {

  @FXML
  private ListView<MediaFile> listView;

  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
  }

  @FXML
  private void handleAddFileToListView() {
    final FileChooser fileChooser = new FileChooser();
    List<File> choosenFiles = fileChooser.showOpenMultipleDialog(MainApp.primaryStage);
    // TODO Stephan: handle no files where chosen
    for (File choosenFile : choosenFiles) {
      if (choosenFile != null) {
        if (this.validateFileFormat(choosenFile.getName()) == false) {
          MainApp.showErrorMessage("Wrong File", "This filetype is not supported.");
          return;
        }
        MediaFile mediaFile = new MediaFile(choosenFile);
        listView.getItems().add(mediaFile);
        listView.getSelectionModel().selectLast();
      }
    }
  }

  @FXML
  private void handleRemove() {
    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
    listView.getItems().remove(selectedIndex);
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
    MediaUploadData mediaUploadData = new MediaUploadData(listView.getItems(), configFile,ModeType.PLAYLIST);
    return mediaUploadData;
  }

}
