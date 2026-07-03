package de.mephisto.vpin.ui.components.doftester;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.TrashBin;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.doftester.DOFTesterSettings;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationItem;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.dropins.DropInManager;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.util.SystemUtil;
import javafx.event.ActionEvent;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class ToyContainerController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ToyContainerController.class);

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

  private Optional<GameRepresentation> game;
  private String toy;

  @FXML
  private void onOpen() {

  }

  @FXML
  private void onDelete() {

  }

  @FXML
  private void onTest(ActionEvent e) {
    Button button = (Button) e.getSource();

    DOFTesterSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.DOF_TESTER_SETTINGS, DOFTesterSettings.class);
    button.setDisable(true);

    JFXFuture.supplyAsync(() -> {
      return client.getDofTesterService().testToy(game.get().getId(), toy, settings.getTestDuration());
    }).thenAcceptLater((b) -> {
      button.setDisable(false);
    });
  }

  public void setData(@NonNull Optional<GameRepresentation> game, @NonNull String toy) {
    this.game = game;
    this.toy = toy;

    filenameLabel.setText(toy);
    filenameLabel.setStyle("-fx-font-size: 15px;-fx-font-weight: bold;");
    sizeLabel.setText("---");
    sizeLabel.setStyle("-fx-font-size: 13px");
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    dragHandler.setStyle("-fx-cursor: hand;");
    dataPanel.setStyle("-fx-cursor: hand;");
//
//    root.setFocusTraversable(true);
//    root.setOnMousePressed(e -> root.requestFocus());
//    root.setOnKeyPressed(e -> {
//      if (e.getCode() == KeyCode.DELETE) {
//        onDelete();
//        e.consume();
//      }
//    });
//
//    root.setOnDragDetected(new EventHandler<MouseEvent>() {
//      public void handle(MouseEvent event) {
//        Dragboard db = root.startDragAndDrop(TransferMode.ANY);
//        db.setDragView(root.snapshot(null, null));
//
//        Map<DataFormat, Object> data = new HashMap<>();
////        data.put(DataFormat.FILES, Arrays.asList(file));
//        db.setContent(data);
//        event.consume();
//      }
//    });
//
//    root.setOnDragDone(new EventHandler<DragEvent>() {
//      @Override
//      public void handle(DragEvent event) {
////        if (dropInButton != null) {
////          dropInButton.hide();
////        }
//      }
//    });
  }
}
