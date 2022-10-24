package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.ValidationTexts;
import de.mephisto.vpin.ui.util.WidgetFactory;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Paint;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.*;

public class TablesController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(TablesController.class);

  @FXML
  private TableColumn<GameRepresentation, String> columnId;

  @FXML
  private TableColumn<GameRepresentation, String> columnDisplayName;

  @FXML
  private TableColumn<GameRepresentation, String> columnRom;

  @FXML
  private TableColumn<GameRepresentation, String> columnRomAlias;

  @FXML
  private TableColumn<GameRepresentation, String> columnNVOffset;

  @FXML
  private TableColumn<GameRepresentation, String> columnB2S;

  @FXML
  private TableColumn<GameRepresentation, String> columnStatus;

  @FXML
  private TableColumn<GameRepresentation, String> columnPUPPack;

  @FXML
  private TableColumn<GameRepresentation, String> columnHsFile;

  @FXML
  private TableView<GameRepresentation> tableView;

  @FXML
  private TextField textfieldSearch;

  @FXML
  private BorderPane screenTopper;

  @FXML
  private BorderPane screenBackglass;

  @FXML
  private BorderPane screenDMD;

  @FXML
  private BorderPane screenPlayfield;

  @FXML
  private BorderPane screenApron;

  @FXML
  private BorderPane screenOther2;

  @FXML
  private BorderPane screenWheel;

  @FXML
  private BorderPane screenInfo;

  @FXML
  private BorderPane screenHelp;

  @FXML
  private BorderPane screenLoading;

  @FXML
  private BorderPane screenAudio;

  @FXML
  private BorderPane screenAudioLaunch;

  @FXML
  private Label labelTableCount;

  @FXML
  private Accordion accordion;

  @FXML
  private TitledPane titledPaneMedia;

  @FXML
  private Label labelId;

  @FXML
  private Label labelRom;

  @FXML
  private Label labelRomAlias;

  @FXML
  private Label labelNVOffset;

  @FXML
  private Label labelFilename;

  @FXML
  private Label labelLastPlayed;

  @FXML
  private Label labelTimesPlayed;

  @FXML
  private Label labelHSFilename;

  @FXML
  private Slider volumeSlider;

  @FXML
  private Label validationErrorLabel;

  @FXML
  private Node validationError;

  @FXML
  private TextArea highscoreTextArea;

  @FXML
  private ImageView rawDirectB2SImage;

  @FXML
  private Button openDirectB2SImageButton;

  @FXML
  private Button editHsFileNameBtn;

  @FXML
  private Button editRomNameBtn;

  @FXML
  private Label resolutionLabel;

  // Add a public no-args constructor
  public TablesController() {
  }

  private VPinStudioClient client;
  private ObservableList<GameRepresentation> data;
  private List<GameRepresentation> games;

  @FXML
  private void onPlayClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    BorderPane borderPane = (BorderPane) source.getParent();
    MediaView mediaView = (MediaView) borderPane.getCenter();

    FontIcon icon = (FontIcon) source.getChildrenUnmodifiable().get(0);
    String iconLiteral = icon.getIconLiteral();
    if (iconLiteral.equals("bi-play")) {
      mediaView.getMediaPlayer().setMute(false);
      mediaView.getMediaPlayer().setCycleCount(1);
      mediaView.getMediaPlayer().play();
      icon.setIconLiteral("bi-stop");
    }
    else {
      mediaView.getMediaPlayer().stop();
      icon.setIconLiteral("bi-play");
    }
  }

  @FXML
  private void onOpenDirectB2SBackground() {
    GameRepresentation game = tableView.getSelectionModel().selectedItemProperty().get();
    if (game != null) {
      try {
        ByteArrayInputStream s = client.getDirectB2SImage(game);
        byte[] bytes = s.readAllBytes();
        File png = File.createTempFile("vpin-studio-directb2s-", ".png");
        png.deleteOnExit();
        IOUtils.write(bytes, new FileOutputStream(png));
        s.close();

        Desktop.getDesktop().open(png);
      } catch (IOException e) {
        LOG.error("Failed to create image temp file: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onMediaViewClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    BorderPane borderPane = (BorderPane) source.getParent();
    Node center = borderPane.getCenter();
    if (center == null) {
      center = screenPlayfield.getCenter();
    }


    if (center instanceof MediaView) {
      MediaView mediaView = (MediaView) center;
      Media media = mediaView.getMediaPlayer().getMedia();
      String s = media.getSource();
      try {
        Desktop.getDesktop().browse(URI.create(s));
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    else if (center instanceof ImageView) {
      ImageView imageView = (ImageView) center;
      String url = (String) imageView.getUserData();
      try {
        Desktop.getDesktop().browse(URI.create(url));
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  @FXML
  private void onSearchKeyPressed(KeyEvent e) {
    if (e.getCode().equals(KeyCode.ENTER)) {
      tableView.getSelectionModel().select(0);
      tableView.requestFocus();
    }
  }

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
//        TransitionUtil.createTranslateByXTransition(main, 300, 600).playFromStart();
      }
    }
  }

  @FXML
  private void onTableScan() {
    GameRepresentation game = tableView.getSelectionModel().selectedItemProperty().get();
    WidgetFactory.createProgressDialog(new TableScanProgressModel(client, "Scanning Table '" + game + "'", game));
    this.onReload();
  }

  @FXML
  private void onTablesScan() {
    WidgetFactory.createProgressDialog(new TablesScanProgressModel(client, "Scanning Tables"));
    this.onReload();
  }

  @FXML
  private void onRomEdit() {
    GameRepresentation gameRepresentation = tableView.getSelectionModel().selectedItemProperty().get();
    String romName = WidgetFactory.showInputDialog("Enter ROM Name", null, gameRepresentation.getRom());
    if (romName != null) {
      gameRepresentation.setRom(romName);
      client.saveGame(gameRepresentation);
      this.onReload();
    }
  }

  @FXML
  private void onValidate() {
    GameRepresentation game = tableView.getSelectionModel().selectedItemProperty().get();
    Optional<ButtonType> result = WidgetFactory.showConfirmation("Re-validate table '" + game.getGameDisplayName() + "?\nThis will reset the dismissed validations for this table too.", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      game.setIgnoredValidations(null);
      client.saveGame(game);
      onReload();
    }
  }

  @FXML
  private void onDismiss() {
    GameRepresentation game = tableView.getSelectionModel().selectedItemProperty().get();
    Optional<ButtonType> result = WidgetFactory.showConfirmation("Ignore this warning for future validations of table '" + game.getGameDisplayName() + "?", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      String validationState = String.valueOf(game.getValidationState());
      String ignoredValidations = game.getIgnoredValidations();
      if (ignoredValidations == null) {
        ignoredValidations = "";
      }
      List<String> gameIgnoreList = new ArrayList<>(Arrays.asList(ignoredValidations.split(",")));
      if (!gameIgnoreList.contains(validationState)) {
        gameIgnoreList.add(validationState);
      }

      game.setIgnoredValidations(StringUtils.join(gameIgnoreList, ","));
      client.saveGame(game);
      onReload();
    }
  }

  @FXML
  private void onHsFileNameEdit() {
    GameRepresentation gameRepresentation = tableView.getSelectionModel().selectedItemProperty().get();
    String fs = WidgetFactory.showInputDialog("EM Highscore Filename", "Enter the name of the highscore file for this table.\nThe file is located in the 'User' folder.", gameRepresentation.getHsFileName());
    if (fs != null) {
      gameRepresentation.setHsFileName(fs);
      client.saveGame(gameRepresentation);
      this.onReload();
    }
  }

  @FXML
  private void onReload() {
    GameRepresentation gameRepresentation = tableView.getSelectionModel().selectedItemProperty().get();
    List<GameRepresentation> games = client.getGames();
    List<GameRepresentation> filtered = new ArrayList<>();
    String filterValue = textfieldSearch.textProperty().getValue();
    for (GameRepresentation game : games) {
      if (game.getGameDisplayName().toLowerCase().contains(filterValue.toLowerCase())) {
        filtered.add(game);
      }
    }
    data = FXCollections.observableList(filtered);
    tableView.setItems(data);
    tableView.refresh();
    tableView.getSelectionModel().select(gameRepresentation);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = new VPinStudioClient();
    this.accordion.setExpandedPane(titledPaneMedia);

    bindTable();
    bindSearchField();
  }

  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      List<GameRepresentation> filtered = new ArrayList<>();
      for (GameRepresentation game : games) {
        if (game.getGameDisplayName().toLowerCase().contains(filterValue.toLowerCase())) {
          filtered.add(game);
        }
      }
      data = FXCollections.observableArrayList(filtered);
      tableView.setItems(data);
    });
  }

  private void bindTable() {
    games = client.getGames();
    data = FXCollections.observableArrayList(games);
    labelTableCount.setText(data.size() + " tables");

    columnDisplayName.setCellValueFactory(
        new PropertyValueFactory<GameRepresentation, String>("gameDisplayName")
    );

    columnId.setCellValueFactory(
        new PropertyValueFactory<GameRepresentation, String>("id")
    );


    columnRom.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      String rom = value.getRom();
      if (!StringUtils.isEmpty(value.getOriginalRom())) {
        rom = value.getOriginalRom();
      }

      if (value.isRomExists()) {
        return new SimpleStringProperty(rom);
      }

      Label label = new Label(rom);
      String color = "#FF3333";
      label.setStyle("-fx-font-color: " + color + ";-fx-text-fill: " + color + ";-fx-font-weight: bold;");
      return new SimpleObjectProperty(label);
    });

    columnRomAlias.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (!StringUtils.isEmpty(value.getOriginalRom())) {
        return new SimpleStringProperty(value.getRom());
      }
      return new SimpleStringProperty("-");
    });

    columnNVOffset.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.getNvOffset() > 0) {
        return new SimpleStringProperty(String.valueOf(value.getNvOffset()));
      }
      return new SimpleStringProperty("");
    });

    columnB2S.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isDirectB2SAvailable()) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(18);
        fontIcon.setIconColor(Paint.valueOf("#66FF66"));
        fontIcon.setIconLiteral("bi-check-circle");
        return new SimpleObjectProperty(fontIcon);
      }
      return new SimpleStringProperty("");
    });

    columnPUPPack.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isPupPackAvailable()) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(18);
        fontIcon.setIconColor(Paint.valueOf("#66FF66"));
        fontIcon.setIconLiteral("bi-check-circle");
        return new SimpleObjectProperty(fontIcon);
      }
      return new SimpleStringProperty("");
    });

    columnStatus.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      int validationState = value.getValidationState();
      if (validationState > 0) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(18);
        fontIcon.setCursor(Cursor.HAND);
        fontIcon.setIconColor(Paint.valueOf("#FF3333"));
        fontIcon.setIconLiteral("bi-exclamation-circle");
        return new SimpleObjectProperty(fontIcon);
      }

      FontIcon fontIcon = new FontIcon();
      fontIcon.setIconSize(18);
      fontIcon.setIconColor(Paint.valueOf("#66FF66"));
      fontIcon.setIconLiteral("bi-check-circle");
      return new SimpleObjectProperty(fontIcon);
    });

    columnHsFile.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      String hsFileName = value.getHsFileName();
      return new SimpleStringProperty(hsFileName);
    });


    tableView.setItems(data);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      refreshView(newSelection);
    });

    if (!data.isEmpty()) {
      tableView.getSelectionModel().select(0);
    }

    volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
        GameRepresentation game = tableView.getSelectionModel().selectedItemProperty().get();
        BindingUtil.debouncer.debounce("tableVolume" + game.getId(), () -> {
          int value = t1.intValue();
          if (value == 0) {
            value = 1;
          }
          game.setVolume(value);
          client.saveGame(game);
        }, 1000);
      }
    });

    titledPaneMedia.expandedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean expanded) {
        GameRepresentation game = tableView.getSelectionModel().selectedItemProperty().get();
        if (expanded) {
          refreshView(game);
        }
        else {
          resetMedia();
        }
      }
    });
  }

  private void resetMedia() {
    disposeMediaPane(screenAudioLaunch);
    disposeMediaPane(screenAudio);
    disposeMediaPane(screenLoading);
    disposeMediaPane(screenHelp);
    disposeMediaPane(screenInfo);
    disposeMediaPane(screenDMD);
    disposeMediaPane(screenBackglass);
    disposeMediaPane(screenTopper);
    disposeMediaPane(screenApron);
    disposeMediaPane(screenPlayfield);
    disposeMediaPane(screenOther2);
    disposeMediaPane(screenWheel);
  }

  private void refreshView(@Nullable GameRepresentation game) {
    editHsFileNameBtn.setDisable(game == null);
    editRomNameBtn.setDisable(game == null);

    if (game != null) {
      volumeSlider.setDisable(false);
      volumeSlider.setValue(game.getVolume());

      labelId.setText(String.valueOf(game.getId()));
      labelRom.setText(game.getOriginalRom() != null ? game.getOriginalRom() : game.getRom());
      labelRomAlias.setText(game.getOriginalRom() != null ? game.getRom() : "-");
      labelNVOffset.setText(game.getNvOffset() > 0 ? String.valueOf(game.getNvOffset()) : "-");
      labelFilename.setText(game.getGameFileName());
      labelLastPlayed.setText(game.getLastPlayed() != null ? game.getLastPlayed().toString() : "-");
      labelTimesPlayed.setText(String.valueOf(game.getNumberPlays()));
      labelHSFilename.setText(game.getHsFileName());

      refreshDirectB2SPreview(game);

      validationError.setVisible(game.getValidationState() > 0);
      if (game.getValidationState() > 0) {
        validationErrorLabel.setText(ValidationTexts.getValidationMessage(game));
      }

      if (titledPaneMedia.isExpanded()) {
        refreshMedia(game);
      }

      String rawHighscore = game.getRawHighscore();
      if (rawHighscore != null) {
        highscoreTextArea.setText(rawHighscore);
      }
      else {
        highscoreTextArea.setText("");
      }

      NavigationController.setBreadCrumb(Arrays.asList("Tables", game.getGameDisplayName()));
    }
    else {
      resetMedia();
      volumeSlider.setValue(100);
      volumeSlider.setDisable(true);

      labelId.setText("-");
      labelRom.setText("-");
      labelRomAlias.setText("-");
      labelNVOffset.setText("-");
      labelFilename.setText("-");
      labelLastPlayed.setText("-");
      labelTimesPlayed.setText("-");
      labelHSFilename.setText("-");

      refreshDirectB2SPreview(null);

      highscoreTextArea.setText("");

      validationError.setVisible(false);
      NavigationController.setBreadCrumb(Arrays.asList("Tables"));
    }
  }

  private void refreshDirectB2SPreview(@Nullable GameRepresentation game) {
    try {
      openDirectB2SImageButton.setVisible(false);
      openDirectB2SImageButton.setTooltip(new Tooltip("Open directb2s image"));
      rawDirectB2SImage.setVisible(false);

      if(game != null) {
        InputStream input = client.getDirectB2SImage(game);
        javafx.scene.image.Image image = new Image(input);
        rawDirectB2SImage.setVisible(true);
        rawDirectB2SImage.setImage(image);
        input.close();

        if (image.getWidth() > 300) {
          openDirectB2SImageButton.setVisible(true);
          resolutionLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
        }
        else {
          resolutionLabel.setText("");
        }
      }
      else {
        resolutionLabel.setText("");
      }
    } catch (IOException e) {
      LOG.error("Failed to load raw b2s: " + e.getMessage(), e);
    }
  }

  private void refreshMedia(GameRepresentation game) {
    GameMediaRepresentation gameMedia = client.getGameMedia(game.getId());
    GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.Topper);
    WidgetFactory.createMediaContainer(screenTopper, client, item);

    item = gameMedia.getItem(PopperScreen.BackGlass);
    WidgetFactory.createMediaContainer(screenBackglass, client, item);

    item = gameMedia.getItem(PopperScreen.Audio);
    WidgetFactory.createMediaContainer(screenAudio, client, item);

    item = gameMedia.getItem(PopperScreen.AudioLaunch);
    WidgetFactory.createMediaContainer(screenAudioLaunch, client, item);

    item = gameMedia.getItem(PopperScreen.DMD);
    WidgetFactory.createMediaContainer(screenDMD, client, item);

    item = gameMedia.getItem(PopperScreen.GameInfo);
    WidgetFactory.createMediaContainer(screenInfo, client, item);

    item = gameMedia.getItem(PopperScreen.GameHelp);
    WidgetFactory.createMediaContainer(screenHelp, client, item);

    item = gameMedia.getItem(PopperScreen.PlayField);
    WidgetFactory.createMediaContainer(screenPlayfield, client, item);

    item = gameMedia.getItem(PopperScreen.Menu);
    WidgetFactory.createMediaContainer(screenApron, client, item);

    item = gameMedia.getItem(PopperScreen.Loading);
    WidgetFactory.createMediaContainer(screenLoading, client, item);

    item = gameMedia.getItem(PopperScreen.Other2);
    WidgetFactory.createMediaContainer(screenOther2, client, item);

    item = gameMedia.getItem(PopperScreen.Wheel);
    WidgetFactory.createMediaContainer(screenWheel, client, item);
  }

  private void disposeMediaPane(BorderPane parent) {
    if (parent.getCenter() != null) {
      WidgetFactory.disposeMediaBorderPane(parent);
    }
  }

  @Override
  public void dispose() {
    resetMedia();
  }
}