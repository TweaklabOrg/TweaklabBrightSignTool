package ch.tweaklab.player.gui.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import ch.tweaklab.player.configurator.UploadFile;
import ch.tweaklab.player.configurator.XmlConfigCreator;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.model.ModeType;

/**
 * Controller of PushButton Configuration View
 * 
 * @author Alain
 *
 */
public class GpioTabController extends TabController {

  @FXML
  private AnchorPane rootPane;

  @FXML
  private CheckBox retriggerEnabledCheckbox;

  @FXML
  private TextField retriggerDelayField;

  @FXML
  private Label loopfileNameLabel;

  MediaFile[] gpioFiles;
  private static int NUMBER_OF_BUTTONS = 4;
  MediaFile loopFile;

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
      if (this.validateFileFormat(choosenFile.getName()) == false){
        MainApp.showErrorMessage("Wrong File", "This filetype is not supported. Add this type in the property file if you need it.");
        return;
      }
      String fileNameToDisplay = choosenFile.getName();
      if (fileNameToDisplay.length() > MAX_FILE_NAME_LENGTH_SHOW) {
        fileNameToDisplay = fileNameToDisplay.substring(0, 22) + "...";
      }
      loopfileNameLabel.setText(fileNameToDisplay);
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
      if (this.validateFileFormat(choosenFile.getName()) == false){
        MainApp.showErrorMessage("Wrong File", "This filetype is not supported.");
        return;
      }
      MediaFile mediaFile = new MediaFile(choosenFile);
      gpioFiles[buttonNumber] = mediaFile;

      // get label with same id-number as clicked button
      Label fileNameLabel = (Label) rootPane.lookup("#fileNameLabel" + buttonNumber);

      String fileNameToDisplay = choosenFile.getName();
      if (fileNameToDisplay.length() > MAX_FILE_NAME_LENGTH_SHOW) {
        fileNameToDisplay = fileNameToDisplay.substring(0, 22) + "...";
      }
      fileNameLabel.setText(fileNameToDisplay);
    }

  }

  @FXML
  private void reset() {
    // rest gpio files
    gpioFiles = new MediaFile[NUMBER_OF_BUTTONS];
    for (int i = 0; i < NUMBER_OF_BUTTONS; i++) {
      Label fileNameLabel = (Label) rootPane.lookup("#fileNameLabel" + i);
      fileNameLabel.setText("None");
    }

    // reset Loopfile
    this.loopFile = null;
    this.loopfileNameLabel.setText("none");

    retriggerDelayField.setText("2000");
    retriggerEnabledCheckbox.setSelected(true);
  }

  @Override
  public MediaUploadData getMediaUploadData() {
    UploadFile gpioConfigFile = XmlConfigCreator.createGpioXML(loopFile, gpioFiles, retriggerEnabledCheckbox.isSelected(), retriggerDelayField.getText());

    ArrayList<MediaFile> uploadList = new ArrayList<MediaFile>(Arrays.asList(gpioFiles));
    uploadList.add(loopFile);

    MediaUploadData mediaUploadData = new MediaUploadData(uploadList, gpioConfigFile,ModeType.GPIO);
    return mediaUploadData;
  }

}
