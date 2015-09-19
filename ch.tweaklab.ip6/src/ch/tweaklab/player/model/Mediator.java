package ch.tweaklab.player.model;

import ch.tweaklab.player.connector.Connector;
import ch.tweaklab.player.gui.controller.MainApp;
import ch.tweaklab.player.gui.controller.RootPageController;
import ch.tweaklab.player.gui.controller.UploadScreenController;

/**
 * singelton class which contains data to communicate over the applications
 * 
 * @author Alain
 *
 */
public class Mediator {

  private RootPageController rootController;
  private UploadScreenController uploadController;

  private Boolean isConnected = false;

  private Connector connector;

  private MediaUploadData mediaUploadData;
  private SystemUploadData systemUploadData;

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

  public MediaUploadData getUploadData() {
    return mediaUploadData;
  }

  public void setMediaUploadData(MediaUploadData mediaUploadData) {
    if (isConnected) {
      this.mediaUploadData = mediaUploadData;
      this.uploadController.updateCurrentUploadSetLabel(mediaUploadData.getPlayModus().name());
    } else {
      MainApp.showErrorMessage("Not connected", "Please connect before you add upload content");
    }

  }

  public void disconnectFromDevice() {
    isConnected = false;
    mediaUploadData = null;
    connector.disconnect();
    rootController.loadConnectItems();
  }

  public void connectToDevice(String target) {
    isConnected = connector.connect(target);
    if (isConnected) {
      rootController.addTabs();
      rootController.loadUploadItems();
    } else {
      MainApp.showErrorMessage("Connection failed!", "Please verify the target address.");
    }
  }

  /**
   * Everything below here is in support of Singleton pattern
   */
  private Mediator() {

  }

  public static Mediator getInstance() {
    return MediatorHolder.INSTANCE;
  }

  private static class MediatorHolder {
    private static final Mediator INSTANCE = new Mediator();
  }

}
