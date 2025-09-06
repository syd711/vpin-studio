package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.system.FolderRepresentation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class FolderChooserDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(FolderChooserDialogController.class);

  @FXML
  private TreeView<FolderRepresentation> treeView;

  @FXML
  private Button openBtn;


  private FolderRepresentation selection;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onOpenClick(ActionEvent e) {
    TreeItem<FolderRepresentation> selectedItem = treeView.getSelectionModel().getSelectedItem();
    selection = selectedItem.getValue();

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    treeView.setShowRoot(false);
    treeView.setMaxWidth(Double.MAX_VALUE);
    treeView.setMaxHeight(Double.MAX_VALUE);
  }

  @Override
  public void onDialogCancel() {

  }


  public void setPath(String path) {
    JFXFuture.supplyAsync(() -> client.getFolderChooserService().getRoots())
        .thenAcceptLater(folders -> {
          TreeItem root = new TreeItem();
          for (FolderRepresentation folder : folders) {
            root.getChildren().add(createTreeItem(folder));
          }
          treeView.setRoot(root);
          treeView.refresh();
          treeView.getRoot().setExpanded(true);
        });

  }

  public FolderRepresentation getSelection() {
    return selection;
  }

  private static LazyTreeItem createTreeItem(FolderRepresentation folderRepresentation) {
    LazyTreeItem item = new LazyTreeItem(folderRepresentation);
    item.setGraphic(WidgetFactory.createIcon("mdi2f-folder-outline"));
    item.expandedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          item.setGraphic(WidgetFactory.createIcon("mdi2f-folder-open-outline"));
        }
        else {
          item.setGraphic(WidgetFactory.createIcon("mdi2f-folder-outline"));
        }
      }
    });
    return item;
  }

  static class LazyTreeItem extends TreeItem<FolderRepresentation> {
    private boolean childrenLoaded = false;

    public LazyTreeItem(FolderRepresentation folder) {
      super(folder);

      // Placeholder so that expand arrow shows up before children are loaded
      this.getChildren().add(new TreeItem<>());

      // Listen for expansion
      this.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
        if (isNowExpanded && !childrenLoaded) {
          loadChildren();
        }
      });
    }

    private void loadChildren() {
      childrenLoaded = true;
      this.getChildren().setAll(buildChildren(getValue()));
    }

    @Override
    public boolean isLeaf() {
      return getValue() == null || getValue().getChildren().isEmpty();
    }

    /**
     * Creates child items for directories only.
     */
    private ObservableList<TreeItem<FolderRepresentation>> buildChildren(FolderRepresentation f) {
      if (f != null) {
        FolderRepresentation loadedFolder = client.getFolderChooserService().getFolder(f.getPath());
        ObservableList<TreeItem<FolderRepresentation>> children = FXCollections.observableArrayList();

        for (FolderRepresentation childFile : loadedFolder.getChildren()) {
          children.add(createTreeItem(childFile));
        }
        return children;
      }
      return FXCollections.emptyObservableList();
    }

    @Override
    public String toString() {
      return getValue().toString();
    }
  }
}
