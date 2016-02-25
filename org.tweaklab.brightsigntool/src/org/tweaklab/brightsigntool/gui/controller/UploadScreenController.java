package org.tweaklab.brightsigntool.gui.controller;

import org.tweaklab.brightsigntool.configurator.PlayerDisplaySettings;
import org.tweaklab.brightsigntool.configurator.PlayerGeneralSettings;
import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.configurator.XmlConfigCreator;
import org.tweaklab.brightsigntool.connector.BrightSignWebConnector;
import org.tweaklab.brightsigntool.connector.Connector;
import org.tweaklab.brightsigntool.gui.view.WaitScreen;
import org.tweaklab.brightsigntool.model.Keys;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import org.tweaklab.brightsigntool.util.NetworkUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Pane;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  public Label widthLabel;
  @FXML
  private TextField widthField;
  @FXML
  public Label heightLable;
  @FXML
  private TextField heightField;
  @FXML
  public Label freqLable;
  @FXML
  private TextField frequencyField;
  @FXML
  public Label interlacedLable;
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
    widthField.setTextFormatter(new TextFormatter<String>(new NumberFilter()));
    heightField.setTextFormatter(new TextFormatter<String>(new NumberFilter()));
    frequencyField.setTextFormatter(new TextFormatter<String>(new NumberFilter()));
    volumeField.setTextFormatter(new TextFormatter<String>(new NumberFilter()));
    newIPField.setTextFormatter(new TextFormatter<String>(new IpFilter()));
    subnetField.setTextFormatter(new TextFormatter<String>(new IpFilter()));

    mediator = ControllerMediator.getInstance();
    mediator.setUploadController(this);
    this.targetAddressLabel.setText(mediator.getConnector().getName());

    setDisplaySettingsDefaultValues();
    setGeneralSettingsDefaultValues();

    disableDisplayResolutionElements(this.autoDisplaySolutionCheckbox.isSelected());

    addListenerToCheckboxes();
  }

  public void initData(Map<String, String> data) {
    if (data.containsKey("auto")) {
      boolean value = Boolean.parseBoolean(data.get("auto"));
      this.autoDisplaySolutionCheckbox.selectedProperty().setValue(value);
    }
    if (data.containsKey("width")) {
      this.widthField.textProperty().setValue(data.get("width"));
    }
    if (data.containsKey("height")) {
      this.heightField.textProperty().setValue(data.get("height"));
    }
    if (data.containsKey("freq")) {
      this.frequencyField.textProperty().setValue(data.get("freq"));
    }
    if (data.containsKey("interlaced")) {
      boolean value = Boolean.parseBoolean(data.get("interlaced"));
      this.interlacedCheckbox.selectedProperty().setValue(value);
    }
    if (data.containsKey("volume")) {
      this.volumeField.textProperty().setValue(data.get("volume"));
    }
    if (data.containsKey("name")) {
      this.newHostnameField.textProperty().setValue(data.get("name"));
    }
    if (data.containsKey("ip")) {
      this.newIPField.textProperty().setValue(data.get("ip"));
    }
    if (data.containsKey("netmask")) {
      this.subnetField.textProperty().setValue(data.get("netmask"));
    }
    if (data.containsKey("dhcp")) {
      boolean value = Boolean.parseBoolean(data.get("dhcp"));
      this.dhcpCheckbox.selectedProperty().setValue(value);
    }

    // Unset upload checkboxes if field already programmed on connected target.
    if (data.containsKey("auto") || (data.containsKey("width") && data.containsKey("height")
            && data.containsKey("freq") && data.containsKey("interlaced"))) {
      uploadDisplaySettingsCheckbox.setSelected(false);
      uploadMediaCheckbox.setSelected(false);
    }
    if (data.containsKey("volume") && data.containsKey("name")
            && ((data.containsKey("ip") && data.containsKey("netmask")) || data.containsKey("dhcp"))) {
      uploadGeneralSettingsCheckbox.setSelected(false);
      uploadMediaCheckbox.setSelected(false);
    }


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
    if(!mediator.getConnector().isConnected()) {
      MainApp.showInfoMessage("Device is not connected anymore...");
    }

    try {

      if (!validateFields()) {
        return;
      }
      List<UploadFile> systemFilesForUpload = new ArrayList<UploadFile>();

      // create and upload display.xml
      if (this.uploadDisplaySettingsCheckbox.isSelected()) {
        String interlaced = interlacedCheckbox.isSelected() ? "i" : "p";
        String brightSignResolutionString = widthField.getText() + "x" + heightField.getText()
                + "x" + frequencyField.getText() + interlaced;
        if (autoDisplaySolutionCheckbox.isSelected() || mediator.getConnector().isResolutionSupported(brightSignResolutionString)) {
          UploadFile displaySettingsXML = createDisplaySettingsXML();
          systemFilesForUpload.add(displaySettingsXML);
        } else {
          MainApp.showInfoMessage("Video format not supported by connected player. Try another one or use autoformat");
          return;
        }
      }

      // create and upload settings.xml
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

      // upload jar
      String jarName = Keys.loadProperty(Keys.APP_NAME_PROPS_KEY) + "_" + Keys.loadProperty(Keys.ClIENT_VERSION_PROPS_KEY) +  + ".jar";
      InputStream jarAsStream = this.getClass().getResourceAsStream(
              "/" + Keys.loadProperty(Keys.INCLUDED_JAR_RELATIVE_PATH) + "/" + jarName);
      byte[] scriptAsBytes = IOUtils.toByteArray(jarAsStream);
      systemFilesForUpload.add(new UploadFile(jarName, scriptAsBytes));

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

    boolean result = true;

    if (uploadDisplaySettingsCheckbox.isSelected()) {
      if (this.autoDisplaySolutionCheckbox.isSelected() == false
              && (this.widthField.getText().equals("") || this.frequencyField.getText().equals("") || this.heightField.getText().equals(""))) {
        MainApp.showErrorMessage("Empty field in display settings", "Please select Auto Resolution or set height, width and frequency.");
        result = false;
      }
    }

    if (uploadGeneralSettingsCheckbox.isSelected()) {
      if (newHostnameField.getText().equals("") || volumeField.getText().equals("")) {
        MainApp.showErrorMessage("Empty field in system settings", "Please choose a hostname and a Volume.");
        result = false;
      }
      if (dhcpCheckbox.isSelected() == false && (newIPField.getText().equals("") || subnetField.getText().equals(""))) {
        MainApp.showErrorMessage("Empty field", "Please select DHCP or set IP and subnet.");
        result = false;
      }
    }

    if (uploadMediaCheckbox.isSelected() && ControllerMediator.getInstance().getRootController().getMediaUploadData() == null) {
      MainApp.showErrorMessage("No Media Data", "Please add a media config to upload!");
      result = false;
    }

    return result;
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
    newSettings.setGateway(this.newIPField.getText());
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
    newSettings.setGateway(this.newIPField.getText());
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
    subnetField.setDisable(b);
    subnetLabel.setDisable(b);
  }

  private void disableDisplayResolutionElements(boolean disable) {
    this.widthLabel.setDisable(disable);
    this.widthField.setDisable(disable);
    this.heightLable.setDisable(disable);
    this.heightField.setDisable(disable);
    this.freqLable.setDisable(disable);
    this.frequencyField.setDisable(disable);
    this.interlacedLable.setDisable(disable);
    this.interlacedCheckbox.setDisable(disable);
  }

  private void setGeneralSettingsDefaultValues() {
    PlayerGeneralSettings settings = PlayerGeneralSettings.getDefaulGeneralSettings();

    Connector currentConnector = ControllerMediator.getInstance().getConnector();
    if (currentConnector instanceof BrightSignWebConnector) {
      this.newHostnameField.setText(currentConnector.getName());
      String ip = NetworkUtils.resolveHostName(this.newHostnameField.getText());
      if (!ip.equals("")) {
        this.newIPField.setText(ip);
      }
    } else {
      this.newHostnameField.setText(settings.getHostname());
      this.newIPField.setText(settings.getIp());
    }

    this.dhcpCheckbox.setSelected(settings.getDhcp());
    this.subnetField.setText(settings.getNetmask());
    this.volumeField.setText(settings.getVolume());

    disableIpField(settings.getDhcp());
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

    uploadGeneralSettingsCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        generalSettingsPane.setDisable(!newValue);
      }
    });
  }

  private class IpFilter implements UnaryOperator<TextFormatter.Change> {

    Pattern ipPattern = Pattern.compile("(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9])?(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){0,3}]");

    @Override
    public TextFormatter.Change apply(TextFormatter.Change change) {
      Matcher matcher = ipPattern.matcher(change.getControlNewText());
      // if ether the pattern matches, or the matcher hit the end while matching
      if (matcher.matches() || matcher.hitEnd()) {
        return change;
      } else {
        return null;
      }
    }
  }

  private class NumberFilter implements UnaryOperator<TextFormatter.Change> {

    @Override
    public TextFormatter.Change apply(TextFormatter.Change change) {
      if (change.getText().matches("[0-9]*")) {
        return change;
      } else {
        return null;
      }
    }
  }

}
