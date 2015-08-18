package ch.tweaklab.ip6.media;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javafx.scene.control.Button;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLConfigCreator {

  private final static String WORK_DIRECTORY = "work";
  Properties configFile;

  /**
   * Creates an playlist xml config file and stores it in work foldr
   * 
   * @param mediaFiles
   * @return
   */
  public static File createPlayListXML(List<MediaFile> mediaFiles) {
    File xmlFile = null;

    try {
      // create file in workfolder
      xmlFile = new File(WORK_DIRECTORY + "/playlist.xml");
      if (xmlFile.exists()) {
        xmlFile.delete();
      }
      xmlFile.createNewFile();

      // xml factory
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = null;
      docBuilder = docFactory.newDocumentBuilder();

      // root elements
      Document doc = docBuilder.newDocument();
      Element rootElement = doc.createElement("playlist");
      doc.appendChild(rootElement);

      rootElement.setAttribute("date", new Date().toGMTString());
      Element files = doc.createElement("files");
      rootElement.appendChild(files);

      for (MediaFile mediaFile : mediaFiles) {

        Element file = doc.createElement("file");
        files.appendChild(file);

        Element filename = doc.createElement("filename");
        filename.appendChild(doc.createTextNode(mediaFile.getFile().getName()));
        file.appendChild(filename);

        Element mediaType = doc.createElement("type");
        mediaType.appendChild(doc.createTextNode(mediaFile.getMediaType().toString()));
        file.appendChild(mediaType);

        if (mediaFile.getMediaType() == MediaType.IMAGE) {
          Element displayTime = doc.createElement("displaytime");
          displayTime.appendChild(doc.createTextNode((String.valueOf(mediaFile.getDisplayTime()))));
          file.appendChild(displayTime);
        }

      }
      // write the content to the stream
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = null;

      transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(xmlFile);

      transformer.transform(source, result);

    } catch (Exception e) {
      e.printStackTrace();
      return null;
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
      // create file in workfolder
      xmlFile = new File(WORK_DIRECTORY + "/gpio.xml");
      if (xmlFile.exists()) {
        xmlFile.delete();
      }
      xmlFile.createNewFile();

      // xml factory
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = null;
      docBuilder = docFactory.newDocumentBuilder();

      // root elements
      Document doc = docBuilder.newDocument();
      Element rootElement = doc.createElement("gpio");
      doc.appendChild(rootElement);

      rootElement.setAttribute("date", new Date().toGMTString());

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
      
      
      // write the content to the stream
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = null;

      transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(xmlFile);

      transformer.transform(source, result);

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    return xmlFile;

  }

}
