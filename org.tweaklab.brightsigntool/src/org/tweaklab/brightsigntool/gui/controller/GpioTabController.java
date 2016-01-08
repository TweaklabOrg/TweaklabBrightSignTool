package org.tweaklab.brightsigntool.gui.controller;

import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.configurator.XmlConfigCreator;
import org.tweaklab.brightsigntool.model.MediaFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import org.tweaklab.brightsigntool.model.ModeType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

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
  public Label fileNameLabel0;

  @FXML
  public Label fileNameLabel1;

  @FXML
  public Label fileNameLabel2;

  @FXML
  public Label fileNameLabel3;

  @FXML
  private Label loopfileNameLabel;

  @FXML
  private CheckBox retriggerEnabledCheckbox;

  @FXML
  private TextField retriggerDelayField;

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
      buttonNumber = Integer.parseInt(((Button) source).getId().substring(13));
    }

    if (buttonNumber < 0 || buttonNumber > NUMBER_OF_BUTTONS)
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
      fileNameLabel.setText(fileNameToDisplay);
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
