package org.tweaklab.brightsigntool.configurator;

import org.tweaklab.brightsigntool.model.MediaFile;
import org.tweaklab.brightsigntool.model.MediaType;
import org.tweaklab.brightsigntool.model.ModeType;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XmlConfigCreator {
  private final static Logger LOGGER = Logger.getLogger(XmlConfigCreator.class.getName());


  /**
   * Creates an Display Settings xml file and stores it in work folder
   */
  public static UploadFile createGeneralSettingsXml(PlayerGeneralSettings generalSettings) {
    // xml factory
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder;
    try {
      docBuilder = docFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      LOGGER.log(Level.SEVERE, "Couldn't get a DocumentBuilder out of DocumentBuilderFactory", e);
      throw new IllegalStateException();
    }

    // root elements
    Document doc = docBuilder.newDocument();
    Element rootElement = doc.createElement("general");
    doc.appendChild(rootElement);

    Element nameElement = doc.createElement("name");
    nameElement.appendChild(doc.createTextNode(generalSettings.getHostname()));
    rootElement.appendChild(nameElement);

    Element scriptVersionElement = doc.createElement("scriptVersion");
    scriptVersionElement.appendChild(doc.createTextNode(String.valueOf(generalSettings.getScriptVersion())));
    rootElement.appendChild(scriptVersionElement);

    Element mediaFolderElement = doc.createElement("mediaFolder");
    mediaFolderElement.appendChild(doc.createTextNode(String.valueOf(generalSettings.getMediaFolder())));
    rootElement.appendChild(mediaFolderElement);

    Element ipElement = doc.createElement("ip");
    ipElement.appendChild(doc.createTextNode(generalSettings.getIp()));
    rootElement.appendChild(ipElement);

    Element netmaskElement = doc.createElement("netmask");
    netmaskElement.appendChild(doc.createTextNode(generalSettings.getNetmask()));
    rootElement.appendChild(netmaskElement);

    Element gatewayElement = doc.createElement("gateway");
    gatewayElement.appendChild(doc.createTextNode(generalSettings.getGateway()));
    rootElement.appendChild(gatewayElement);
    Comment comment = doc.createComment("Unfortunately the gateway must be defined to make the network diagnostics happy.");
    gatewayElement.getParentNode().insertBefore(comment, gatewayElement);
    comment = doc.createComment("Leave empty to use the current own IP.");
    gatewayElement.getParentNode().insertBefore(comment, gatewayElement);

    Element dhcpElement = doc.createElement("dhcp");
    dhcpElement.appendChild(doc.createTextNode(generalSettings.getDhcp().toString()));
    rootElement.appendChild(dhcpElement);

    Element sshPasswordElement = doc.createElement("ssh_password");
    // only show insert password when it's actually needed -> debug mode
    if (generalSettings.getDebug()) {
      sshPasswordElement.appendChild(doc.createTextNode(generalSettings.getSshPassword()));
      rootElement.appendChild(sshPasswordElement);
      comment = doc.createComment(" We made the ssh password visible as we use the devices in closed networks only.");
      sshPasswordElement.getParentNode().insertBefore(comment, sshPasswordElement);
    } else {
      sshPasswordElement.appendChild(doc.createTextNode(""));
      rootElement.appendChild(sshPasswordElement);
    }

    Element volumeElement = doc.createElement("volume");
    volumeElement.appendChild(doc.createTextNode(String.valueOf(generalSettings.getVolume())));
    rootElement.appendChild(volumeElement);

    Element tcpPortElement = doc.createElement("tcp_port");
    tcpPortElement.appendChild(doc.createTextNode(String.valueOf(generalSettings.getTcpPort())));
    rootElement.appendChild(tcpPortElement);

    Element debugElement = doc.createElement("debug");
    debugElement.appendChild(doc.createTextNode(generalSettings.getDebug().toString()));
    rootElement.appendChild(debugElement);

    UploadFile xmlFile = transformDocToXmlFile(doc, "settings.xml");
    LOGGER.info("settings.xml created:\n" + new String(xmlFile.getFileAsBytes()));
    return xmlFile;
  }

  /**
   * Creates an Display Settings xml file and stores it in work folder
   */
  public static UploadFile createModeXml(ModeType mode) {

    // xml factory
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder;
    try {
      docBuilder = docFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      LOGGER.log(Level.SEVERE, "Couldn't get a DocumentBuilder out of DocumentBuilderFactory", e);
      throw new IllegalStateException();
    }

    // root elements
    Document doc = docBuilder.newDocument();
    Element modeElement = doc.createElement("mode");
    modeElement.appendChild(doc.createTextNode(mode.toString()));
    doc.appendChild(modeElement);

    UploadFile xmlFile = transformDocToXmlFile(doc, "mode.xml");

    LOGGER.info("mode.xml created:\n" + new String(xmlFile.getFileAsBytes()));
    return xmlFile;
  }


  /**
   * Creates an Display Settings xml file and stores it in work folder
   */
  public static UploadFile createDisplaySettingsXml(PlayerDisplaySettings displaySettings) {
    // xml factory
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder;
    try {
      docBuilder = docFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      LOGGER.log(Level.SEVERE, "Couldn't get a DocumentBuilder out of DocumentBuilderFactory", e);
      throw new IllegalStateException();
    }

    // root elements
    Document doc = docBuilder.newDocument();
    Element rootElement = doc.createElement("displaySettings");
    doc.appendChild(rootElement);

    Element autoElement = doc.createElement("auto");
    autoElement.appendChild(doc.createTextNode(displaySettings.getAuto().toString()));
    rootElement.appendChild(autoElement);

    Element widthElement = doc.createElement("width");
    widthElement.appendChild(doc.createTextNode(String.valueOf(displaySettings.getWidth())));
    rootElement.appendChild(widthElement);

    Element heightElement = doc.createElement("height");
    heightElement.appendChild(doc.createTextNode(String.valueOf(displaySettings.getHeight())));
    rootElement.appendChild(heightElement);

    Element freqElement = doc.createElement("freq");
    freqElement.appendChild(doc.createTextNode(String.valueOf(displaySettings.getFreq())));
    rootElement.appendChild(freqElement);

    Element interlacedElement = doc.createElement("interlaced");
    interlacedElement.appendChild(doc.createTextNode(displaySettings.getInterlaced().toString()));
    rootElement.appendChild(interlacedElement);

    UploadFile xmlFile = transformDocToXmlFile(doc, "display.xml");

    LOGGER.info("display.xml created:\n" + new String(xmlFile.getFileAsBytes()));
    return xmlFile;
  }

  /**
   * Creates an playlist xml config file and stores it in work foldr
   */
  public static UploadFile createPlayListXML(List<MediaFile> mediaFiles) {
    // xml factory
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder;
    try {
      docBuilder = docFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      LOGGER.log(Level.SEVERE, "Couldn't get a DocumentBuilder out of DocumentBuilderFactory", e);
      throw new IllegalStateException();
    }

    // root elements
    Document doc = docBuilder.newDocument();
    Element rootElement = doc.createElement("playlist");
    doc.appendChild(rootElement);

    for (MediaFile mediaFile : mediaFiles) {

      Element file = doc.createElement("task");
      file.setAttribute("type", mediaFile.getMediaType().toString());
      file.appendChild(doc.createTextNode(mediaFile.getFile().getName()));

      if (mediaFile.getMediaType() == MediaType.IMAGE) {
        file.setAttribute("displayTime", (String.valueOf(mediaFile.getDisplayTime())));
      }

      rootElement.appendChild(file);
    }

    UploadFile xmlFile = transformDocToXmlFile(doc, "playlist.xml");

    LOGGER.info("playlist.xml created:\n" + new String(xmlFile.getFileAsBytes()));
    return xmlFile;
  }

  /**
   * @param loopFile  --> file to play if no gpio is selected
   * @param gpioFiles --> files per gpio
   */
  public static UploadFile createGpioXML(MediaFile loopFile, MediaFile[] gpioFiles, Boolean retriggerEnabled, String retriggerDelay) {
    // xml factory
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder;
    try {
      docBuilder = docFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      LOGGER.log(Level.SEVERE, "Couldn't get a DocumentBuilder out of DocumentBuilderFactory.", e);
      throw new IllegalStateException();
    }

    // root elements
    Document doc = docBuilder.newDocument();
    Element rootElement = doc.createElement("gpio");
    doc.appendChild(rootElement);

    // create loop file element
    if (loopFile != null) {

      Element loopElement = doc.createElement("loop");
      loopElement.setAttribute("type", loopFile.getMediaType().toString());
      loopElement.appendChild(doc.createTextNode(loopFile.getFile().getName()));
      rootElement.appendChild(loopElement);
    }

    // create gpio entry for each gpio file
    for (int i = 0; i < gpioFiles.length; i++) {

      if (gpioFiles[i] != null) {

        Element gpioElement = doc.createElement("gpio" + i);
        gpioElement.setAttribute("type", gpioFiles[i].getMediaType().toString());
        gpioElement.appendChild(doc.createTextNode(gpioFiles[i].getFile().getName()));
        rootElement.appendChild(gpioElement);

      }

    }

    // create settings element
    Element retriggerEnabledElement = doc.createElement("retriggerEnabled");
    retriggerEnabledElement.appendChild(doc.createTextNode(retriggerEnabled.toString()));
    rootElement.appendChild(retriggerEnabledElement);

    Element retriggerDelayElement = doc.createElement("retriggerDelay");
    retriggerDelayElement.appendChild(doc.createTextNode(retriggerDelay));
    rootElement.appendChild(retriggerDelayElement);

    UploadFile xmlFile = transformDocToXmlFile(doc, "gpio.xml");

    LOGGER.info("gpio.xml created:\n" + new String(xmlFile.getFileAsBytes()));

    return xmlFile;
  }

  private static UploadFile transformDocToXmlFile(Document doc, String fileName) {

    ByteArrayOutputStream xmlStream = new ByteArrayOutputStream();
    Transformer transformer;

    // write the content to the stream
    TransformerFactory transformerFactory = TransformerFactory.newInstance();

    try {
      transformer = transformerFactory.newTransformer();
    } catch (TransformerConfigurationException e) {
      LOGGER.log(Level.SEVERE, "Couldn't get a Transformer out of TransformerFactory.", e);
      throw new IllegalStateException();
    }

    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

    DOMSource source = new DOMSource(doc);

    StreamResult result = new StreamResult(xmlStream);

    try {
      transformer.transform(source, result);
    } catch (TransformerException e) {
      LOGGER.log(Level.SEVERE, "Error occured while transforming " + doc.getLocalName(), e);
    }

    return new UploadFile(fileName, xmlStream.toByteArray());
  }

}
