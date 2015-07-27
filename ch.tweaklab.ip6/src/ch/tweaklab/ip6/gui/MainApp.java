package ch.tweaklab.ip6.gui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import ch.tweaklab.ip6.gui.controller.RootPageController;

public class MainApp extends Application {

  public static Stage primaryStage;
  private BorderPane rootLayout;

  @Override
  public void start(Stage primaryStage) {
    MainApp.primaryStage = primaryStage;
    MainApp.primaryStage.setTitle("Tweaklab IP6");

    initRootLayout();
  }

  /**
   * Initializes the root layout.
   */
  public void initRootLayout() {
    try {
      // Load root layout from fxml file.
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(this.getClass().getResource("view/RootPage.fxml"));
      rootLayout = (BorderPane) loader.load();

      // Show the scene containing the root layout.
      Scene scene = new Scene(rootLayout);
      primaryStage.setScene(scene);
      primaryStage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  
  public static void showExceptionMessage(Exception e){
    e.printStackTrace();
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle("Exception Dialog");
    alert.setHeaderText("An Exception occured!");
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
  
  public static void showErrorMessage(String errorMessage) {
    Alert alert = new Alert(AlertType.WARNING);
    alert.initOwner(MainApp.primaryStage);
    alert.setTitle("Error!");
    alert.setHeaderText("An Error Occured!");
    alert.setContentText(errorMessage);
    alert.showAndWait();
  }

  public static void showInfoMessage(String message) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.initOwner(MainApp.primaryStage);
    alert.setTitle("Information");
    alert.setHeaderText("Information:");
    alert.setContentText(message);
    alert.showAndWait();
  }
  
  public static void main(String[] args) {
    launch(args);
  }
}
