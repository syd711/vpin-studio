package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.connectors.mania.model.TableScoreDetails;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.players.RankedPlayerRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.fx.ServerFX.client;
import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFont;
import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFontSmall;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaWidgetVPSTableRankController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaWidgetVPSTableRankController.class);

  @FXML
  private BorderPane root;

  @FXML
  private TableView<TableScoreDetails> tableView;

  @FXML
  private TableColumn<TableScoreDetails, String> columnRank;

  @FXML
  private TableColumn<TableScoreDetails, String> columnScore;

  @FXML
  private TableColumn<TableScoreDetails, String> columnName;

  @FXML
  private TableColumn<TableScoreDetails, String> columnVersion;

  @FXML
  private TableColumn<TableScoreDetails, String> columnDate;

  @FXML
  private StackPane tableStack;

  @FXML
  private Label titleLabel;

  private Parent loadingOverlay;
  private List<TableScoreDetails> tableScores;

  // Add a public no-args constructor
  public ManiaWidgetVPSTableRankController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("         No scores listed here?\nBe the first and create a highscore!"));

    columnRank.setCellValueFactory(cellData -> {
      TableScoreDetails value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 18);
      Label label = new Label("#" + (tableScores.indexOf(value) + 1));
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnScore.setCellValueFactory(cellData -> {
      TableScoreDetails value = cellData.getValue();
      Label label = new Label(value.getScoreText());
      label.setFont(getScoreFontSmall());
      return new SimpleObjectProperty(label);
    });


    columnVersion.setCellValueFactory(cellData -> {
      TableScoreDetails value = cellData.getValue();
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

    columnName.setCellValueFactory(cellData -> {
      TableScoreDetails value = cellData.getValue();
      HBox hBox = new HBox();

      Image image = new Image(ServerFX.class.getResourceAsStream("avatar-blank.png"));
      ImageView view = new ImageView(image);

      view.setPreserveRatio(true);
      view.setFitWidth(50);
      view.setFitHeight(50);

      hBox.setAlignment(Pos.CENTER_LEFT);
      hBox.getChildren().add(view);
      hBox.setSpacing(6);

      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(value.getDisplayName());
      label.setFont(defaultFont);
      hBox.getChildren().add(label);

      new Thread(() -> {
        InputStream in = client.getCachedUrlImage(maniaClient.getAccountClient().getAvatarUrl(value.getAccountUUID()));
        if (in == null) {
          in = ServerFX.class.getResourceAsStream("avatar-blank.png");
        }
        final InputStream data = in;
        if (data != null) {
          Platform.runLater(() -> {
            Image i = new Image(data);
            view.setImage(i);
            CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
          });
        }
      }).start();
      return new SimpleObjectProperty(hBox);
    });

    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    if (screenBounds.getWidth() < 2600) {
      columnName.setPrefWidth(280);
    }
    if (screenBounds.getWidth() < 2000) {
      columnName.setPrefWidth(260);
    }

    columnDate.setCellValueFactory(cellData -> {
      TableScoreDetails value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(DateUtil.formatDateTime(value.getCreationDate()));
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Table Ranking...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }


  public void setData(VpsTable vpsTable) {
    if (vpsTable == null) {
      Platform.runLater(() -> {
        titleLabel.setText("Ranking");
        ObservableList<TableScoreDetails> data = FXCollections.emptyObservableList();
        tableView.setItems(data);
        tableView.refresh();
      });
      return;
    }

    titleLabel.setText("Ranking for \"" + vpsTable.getDisplayName() + "\"");
    new Thread(() -> {
      tableScores = maniaClient.getHighscoreClient().getHighscoresByTable(vpsTable.getId());

      Platform.runLater(() -> {
        ObservableList<TableScoreDetails> data = FXCollections.observableList(tableScores);
        tableView.setItems(data);
        tableView.refresh();
      });
    }).start();
  }
}