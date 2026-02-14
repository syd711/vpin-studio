package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.players.RankedPlayerRepresentation;
import de.mephisto.vpin.restclient.preferences.OverlaySettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.fx.ServerFX.client;

public class WidgetPlayerRankController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private BorderPane root;

  @FXML
  private TableView<RankedPlayerRepresentation> tableView;

  @FXML
  private TableColumn<RankedPlayerRepresentation, Label> columnRank;

  @FXML
  private TableColumn<RankedPlayerRepresentation, Label> columnPoints;

  @FXML
  private TableColumn<RankedPlayerRepresentation, HBox> columnName;

  @FXML
  private TableColumn<RankedPlayerRepresentation, Label> columnFirst;

  @FXML
  private TableColumn<RankedPlayerRepresentation, Label> columnSecond;

  @FXML
  private TableColumn<RankedPlayerRepresentation, Label> columnThird;

  @FXML
  private TableColumn<RankedPlayerRepresentation, Label> columnComps;

  @FXML
  private StackPane tableStack;

  // Add a public no-args constructor
  public WidgetPlayerRankController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("                     No players listed here?\nCreate players to match their initials with highscores."));

    columnRank.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 18);
      Label label = new Label("#" + value.getRank());
      label.setFont(defaultFont);
      return new SimpleObjectProperty<>(label);
    });

    columnPoints.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 18);
      Label label = new Label(String.valueOf(value.getPoints()));
      label.setFont(defaultFont);
      return new SimpleObjectProperty<>(label);
    });

    columnName.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
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
      Label label = new Label(value.getName());
      label.setFont(defaultFont);
      hBox.getChildren().add(label);

      new Thread(() -> {
        InputStream in = null;
        if (!StringUtils.isEmpty(value.getAvatarUrl())) {
          in = client.getCachedUrlImage(value.getAvatarUrl());
        }
        else if (value.getAvatarUuid() != null) {
          in = new ByteArrayInputStream(client.getAssetService().getAsset(AssetType.AVATAR, value.getAvatarUuid()).readAllBytes());
        }

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
      return new SimpleObjectProperty<>(hBox);
    });

    OverlaySettings overlaySettings = ServerFX.client.getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);
    MonitorInfo screenBounds = ServerFX.client.getSystemService().getScreenInfo(overlaySettings.getOverlayScreenId());
    if (screenBounds.getWidth() < 2600) {
      columnName.setPrefWidth(280);
    }
    if (screenBounds.getWidth() < 2000) {
      columnName.setPrefWidth(260);
    }

    columnFirst.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getFirst()));
      label.setFont(defaultFont);
      return new SimpleObjectProperty<>(label);
    });

    columnSecond.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getSecond()));
      label.setFont(defaultFont);
      return new SimpleObjectProperty<>(label);
    });

    columnThird.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getThird()));
      label.setFont(defaultFont);
      return new SimpleObjectProperty<>(label);
    });

    columnComps.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getCompetitionsWon()));
      label.setFont(defaultFont);
      return new SimpleObjectProperty<>(label);
    });

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      /*Parent loadingOverlay =*/ loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Ranking...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }

  public void refresh() {
    new Thread(() -> {
      List<RankedPlayerRepresentation> rankedPlayers = client.getPlayerService().getRankedPlayers();

      Platform.runLater(() -> {
        ObservableList<RankedPlayerRepresentation> data = FXCollections.observableList(rankedPlayers);
        tableView.setItems(data);
        tableView.refresh();
      });
    }).start();
  }
}