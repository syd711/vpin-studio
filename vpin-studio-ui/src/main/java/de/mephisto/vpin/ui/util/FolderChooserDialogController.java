package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.system.FolderRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class FolderChooserDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(FolderChooserDialogController.class);

  @FXML
  private TreeView<FolderRepresentation> treeView;

  @FXML
  private Button openBtn;

  @FXML
  private TextField pathField;

  private FolderRepresentation selection;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.selection = null;
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
    openBtn.setDisable(true);

    treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<FolderRepresentation>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<FolderRepresentation>> observable, TreeItem<FolderRepresentation> oldValue, TreeItem<FolderRepresentation> newValue) {
        openBtn.setDisable(newValue == null);
        if(newValue != null) {
          pathField.setText(newValue.getValue().getPath());
        }
        else {
          pathField.setText("");
        }
      }
    });
  }

  @Override
  public void onDialogCancel() {
    this.selection = null;
  }


  public void setPath(String path) {
    JFXFuture.supplyAsync(() -> client.getFolderChooserService().getRoots())
        .thenAcceptLater(folders -> {
          TreeItem root = new TreeItem();
          List<LazyTreeItem> roots = new ArrayList<>();
          for (FolderRepresentation folder : folders) {
            roots.add(createTreeItem(folder));
          }
          root.getChildren().addAll(roots);
          treeView.setRoot(root);
          treeView.refresh();
          treeView.getRoot().setExpanded(true);

          treeView.getRoot().getChildren().get(0).setExpanded(true);

          if (path != null) {
            for (TreeItem t : roots) {
              LazyTreeItem lazyTreeItem = (LazyTreeItem) t;
              FolderRepresentation r = lazyTreeItem.getValue();
              if (path.startsWith(r.getName())) {
                LazyTreeItem expand = expand(lazyTreeItem, path);
                if (expand != null) {
                  treeView.getSelectionModel().select(expand);
                  int row = treeView.getRow(expand);
                  if (row > 5) {
                    treeView.scrollTo(row - 5);
                  }
                  else {
                    treeView.scrollTo(row);
                  }

                }
              }
            }
          }
        });
  }

  private LazyTreeItem expand(@Nullable LazyTreeItem parent, @NonNull String path) {
    List<TreeItem<FolderRepresentation>> children = parent.getChildren();
    if (children != null) {
      for (TreeItem<FolderRepresentation> child : children) {
        if (path.startsWith(child.getValue().getPath())) {
          child.setExpanded(true);
          if (path.equals(child.getValue().getPath())) {
            return (LazyTreeItem) child;
          }
          return expand((LazyTreeItem) child, path);
        }
      }
    }
    return null;
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
