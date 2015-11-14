package ch.tweaklab.player.gui.controller;

import ch.tweaklab.player.configurator.PlayerDisplaySettings;
import ch.tweaklab.player.configurator.PlayerGeneralSettings;
import ch.tweaklab.player.configurator.UploadFile;
import ch.tweaklab.player.configurator.XmlConfigCreator;
import ch.tweaklab.player.connector.BrightSignWebConnector;
import ch.tweaklab.player.connector.Connector;
import ch.tweaklab.player.gui.view.WaitScreen;
import ch.tweaklab.player.model.Keys;
import ch.tweaklab.player.model.MediaUploadData;
import ch.tweaklab.player.util.NetworkUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
  private CheckBox uploadGeneralSettingsCheckbox;
  @FXML
  private TextField volumeField;

  @FXML
  private TextField newHostnameField;

  @FXML
  private Label newIPlabel;
  @FXML
  private TextField newIPField;

  @FXML
  private Label gatewayLabel;
  @FXML
  private TextField gatewayField;

  @FXML
  private Label subnetLabel;
  @FXML
  private TextField subnetField;
  @FXML
  private CheckBox dhcpCheckbox;

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
    this.targetAddressLabel.setText(mediator.getConnector().getName());

    setDisplaySettingsDefaultValues();
    setGeneralSettingsDefaultValues();

    disableDisplayResolutionElements(this.autoDisplaySolutionCheckbox.isSelected());

    addListenerToCheckboxes();

  }

  @FXML
  private void handleDisconnect() {

    ControllerMediator.getInstance().disconnectFromDevice();
  }

  /**
   * Starts the upload Thread
   */
  @FXML
  private void handleUpload() {

    try {

      if (!validateFields()) {
        return;
      }
      List<UploadFile> systemFilesForUpload = new ArrayList<UploadFile>();

      // create and upload settings.xml
      if (this.uploadDisplaySettingsCheckbox.isSelected()) {
        UploadFile displaySettingsXML = createDisplaySettingsXML();
        systemFilesForUpload.add(displaySettingsXML);
      }

      // create and upload display.xml
      if (this.uploadGeneralSettingsCheckbox.isSelected()) {
        UploadFile generalSettingsXml;
        // if the player will be reset completelty, initialize
        if (this.uploadDisplaySettingsCheckbox.isSelected() && this.uploadMediaCheckbox.isSelected()) {
          generalSettingsXml = createInitialGeneralDisplaySettingsXML();
        } else {
          generalSettingsXml = createGeneralDisplaySettingsXML();
        }
        systemFilesForUpload.add(generalSettingsXml);
      }

      // upload bs-scripts
      List<UploadFile> scripts = getScripts();
      systemFilesForUpload.addAll(scripts);

      // Create Upload Task and add Events
      Connector connector = ControllerMediator.getInstance().getConnector();
      MediaUploadData mediaUploadData = null;
      if (uploadMediaCheckbox.isSelected()) {

        mediaUploadData = ControllerMediator.getInstance().getRootController().getMediaUploadData();

        UploadFile modeXml = XmlConfigCreator.createModeXml(mediaUploadData.getMode());

        systemFilesForUpload.add(modeXml);
        if (mediaUploadData.getUploadList().size() < 1) {
          MainApp.showErrorMessage("no Files", "No media files for upload added!");
          return;
        }
      }

      // Show waitscreen
      waitScreen = new WaitScreen();
      waitScreen.setOnCancel(event -> uploadTask.cancel());
      waitScreen.setOnClose(event -> uploadTask.cancel());

      uploadTask = connector.upload(mediaUploadData, systemFilesForUpload);

      uploadTask.setOnSucceeded(event -> uploadTaskSucceedFinish());
      uploadTask.setOnCancelled(event -> uploadTaskAbortFinish());
      uploadTask.setOnFailed(event -> uploadTaskAbortFinish());

      uploadThread = new Thread(uploadTask);
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
    MainApp.showErrorMessage("Upload Failed", "An error occured during upload. Some files are not uploaded!");
  }

  private Boolean validateFields() {
    if (uploadDisplaySettingsCheckbox.isSelected() == false && uploadGeneralSettingsCheckbox.isSelected() == false
        && uploadMediaCheckbox.isSelected() == false) {

      MainApp.showInfoMessage("Only BrightSign scripts will be uploaded!");
    }

    if (ControllerMediator.getInstance().getRootController().getMediaUploadData() == null) {
      MainApp.showErrorMessage("No Media Data", "Please add a media config to upload!");
      return false;
    }

    if (this.autoDisplaySolutionCheckbox.isSelected() == false
        && (this.widthField.getText().equals("") || this.frequencyField.getText().equals("") || this.heightField.getText().equals("")
            || this.newHostnameField.getText().equals("") || this.volumeField.getText().equals(""))) {
      MainApp.showErrorMessage("Empty Field", "Please select Auto Resolution or set some values!");
      return false;
    }

    return true;

  }

  private UploadFile createDisplaySettingsXML() {
    PlayerDisplaySettings displaySettings = PlayerDisplaySettings.getDefaultDisplaySettings();
    displaySettings.setAuto(this.autoDisplaySolutionCheckbox.isSelected());
    displaySettings.setWidth(this.widthField.getText());
    displaySettings.setHeight(this.heightField.getText());
    displaySettings.setFreq(this.frequencyField.getText());
    displaySettings.setInterlaced(this.interlacedCheckbox.isSelected());
    UploadFile xmlFile = XmlConfigCreator.createDisplaySettingsXml(displaySettings);
    return xmlFile;
  }

  private UploadFile createInitialGeneralDisplaySettingsXML() {
    PlayerGeneralSettings defaultSettings = PlayerGeneralSettings.getDefaulGeneralSettings();
    PlayerGeneralSettings newSettings = defaultSettings;
    newSettings.setHostname(this.newHostnameField.getText());
    newSettings.setIp(this.newIPField.getText());
    newSettings.setVolume(Integer.parseInt(this.volumeField.getText()));
    newSettings.setNetmask(this.subnetField.getText());
    newSettings.setGateway(this.gatewayField.getText());
    newSettings.setDhcp(this.dhcpCheckbox.isSelected());
    UploadFile xmlFile = XmlConfigCreator.createGeneralSettingsXml(newSettings);
    return xmlFile;
  }

  private UploadFile createGeneralDisplaySettingsXML() {
    PlayerGeneralSettings defaultSettings = PlayerGeneralSettings.getDefaulGeneralSettings();
    PlayerGeneralSettings newSettings = defaultSettings;
    newSettings.setHostname(this.newHostnameField.getText());
    newSettings.setIp(this.newIPField.getText());
    newSettings.setVolume(Integer.parseInt(this.volumeField.getText()));
    newSettings.setNetmask(this.subnetField.getText());
    newSettings.setGateway(this.gatewayField.getText());
    newSettings.setDhcp(this.dhcpCheckbox.isSelected());
    newSettings.setInitialize(false);
    UploadFile xmlFile = XmlConfigCreator.createGeneralSettingsXml(newSettings);
    return xmlFile;
  }

  private List<UploadFile> getScripts() {
    String[] scriptFileNames = Keys.loadProperty(Keys.BS_SCRIPTS_PROPS_KEY).split(";");
    List<UploadFile> scriptFiles = new ArrayList<UploadFile>();

    for (String scriptName : scriptFileNames) {
      try {
        InputStream scriptInputStream = Keys.class.getResourceAsStream("/bs-scripts/" + scriptName);
        byte[] scriptAsBytes = IOUtils.toByteArray(scriptInputStream);
        UploadFile configFile = new UploadFile(scriptName, scriptAsBytes);
        scriptFiles.add(configFile);

      } catch (Exception e) {
        MainApp.showErrorMessage("File not found!", "BrightSign Script " + scriptName + " not found!");
        e.printStackTrace();
      }

    }

    return scriptFiles;
  }

  private void disableIpField(boolean b) {
    newIPField.setDisable(b);
    newIPlabel.setDisable(b);
    gatewayField.setDisable(b);
    gatewayLabel.setDisable(b);
    subnetField.setDisable(b);
    subnetLabel.setDisable(b);
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
      this.newHostnameField.setText(currentConnector.getName());
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

    disableIpField(Boolean.valueOf(settings.getDhcp()));
  }

  private void setDisplaySettingsDefaultValues() {
    PlayerDisplaySettings displaySettings = PlayerDisplaySettings.getDefaultDisplaySettings();

    this.autoDisplaySolutionCheckbox.setSelected(displaySettings.getAuto());
    this.widthField.setText(String.valueOf(displaySettings.getWidth()));
    this.heightField.setText(String.valueOf(displaySettings.getHeight()));
    this.interlacedCheckbox.setSelected(displaySettings.getInterlaced());
    this.frequencyField.setText(String.valueOf(displaySettings.getFreq()));

    disableDisplayResolutionElements(displaySettings.getAuto());
  }

  public void updateCurrentUploadSetLabel(String playType) {
    this.currentUploadSetLabel.setText(playType);
  }

  private void addListenerToCheckboxes() {
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


    dhcpCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      public void changed(ObservableValue ov, Boolean old_val, Boolean new_val) {
        disableIpField(new_val);
      }
    });

  }

}
