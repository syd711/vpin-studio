package de.mephisto.vpin.ui.dropins;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.Studio;
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
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;

public class DropInContainerController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(DropInContainerController.class);

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
  private ImageView imageView;

  @FXML
  private HBox imageWrapper;

  @FXML
  private Separator installSeparator;

  private MenuButton dropInButton;
  private File file;

  @FXML
  private void onOpen() {
    SystemUtil.openFile(file);
  }

  @FXML
  private void onDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete File", "Delete \"" + file.getAbsolutePath() + "\"?");
    if (result.get().equals(ButtonType.OK)) {
      if (!file.delete()) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Deletion failed, another process is blocking this file.");
      }
    }
  }

  @FXML
  private void onInstall() {
    DropInManager.getInstance().install(file);
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
      try {
        imageView.setImage(new Image(new FileInputStream(file)));
      }
      catch (FileNotFoundException e) {
        LOG.error("Failed to set image: " + e, e);
      }
    }
    else {
      dataPanel.setPrefWidth(376);
      imageWrapper.setVisible(false);
    }

    boolean hidden = !AssetType.isInstallable(suffix);
    this.installBtn.setVisible(!hidden);
    this.installSeparator.setVisible(!hidden);

    if (hidden) { //TODO wip
      dataPanel.setPrefWidth(356);
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
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
