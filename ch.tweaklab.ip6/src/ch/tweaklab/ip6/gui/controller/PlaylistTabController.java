package ch.tweaklab.ip6.gui.controller;

import java.io.File;
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
import ch.tweaklab.ip6.connector.Connector;
import ch.tweaklab.ip6.gui.MainApp;
import ch.tweaklab.ip6.gui.view.WaitScreen;
import ch.tweaklab.ip6.model.ApplicationData;
import ch.tweaklab.ip6.model.MediaFile;
import ch.tweaklab.ip6.model.MediaType;

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
  

  @FXML
  private ImageView imageView;

  WaitScreen waitScreen;
  Task<Boolean> uploadTask;
  private Thread uploadThread;

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
          selectedMediaFile.setDisplayTime(newValue);
        }
      }
    });
    uploadButton.setDisable(true);

  }

  private void addTabs() {
    // TODO Auto-generated method stub

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
    if(listView.getItems().size() < 1){
      uploadButton.setDisable(true);
    }
  }

  @FXML
  private void handleUpload() {
    //validate data
    if (ApplicationData.getConnector() == null || ApplicationData.getConnector().getIsConnected() == false) {
      MainApp.showErrorMessage("Not connected!", "You are currently not connected to a device. Please connect before upload");
      return;
    }

    try {
      // Show waitscreen
      waitScreen = new WaitScreen();
      waitScreen.setOnCancel(event -> uploadTask.cancel());
      waitScreen.setOnClose(event -> uploadTask.cancel());

      // Create Upload Task and add Events
      Connector connector = ApplicationData.getConnector();
      uploadTask = connector.uploadMediaFiles(listView.getItems());

      uploadTask.setOnSucceeded(event -> uploadTaskSucceedFinish());
      uploadTask.setOnCancelled(event -> uploadTaskAbortFinish());
      uploadTask.setOnFailed(event -> uploadTaskAbortFinish());

      uploadThread = new Thread(uploadTask);
      uploadThread.setDaemon(true);
      uploadThread.start();
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
    }
  }

  private void uploadTaskSucceedFinish() {
    try {
      if (uploadTask.get()) {
        waitScreen.closeScreen();
        MainApp.showInfoMessage("Upload finished!");
        clearPlaylist();
      } else {
        uploadTaskAbortFinish();
      }
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
    }
  }

  private void uploadTaskAbortFinish() {
    waitScreen.closeScreen();
    MainApp.showErrorMessage("Upload Failed", "An error occured during upload. Some files are not uploaded!");
  }

  @FXML
  private void handleWaitScreenCancelButton() {
    uploadTask.cancel();
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
      this.displayTimeField.setText(selectedMediaFile.getDisplayTime());
      if (selectedMediaFile.getMediaType() == MediaType.IMAGE) {
        this.displayTimeField.setVisible(true);
        this.displayTimeLabel.setVisible(true);
        String path = "file:///" + selectedMediaFile.getFile().getAbsolutePath().replace("\\", "/");
        image = new Image(path, true);

        this.imageView.setImage(image);
      } else {
        String imagepath = "resources/" + selectedMediaFile.getMediaType().toString().toLowerCase() + ".png";
        image = new Image(imagepath, true);

        this.displayTimeField.setVisible(false);
        this.displayTimeLabel.setVisible(false);
      }
      this.imageView.setImage(image);
    }
  }

  private void clearPlaylist() {
    this.listView.getItems().clear();
    clearMediaFileDetailInformations();
  }

  private void clearMediaFileDetailInformations() {
    this.fileNameField.setText("");
    this.mediaTypeField.setText("");
    this.fileSizeLabel.setText("");
    this.displayTimeField.setVisible(false);
    this.displayTimeLabel.setVisible(false);
  }

}
