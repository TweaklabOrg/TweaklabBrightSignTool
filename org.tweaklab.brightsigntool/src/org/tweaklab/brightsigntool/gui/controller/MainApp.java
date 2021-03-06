package org.tweaklab.brightsigntool.gui.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.tweaklab.brightsigntool.model.Keys;
import org.tweaklab.brightsigntool.util.LoggerSetup;
import org.tweaklab.brightsigntool.util.OSValidator;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts the GUI
 *
 * @author Alain + Stephan
 */
public class MainApp extends Application {

  private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
  public static Stage primaryStage;
  private BorderPane rootLayout;

  public static void main(String[] args) {
    try {
      launch(args);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unexpected Exception!", e);
      new Alert(Alert.AlertType.NONE, "An unknown error occured.", ButtonType.OK).showAndWait();
    }
  }

  @Override
  public void start(Stage primaryStage) {
    // start logging environment
    LoggerSetup.setup();

    //     Verify if platform is supported
    if(OSValidator.isMac()) {
      // ok
    } else if(OSValidator.isWindows()) {
      LOGGER.info("Running on Windows. Limited support.");
      new Alert(Alert.AlertType.NONE,
              "Windows has limited support at the moment. Some parts might work anyway.\n" +
                      "Feel free to extend support. Find the code on github.",
              ButtonType.OK).showAndWait();
    } else {
      LOGGER.info("Aborting as platform is neither mac or windows.");
      new Alert(Alert.AlertType.NONE,
              "Sorry. Platform currently not supported. Some parts might work anyway.\n " +
                      "Feel free to extend support. Find the code on github.",
              ButtonType.OK).showAndWait();
    }

    MainApp.primaryStage = primaryStage;
    MainApp.primaryStage.setTitle(Keys.loadProperty(Keys.APP_NICE_NAME_PROPS_KEY) + " " + Keys.loadProperty(Keys.ClIENT_VERSION_PROPS_KEY));
    MainApp.primaryStage.setResizable(false);

    initRootLayout();
  }

  /**
   * Initializes the root layout.
   */
  public void initRootLayout() {
    try {
      // Load root layout from fxml file.
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(this.getClass().getResource(Keys.ROOT_PAGE_FXML_PATH));
      rootLayout = loader.load();

      // Show the scene containing the root layout.
      Scene scene = new Scene(rootLayout);
      primaryStage.setScene(scene);
      primaryStage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
