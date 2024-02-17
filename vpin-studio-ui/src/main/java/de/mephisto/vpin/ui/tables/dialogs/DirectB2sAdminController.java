package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.tables.TablesSidebarDirectB2SController;
import de.mephisto.vpin.ui.tables.models.B2SGlowing;
import de.mephisto.vpin.ui.tables.models.B2SLedType;
import de.mephisto.vpin.ui.tables.models.B2SVisibility;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DirectB2sAdminController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2sAdminController.class);

  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 100;

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


  //-- Editors

  @FXML
  private ComboBox<B2SVisibility> hideGrill;

  @FXML
  private CheckBox hideB2SDMD;

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
  private Button restoreBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private ListView<DirectB2S> directb2sList;

  private DirectB2SData tableData;
  private DirectB2STableSettings tableSettings;
  private boolean saveEnabled;

  private TablesSidebarController tablesSidebarController;

  @FXML
  private void onRestore(ActionEvent e) {

  }

  @FXML
  private void onDelete(ActionEvent e) {

  }

  @FXML
  private void onCancel(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    hideGrill.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.VISIBILITIES));
    hideGrill.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      tableSettings.setHideGrill(t1.getId());
      save();
    });

    hideB2SDMD.selectedProperty().addListener((observable, oldValue, newValue) -> {
      tableSettings.setHideB2SDMD(newValue);
      save();
    });

    hideDMD.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.VISIBILITIES));
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


    glowing.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.GLOWINGS));
    glowing.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      tableSettings.setGlowIndex(t1.getId());
      save();
    });

    lightBulbOn.selectedProperty().addListener((observable, oldValue, newValue) -> {
      tableSettings.setGlowBulbOn(newValue);
    });

    usedLEDType.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.LED_TYPES));
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

    List<DirectB2S> backglasses = client.getBackglassServiceClient().getBackglasses();
    this.directb2sList.setItems(FXCollections.observableList(backglasses));
    this.directb2sList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DirectB2S>() {
      @Override
      public void changed(ObservableValue<? extends DirectB2S> observable, DirectB2S oldValue, DirectB2S newValue) {
        refresh(newValue);
      }
    });
  }

  private void save() {

//    if (this.game.isPresent()) {
//      GameRepresentation g = this.game.get();
//      try {
//        if (this.saveEnabled) {
//          client.getBackglassServiceClient().saveTableSettings(g.getId(), this.tableSettings);
//        }
//      } catch (Exception e) {
//        LOG.error("Failed to save B2STableSettings.xml: " + e.getMessage(), e);
//        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save B2STableSettings.xml: " + e.getMessage());
//      }
//    }
  }

  @Override
  public void onDialogCancel() {
  }

  private void refresh(DirectB2S newValue) {
    this.saveEnabled = false;

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

    if (newValue != null) {
      this.tableData = client.getBackglassServiceClient().getDirectB2SData(newValue.getEmulatorId(), newValue.getName());
      this.tableSettings = client.getBackglassServiceClient().getTableSettings(this.tableData.getGameId());

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
        hideGrill.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getHideGrill()).findFirst().get());
        hideB2SDMD.selectedProperty().setValue(tableSettings.isHideB2SDMD());
        hideDMD.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getHideDMD()).findFirst().get());
        skipLampFrames.getValueFactory().valueProperty().set(tableSettings.getLampsSkipFrames());
        skipGIFrames.getValueFactory().valueProperty().set(tableSettings.getGiStringsSkipFrames());
        skipSolenoidFrames.getValueFactory().valueProperty().set(tableSettings.getSolenoidsSkipFrames());
        skipLEDFrames.getValueFactory().valueProperty().set(tableSettings.getLedsSkipFrames());
        lightBulbOn.selectedProperty().setValue(tableSettings.isGlowBulbOn());
        glowing.setValue(TablesSidebarDirectB2SController.GLOWINGS.stream().filter(v -> v.getId() == tableSettings.getGlowIndex()).findFirst().get());
        usedLEDType.setValue(TablesSidebarDirectB2SController.LED_TYPES.stream().filter(v -> v.getId() == tableSettings.getUsedLEDType()).findFirst().get());
        startBackground.selectedProperty().setValue(tableSettings.isStartBackground());
        bringBGFromTop.selectedProperty().setValue(tableSettings.isFormToFront());
      }


      skipGIFrames.setDisable(tableData.getIlluminations() == 0);
      skipSolenoidFrames.setDisable(tableData.getIlluminations() == 0);
      skipLEDFrames.setDisable(tableData.getIlluminations() == 0 || usedLEDType.getValue() == null || usedLEDType.getValue().getId() == 2);
      skipLampFrames.setDisable(tableData.getIlluminations() == 0);

      glowing.setDisable(usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 2);
      lightBulbOn.setDisable(usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 2);

      this.saveEnabled = true;
    }
  }

  public void setTableSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}
