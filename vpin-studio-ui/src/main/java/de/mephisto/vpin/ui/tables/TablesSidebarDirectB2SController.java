package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.tables.models.B2SGlowing;
import de.mephisto.vpin.ui.tables.models.B2SLedType;
import de.mephisto.vpin.ui.tables.models.B2SVisibility;
import de.mephisto.vpin.ui.util.MediaUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

import java.io.ByteArrayInputStream;
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
  private VBox dataBox;

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
  private CheckBox hideB2SDMD;

  @FXML
  private CheckBox startAsExe;

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
  private CheckBox startBackground;

  @FXML
  private CheckBox bringBGFromTop;

  @FXML
  private Button backglassManagerBtn;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  private DirectB2SData tableData;
  private DirectB2STableSettings tableSettings;
  private boolean saveEnabled;


  // Add a public no-args constructor
  public TablesSidebarDirectB2SController() {
  }

  @FXML
  private void onBackglassManager() {
    TableDialogs.openDirectB2sManagerDialog(tablesSidebarController);
  }

  @FXML
  private void onUpload(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    if (game.isPresent()) {
      TableDialogs.directBackglassUpload(stage, game.get());
    }
  }

  @FXML
  private void onOpenDirectB2SBackground() {
    if (game.isPresent() && game.get().isDirectB2SAvailable()) {
      byte[] bytesEncoded = org.apache.commons.codec.binary.Base64.decodeBase64(tableData.getBackgroundBase64());
      MediaUtil.openMedia(new ByteArrayInputStream(bytesEncoded));
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
      refreshView(this.game);
    });

    hideB2SDMD.selectedProperty().addListener((observable, oldValue, newValue) -> {
      tableSettings.setHideB2SDMD(newValue);
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
      tableSettings.setStartAsEXE(newValue);
      save();
    });

    usedLEDType.setItems(FXCollections.observableList(LED_TYPES));
    usedLEDType.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      tableSettings.setUsedLEDType(t1.getId());
      glowing.setDisable(t1.getId() == 1);
      lightBulbOn.setDisable(t1.getId() == 1);
      lightBulbOn.setSelected(false);
      save();
    });

    startBackground.selectedProperty().addListener((observable, oldValue, newValue) -> {
      tableSettings.setStartBackground(newValue);
      save();
    });

    bringBGFromTop.selectedProperty().addListener((observable, oldValue, newValue) -> {
      tableSettings.setFormToFront(newValue);
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
      } catch (Exception e) {
        LOG.error("Failed to save B2STableSettings.xml: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save B2STableSettings.xml: " + e.getMessage());
      }
    }
  }


  private void setSaveEnabled(boolean b) {
    if (b) {
      try {
        Thread.sleep(DEBOUNCE_MS + 100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    this.saveEnabled = b;
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    setSaveEnabled(false);

    openDefaultPictureBtn.setDisable(!g.isPresent() || !g.get().isDirectB2SAvailable());
    uploadBtn.setDisable(!g.isPresent());
    dataBoxScrollPane.setVisible(g.isPresent() && g.get().isDirectB2SAvailable());
    emptyDataBox.setVisible(!g.isPresent() || !g.get().isDirectB2SAvailable());

    this.tableSettings = null;

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

    if (g.isPresent() && g.get().isDirectB2SAvailable()) {
      this.tableSettings = client.getBackglassServiceClient().getTableSettings(g.get().getId());
      this.tableData = client.getBackglassServiceClient().getDirectB2SData(g.get().getId());

      nameLabel.setText(tableData.getName());
      typeLabel.setText(DirectB2SData.getTableType(tableData.getTableType()));
      authorLabel.setText(tableData.getAuthor());
      artworkLabel.setText(tableData.getArtwork());
      grillLabel.setText(String.valueOf(tableData.getGrillHeight()));
      b2sElementsLabel.setText(String.valueOf(tableData.getB2sElements()));
      playersLabel.setText(String.valueOf(tableData.getNumberOfPlayers()));
      filesizeLabel.setText(FileUtils.readableFileSize(tableData.getFilesize()));
      bulbsLabel.setText(String.valueOf(tableData.getIlluminations()));
      modificationDateLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tableData.getModificationDate()));

      hideGrill.setDisable(tableData.getGrillHeight() == 0);

      byte[] bytesEncoded = org.apache.commons.codec.binary.Base64.decodeBase64(tableData.getBackgroundBase64());
      if (bytesEncoded != null) {
        Image image = new Image(new ByteArrayInputStream(bytesEncoded));
        if (tableData.getGrillHeight() > 0 && tableSettings != null && tableSettings.getHideGrill() == 1) {
          PixelReader reader = image.getPixelReader();
          image = new WritableImage(reader, 0, 0, (int) image.getWidth(), (int) (image.getHeight() - tableData.getGrillHeight()));
        }
        thumbnailImage.setImage(image);
        resolutionLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
      }
      else {
        thumbnailImage.setImage(null);
        resolutionLabel.setText("Failed to read image data.");
      }

      byte[] dmdBytesEncoded = org.apache.commons.codec.binary.Base64.decodeBase64(tableData.getDmdBase64());
      if (dmdBytesEncoded != null) {
        Image image = new Image(new ByteArrayInputStream(dmdBytesEncoded));
        dmdThumbnailImage.setImage(image);
        dmdResolutionLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
      }
      else {
        dmdResolutionLabel.setText("No DMD background available.");
      }

      if (tableSettings != null) {
        hideGrill.setValue(VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getHideGrill()).findFirst().get());
        hideB2SDMD.selectedProperty().setValue(tableSettings.isHideB2SDMD());
        hideDMD.setValue(VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getHideDMD()).findFirst().get());
        skipLampFrames.getValueFactory().valueProperty().set(tableSettings.getLampsSkipFrames());
        skipGIFrames.getValueFactory().valueProperty().set(tableSettings.getGiStringsSkipFrames());
        skipSolenoidFrames.getValueFactory().valueProperty().set(tableSettings.getSolenoidsSkipFrames());
        skipLEDFrames.getValueFactory().valueProperty().set(tableSettings.getLedsSkipFrames());
        lightBulbOn.selectedProperty().setValue(tableSettings.isGlowBulbOn());
        glowing.setValue(GLOWINGS.stream().filter(v -> v.getId() == tableSettings.getGlowIndex()).findFirst().get());
        usedLEDType.setValue(LED_TYPES.stream().filter(v -> v.getId() == tableSettings.getUsedLEDType()).findFirst().get());
        startBackground.selectedProperty().setValue(tableSettings.isStartBackground());
        bringBGFromTop.selectedProperty().setValue(tableSettings.isFormToFront());
        startAsExe.selectedProperty().setValue(tableSettings.isStartAsEXE());
      }


      skipGIFrames.setDisable(tableData.getIlluminations() == 0);
      skipSolenoidFrames.setDisable(tableData.getIlluminations() == 0);
      skipLEDFrames.setDisable(tableData.getIlluminations() == 0 || usedLEDType.getValue() == null || usedLEDType.getValue().getId() == 2);
      skipLampFrames.setDisable(tableData.getIlluminations() == 0);

      glowing.setDisable(usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 2);
      lightBulbOn.setDisable(usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 2);

      setSaveEnabled(true);
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  @Override
  public void preferencesChanged(PreferenceType preferenceType) {
    if(preferenceType.equals(PreferenceType.backglassServer)) {
      this.setGame(this.game);
    }
  }
}