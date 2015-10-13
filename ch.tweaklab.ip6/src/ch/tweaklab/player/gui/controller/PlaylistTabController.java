package ch.tweaklab.player.gui.controller;

import ch.tweaklab.player.configurator.UploadFile;
import ch.tweaklab.player.configurator.XmlConfigCreator;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.model.MediaType;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.model.ModeType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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

  @FXML
  private TextField displayTimeField;

  @FXML
  private Label displayTimeLabel;

  @FXML
  private Label fileNameField;

  @FXML
  private Label mediaTypeField;

  @FXML
  private Label fileSizeLabel;

  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
    clearMediaFileDetailInformations();

    displayTimeField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
        MediaFile selectedMediaFile = listView.getSelectionModel().getSelectedItem();
        if (selectedMediaFile != null && selectedMediaFile.getMediaType() == MediaType.IMAGE) {
          selectedMediaFile.setDisplayTime(Integer.valueOf(newValue));
        }
      }
    });

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
        handleMouseClickedInListView();
      }
    }
  }

  @FXML
  private void handleRemove() {
    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
    listView.getItems().remove(selectedIndex);
    clearMediaFileDetailInformations();
    if (listView.getItems().size() < 1) {
    }
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

  @FXML
  private void handleMouseClickedInListView() {
    MediaFile selectedMediaFile = listView.getSelectionModel().getSelectedItem();
    if (selectedMediaFile != null) {
      this.fileNameField.setText(selectedMediaFile.getFile().getName());
      this.mediaTypeField.setText(selectedMediaFile.getMediaType().toString());
      this.fileSizeLabel.setText(selectedMediaFile.getFileSize());
      this.displayTimeField.setText(String.valueOf(selectedMediaFile.getDisplayTime()));
      if (selectedMediaFile.getMediaType() == MediaType.IMAGE) {
        this.displayTimeField.setVisible(true);
        this.displayTimeLabel.setVisible(true);

      } else {

        this.displayTimeField.setVisible(false);
        this.displayTimeLabel.setVisible(false);
      }

    }
  }

  // private void clearPlaylist() {
  // this.listView.getItems().clear();
  // clearMediaFileDetailInformations();
  // uploadButton.setDisable(true);
  // this.imageView.setImage(null);
  // }

  private void clearMediaFileDetailInformations() {
    this.fileNameField.setText("");
    this.mediaTypeField.setText("");
    this.fileSizeLabel.setText("");
    this.displayTimeField.setVisible(false);
    this.displayTimeLabel.setVisible(false);
  }

  @Override
  public MediaUploadData getMediaUploadData() {
	UploadFile configFile = XmlConfigCreator.createPlayListXML(listView.getItems());
    MediaUploadData mediaUploadData = new MediaUploadData(listView.getItems(), configFile,ModeType.PLAYLIST);
    return mediaUploadData;
  }

}
