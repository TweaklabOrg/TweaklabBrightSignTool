package ch.tweaklab.ip6.gui;

import ch.tweaklab.ip6.connector.Connector;
/**
 * Static class which contains the current used connector class.
 * @author Alf
 *
 */
public class ApplicationData {

  
 private static Connector connector;


public static Connector getConnector() {
  return connector;
}

public static void setConnector(Connector connector) {
  ApplicationData.connector = connector;
}
  
  
}
