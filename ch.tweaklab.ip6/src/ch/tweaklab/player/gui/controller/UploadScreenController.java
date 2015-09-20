package ch.tweaklab.player.gui.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import ch.tweaklab.player.configurator.PlayerDisplaySettings;
import ch.tweaklab.player.configurator.PlayerGeneralSettings;
import ch.tweaklab.player.configurator.XMLConfigCreator;
import ch.tweaklab.player.connector.BrightSignWebConnector;
import ch.tweaklab.player.connector.Connector;
import ch.tweaklab.player.gui.view.WaitScreen;
import ch.tweaklab.player.model.Keys;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.util.KeyValueData;

/**
 * /*
 * 
 * @author Alf
 *
 */
public class UploadScreenController {

  @FXML
  private Label hostNameLabel;
  @FXML
  private Label targetAddressLabel;
  @FXML
  private CheckBox uploadDisplaySettingsCheckbox;
  @FXML
  private CheckBox autoDisplaySolutionCheckbox;
  @FXML
  private TextField widthField;
  @FXML
  private TextField heightField;
  @FXML
  private TextField frequencyField;
  @FXML
  private CheckBox interlacedCheckbox;

  @FXML
  private Pane displaySettingsPane;
  @FXML
  private Pane generalSettingsPane;

  @FXML
  private CheckBox uploadGeneralSettingsheckbox;
  @FXML
  private CheckBox initalizeSystemCheckobx;
  @FXML
  private TextField newHostnameField;
  @FXML
  private TextField newIPField;
  @FXML
  private TextField volumeField;
  @FXML
  private CheckBox uploadScriptsCheckbox;

  WaitScreen waitScreen;
  Task<Boolean> uploadTask;
  private Thread uploadThread;

  @FXML
  private Label currentUploadSetLabel;

  ControllerMediator mediator;

  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
    mediator = ControllerMediator.getInstance();
    this.hostNameLabel.setText(ControllerMediator.getInstance().getConnector().getTarget());
    mediator.setUploadController(this);
    this.targetAddressLabel.setText(mediator.getConnector().getTarget());

    setDisplaySettingsDefaultValues();
    setGeneralSettingsDefaultValues();

    disableDisplayResolutionElements(this.autoDisplaySolutionCheckbox.isSelected());

    autoDisplaySolutionCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
        disableDisplayResolutionElements(new_val);
      }
    });

    uploadDisplaySettingsCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
        displaySettingsPane.setDisable(!new_val);
      }
    });

    uploadGeneralSettingsheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
        generalSettingsPane.setDisable(!new_val);
      }
    });

  }

  private void disableDisplayResolutionElements(boolean disable) {
    this.widthField.setDisable(disable);
    this.heightField.setDisable(disable);
    this.frequencyField.setDisable(disable);
    this.interlacedCheckbox.setDisable(disable);
  }

  private void setGeneralSettingsDefaultValues() {
    PlayerGeneralSettings settings = PlayerGeneralSettings.getDefaulGeneralSettings();

    this.newHostnameField.setText(settings.getHostname());
    if (mediator.getConnector() instanceof BrightSignWebConnector) {
      this.newIPField.setText(mediator.getConnector().getTarget());
    } else {
      this.newIPField.setText(settings.getIp());
    }
    this.initalizeSystemCheckobx.setSelected(settings.getInitialize());
    this.volumeField.setText(String.valueOf(settings.getVolume()));

  }

  private void setDisplaySettingsDefaultValues() {
    PlayerDisplaySettings displaySettings = PlayerDisplaySettings.getDefaultDisplaySettings();

    this.autoDisplaySolutionCheckbox.setSelected(displaySettings.getAuto());
    this.widthField.setText(String.valueOf(displaySettings.getWidth()));
    this.heightField.setText(String.valueOf(displaySettings.getHeight()));
    this.interlacedCheckbox.setSelected(displaySettings.getInterlaced());
    this.frequencyField.setText(String.valueOf(displaySettings.getFreq()));
  }

  private Boolean validateFields() {
    if (ControllerMediator.getInstance().getUploadData() == null) {
      MainApp.showErrorMessage("No Media Data", "Please add a media config to upload!");
      return false;
    }

    if (this.widthField.getText().equals("") || this.frequencyField.getText().equals("") || this.heightField.getText().equals("")
        || this.newHostnameField.getText().equals("") || this.newIPField.getText().equals("") || this.volumeField.getText().equals("")) {
      MainApp.showErrorMessage("Empty Field", "No empty fields are allowed!");
      return false;
    }

    return true;

  }

  @FXML
  private void handleDisconnect() {

    ControllerMediator.getInstance().disconnectFromDevice();
  }

  public void updateCurrentUploadSetLabel(String playType) {
    this.currentUploadSetLabel.setText(playType);
  }

  @FXML
  private void handleUpload() {

    try {

      if (!validateFields()) {
        return;
      }
      List<File> systemFilesForUpload = new ArrayList<File>();

      if (this.uploadDisplaySettingsCheckbox.isSelected()) {
        File displaySettingsXML = createDisplaySettingsXML();
        systemFilesForUpload.add(displaySettingsXML);
      }

      if (this.uploadGeneralSettingsheckbox.isSelected()) {
        File generalSettingsXml = createGeneralDisplaySettingsXML();
        systemFilesForUpload.add(generalSettingsXml);
      }

      if (this.uploadScriptsCheckbox.isSelected()) {

        List<File> scripts = getScripts();
        systemFilesForUpload.addAll(scripts);

      }

      // Show waitscreen
      waitScreen = new WaitScreen();
      waitScreen.setOnCancel(event -> uploadTask.cancel());
      waitScreen.setOnClose(event -> uploadTask.cancel());

      // Create Upload Task and add Events
      Connector connector = ControllerMediator.getInstance().getConnector();
      MediaUploadData uploadData = ControllerMediator.getInstance().getUploadData();

      uploadTask = connector.upload(uploadData, systemFilesForUpload);

      uploadTask.setOnSucceeded(event -> uploadTaskSucceedFinish());
      uploadTask.setOnCancelled(event -> uploadTaskAbortFinish());
      uploadTask.setOnFailed(event -> uploadTaskAbortFinish());

      uploadThread = new Thread(uploadTask);
      uploadThread.start();
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
    }
  }

  private File createDisplaySettingsXML() {
    PlayerDisplaySettings displaySettings = PlayerDisplaySettings.getDefaultDisplaySettings();
    displaySettings.setAuto(this.autoDisplaySolutionCheckbox.isSelected());
    displaySettings.setWidth(Integer.parseInt(this.widthField.getText()));
    displaySettings.setHeight(Integer.parseInt(this.heightField.getText()));
    displaySettings.setFreq(Integer.parseInt(this.frequencyField.getText()));
    displaySettings.setInterlaced(this.interlacedCheckbox.isSelected());
    File xmlFile = XMLConfigCreator.createDisplaySettingsXml(displaySettings);
    return xmlFile;
  }

  private File createGeneralDisplaySettingsXML() {
    PlayerGeneralSettings settings = PlayerGeneralSettings.getDefaulGeneralSettings();
    settings.setInitialize(this.initalizeSystemCheckobx.isSelected());
    settings.setHostname(this.newHostnameField.getText());
    settings.setIp(this.newIPField.getText());
    settings.setVolume(Integer.parseInt(this.volumeField.getText()));
    settings.setGateway(this.newIPField.getText());
    File xmlFile = XMLConfigCreator.createGeneralSettingsXml(settings);
    return xmlFile;
  }

  private List<File> getScripts() {
    String scriptsFolderPath = Keys.loadProperty(Keys.SCRIPTS_DIRECTORY_PROPS_KEY);
    List<File> scriptFiles = new ArrayList<File>();

    try {
      Files.walk(Paths.get(scriptsFolderPath)).forEach(filePath -> {
        if (Files.isRegularFile(filePath)) {
          scriptFiles.add(filePath.toFile());
        }
      });
    } catch (IOException e) {
      MainApp.showExceptionMessage(e);
      e.printStackTrace();
    }

    return scriptFiles;
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
