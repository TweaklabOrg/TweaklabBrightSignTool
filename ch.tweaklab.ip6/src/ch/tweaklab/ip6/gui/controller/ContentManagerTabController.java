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
import javafx.stage.FileChooser;
import ch.tweaklab.ip6.connector.Connector;
import ch.tweaklab.ip6.gui.MainApp;
import ch.tweaklab.ip6.gui.view.WaitScreen;
import ch.tweaklab.ip6.model.ApplicationData;
import ch.tweaklab.ip6.model.MediaFile;
import ch.tweaklab.ip6.model.MediaType;
/**
 * Controller Class for ContentManagerTab.fxml
 * Manages Upload of a playlist to device
 * @author Alf
 *
 */
public class ContentManagerTabController {

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
  private Button waitViewButton;

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
  }

  @FXML
  private void handleAddFileToListView() {
    final FileChooser fileChooser = new FileChooser();
    File choosenFile = fileChooser.showOpenDialog(MainApp.primaryStage);
    if (choosenFile != null) {
      MediaFile mediaFile = new MediaFile(choosenFile);
      listView.getItems().add(mediaFile);
      // fileList.add(choosenFile);
    }
  }

  @FXML
  private void handleRemove() {
    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
    listView.getItems().remove(selectedIndex);
    clearMediaFileDetailInformations();
  }

  @FXML
  private void handleUpload() {
    try {
      // Show waitscreen
      waitScreen = new WaitScreen();
      waitScreen.setOnCancel(event -> uploadTask.cancel());
      waitScreen.setOnClose(event -> uploadTask.cancel());

      // Create Upload Task and add Events
      Connector connector = ApplicationData.getConnector();
      uploadTask = connector.getUploadMediaFilesTask(listView.getItems());

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
      } else {
        uploadTaskAbortFinish();
      }
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
    }
  }

  private void uploadTaskAbortFinish() {
    waitScreen.closeScreen();
    MainApp.showErrorMessage("An error occured during upload. Some files are not uploaded!");
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
    if (selectedMediaFile != null) {
      this.fileNameField.setText(selectedMediaFile.getFile().getName());
      this.mediaTypeField.setText(selectedMediaFile.getMediaType().toString());
      this.fileSizeLabel.setText(selectedMediaFile.getFileSize());
      this.displayTimeField.setText(selectedMediaFile.getDisplayTime());
      if (selectedMediaFile.getMediaType() == MediaType.IMAGE) {
        this.displayTimeField.setVisible(true);
        this.displayTimeLabel.setVisible(true);
      } else {
        this.displayTimeField.setVisible(false);
        this.displayTimeLabel.setVisible(false);
      }
    }
  }

  private void clearMediaFileDetailInformations() {
    this.fileNameField.setText("");
    this.mediaTypeField.setText("");
    this.fileSizeLabel.setText("");
    this.displayTimeField.setVisible(false);
    this.displayTimeLabel.setVisible(false);
  }

}
