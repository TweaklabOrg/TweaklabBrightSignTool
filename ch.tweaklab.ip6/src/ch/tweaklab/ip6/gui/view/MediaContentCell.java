package ch.tweaklab.ip6.gui.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
/**
 * Cell Class for ListView in MediaContent Page. 
 * Needed for Drag and Drop
 * @author 
 *
 */
public class MediaContentCell extends ListCell<File> {
  

  public MediaContentCell() {
      ListCell thisCell = this;

      setOnDragDetected(event -> {
          if (getItem() == null) {
              return;
          }

          Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
          ClipboardContent content = new ClipboardContent();
          content.putString(getItem().getName());
          List<File> fileList = new ArrayList<File>();
          fileList.add(getItem());
          content.putFiles(fileList);
          
          dragboard.setDragView(
              //set an image
              null
          );
          dragboard.setContent(content);

          event.consume();
      });

      setOnDragOver(event -> {
          if (event.getGestureSource() != thisCell &&
                 event.getDragboard().hasString()) {
              event.acceptTransferModes(TransferMode.MOVE);
          }

          event.consume();
      });

      setOnDragEntered(event -> {
          if (event.getGestureSource() != thisCell &&
                  event.getDragboard().hasString()) {
              setOpacity(0.3);
          }
      });

      setOnDragExited(event -> {
          if (event.getGestureSource() != thisCell &&
                  event.getDragboard().hasString()) {
              setOpacity(1);
          }
      });

      setOnDragDropped(event -> {
          if (getItem() == null) {
              return;
          }

          Dragboard db = event.getDragboard();
          boolean success = false;

          if (db.hasFiles()) {
              ObservableList<File> itemsCopy = getListView().getItems();
              int draggedIdx = itemsCopy.indexOf(db.getFiles().get(0));
              int thisIdx = itemsCopy.indexOf(getItem());
             
              File temp = itemsCopy.get(draggedIdx);
              itemsCopy.set(draggedIdx, getItem());
              itemsCopy.set(thisIdx, temp);

              success = true;
          }
          event.setDropCompleted(success);

          event.consume();
      });

    setOnDragDone(DragEvent::consume);
  }
  @Override
  protected void updateItem(File item, boolean empty) {
      super.updateItem(item, empty);

      if (empty || item == null) {
          setGraphic(null);
      } else {
       Label label = new Label(item.getName());
         setGraphic(label);
      }
  }

}
