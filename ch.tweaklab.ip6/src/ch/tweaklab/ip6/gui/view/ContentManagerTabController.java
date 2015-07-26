package ch.tweaklab.ip6.gui.view;

import java.io.File;
import java.util.Collections;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import ch.tweaklab.ip6.application.model.ApplicationData;
import ch.tweaklab.ip6.connector.Connector;
public class ContentManagerTabController {

  
  @FXML
  private ListView<File> listView;
  private RootPageController rootPageController;
  
 // private ArrayList<File> fileList;
  
  /**
   * Initializes the controller class. This method is automatically called after the fxml file has
   * been loaded.
   */
  @FXML
  private void initialize() {
 // fileList = new ArrayList<File>();
  listView.setCellFactory(param -> new MediaContentCell());
  }
  
  @FXML
  private void handleAddFileToListView(){
    final FileChooser fileChooser = new FileChooser();
    File choosenFile = fileChooser.showOpenDialog(rootPageController.getDialogStage());
    if (choosenFile != null) {
      listView.getItems().add(choosenFile);
      //fileList.add(choosenFile);
    }
  }
  
  
  @FXML
  private void handleDelete(){
    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
    listView.getItems().remove(selectedIndex);
  }
  
  public void setRootPageController(RootPageController rootPageController) {
    this.rootPageController = rootPageController;
  }
  
  @FXML
  private void handleUpload(){
    Connector connector = ApplicationData.getConnector();
    connector.uploadMediaFiles(listView.getItems());
    listView.getItems().clear();
    this.rootPageController.showInfoMessage("Upload finished");
  }

  @FXML
  private void moveItemUp(){
    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
    if(selectedIndex > 0){
    Collections.swap(listView.getItems(), selectedIndex,selectedIndex -1);
    listView.getSelectionModel().select(selectedIndex-1);
    }
  }
  
  @FXML
  private void moveItemDown(){
    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
    if(selectedIndex < listView.getItems().size()-1)
    Collections.swap(listView.getItems(), selectedIndex,selectedIndex +1);
    listView.getSelectionModel().select(selectedIndex+1);
  }
  

    
}


