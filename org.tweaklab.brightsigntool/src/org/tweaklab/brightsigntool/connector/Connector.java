package org.tweaklab.brightsigntool.connector;

/**
 * Absctract Class for Connector Classes.
 * This methods are called in the GUI via reflection.
 * the current used connector class is stored in ApplicationData.java
 */

import javafx.concurrent.Task;
import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.model.MediaUploadData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Connector {

  public static final Logger LOGGER = Logger.getLogger(Connector.class.getName());
  protected String target = "";
  protected String name = "";
 
 

 /**
  * connect to a device
  */
  public abstract boolean connect(String target); 
  
  /**
   * disconnect from current device
   */
  public abstract boolean disconnect();
  
  public abstract Task<Boolean> upload(MediaUploadData uploadData, List<UploadFile> systemFiles);


  /**
   * Check if Device is currently connected
   */
  abstract public Boolean isConnected();

  /**
   * get the current host
   */
  public String getTarget() {
    return target;
  }
  

  public String getName() {
	return name;
}

  public abstract Task<List<String>> getPossibleTargets();

  public abstract Map<String, String> getSettingsOnDevice();

  public abstract boolean isResolutionSupported(String brightSignResolutionString);

  protected void collectEntries(InputStream file, Map<String, String> result, DocumentBuilder builder) {
    Document xml = getDocFromXML(file, builder);
    if (xml != null) {
      Element settings = xml.getDocumentElement();
      Node entry = settings.getChildNodes().item(0);
      while (entry != null) {
        if (entry.getNodeType() == Node.ELEMENT_NODE) {
          result.put(entry.getNodeName(), entry.getTextContent());
        }
        entry = entry.getNextSibling();
      }
    }
  }

  protected void collectMode(InputStream file, Map<String, String> result, DocumentBuilder builder) {
    Document xml = getDocFromXML(file, builder);
    if (xml != null) {
      Element mode = xml.getDocumentElement();
      result.put(mode.getTagName(), mode.getTextContent());
    }
  }

  protected Document getDocFromXML(InputStream file, DocumentBuilder builder) {
    Document xml = null;
    try {
      xml = builder.parse(file);
    } catch (SAXException e) {
      LOGGER.log(Level.WARNING, "Couldn't parse " + file, e);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Couldn't find " + file, e);
    }
    return xml;
  }
}
