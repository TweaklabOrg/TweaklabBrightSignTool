package ch.tweaklab.ip6.gui.view;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ch.tweaklab.ip6.gui.controller.MainApp;
/**
 * This class contains a window with a wait screen and blocks the main windows.
 * 
 * @author Alf
 *
 */
public class WaitScreen {

  Stage stage;
  Button button;

  public WaitScreen() {

    AnchorPane layout;
    stage = new Stage();
    stage.setResizable(false);
    stage.initModality(Modality.WINDOW_MODAL);
    stage.initOwner(MainApp.primaryStage);
    try {
      layout = (AnchorPane) FXMLLoader.load(getClass().getResource("WaitScreen.fxml"));
      Scene scene = new Scene(layout);
      button = new Button();
      button.setText("Cancel Upload");
      layout.getChildren().add(button);
      button.setLayoutX(90);
      button.setLayoutY(200);
      stage.setScene(scene);
      stage.show();
    } catch (IOException e) {
      MainApp.showExceptionMessage(e);
    }
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
