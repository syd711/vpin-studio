package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.connectors.mania.model.ManiaVpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.ManiaController;
import de.mephisto.vpin.restclient.mania.TarcisioWheelsDB;
import de.mephisto.vpin.ui.tournaments.VpsTableContainer;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaWidgetVPSTableAlxController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaWidgetVPSTableAlxController.class);

  @FXML
  private BorderPane root;

  @FXML
  private TableView<ManiaVpsTable> tableView;

  @FXML
  private TableColumn<ManiaVpsTable, String> columnRank;

  @FXML
  private TableColumn<ManiaVpsTable, String> columnScores;

  @FXML
  private TableColumn<ManiaVpsTable, String> columnName;

  @FXML
  private TableColumn<ManiaVpsTable, String> columnVersion;

  @FXML
  private StackPane tableStack;

  @FXML
  private Button tableStatsBtn;

  private Parent loadingOverlay;

  private List<ManiaVpsTable> vpsTables;

  private ManiaController maniaController;

  // Add a public no-args constructor
  public ManiaWidgetVPSTableAlxController() {
  }

  @FXML
  private void onReload() {
    refresh();
  }

  @FXML
  private void onTableStats() {
    ManiaVpsTable selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      VpsTable tableById = Studio.client.getVpsService().getTableById(selectedItem.getVpsTableId());
      if (tableById != null) {
        maniaController.selectVpsTable(tableById);
      }
    }
  }

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
        onTableStats();
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("No score submitted yet."));
    tableStatsBtn.setDisable(true);

    columnRank.setCellValueFactory(cellData -> {
      ManiaVpsTable value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 18);
      Label label = new Label("#" + (vpsTables.indexOf(value) + 1));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnScores.setCellValueFactory(cellData -> {
      ManiaVpsTable value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 18);
      Label label = new Label(String.valueOf(value.getScored()));
      label.getStyleClass().add("default-text-color");
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnName.setCellValueFactory(cellData -> {
      ManiaVpsTable value = cellData.getValue();
      VpsTable tableById = Studio.client.getVpsService().getTableById(value.getVpsTableId());
      if (tableById != null) {
        VpsTableVersion tableVersionById = tableById.getTableVersionById(value.getVpsVersionId());
        if (tableVersionById != null) {
          HBox hBox = new HBox(3);
          hBox.setAlignment(Pos.CENTER_LEFT);

          InputStream imageInput = TarcisioWheelsDB.getWheelImage(Studio.class, client, tableById.getId());
          Image image = new Image(imageInput);
          ImageView imageView = new ImageView(image);
          imageView.setPreserveRatio(true);
          imageView.setFitWidth(100);
          imageView.setFitWidth(100);
          hBox.getChildren().add(imageView);

          VpsTableContainer c = new VpsTableContainer(tableById, "");
          hBox.getChildren().add(c);
          return new SimpleObjectProperty(hBox);
        }

      }
      return new SimpleObjectProperty("-not available-");
    });

    columnVersion.setCellValueFactory(cellData -> {
      ManiaVpsTable value = cellData.getValue();
      VpsTable tableById = Studio.client.getVpsService().getTableById(value.getVpsTableId());
      if (tableById != null) {
        VpsTableVersion tableVersionById = tableById.getTableVersionById(value.getVpsVersionId());
        if (tableVersionById != null) {
          VpsVersionContainer vpsVersionContainer = new VpsVersionContainer(tableById, tableVersionById, "", false);
          return new SimpleObjectProperty(vpsVersionContainer);
        }

      }
      return new SimpleObjectProperty("-not available-");
    });

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Ranking...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ManiaVpsTable>() {
      @Override
      public void changed(ObservableValue<? extends ManiaVpsTable> observable, ManiaVpsTable oldValue, ManiaVpsTable newValue) {
        tableStatsBtn.setDisable(newValue == null);
      }
    });
  }

  public void refresh() {
    new Thread(() -> {
      vpsTables = maniaClient.getVpsTableClient().getVpsTables();

      Platform.runLater(() -> {
        ObservableList<ManiaVpsTable> data = FXCollections.observableList(vpsTables);
        tableView.setItems(data);
        tableView.refresh();
      });
    }).start();
  }

  public void setManiaController(ManiaController maniaController) {
    this.maniaController = maniaController;
  }

}