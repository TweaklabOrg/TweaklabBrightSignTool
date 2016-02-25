package org.tweaklab.brightsigntool.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.configurator.XmlConfigCreator;
import org.tweaklab.brightsigntool.model.MediaFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import org.tweaklab.brightsigntool.model.ModeType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller of PushButton Configuration View
 *
 * @author Alain
 */
public class GpioTabController extends TabController {
  private static final Logger LOGGER = Logger.getLogger(GpioTabController.class.getName());
  private static int NUMBER_OF_BUTTONS = 4;
  private static int MAX_FILE_NAME_LENGTH_SHOW = 25;
  @FXML
  public Label fileNameLabel0;
  @FXML
  public Label fileNameLabel1;
  @FXML
  public Label fileNameLabel2;
  @FXML
  public Label fileNameLabel3;
  MediaFile[] gpioFiles;
  MediaFile loopFile;
  @FXML
  private AnchorPane rootPane;
  @FXML
  private Label loopfileNameLabel;
  @FXML
  private CheckBox retriggerEnabledCheckbox;
  @FXML
  private TextField retriggerDelayField;

  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
    reset();
    retriggerEnabledCheckbox.selectedProperty().addListener((observable, oldValue, newValue) ->
            retriggerDelayField.setDisable(!newValue));
  }

  @Override
  public void setContent(Map<String, String> content) {
    // TODO: building filemanagement to make that possible. For ex. skip mediaupload of already existing files, but allow modifications on settings.
//    if (content.containsKey("gpio0")) {
//      fileNameLabel0.textProperty().setValue(content.get("gpio0"));
//    }
//    if (content.containsKey("gpio1")) {
//      fileNameLabel1.textProperty().setValue(content.get("gpio1"));
//    }
//    if (content.containsKey("gpio2")) {
//      fileNameLabel2.textProperty().setValue(content.get("gpio2"));
//    }
//    if (content.containsKey("gpio3")) {
//      fileNameLabel3.textProperty().setValue(content.get("gpio3"));
//    }
//    if (content.containsKey("loop")) {
//      loopfileNameLabel.textProperty().setValue(content.get("loop"));
//    }
//    if (content.containsKey("retriggerEnabled")) {
//      boolean value = Boolean.parseBoolean(content.get("retriggerEnabled"));
//      retriggerEnabledCheckbox.selectedProperty().setValue(value);
//    }
//    if (content.containsKey("retriggerDelay")) {
//      retriggerDelayField.textProperty().setValue(content.get("retriggerDelay"));
//    }
  }

  @FXML
  private void handleChooseLoopFile() {
    final FileChooser fileChooser = new FileChooser();
    File chosenFile = fileChooser.showOpenDialog(MainApp.primaryStage);
    if (chosenFile != null) {
      loopFile = new MediaFile(chosenFile);
      if (this.validateFileFormat(chosenFile.getName()) == false) {
        MainApp.showErrorMessage("Wrong File", "This filetype is not supported. Add this type in the property file if you need it.");
        LOGGER.log(Level.INFO, "Format of chosen file is not mentioned in properties: " + chosenFile.getName());
        return;
      }
      String fileNameToDisplay = chosenFile.getName();
      if (fileNameToDisplay.length() > MAX_FILE_NAME_LENGTH_SHOW) {
        // TODO: move file name fold length to properties
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
      buttonNumber = Integer.parseInt(((Button) source).getId().substring(13));
    }


    if (buttonNumber < 0 || buttonNumber > NUMBER_OF_BUTTONS)
      throw new RuntimeException("Invalid Button ID");

    // get file
    final FileChooser fileChooser = new FileChooser();
    File chosenFile = fileChooser.showOpenDialog(MainApp.primaryStage);

    if (chosenFile != null) {
      if (this.validateFileFormat(chosenFile.getName()) == false) {
        MainApp.showErrorMessage("Wrong File", "This filetype is not supported.");
        LOGGER.log(Level.INFO, "Format of chosen file is not mentioned in properties: " + chosenFile.getName());
        return;
      }
      MediaFile mediaFile = new MediaFile(chosenFile);
      gpioFiles[buttonNumber] = mediaFile;

      // get label with same id-number as clicked button
      Label fileNameLabel = (Label) rootPane.lookup("#fileNameLabel" + buttonNumber);

      String fileNameToDisplay = chosenFile.getName();
      fileNameLabel.setText(fileNameToDisplay);
      LOGGER.log(Level.INFO, "File " + chosenFile.getName() + " was set for gpio " + buttonNumber);
    }
  }

  @FXML
  private void reset() {
    // rest gpio files
    gpioFiles = new MediaFile[NUMBER_OF_BUTTONS];
    for (int i = 0; i < NUMBER_OF_BUTTONS; i++) {
      Label fileNameLabel = (Label) rootPane.lookup("#fileNameLabel" + i);
      fileNameLabel.setText("no file selected");
    }

    // reset Loopfile
    this.loopFile = null;
    this.loopfileNameLabel.setText("no file selected");

    retriggerDelayField.setText("2000");
    retriggerEnabledCheckbox.setSelected(true);
  }

  @Override
  public MediaUploadData getMediaUploadData() {
    UploadFile gpioConfigFile = XmlConfigCreator.createGpioXML(loopFile, gpioFiles, retriggerEnabledCheckbox.isSelected(), retriggerDelayField.getText());

    ArrayList<MediaFile> uploadList = new ArrayList<MediaFile>(Arrays.asList(gpioFiles));
    uploadList.add(loopFile);

    MediaUploadData mediaUploadData = new MediaUploadData(uploadList, gpioConfigFile, ModeType.GPIO);
    return mediaUploadData;
  }

}
