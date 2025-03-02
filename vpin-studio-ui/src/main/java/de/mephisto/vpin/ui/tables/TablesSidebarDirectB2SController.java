package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.tables.models.B2SFormPosition;
import de.mephisto.vpin.ui.tables.models.B2SGlowing;
import de.mephisto.vpin.ui.tables.models.B2SLedType;
import de.mephisto.vpin.ui.tables.models.B2SVisibility;
import de.mephisto.vpin.ui.util.MediaUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarDirectB2SController implements Initializable, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarDirectB2SController.class);

  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 100;

  //WE ARE CONFIGURING HIDE FIELDS HERE; SO VALUES ARE SET HERE INVERTED
  public final static List<B2SVisibility> VISIBILITIES = Arrays.asList(new B2SVisibility(0, "Visible"),
      new B2SVisibility(1, "Hidden"),
      new B2SVisibility(2, "Standard"));

  public final static List<B2SFormPosition> FORM_POSITIONS = Arrays.asList(
    new B2SFormPosition(DirectB2ServerSettings.FORM_TO_BACK, "Form To Back"),
    new B2SFormPosition(DirectB2ServerSettings.FORM_TO_FRONT, "Form To Front"),
    new B2SFormPosition(DirectB2ServerSettings.FORM_TO_STANDARD, "Standard")
  );

  public final static List<B2SLedType> LED_TYPES = Arrays.asList(new B2SLedType(1, "Simple LEDs"),
      new B2SLedType(2, "Dream7 LEDs"));

  public final static List<B2SGlowing> GLOWINGS = Arrays.asList(new B2SGlowing(0, "Off"),
      new B2SGlowing(1, "Low"),
      new B2SGlowing(2, "Medium"),
      new B2SGlowing(3, "High"),
      new B2SGlowing(-1, "Default"));

  @FXML
  private Label nameLabel;

  @FXML
  private Label typeLabel;

  @FXML
  private Label authorLabel;

  @FXML
  private Label artworkLabel;

  @FXML
  private Label grillLabel;

  @FXML
  private Label b2sElementsLabel;

  @FXML
  private Label playersLabel;

  @FXML
  private Label bulbsLabel;

  @FXML
  private Label filesizeLabel;

  @FXML
  private Label resolutionLabel;

  @FXML
  private Label dmdResolutionLabel;

  @FXML
  private Label modificationDateLabel;

  @FXML
  private ImageView thumbnailImage;

  @FXML
  private ImageView dmdThumbnailImage;

  @FXML
  private Button openDefaultPictureBtn;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button dmdPositionBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private VBox dataBox;

  @FXML
  private Pane b2sSettings;

  @FXML
  private VBox emptyDataBox;

  @FXML
  private Pane directb2sRoot;

  @FXML
  private ScrollPane dataBoxScrollPane;

  //-- Editors

  @FXML
  private ComboBox<B2SVisibility> hideGrill;

  @FXML
  private CheckBox hideB2SBackglass;

  @FXML
  private CheckBox hideB2SDMD;

  @FXML
  private CheckBox startAsExe;

  @FXML
  private CheckBox startAsExeServer;

  @FXML
  private ComboBox<B2SVisibility> hideDMD;

  @FXML
  private Spinner<Integer> skipLampFrames;

  @FXML
  private Spinner<Integer> skipGIFrames;

  @FXML
  private Spinner<Integer> skipSolenoidFrames;

  @FXML
  private Spinner<Integer> skipLEDFrames;

  @FXML
  private CheckBox lightBulbOn;

  @FXML
  private ComboBox<B2SGlowing> glowing;

  @FXML
  private ComboBox<B2SLedType> usedLEDType;

  @FXML
  private ComboBox<B2SVisibility> startBackground;

  @FXML
  private ComboBox<B2SFormPosition> formToPosition;

  @FXML
  private Button backglassManagerBtn;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesController tablesController;

  private DirectB2SData tableData;
  private DirectB2STableSettings tableSettings;
  private boolean saveEnabled;


  // Add a public no-args constructor
  public TablesSidebarDirectB2SController() {
  }

  @FXML
  private void onDMDPosition() {
    if (game.isPresent()) {
      TableDialogs.openDMDPositionDialog(game.get());
    }
  }

  @FXML
  private void onBackglassManager() {
    tablesController.switchToBackglassManagerTab(game.orElse(null));
  }

  @FXML
  private void onReload() {
    this.reloadBtn.setDisable(true);

    if (game.isPresent()) {
      client.getBackglassServiceClient().clearCache();
      EventManager.getInstance().notifyTableChange(game.get().getId(), null);
    }
  }

  @FXML
  private void onDelete(ActionEvent e) {
    try {
      if (this.game.isPresent()) {
        GameRepresentation gameRepresentation = this.game.get();
        if (tableData != null) {
          Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Backglass", "Delete backglass file \"" + tableData.getFilename() + "\"?", null, "Delete");
          if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            client.getBackglassServiceClient().deleteBackglass(tableData.getEmulatorId(), tableData.getFilename());
            EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
          }
        }
      }
    }
    catch (Exception ex) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to delete backglass file: " + ex.getMessage());
    }
  }

  @FXML
  private void onUpload(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    if (game.isPresent()) {
      TableDialogs.directUpload(stage, AssetType.DIRECTB2S, game.get(), null);
    }
  }

  @FXML
  private void onOpenDirectB2SBackground() {
    if (tableData != null) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sBackground(tableData)) {
        MediaUtil.openMedia(in);
      }
      catch (IOException ioe) {
        LOG.error("Cannot open media for game " + game.get().getId(), ioe);
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBoxScrollPane.managedProperty().bindBidirectional(dataBoxScrollPane.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());


    Image image5 = new Image(Studio.class.getResourceAsStream("b2s.png"));
    ImageView view5 = new ImageView(image5);
    view5.setFitWidth(18);
    view5.setFitHeight(18);
    backglassManagerBtn.setGraphic(view5);

    dataBoxScrollPane.setVisible(false);
    emptyDataBox.setVisible(true);

    hideGrill.setItems(FXCollections.observableList(VISIBILITIES));
    hideGrill.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      tableSettings.setHideGrill(t1.getId());
      save();

      this.tableSettings = client.getBackglassServiceClient().getTableSettings(game.get().getId());
      this.tableData = client.getBackglassServiceClient().getDirectB2SData(game.get().getId());
      refreshView(this.game);
    });

    hideB2SDMD.selectedProperty().addListener((observable, oldValue, newValue) -> {
      tableSettings.setHideB2SDMD(newValue);
      save();
    });

    hideB2SBackglass.selectedProperty().addListener((observable, oldValue, newValue) -> {
      tableSettings.setHideB2SBackglass(newValue);
      save();
    });

    hideDMD.setItems(FXCollections.observableList(VISIBILITIES));
    hideDMD.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      tableSettings.setHideDMD(t1.getId());
      save();
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipLampFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("skipLampFrames", () -> {
        tableSettings.setLampsSkipFrames(t1);
        save();
      }, DEBOUNCE_MS);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipGIFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("skipGIFrames", () -> {
        tableSettings.setGiStringsSkipFrames(t1);
        save();
      }, DEBOUNCE_MS);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipSolenoidFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("skipSolenoidFrames", () -> {
        tableSettings.setSolenoidsSkipFrames(t1);
        save();
      }, DEBOUNCE_MS);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipLEDFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("skipLEDFrames", () -> {
        tableSettings.setLedsSkipFrames(t1);
        save();
      }, DEBOUNCE_MS);
    });


    glowing.setItems(FXCollections.observableList(GLOWINGS));
    glowing.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      tableSettings.setGlowIndex(t1.getId());
      save();
    });

    lightBulbOn.selectedProperty().addListener((observable, oldValue, newValue) -> {
      tableSettings.setGlowBulbOn(newValue);
      save();
    });

    startAsExe.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        tableSettings.setStartAsEXE(newValue);
      }
      else {
        tableSettings.setStartAsEXE(null);
      }
      save();
    });

    usedLEDType.setItems(FXCollections.observableList(LED_TYPES));
    usedLEDType.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (t1 != null) {
        tableSettings.setUsedLEDType(t1.getId());
        glowing.setDisable(t1.getId() == 1);
        lightBulbOn.setDisable(t1.getId() == 1);
        lightBulbOn.setSelected(false);
        save();
      }
    });

    startBackground.setItems(FXCollections.observableList(VISIBILITIES));
    startBackground.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      tableSettings.setStartBackground(t1.getId());
      save();
    });

    formToPosition.setItems(FXCollections.observableList(FORM_POSITIONS));
    formToPosition.valueProperty().addListener((observable, oldValue, newValue) -> {
      tableSettings.setFormToPosition(newValue.getId());
      save();
    });
  }

  private void save() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      try {
        if (this.saveEnabled) {
          this.tableSettings = client.getBackglassServiceClient().saveTableSettings(g.getId(), this.tableSettings);
        }
      }
      catch (Exception e) {
        LOG.error("Failed to save B2STableSettings.xml: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save B2STableSettings.xml: " + e.getMessage());
      }
    }
  }


  private void setSaveEnabled(boolean b) {
    if (b) {
      try {
        Thread.sleep(DEBOUNCE_MS + 100);
      }
      catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    this.saveEnabled = b;
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.tableSettings = null;
    if (game.isPresent()) {
      this.tableSettings = client.getBackglassServiceClient().getTableSettings(game.get().getId());
      this.tableData = client.getBackglassServiceClient().getDirectB2SData(game.get().getId());
    }
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    setSaveEnabled(false);

    boolean directb2sAvailable = g.isPresent() && g.get().getDirectB2SPath() != null;
    openDefaultPictureBtn.setDisable(!g.isPresent() || !directb2sAvailable);
    uploadBtn.setDisable(!g.isPresent());
    reloadBtn.setDisable(!g.isPresent());
    dataBoxScrollPane.setVisible(g.isPresent() && directb2sAvailable);
    emptyDataBox.setVisible(!g.isPresent() || !directb2sAvailable);

    setDisable(b2sSettings, true);

    nameLabel.setText("-");
    typeLabel.setText("-");
    authorLabel.setText("-");
    artworkLabel.setText("-");
    b2sElementsLabel.setText("-");
    grillLabel.setText("-");
    playersLabel.setText("-");
    bulbsLabel.setText("-");
    filesizeLabel.setText("-");
    modificationDateLabel.setText("-");
    thumbnailImage.setImage(new Image(Studio.class.getResourceAsStream("empty-preview.png")));
    dmdThumbnailImage.setImage(new Image(Studio.class.getResourceAsStream("empty-preview.png")));
    resolutionLabel.setText("");
    dmdResolutionLabel.setText("");

    deleteBtn.setDisable(!g.isPresent() || !directb2sAvailable);
    dmdPositionBtn.setDisable(!g.isPresent() || !directb2sAvailable);
    backglassManagerBtn.setDisable(!g.isPresent() || !directb2sAvailable);

    if (g.isPresent() && directb2sAvailable) {
      new Thread(() -> {
        Platform.runLater(() -> {
          nameLabel.setText(tableData.getName());
          typeLabel.setText(DirectB2SData.getTableType(tableData.getTableType()));
          authorLabel.setText(tableData.getAuthor());
          artworkLabel.setText(tableData.getArtwork());
          grillLabel.setText(String.valueOf(tableData.getGrillHeight()));
          b2sElementsLabel.setText(String.valueOf(tableData.getB2sElements()));
          playersLabel.setText(String.valueOf(tableData.getNumberOfPlayers()));
          filesizeLabel.setText(FileUtils.readableFileSize(tableData.getFilesize()));
          bulbsLabel.setText(String.valueOf(tableData.getIlluminations()));
          modificationDateLabel.setText(tableData.getModificationDate() != null ? SimpleDateFormat.getDateTimeInstance().format(tableData.getModificationDate()) : "");

          hideGrill.setDisable(tableData.getGrillHeight() == 0);

          if (tableData.isBackgroundAvailable()) {
            resolutionLabel.setText("Loading...");
            new Thread(() -> {
              try (InputStream in = client.getBackglassServiceClient().getDirectB2sBackground(tableData)) {
                Image image = new Image(in);
                if (tableData.getGrillHeight() > 0 && tableSettings != null && tableSettings.getHideGrill() == 1) {
                  PixelReader reader = image.getPixelReader();
                  image = new WritableImage(reader, 0, 0, (int) image.getWidth(), (int) (image.getHeight() - tableData.getGrillHeight()));
                }
                final Image imageToLoad = image;
                Platform.runLater(() -> {
                  thumbnailImage.setImage(imageToLoad);
                  resolutionLabel.setText("Resolution: " + (int) imageToLoad.getWidth() + " x " + (int) imageToLoad.getHeight());
                });
              }
              catch (IOException ioe) {
                LOG.error("Cannot load background Image for game " + g.get().getId(), ioe);
              }
            }, "B2S Image Loader").start();
          }
          else {
            thumbnailImage.setImage(null);
            resolutionLabel.setText("Failed to read image data.");
          }

          if (tableData.isDmdImageAvailable()) {
            dmdResolutionLabel.setText("Loading...");
            new Thread(() -> {
              try (InputStream in = client.getBackglassServiceClient().getDirectB2sDmd(tableData)) {
                Image image = new Image(in);
                Platform.runLater(() -> {
                  dmdThumbnailImage.setImage(image);
                  dmdResolutionLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
                });
              }
              catch (IOException ioe) {
                LOG.error("Cannot load DMD Image for game " + g.get().getId(), ioe);
              }
            }, "B2S DMD Loader").start();
          }
          else {
            dmdResolutionLabel.setText("No DMD background available.");
          }

          if (tableSettings != null) {
            hideGrill.setValue(VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getHideGrill()).findFirst().orElse(null));
            hideB2SDMD.selectedProperty().setValue(tableSettings.isHideB2SDMD());
            hideB2SBackglass.selectedProperty().setValue(tableSettings.isHideB2SBackglass());
            hideDMD.setValue(VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getHideDMD()).findFirst().orElse(null));
            skipLampFrames.getValueFactory().valueProperty().set(tableSettings.getLampsSkipFrames());
            skipGIFrames.getValueFactory().valueProperty().set(tableSettings.getGiStringsSkipFrames());
            skipSolenoidFrames.getValueFactory().valueProperty().set(tableSettings.getSolenoidsSkipFrames());
            skipLEDFrames.getValueFactory().valueProperty().set(tableSettings.getLedsSkipFrames());
            lightBulbOn.selectedProperty().setValue(tableSettings.isGlowBulbOn());
            glowing.setValue(GLOWINGS.stream().filter(v -> v.getId() == tableSettings.getGlowIndex()).findFirst().orElse(null));
            usedLEDType.setValue(LED_TYPES.stream().filter(v -> v.getId() == tableSettings.getUsedLEDType()).findFirst().orElse(null));
            startBackground.setValue(VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getStartBackground()).findFirst().orElse(null));
            formToPosition.setValue(FORM_POSITIONS.stream().filter(v -> v.getId() == tableSettings.getFormToPosition()).findFirst().orElse(null));

            boolean tableLaunchAsExe = tableSettings.getStartAsEXE() != null && tableSettings.getStartAsEXE();
            startAsExe.setSelected(tableLaunchAsExe);

            DirectB2ServerSettings serverSettings = client.getBackglassServiceClient().getServerSettings();
            if (serverSettings != null) {
              boolean serverLaunchAsExe = serverSettings.getDefaultStartMode() == DirectB2ServerSettings.EXE_START_MODE;
              startAsExeServer.setSelected(serverLaunchAsExe);
            }
          }

          setDisable(b2sSettings, false);
          startAsExeServer.setDisable(true);

          skipGIFrames.setDisable(tableData.getIlluminations() == 0);
          skipSolenoidFrames.setDisable(tableData.getIlluminations() == 0);
          skipLampFrames.setDisable(tableData.getIlluminations() == 0);
          skipLEDFrames.setDisable(tableData.getIlluminations() == 0);
          glowing.setDisable(usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 1);
          lightBulbOn.setDisable(usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 1);

          setSaveEnabled(true);
        });
      }).start();
    }
  }

  private void setDisable(Pane parent, boolean disabled) {
    for (Node child : parent.getChildren()) {
      child.setDisable(disabled);
    }
  }

  public void setRootController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

  @Override
  public void preferencesChanged(PreferenceType preferenceType) {
    if (preferenceType.equals(PreferenceType.backglassServer)) {
      this.setGame(this.game);
    }
  }
}