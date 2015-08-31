package ch.tweaklab.ip6.gui.controller;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import ch.tweaklab.ip6.connector.Connector;
import ch.tweaklab.ip6.gui.model.Context;
import ch.tweaklab.ip6.gui.view.WaitScreen;
import ch.tweaklab.ip6.mediaLogic.MediaFile;
import ch.tweaklab.ip6.mediaLogic.MediaType;
import ch.tweaklab.ip6.mediaLogic.XMLConfigCreator;

/**
 * Controller of PushButton Configuration View
 * 
 * @author Alain
 *
 */
public class GpioTabController {

  @FXML
  private AnchorPane rootPane;

  @FXML
  private CheckBox retriggerEnabledCheckbox;

  @FXML
  private TextField retriggerDelayField;

  @FXML
  private Label loopfileNameLabel;

  @FXML
  private ImageView loopFileImageView;

  MediaFile[] gpioFiles;
  private static int NUMBER_OF_BUTTONS = 4;
  MediaFile loopFile;

  WaitScreen waitScreen;
  Task<Boolean> uploadTask;
  private Thread uploadThread;
  
  private static int MAX_FILE_NAME_LENGTH_SHOW = 25;
  

  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
    reset();

  }

  @FXML
  private void handleChooseLoopFile() {
    final FileChooser fileChooser = new FileChooser();
    File choosenFile = fileChooser.showOpenDialog(MainApp.primaryStage);
    if (choosenFile != null) {
      loopFile = new MediaFile(choosenFile);
      
      String fileNameToDisplay = choosenFile.getName();
      if(fileNameToDisplay.length() > MAX_FILE_NAME_LENGTH_SHOW){
        fileNameToDisplay = fileNameToDisplay.substring(0,22) + "...";
      }  
      loopfileNameLabel.setText(fileNameToDisplay);
      if (loopFile != null) {
        Image image = getMediaFileImage(loopFile);
        loopFileImageView.setImage(image);
      }
    }

  }

  @FXML
  private void handleChooseGpioFile(ActionEvent event) {

    int buttonNumber = -1;

    // check which button was clicked
    Object source = event.getSource();
    if (source instanceof Button && ((Button) source).getId().startsWith("chooseFileBtn")) {
      buttonNumber = Integer.valueOf(((Button) source).getId().substring(13));
    }

    if (buttonNumber < 0 || NUMBER_OF_BUTTONS > NUMBER_OF_BUTTONS)
      throw new RuntimeException("Invalid Button ID");

    // get file
    final FileChooser fileChooser = new FileChooser();
    File choosenFile = fileChooser.showOpenDialog(MainApp.primaryStage);

    if (choosenFile != null) {
      MediaFile mediaFile = new MediaFile(choosenFile);
      gpioFiles[buttonNumber] = mediaFile;
      
      //get label with same id-number as clicked button 
      Label fileNameLabel = (Label) rootPane.lookup("#fileNameLabel" + buttonNumber);
     
      String fileNameToDisplay = choosenFile.getName();
      if(fileNameToDisplay.length() > MAX_FILE_NAME_LENGTH_SHOW){
        fileNameToDisplay = fileNameToDisplay.substring(0,22) + "...";
      }
      fileNameLabel.setText(fileNameToDisplay);

      if (mediaFile != null) {
        ImageView imageView = (ImageView) rootPane.lookup("#imageView" + buttonNumber);
        Image image = getMediaFileImage(mediaFile);
        imageView.setImage(image);
      }
    }

  }

  private Image getMediaFileImage(MediaFile mediaFile) {
    Image image;
    if (mediaFile.getMediaType() == MediaType.IMAGE) {
      String path = "file:///" + mediaFile.getFile().getAbsolutePath().replace("\\", "/");
      image = new Image(path, true);
    } else {
      InputStream imageStream = getClass().getClassLoader().getResourceAsStream(mediaFile.getMediaType().toString().toLowerCase() + ".png");
      image = new Image(imageStream);
    }
    return image;
  }

  @FXML
  private void reset() {
    // rest gpio files
    gpioFiles = new MediaFile[NUMBER_OF_BUTTONS];
    for (int i = 0; i < NUMBER_OF_BUTTONS; i++) {
      Label fileNameLabel = (Label) rootPane.lookup("#fileNameLabel" + i);
      fileNameLabel.setText("None");
      ImageView imageView = (ImageView) rootPane.lookup("#imageView" + i);
      imageView.setImage(null);
    }

    // reset Loopfile
    this.loopFileImageView.setImage(null);
    this.loopFile = null;
    this.loopfileNameLabel.setText("none");

    retriggerDelayField.setText("2000");
    retriggerEnabledCheckbox.setSelected(true);
  }

  @FXML
  private void handleUpload() {
    if (validateData() == false) {
      return;
    }

    try {
      // Show waitscreen
      waitScreen = new WaitScreen();
      waitScreen.setOnCancel(event -> {
        if (uploadTask != null)
          uploadTask.cancel();
        else
          waitScreen.closeScreen();
      });
      waitScreen.setOnClose(event -> {
        if (uploadTask != null)
          uploadTask.cancel();
        else
          waitScreen.closeScreen();
      });

      // Create Upload Task and add Events
      Connector connector = Context.getConnector();
      File gpioConfigFile = XMLConfigCreator.createGpioXML(loopFile, gpioFiles, retriggerEnabledCheckbox.isSelected(), retriggerDelayField.getText());

      ArrayList<MediaFile> uploadList = new ArrayList<MediaFile>(Arrays.asList(gpioFiles));
      uploadList.add(loopFile);
      uploadTask = connector.uploadMediaFiles(uploadList, gpioConfigFile);

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
