package de.mephisto.vpin.ui.dropins;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.commons.utils.TrashBin;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationItem;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.*;

public class DropInContainerController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(DropInContainerController.class);

  private final static int IMAGE_WIDTH = 100;

  @FXML
  private BorderPane root;

  @FXML
  private VBox dataPanel;

  @FXML
  private VBox dragHandler;

  @FXML
  private Label filenameLabel;

  @FXML
  private Label sizeLabel;

  @FXML
  private Button installBtn;

  @FXML
  private Button openBtn;

  @FXML
  private ImageView imageView;

  @FXML
  private HBox imageWrapper;

  @FXML
  private Separator installSeparator;

  private MenuButton dropInButton;

  private File file;

  @FXML
  private void onOpen() {
    if (file.isFile()) {
      SystemUtil.openFolder(file.getParentFile());
    }
    else {
      SystemUtil.openFolder(file);
    }
  }

  @FXML
  private void onDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete File", "Move \"" + file.getName() + "\" to trash bin?");
    if (result.get().equals(ButtonType.OK)) {
      if (!TrashBin.moveTo(file)) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Deletion failed, another process is blocking this file.");
      }
    }
    DropInManager.getInstance().reload();
  }

  @FXML
  private void onInstall() {
    NavigationItem activeNavigation = NavigationController.getActiveNavigation();
    if (activeNavigation.equals(NavigationItem.Tables) && TablesController.INSTANCE.isTablesSelected()) {
      DropInManager.getInstance().install(file);
    }
    else {
      WidgetFactory.showAlert(Studio.stage, "Invalid View", "Drop-ins can only be installed when the table overview is selected.");
    }
  }

  public void setData(@Nullable MenuButton dropInButton, @NonNull File file) {
    this.dropInButton = dropInButton;
    this.file = file;
    filenameLabel.setText(file.getName());
    filenameLabel.setStyle("-fx-font-size: 15px;-fx-font-weight: bold;");
    filenameLabel.setTooltip(new Tooltip(file.getName()));
    sizeLabel.setText("" + FileUtils.readableFileSize(file.length()) + ", created " + DateUtil.formatDateTime(new Date(file.lastModified())));
    sizeLabel.setStyle("-fx-font-size: 13px");

    String suffix = FilenameUtils.getExtension(file.getName()).toLowerCase();
    boolean imagePreview = Arrays.asList("png", "jpg").contains(suffix);
    if (imagePreview) {
      boolean hidden = true;//!AssetType.isInstallable(suffix);
      this.installBtn.setVisible(!hidden);
      this.installSeparator.setVisible(!hidden);
      try {
        FileInputStream fileInputStream = new FileInputStream(file);
        Image image = new Image(fileInputStream);
        fileInputStream.close();
        imageView.setImage(image);
      }
      catch (Exception e) {
        LOG.error("Failed to set image: " + e, e);
      }
      dataPanel.setPrefWidth(dataPanel.getPrefWidth() + 70);
    }
    else {
      dataPanel.setPrefWidth(dataPanel.getPrefWidth() + IMAGE_WIDTH);
      imageWrapper.setVisible(false);
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    openBtn.setDisable(!OSUtil.isWindows());//TODO

    installBtn.managedProperty().bindBidirectional(installBtn.visibleProperty());
    installSeparator.managedProperty().bindBidirectional(installSeparator.visibleProperty());
    imageWrapper.managedProperty().bindBidirectional(imageWrapper.visibleProperty());
    dragHandler.setStyle("-fx-cursor: hand;");
    dataPanel.setStyle("-fx-cursor: hand;");
    root.setOnDragDetected(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent event) {
        /* drag was detected, start a drag-and-drop gesture*/
        /* allow any transfer mode */
        Dragboard db = root.startDragAndDrop(TransferMode.ANY);
        db.setDragView(root.snapshot(null, null));

        Map<DataFormat, Object> data = new HashMap<>();
        data.put(DataFormat.FILES, Arrays.asList(file));
        db.setContent(data);
        event.consume();

        ((BorderPane) event.getSource()).getParent().getParent().getParent().setVisible(false);
      }
    });

    root.setOnDragDone(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        ((BorderPane) event.getSource()).getParent().getParent().getParent().setVisible(true);
      }
    });
  }
}
