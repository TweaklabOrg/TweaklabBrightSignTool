package ch.tweaklab.player.gui.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import ch.tweaklab.player.configurator.PlayerDisplaySettings;
import ch.tweaklab.player.configurator.PlayerGeneralSettings;
import ch.tweaklab.player.configurator.XMLConfigCreator;
import ch.tweaklab.player.connector.BrightSignWebConnector;
import ch.tweaklab.player.connector.Connector;
import ch.tweaklab.player.gui.view.WaitScreen;
import ch.tweaklab.player.model.Keys;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.util.NetworkUtils;

/**
 * /*
 * 
 * @author Alain
 *
 */
public class UploadScreenController {

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
  private CheckBox uploadMediaCheckbox;

  @FXML
  private Pane displaySettingsPane;
  @FXML
  private Pane generalSettingsPane;

  @FXML
  private CheckBox uploadGeneralSettingsheckbox;
  @FXML
  private TextField newHostnameField;
  @FXML
  private TextField newIPField;
  @FXML
  private TextField volumeField;
  @FXML
  private TextField gatewayField;
  @FXML
  private TextField subnetField;
  @FXML
  private CheckBox dhcpCheckbox;

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

    Connector currentConnector = ControllerMediator.getInstance().getConnector();
    if (currentConnector instanceof BrightSignWebConnector) {
      this.newHostnameField.setText(currentConnector.getTarget());
      String ip = NetworkUtils.resolveHostName(this.newHostnameField.getText());
      if (ip != "") {
        this.newIPField.setText(ip);
      }
    } else {
      this.newHostnameField.setText(settings.getHostname());
      this.newIPField.setText(settings.getIp());
    }

    this.dhcpCheckbox.setSelected(Boolean.valueOf(settings.getDhcp()));
    this.gatewayField.setText(settings.getGateway());
    this.subnetField.setText(settings.getNetmask());
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
    if (ControllerMediator.getInstance().getRootController().getMediaUploadData() == null) {
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

      // create and upload settings.xml
      if (this.uploadDisplaySettingsCheckbox.isSelected()) {
        File displaySettingsXML = createDisplaySettingsXML();
        systemFilesForUpload.add(displaySettingsXML);
      }

      // create and upload display.xml
      if (this.uploadGeneralSettingsheckbox.isSelected()) {
        File generalSettingsXml = createGeneralDisplaySettingsXML();

        systemFilesForUpload.add(generalSettingsXml);
      }

      // upload bs-scripts
      List<File> scripts = getScripts();
      systemFilesForUpload.addAll(scripts);

      // Show waitscreen
      waitScreen = new WaitScreen();
      waitScreen.setOnCancel(event -> uploadTask.cancel());
      waitScreen.setOnClose(event -> uploadTask.cancel());

      // Create Upload Task and add Events
      Connector connector = ControllerMediator.getInstance().getConnector();
      MediaUploadData uploadData = null;
      if (uploadMediaCheckbox.isSelected()) {

        uploadData = ControllerMediator.getInstance().getRootController().getMediaUploadData();
      }
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
    PlayerGeneralSettings defaultSettings = PlayerGeneralSettings.getDefaulGeneralSettings();
    PlayerGeneralSettings newSettings = defaultSettings;
    newSettings.setHostname(this.newHostnameField.getText());
    newSettings.setIp(this.newIPField.getText());
    newSettings.setVolume(Integer.parseInt(this.volumeField.getText()));
    newSettings.setGateway(this.newIPField.getText());
    newSettings.setNetmask(this.subnetField.getText());
    newSettings.setGateway(this.gatewayField.getText());
    newSettings.setDhcp(this.dhcpCheckbox.isSelected());
    File xmlFile = XMLConfigCreator.createGeneralSettingsXml(newSettings);
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
