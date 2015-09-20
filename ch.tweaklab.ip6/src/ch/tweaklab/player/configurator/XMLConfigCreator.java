package ch.tweaklab.player.configurator;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.tweaklab.player.gui.controller.MainApp;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.model.MediaType;

public class XMLConfigCreator {

  private final static String WORK_DIRECTORY = "work";

  /**
   * Creates an Display Settings xml file and stores it in work folder
   * 
   * @param mediaFiles
   * @return
   */
  public static File createGeneralSettingsXml(PlayerGeneralSettings generalSettings) {
    File xmlFile = null;

    try {
      // xml factory
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = null;
      docBuilder = docFactory.newDocumentBuilder();

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

      Element modeElement = doc.createElement("mode");
      modeElement.appendChild(doc.createTextNode(String.valueOf(generalSettings.getMode())));
      rootElement.appendChild(modeElement);

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
      comment = doc.createComment("Chose any reachable ip if you don't have a gateway.");
      gatewayElement.getParentNode().insertBefore(comment, gatewayElement);

      Element dhcpElement = doc.createElement("dhcp");
      dhcpElement.appendChild(doc.createTextNode(generalSettings.getDhcp().toString()));
      rootElement.appendChild(dhcpElement);

      Element sshPasswordElement = doc.createElement("ssh_password");
      sshPasswordElement.appendChild(doc.createTextNode(generalSettings.getSshPassword()));
      rootElement.appendChild(sshPasswordElement);
      comment = doc.createComment(" We made the ssh password visible as we use the devices in closed networks only.");
      sshPasswordElement.getParentNode().insertBefore(comment, sshPasswordElement);
      comment = doc.createComment("Like that you can always find out the password by reading the SD.");
      sshPasswordElement.getParentNode().insertBefore(comment, sshPasswordElement);

      Element volumeElement = doc.createElement("volume");
      volumeElement.appendChild(doc.createTextNode(String.valueOf(generalSettings.getVolume())));
      rootElement.appendChild(volumeElement);

      Element initializeElement = doc.createElement("initialize");
      initializeElement.appendChild(doc.createTextNode(generalSettings.getInitialize().toString()));
      rootElement.appendChild(initializeElement);
      comment = doc.createComment(" If <initialize> is set to true, the registry will be cleared and all settings will be set as configured in the xmls.");
      sshPasswordElement.getParentNode().insertBefore(comment, initializeElement);

      Element tcpPortElement = doc.createElement("tcp_port");
      tcpPortElement.appendChild(doc.createTextNode(String.valueOf(generalSettings.getTcpPort())));
      rootElement.appendChild(tcpPortElement);

      Element debugElement = doc.createElement("debug");
      debugElement.appendChild(doc.createTextNode(generalSettings.getDebug().toString()));
      rootElement.appendChild(debugElement);

      xmlFile = transformDocToXmlFile(doc, WORK_DIRECTORY + "/settings.xml");

    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
      e.printStackTrace();
    }

    return xmlFile;

  }

  /**
   * Creates an Display Settings xml file and stores it in work folder
   * 
   * @param mediaFiles
   * @return
   */
  public static File createDisplaySettingsXml(PlayerDisplaySettings displaySettings) {
    File xmlFile = null;

    try {
      // xml factory
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = null;
      docBuilder = docFactory.newDocumentBuilder();

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

      xmlFile = transformDocToXmlFile(doc, WORK_DIRECTORY + "/display.xml");

    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
      e.printStackTrace();
    }

    return xmlFile;

  }

  /**
   * Creates an playlist xml config file and stores it in work foldr
   * 
   * @param mediaFiles
   * @return
   */
  public static File createPlayListXML(List<MediaFile> mediaFiles) {
    File xmlFile = null;

    try {

      // xml factory
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = null;
      docBuilder = docFactory.newDocumentBuilder();

      // root elements
      Document doc = docBuilder.newDocument();
      Element rootElement = doc.createElement("playlist");
      doc.appendChild(rootElement);
      Element files = doc.createElement("files");
      rootElement.appendChild(files);

      for (MediaFile mediaFile : mediaFiles) {

        Element file = doc.createElement("file");
        file.setAttribute("type", mediaFile.getMediaType().toString());
        file.appendChild(doc.createTextNode(mediaFile.getFile().getName()));

        if (mediaFile.getMediaType() == MediaType.IMAGE) {
          file.setAttribute("displayTime", (String.valueOf(mediaFile.getDisplayTime())));
        }

        files.appendChild(file);

      }

      xmlFile = transformDocToXmlFile(doc, WORK_DIRECTORY + "/playlist.xml");

    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
      e.printStackTrace();
    }

    return xmlFile;

  }

  /**
   * 
   * @param loopFile --> file to play if no gpio is selected
   * @param gpioFiles --> files per gpio
   * @param retriggerEnabled
   * @param retriggerDelay
   * @return
   */
  public static File createGpioXML(MediaFile loopFile, MediaFile[] gpioFiles, Boolean retriggerEnabled, String retriggerDelay) {
    File xmlFile = null;

    try {

      // xml factory
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = null;
      docBuilder = docFactory.newDocumentBuilder();

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

      Element retriggerDelayElement = doc.createElement("retriggerEnabled");
      retriggerDelayElement.appendChild(doc.createTextNode(retriggerDelay));
      rootElement.appendChild(retriggerDelayElement);

      xmlFile = transformDocToXmlFile(doc, WORK_DIRECTORY + "/gpio.xml");

    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
      e.printStackTrace();
    }

    return xmlFile;

  }

  private static File transformDocToXmlFile(Document doc, String filePath) {
    File xmlFile = null;
    Transformer transformer = null;
    TransformerFactory transformerFactory = null;
    try {

      // write the content to the stream
      transformerFactory = TransformerFactory.newInstance();

      transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      DOMSource source = new DOMSource(doc);

      // create file in workfolder
      xmlFile = new File(filePath);
      if (xmlFile.exists()) {
        xmlFile.delete();
      }
      xmlFile.createNewFile();
      StreamResult result = new StreamResult(xmlFile);

      transformer.transform(source, result);
    } catch (Exception e) {
      MainApp.showExceptionMessage(e);
      e.printStackTrace();
    } finally {

    }
    return xmlFile;
  }

}
