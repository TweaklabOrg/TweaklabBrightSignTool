package org.tweaklab.brightsigntool.gui.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.tweaklab.brightsigntool.model.Keys;
import org.tweaklab.brightsigntool.util.LoggerSetup;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts the GUI
 * @author Alain
 *
 */
public class MainApp extends Application {

  private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
  public static Stage primaryStage;
  private BorderPane rootLayout;

  @Override
  public void start(Stage primaryStage) {
    // start logging environment
    LoggerSetup.setup();

    MainApp.primaryStage = primaryStage;
    MainApp.primaryStage.setTitle(Keys.APP_NICE_NAME_PROPS_KEY + " " + Keys.ClIENT_VERSION_PROPS_KEY);
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
      rootLayout = (BorderPane) loader.load();

      // Show the scene containing the root layout.
      Scene scene = new Scene(rootLayout);
      primaryStage.setScene(scene);
      primaryStage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Shows an Dialog Windows with the Exception stacktrace
   * @param e
   */
  public static void showExceptionMessage(Exception e) {
    e.printStackTrace();
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle("Exception Dialog");
    alert.setHeaderText("An Exception occured!");
    alert.setContentText(e.getMessage());
    alert.initOwner(MainApp.primaryStage);

    // Create expandable Exception.
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    String exceptionText = sw.toString();

    Label label = new Label("The exception stacktrace was:");

    TextArea textArea = new TextArea(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);

    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    GridPane expContent = new GridPane();
    expContent.setMaxWidth(Double.MAX_VALUE);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);

    // Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent);

    alert.showAndWait();

  }

  /**
   * shows an error dialog
   */
  public static void showErrorMessage(String header, String content) {
    Alert alert = new Alert(AlertType.WARNING);
    alert.initOwner(MainApp.primaryStage);
    alert.setTitle("Error!");
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }

  /**
   * shows an information dialog
   * @param message
   */
  public static void showInfoMessage(String message) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.initOwner(MainApp.primaryStage);
    alert.setTitle("Information");
    alert.setHeaderText("Information:");
    alert.setContentText(message);
    alert.showAndWait();
  }

  public static void main(String[] args) {
    try {
      launch(args);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unexpected Exception!", e);
    }
  }
}
