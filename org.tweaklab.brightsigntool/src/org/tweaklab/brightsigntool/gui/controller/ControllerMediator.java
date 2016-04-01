package org.tweaklab.brightsigntool.gui.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.tweaklab.brightsigntool.connector.Connector;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * singelton class which contains data to communicate over the applications
 *
 * @author Alain + Stephan
 */
public class ControllerMediator {
  private static final Logger LOGGER = Logger.getLogger(ControllerMediator.class.getName());

  private RootPageController rootController;

  private Boolean isConnected = false;

  private Connector connector;

  private ControllerMediator() {
  }

  public static ControllerMediator getInstance() {
    return MediatorHolder.INSTANCE;
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
      LOGGER.log(Level.INFO, "Tried to disconnect " + connector.getTarget() + ", but still seems to be connected.");
      new Alert(Alert.AlertType.NONE,
              "Tried to disconnect " + connector.getTarget() + ", but still seems to be connected.",
              ButtonType.OK).showAndWait();
    }
    rootController.disconnectFromDevice();
  }

  public void connectToDevice(String target) {
    isConnected = connector.connect(target);
    if (isConnected) {
      rootController.connectToDevice(connector.getSettingsOnDevice());
    } else {
      LOGGER.log(Level.WARNING, "An error occurred while connecting to device " + target);
      // TODO move that message to connect, as now sometimes it shows two messages for one error.
      new Alert(Alert.AlertType.NONE, "Connection failed! Please verify the target address.", ButtonType.OK).showAndWait();
    }
  }

  private static class MediatorHolder {
    private static final ControllerMediator INSTANCE = new ControllerMediator();
  }
}
