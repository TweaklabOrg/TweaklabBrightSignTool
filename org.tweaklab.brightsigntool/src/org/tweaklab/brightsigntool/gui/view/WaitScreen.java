package org.tweaklab.brightsigntool.gui.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.tweaklab.brightsigntool.gui.controller.MainApp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains a window with a wait screen and blocks the main windows.
 * 
 * @author Alf
 *
 */
public class WaitScreen {
  private static final Logger LOGGER = Logger.getLogger(WaitScreen.class.getName());

  Stage stage;
  Button button;

  public WaitScreen() {

    AnchorPane layout = null;
    stage = new Stage();
    stage.setResizable(false);
    stage.initModality(Modality.WINDOW_MODAL);
    stage.initOwner(MainApp.primaryStage);
    try {
      layout = (AnchorPane) FXMLLoader.load(getClass().getResource("WaitScreen.fxml"));
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "FXMLLoader can't load WaitScreen.fxml.", e);
    }
    Scene scene = new Scene(layout);
    button = new Button();
    button.setText("Cancel Upload");
    layout.getChildren().add(button);
    button.setLayoutX(90);
    button.setLayoutY(200);
    stage.setScene(scene);
    stage.show();
  }

  /**
   * close the wait windows
   */
  public void closeScreen() {
    stage.close();
  }

  /**
   * show the wait window
   */
  public void showScreen() {
    stage.show();
  }

  /**
   * Set an eventhandler for the Cancel Button
   * @param handler
   */
  public void setOnCancel(EventHandler<ActionEvent> handler) {
    button.setOnAction(handler);
  }
/**
 * Set an eventhandler for closing of the window
 * @param handler
 */
  public void setOnClose(EventHandler<WindowEvent> handler) {
    stage.setOnCloseRequest(handler);
  }

}
