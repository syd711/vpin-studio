package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetsService;
import de.mephisto.vpin.popper.PopperAssetAdapter;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class TablePopperMediaSelectionController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TablePopperMediaSelectionController.class);

  @FXML
  private BorderPane mediaPane;

  @FXML
  private Button searchBtn;

  @FXML
  private Button previewBtn;

  @FXML
  private Button downloadBtn;

  @FXML
  private TextField searchField;

  @FXML
  private Label assetLabel;

  @FXML
  private ListView<TableAsset> assetList;

  private GameRepresentation game;
  private PopperScreen screen;
  private TablesSidebarController tablesSidebarController;
  private TableAssetsService tableAssetsService;

  @FXML
  private void onSearch() {
    String term = searchField.getText().trim();
    if (!StringUtils.isEmpty(term)) {
      List<TableAsset> items = Studio.client.getPinUPPopperService().searchTableAsset(screen, term);
      ObservableList<TableAsset> assets = FXCollections.observableList(items);
      assetList.getItems().removeAll(assetList.getItems());
      assetList.setItems(assets);
      assetList.refresh();
    }
    else {
      assetList.getItems().removeAll(assetList.getItems());
      assetList.setItems(FXCollections.emptyObservableList());
      assetList.refresh();
    }
  }

  @FXML
  private void onPreview() {
    TableAsset tableAsset = assetList.getSelectionModel().getSelectedItem();
    if (tableAsset == null) {
      return;
    }
    downloadBtn.setVisible(true);
    assetLabel.setText(tableAsset.toString());

    String mimeType = tableAsset.getMimeType();
    String baseType = mimeType.split("/")[0];

    if (baseType.equals("image")) {
      ImageView imageView = new ImageView();
      imageView.setFitWidth(400 - 10);
      imageView.setFitHeight(500 - 20);
      imageView.setPreserveRatio(true);

      Image image = new Image(tableAsset.getUrl());
      imageView.setImage(image);
      imageView.setUserData(tableAsset);

      mediaPane.setCenter(imageView);
    }
    else if (baseType.equals("audio")) {
      VBox vBox = new VBox();
      vBox.setAlignment(Pos.BASELINE_CENTER);

      FontIcon fontIcon = new FontIcon();
      fontIcon.setIconSize(48);
      fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
      fontIcon.setIconLiteral("bi-stop");

      Button playBtn = new Button();
      playBtn.setGraphic(fontIcon);
      vBox.getChildren().add(playBtn);

      Media media = new Media(tableAsset.getUrl());
      MediaPlayer mediaPlayer = new MediaPlayer(media);
      mediaPlayer.setAutoPlay(true);
      mediaPlayer.setCycleCount(-1);
      mediaPlayer.setMute(false);
      mediaPlayer.setOnError(() -> {
        LOG.error("Media player error: " + mediaPlayer.getError());
        mediaPlayer.stop();
        mediaPlayer.dispose();

        Label label = new Label("Media Error");
        label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
        label.setUserData(tableAsset);
        vBox.getChildren().add(label);
        mediaPane.setCenter(label);
      });


      MediaView mediaView = new MediaView(mediaPlayer);
      vBox.getChildren().add(mediaView);
      mediaPane.setCenter(vBox);

      mediaPlayer.setOnEndOfMedia(() -> {
        fontIcon.setIconLiteral("bi-play");
      });

      playBtn.setOnAction(event -> {
        String iconLiteral = fontIcon.getIconLiteral();
        if (iconLiteral.equals("bi-play")) {
          mediaView.getMediaPlayer().setMute(false);
          mediaView.getMediaPlayer().setCycleCount(1);
          mediaView.getMediaPlayer().play();
          fontIcon.setIconLiteral("bi-stop");
        }
        else {
          mediaView.getMediaPlayer().stop();
          fontIcon.setIconLiteral("bi-play");
        }
      });

    }
    else if (baseType.equals("video")) {
      Media media = new Media(tableAsset.getUrl());
      MediaPlayer mediaPlayer = new MediaPlayer(media);
      mediaPlayer.setAutoPlay(true);
      mediaPlayer.setStopTime(Duration.seconds(5));
      mediaPlayer.setCycleCount(-1);
      mediaPlayer.setMute(true);
      mediaPlayer.setOnError(() -> {
        LOG.error("Media player error: " + mediaPlayer.getError() + ", URL: " + tableAsset.getUrl());
        mediaPlayer.stop();
        mediaPlayer.dispose();

        Label label = new Label("  Media available\n(but not playable)");
        label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00; -fx-font-weight: bold;");
        label.setUserData(tableAsset);
        mediaPane.setCenter(label);
      });

      MediaView mediaView = new MediaView(mediaPlayer);
      mediaView.setUserData(tableAsset);
      mediaView.setPreserveRatio(true);
      mediaView.setFitWidth(400 - 10);
      mediaView.setFitHeight(400 - 10);

      mediaPane.setCenter(mediaView);
    }
  }

  @FXML
  private void onDownload(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    TableAsset tableAsset = this.assetList.getSelectionModel().getSelectedItem();
    if (tableAsset != null) {
      Dialogs.createProgressDialog(stage, new TableAssetDownloadProgressModel(screen, game, tableAsset));
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
  }

  @FXML
  private void onCancel(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    previewBtn.setDisable(true);
    downloadBtn.setVisible(false);

    Label label = new Label("No asset preview activated.");
    label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
    mediaPane.setCenter(label);

    tableAssetsService = new TableAssetsService();
    tableAssetsService.registerAdapter(new PopperAssetAdapter());

    this.assetList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TableAsset>() {
      @Override
      public void changed(ObservableValue<? extends TableAsset> observable, TableAsset oldValue, TableAsset tableAsset) {
        disposeAll();
        assetLabel.setText("");
        previewBtn.setDisable(tableAsset == null);
        downloadBtn.setVisible(false);

        Label label = new Label("No asset preview activated.");
        label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
        mediaPane.setCenter(label);
      }
    });

    this.assetList.setOnMouseClicked(click -> {
      if (click.getClickCount() == 2) {
        onPreview();
      }
    });
  }

  private void disposeAll() {
    Node center = mediaPane.getCenter();
    if (center instanceof MediaView) {
      MediaView mediaView = (MediaView) center;
      mediaView.getMediaPlayer().stop();
      mediaView.getMediaPlayer().dispose();
    }
    else if (center instanceof VBox) {
      MediaView mediaView = (MediaView) ((VBox) center).getChildren().get(1);
      mediaView.getMediaPlayer().stop();
      mediaView.getMediaPlayer().dispose();
    }

    mediaPane.setCenter(null);
  }

  @Override
  public void onDialogCancel() {

  }


  public void setGame(GameRepresentation game, PopperScreen screen) {
    this.game = game;
    this.screen = screen;

    String term = game.getGameDisplayName();
    term = term.replaceAll("the", "");
    term = term.replaceAll("The", "");
    term = term.replaceAll(", ", "");
    term = term.replaceAll("-", "");
    term = term.replaceAll("'", "");
    term = term.replaceAll("\\(", "");
    term = term.replaceAll("\\)", "");
    term = term.replaceAll("\\[", "");
    term = term.replaceAll("\\]", "");
    term = term.replaceAll("MOD", "");
    term = term.replaceAll("VOW", "");
    term = term.replaceAll("VR ", "");
    term = term.replaceAll("Room ", "");

    String[] terms = term.split(" ");

    List<String> sanitizedTerms = new ArrayList<>();
    for (String s : terms) {
      if (!StringUtils.isEmpty(s)) {
        String value = s.trim();
        try {
          if (value.length() == 4) {
            Integer.parseInt(value);
            continue;
          }
        } catch (NumberFormatException e) {
        }

        sanitizedTerms.add(s.trim());
      }

      if (sanitizedTerms.size() == 2) {
        break;
      }
    }

    if (sanitizedTerms.isEmpty()) {
      this.searchField.setText(game.getGameDisplayName());
    }
    else {
      this.searchField.setText(String.join(" ", sanitizedTerms));
    }

    onSearch();
  }

  public void setTableSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}
