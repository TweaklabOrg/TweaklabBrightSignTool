package ch.tweaklab.ip6.application.model;

import ch.tweaklab.ip6.connector.Connector;

public class ApplicationData {

  
 private static Connector connector;

public static Connector getConnector() {
  return connector;
}

public static void setConnector(Connector connector) {
  ApplicationData.connector = connector;
}
  
  
}
