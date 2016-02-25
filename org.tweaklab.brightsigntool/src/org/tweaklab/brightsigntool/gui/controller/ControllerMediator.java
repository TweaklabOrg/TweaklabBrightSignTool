package org.tweaklab.brightsigntool.gui.controller;

import org.tweaklab.brightsigntool.connector.Connector;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * singelton class which contains data to communicate over the applications
 * 
 * @author Alain
 *
 */
public class ControllerMediator {
  private static final Logger LOGGER = Logger.getLogger(ControllerMediator.class.getName());

  private RootPageController rootController;
  private UploadScreenController uploadController;
 
  private Boolean isConnected = false;

  private Connector connector;


  public UploadScreenController getUploadController() {
    return uploadController;
  }

  public void setUploadController(UploadScreenController uploadController) {
    this.uploadController = uploadController;
  }

  public RootPageController getRootController() {
    return rootController;
  }

  public void setRootController(RootPageController rootController) {
    this.rootController = rootController;
  }

  public Connector getConnector() {
    return connector;
  }

  public void setConnector(Connector connector) {
    this.connector = connector;
  }


  public void disconnectFromDevice() {
    isConnected = false;
    if (!connector.disconnect()) {
      MainApp.showInfoMessage("Device might not have been disconnected correctly.");
      LOGGER.log(Level.INFO, "Tried to disconnect " + connector.getTarget() + ", but still seems to be connected.");
    }
    rootController.disconnectFromDevice();
  }

  public void connectToDevice(String target) {
    isConnected = connector.connect(target);
    if (isConnected) {
      rootController.connectToDevice(connector.getSettingsOnDevice());
    } else {
      MainApp.showErrorMessage("Connection failed!", "Please verify the target address.");
      LOGGER.log(Level.WARNING, "An error occurred while connecting to device " + target);
    }
  }

  /**
   * Everything below here is in support of Singleton pattern
   */
  private ControllerMediator() {

  }

  public static ControllerMediator getInstance() {
    return MediatorHolder.INSTANCE;
  }

  private static class MediatorHolder {
    private static final ControllerMediator INSTANCE = new ControllerMediator();
  }
}
