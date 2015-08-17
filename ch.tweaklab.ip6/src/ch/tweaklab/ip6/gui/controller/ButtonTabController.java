package ch.tweaklab.ip6.gui.controller;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import ch.tweaklab.ip6.connector.Connector;
import ch.tweaklab.ip6.gui.model.Context;
import ch.tweaklab.ip6.gui.view.WaitScreen;
import ch.tweaklab.ip6.media.MediaFile;
import ch.tweaklab.ip6.media.MediaType;
import ch.tweaklab.ip6.media.XMLConfigCreator;

/**
 * Controller of PushButton Configuration View
 * 
 * @author Alain
 *
 */
public class ButtonTabController {

  MediaFile[] mediaFiles;
  int numberOfButtons = 4;

  @FXML
  private AnchorPane rootPane;

  WaitScreen waitScreen;
  Task<Boolean> uploadTask;
  private Thread uploadThread;

  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
    mediaFiles = new MediaFile[numberOfButtons];
    reset();
  }

  @FXML
  private void handleChooseFile(ActionEvent event) {

    int buttonNumber = -1;

    // check which button was clicked
    Object source = event.getSource();
    if (source instanceof Button && ((Button) source).getId().startsWith("chooseFileBtn")) {
      buttonNumber = Integer.valueOf(((Button) source).getId().substring(13));
    }

    if (buttonNumber < 0 || numberOfButtons > numberOfButtons)
      throw new RuntimeException("Invalid Button ID");

    // get file
    final FileChooser fileChooser = new FileChooser();
    File choosenFile = fileChooser.showOpenDialog(MainApp.primaryStage);

    if (choosenFile != null) {
      MediaFile mediaFile = new MediaFile(choosenFile);
      mediaFiles[buttonNumber] = mediaFile;
      Label fileNameLabel = (Label) rootPane.lookup("#fileNameLabel" + buttonNumber);
      fileNameLabel.setText(choosenFile.getName());

      if (mediaFile != null) {
        Image image;
        ImageView imageView = (ImageView) rootPane.lookup("#imageView" + buttonNumber);
        if (mediaFile.getMediaType() == MediaType.IMAGE) {
          String path = "file:///" + mediaFile.getFile().getAbsolutePath().replace("\\", "/");
          image = new Image(path, true);
        } else {
          InputStream imageStream = getClass().getClassLoader().getResourceAsStream(mediaFile.getMediaType().toString().toLowerCase() + ".png");
          image = new Image(imageStream);
        }
        imageView.setImage(image);
      }
    }

  }

  @FXML
  private void reset() {
    for (int i = 0; i < numberOfButtons; i++) {
      Label fileNameLabel = (Label) rootPane.lookup("#fileNameLabel" + i);
      fileNameLabel.setText("None");
      ImageView imageView = (ImageView) rootPane.lookup("#imageView" + i);
      imageView.setImage(null);
    }
  }

  @FXML
  private void handleUpload() {
    if (validateData() == false) {
      return;
    }

    try {
      // Show waitscreen
      waitScreen = new WaitScreen();
      waitScreen.setOnCancel(event -> uploadTask.cancel());
      waitScreen.setOnClose(event -> uploadTask.cancel());

      // Create Upload Task and add Events
      Connector connector = Context.getConnector();
      File buttonConfigFile = XMLConfigCreator.createButtontXML(mediaFiles);
             
      uploadTask = connector.uploadMediaFiles(Arrays.asList(mediaFiles), buttonConfigFile);

      uploadTask.setOnSucceeded(event -> uploadTaskSucceedFinish());
      uploadTask.setOnCancelled(event -> uploadTaskAbortFinish());
      uploadTask.setOnFailed(event -> uploadTaskAbortFinish());

      uploadThread = new Thread(uploadTask);
      uploadThread.setDaemon(false);
      uploadThread.start();
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
    }
  }

  private Boolean validateData() {
    // validate data
    if (Context.getConnector() == null || Context.getConnector().getIsConnected() == false) {
      MainApp.showErrorMessage("Not connected!", "You are currently not connected to a device. Please connect before upload");
      return false;
    }
    return true;
  }

  private void uploadTaskSucceedFinish() {
    try {
      if (uploadTask.get()) {
        waitScreen.closeScreen();
        MainApp.showInfoMessage("Upload finished!");
        reset();
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

}
