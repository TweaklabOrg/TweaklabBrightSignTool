package ch.tweaklab.player.gui.controller;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import ch.tweaklab.player.configurator.XMLConfigCreator;
import ch.tweaklab.player.connector.Connector;
import ch.tweaklab.player.gui.view.WaitScreen;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.model.MediaType;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.model.PlayModusType;

/**
 * Controller Class for ContentManagerTab.fxml Manages Upload of a playlist to device
 * 
 * @author Alf
 *
 */
public class PlaylistTabController {

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

  @FXML
  private Button uploadButton;


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
    uploadButton.setDisable(true);

  }

  @FXML
  private void handleAddFileToListView() {
    final FileChooser fileChooser = new FileChooser();
    File choosenFile = fileChooser.showOpenDialog(MainApp.primaryStage);
    if (choosenFile != null) {
      MediaFile mediaFile = new MediaFile(choosenFile);
      listView.getItems().add(mediaFile);
      uploadButton.setDisable(false);
      listView.getSelectionModel().selectLast();
      handleMouseClickedInListView();
    }
  }

  @FXML
  private void handleRemove() {
    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
    listView.getItems().remove(selectedIndex);
    clearMediaFileDetailInformations();
    if (listView.getItems().size() < 1) {
      uploadButton.setDisable(true);
    }
  }

  @FXML
  private void hhandleAddConfigToUpload() {
    try {
      File configFile = XMLConfigCreator.createPlayListXML(listView.getItems());
      
      MediaUploadData mediaUploadData = new MediaUploadData(PlayModusType.PLAYLIST, listView.getItems(), configFile );
      ControllerMediator.getInstance().setMediaUploadData(mediaUploadData);

    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
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
    Image image;
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

}
