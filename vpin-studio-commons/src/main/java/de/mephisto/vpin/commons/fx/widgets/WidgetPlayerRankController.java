package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.restclient.representations.RankedPlayerRepresentation;
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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class WidgetPlayerRankController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(WidgetPlayerRankController.class);

  @FXML
  private BorderPane root;

  @FXML
  private TableView<RankedPlayerRepresentation> tableView;

  @FXML
  private TableColumn<RankedPlayerRepresentation, String> columnRank;

  @FXML
  private TableColumn<RankedPlayerRepresentation, String> columnPoints;

  @FXML
  private TableColumn<RankedPlayerRepresentation, String> columnName;

  @FXML
  private TableColumn<RankedPlayerRepresentation, String> columnFirst;

  @FXML
  private TableColumn<RankedPlayerRepresentation, String> columnSecond;

  @FXML
  private TableColumn<RankedPlayerRepresentation, String> columnThird;

  @FXML
  private TableColumn<RankedPlayerRepresentation, String> columnComps;

  @FXML
  private StackPane tableStack;

  private Parent loadingOverlay;

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
      return new SimpleObjectProperty(label);
    });

    columnPoints.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 18);
      Label label = new Label(String.valueOf(value.getPoints()));
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnName.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
      HBox hBox = new HBox();

      Image image = null;
      if (!StringUtils.isEmpty(value.getAvatarUrl())) {
        image = new Image(value.getAvatarUrl());
      }
      else if (value.getAvatarUuid() != null) {
        image = new Image(OverlayWindowFX.client.getAsset(AssetType.AVATAR, value.getAvatarUuid()));
      }

      ImageView view = new ImageView(image);
      view.setPreserveRatio(true);
      view.setFitWidth(50);
      view.setFitHeight(50);
      CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));

      hBox.setAlignment(Pos.CENTER_LEFT);
      hBox.getChildren().add(view);
      hBox.setSpacing(6);

      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(value.getName());
      label.setFont(defaultFont);
      hBox.getChildren().add(label);

      return new SimpleObjectProperty(hBox);
    });

    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    if(screenBounds.getWidth() < 2600) {
      columnName.setPrefWidth(280);
    }
    if(screenBounds.getWidth() < 2000) {
      columnName.setPrefWidth(260);
    }

    columnFirst.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getFirst()));
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnSecond.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getSecond()));
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnThird.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getThird()));
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    columnComps.setCellValueFactory(cellData -> {
      RankedPlayerRepresentation value = cellData.getValue();
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 18);
      Label label = new Label(String.valueOf(value.getCompetitionsWon()));
      label.setFont(defaultFont);
      return new SimpleObjectProperty(label);
    });

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Ranking...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }

  public void refresh() {
    tableStack.getChildren().add(loadingOverlay);
    new Thread(() -> {
      List<RankedPlayerRepresentation> rankedPlayers = OverlayWindowFX.client.getRankedPlayers();

      Platform.runLater(() -> {
        ObservableList<RankedPlayerRepresentation> data = FXCollections.observableList(rankedPlayers);
        tableView.setItems(data);
        tableView.refresh();

        tableStack.getChildren().remove(loadingOverlay);
      });
    }).start();
  }
}