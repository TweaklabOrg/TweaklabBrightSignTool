package org.tweaklab.brightsigntool.gui.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.apache.commons.io.IOUtils;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * /*
 *
 * @author Alain + Stephan
 */
public class UploadScreenController {
  private static final Logger LOGGER = Logger.getLogger(UploadScreenController.class.getName());
  @FXML
  public Label widthLabel;
  @FXML
  public Label heightLable;
  @FXML
  public Label freqLable;
  @FXML
  public Label interlacedLable;
  WaitScreen waitScreen;
  Task<Boolean> uploadTask;
  ControllerMediator mediator;
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
  private Label subnetLabel;
  @FXML
  private TextField subnetField;
  @FXML
  private CheckBox dhcpCheckbox;
  private Thread uploadThread;
  @FXML
  private Label currentUploadSetLabel;

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
    LOGGER.info("Starting Upload.");
    if (!mediator.getConnector().isConnected()) {
      LOGGER.info("Upload aborted as device is not connected anymore.");
      new Alert(Alert.AlertType.NONE, "Device is not connected anymore...", ButtonType.OK).showAndWait();
    }

    if (!validateFields()) {

      return;
    }
    List<UploadFile> systemFilesForUpload = new ArrayList<>();

    // create and upload display.xml
    if (this.uploadDisplaySettingsCheckbox.isSelected()) {
      String interlaced = interlacedCheckbox.isSelected() ? "i" : "p";
      String brightSignResolutionString = widthField.getText() + "x" + heightField.getText()
              + "x" + frequencyField.getText() + interlaced;
      if (autoDisplaySolutionCheckbox.isSelected() || mediator.getConnector().isResolutionSupported(brightSignResolutionString)) {
        UploadFile displaySettingsXML = createDisplaySettingsXML();
        systemFilesForUpload.add(displaySettingsXML);
      } else {
        new Alert(Alert.AlertType.NONE, "Video format not supported by connected player. Try another one or use autoformat",
                ButtonType.OK).showAndWait();
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
    String jarName = Keys.loadProperty(Keys.APP_NAME_PROPS_KEY) + ".jar";
    InputStream jarAsStream = this.getClass().getResourceAsStream(
            "/" + Keys.loadProperty(Keys.INCLUDED_JAR_RELATIVE_PATH) + "/" + jarName);
    byte[] scriptAsBytes = new byte[0];
    try {
      scriptAsBytes = IOUtils.toByteArray(jarAsStream);
    } catch (IOException e) {
      LOGGER.severe("Can't convert TweaklabBrightSignTool.jar-InputStream to byte[].");
    }
    systemFilesForUpload.add(new UploadFile(jarName, scriptAsBytes));

    // Create Upload Task and add Events
    Connector connector = ControllerMediator.getInstance().getConnector();
    MediaUploadData mediaUploadData = null;
    if (uploadMediaCheckbox.isSelected()) {

      mediaUploadData = ControllerMediator.getInstance().getRootController().getMediaUploadData();

      UploadFile modeXml = XmlConfigCreator.createModeXml(mediaUploadData.getMode());

      systemFilesForUpload.add(modeXml);
      if (mediaUploadData.getUploadList().size() < 1) {
        new Alert(Alert.AlertType.NONE, "No media was selected.", ButtonType.OK).showAndWait();
        return;
      }
    }

    // Show waitscreen
    waitScreen = new WaitScreen();
    waitScreen.setOnCancel(event -> uploadTask.cancel());
    waitScreen.setOnClose(event -> uploadTask.cancel());

    uploadTask = connector.upload(mediaUploadData, systemFilesForUpload);

    uploadTask.setOnSucceeded(event -> uploadTaskSucceedFinish(uploadTask));
    uploadTask.setOnCancelled(event -> uploadTaskCancelledFinish(uploadTask));
    uploadTask.setOnFailed(event -> uploadTaskAbortFinish(uploadTask));

    uploadThread = new Thread(uploadTask);
    uploadThread.start();
  }

  private void uploadTaskSucceedFinish(Task<Boolean> task) {
    try {
      if (uploadTask.get()) {
        waitScreen.closeScreen();
        new Alert(Alert.AlertType.NONE, "Upload finished!", ButtonType.OK).showAndWait();
      } else {
        uploadTaskCancelledFinish(task);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "An exception occured. ", e);
    }
  }

  private void uploadTaskCancelledFinish(Task<Boolean> task) {
    waitScreen.closeScreen();
    if (task.getMessage().equals("")) {
      new Alert(Alert.AlertType.NONE, "Upload cancelled. Parts might be uploaded.", ButtonType.OK).showAndWait();
    } else {
      new Alert(Alert.AlertType.NONE, task.getMessage(), ButtonType.OK).showAndWait();
    }
  }

  private void uploadTaskAbortFinish(Task<Boolean> task) {
    waitScreen.closeScreen();
    try {
      throw task.getException();
    } catch (Throwable throwable) {
      LOGGER.log(Level.SEVERE, "An unhandled exception occured while uploading.", throwable);
      new Alert(Alert.AlertType.NONE, "Sorry. A unknown error occured.", ButtonType.OK).showAndWait();
    }
  }

  private Boolean validateFields() {
    if (!uploadDisplaySettingsCheckbox.isSelected() && !uploadGeneralSettingsCheckbox.isSelected()
            && !uploadMediaCheckbox.isSelected()) {
      new Alert(Alert.AlertType.NONE, "Only BrightSign scripts will be uploaded!", ButtonType.OK).showAndWait();
      LOGGER.info("Only BrightSign scripts will be uploaded!");
    }

    boolean result = true;

    if (uploadDisplaySettingsCheckbox.isSelected()) {
      if (!this.autoDisplaySolutionCheckbox.isSelected()
              && (this.widthField.getText().equals("") || this.frequencyField.getText().equals("") || this.heightField.getText().equals(""))) {
        new Alert(Alert.AlertType.NONE,
                "Empty field in display settings. Please select Auto Resolution or set height, width and frequency.",
                ButtonType.OK).showAndWait();
        LOGGER.info("Empty field in display settings.");
        result = false;
      }
    }

    if (uploadGeneralSettingsCheckbox.isSelected()) {
      if (newHostnameField.getText().equals("") || volumeField.getText().equals("")) {
        new Alert(Alert.AlertType.NONE,
                "Empty field in system settings. Please choose a hostname and a Volume.",
                ButtonType.OK).showAndWait();
        LOGGER.info("Empty field in system settings.");
        result = false;
      }
      if (!dhcpCheckbox.isSelected() && (newIPField.getText().equals("") || subnetField.getText().equals(""))) {
        new Alert(Alert.AlertType.NONE,
                "Empty field. Please select DHCP or set IP and subnet.",
                ButtonType.OK).showAndWait();
        LOGGER.info("Neither DHCP or IP is set.");
        result = false;
      }
    }

    // TODO Find another way to verify validity of media upload settings. Bad solution for ex. becaus it generates an extra log entry everytime the xml is generated.
    if (uploadMediaCheckbox.isSelected() && ControllerMediator.getInstance().getRootController().getMediaUploadData() == null) {
      new Alert(Alert.AlertType.NONE, "No Media Data. Please add a media config to upload!",
              ButtonType.OK).showAndWait();
      LOGGER.info("No media data set.");
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
    return XmlConfigCreator.createDisplaySettingsXml(displaySettings);
  }

  private UploadFile createInitialGeneralDisplaySettingsXML() {
    PlayerGeneralSettings newSettings = PlayerGeneralSettings.getDefaulGeneralSettings();
    newSettings.setHostname(this.newHostnameField.getText());
    newSettings.setIp(this.newIPField.getText());
    newSettings.setVolume(Integer.parseInt(this.volumeField.getText()));
    newSettings.setNetmask(this.subnetField.getText());
    newSettings.setGateway(this.newIPField.getText());
    newSettings.setDhcp(this.dhcpCheckbox.isSelected());
    return XmlConfigCreator.createGeneralSettingsXml(newSettings);
  }

  private UploadFile createGeneralDisplaySettingsXML() {
    PlayerGeneralSettings newSettings = PlayerGeneralSettings.getDefaulGeneralSettings();
    newSettings.setHostname(this.newHostnameField.getText());
    newSettings.setIp(this.newIPField.getText());
    newSettings.setVolume(Integer.parseInt(this.volumeField.getText()));
    newSettings.setNetmask(this.subnetField.getText());
    newSettings.setGateway(this.newIPField.getText());
    newSettings.setDhcp(this.dhcpCheckbox.isSelected());
    newSettings.setInitialize(false);
    return XmlConfigCreator.createGeneralSettingsXml(newSettings);
  }

  private List<UploadFile> getScripts() {
    String[] scriptFileNames = Keys.loadProperty(Keys.BS_SCRIPTS_PROPS_KEY).split(";");
    List<UploadFile> scriptFiles = new ArrayList<>();

    for (String scriptName : scriptFileNames) {
      InputStream scriptInputStream = Keys.class.getResourceAsStream("/" + Keys.SCRIPTS_DIRECTORY + "/" + scriptName);
      byte[] scriptAsBytes = null;
      try {
        scriptAsBytes = IOUtils.toByteArray(scriptInputStream);
      } catch (Exception e) {
        new Alert(Alert.AlertType.NONE, "BrightSign Script " + scriptName + " not found!", ButtonType.OK).showAndWait();
        LOGGER.severe("BrightSign Script " + scriptName + " not found!");
      }
      UploadFile configFile = new UploadFile(scriptName, scriptAsBytes);
      scriptFiles.add(configFile);
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

    LOGGER.info("General settings set to default values.");
  }

  private void setDisplaySettingsDefaultValues() {
    PlayerDisplaySettings displaySettings = PlayerDisplaySettings.getDefaultDisplaySettings();

    this.autoDisplaySolutionCheckbox.setSelected(displaySettings.getAuto());
    this.widthField.setText(String.valueOf(displaySettings.getWidth()));
    this.heightField.setText(String.valueOf(displaySettings.getHeight()));
    this.interlacedCheckbox.setSelected(displaySettings.getInterlaced());
    this.frequencyField.setText(String.valueOf(displaySettings.getFreq()));

    disableDisplayResolutionElements(displaySettings.getAuto());

    LOGGER.info("Display settings set to default values");
  }

  private void addListenerToCheckboxes() {
    autoDisplaySolutionCheckbox.selectedProperty().addListener((ov, old_val, new_val) -> {
      disableDisplayResolutionElements(new_val);
    });

    uploadDisplaySettingsCheckbox.selectedProperty().addListener((ov, old_val, new_val) -> {
      displaySettingsPane.setDisable(!new_val);
    });

    dhcpCheckbox.selectedProperty().addListener((ov, old_val, new_val) -> {
      disableIpField(new_val);
    });

    uploadGeneralSettingsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      generalSettingsPane.setDisable(!newValue);
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
