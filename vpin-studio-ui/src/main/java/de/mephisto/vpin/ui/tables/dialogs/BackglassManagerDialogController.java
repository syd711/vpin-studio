package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.tables.TablesSidebarDirectB2SController;
import de.mephisto.vpin.ui.tables.models.B2SGlowing;
import de.mephisto.vpin.ui.tables.models.B2SLedType;
import de.mephisto.vpin.ui.tables.models.B2SVisibility;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class BackglassManagerDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassManagerDialogController.class);

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
  private Button renameBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button duplicateBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private TextField searchField;

  @FXML
  private Label gameLabel;

  @FXML
  private Label gameFilenameLabel;

  @FXML
  private Button dataManagerBtn;


  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private ListView<DirectB2S> directb2sList;

  private DirectB2SData tableData;
  private DirectB2STableSettings tableSettings;
  private boolean saveEnabled;

  private TablesSidebarController tablesSidebarController;
  private List<DirectB2S> backglasses;
  private GameRepresentation game;

  @FXML
  private void onUpload(ActionEvent e) {
    if (game != null) {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      TableDialogs.directBackglassUpload(stage, game);
    }
  }

  @FXML
  private void onTableDataManager(ActionEvent e) {
    if (game != null) {
      onCancel(e);
      Platform.runLater(() -> {
        TableDialogs.openTableDataDialog(tablesSidebarController.getTablesController(), this.game);
      });
    }
  }

  @FXML
  private void onRename(ActionEvent e) {
    DirectB2S selectedItem = directb2sList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      String newName = WidgetFactory.showInputDialog(stage, "Rename Backglass", "Enter new name for backglass file \"" + selectedItem.getName() + ".directb2s\"", null, null, selectedItem.getName());
      if (newName != null) {
        if (!FileUtils.isValidFilename(newName)) {
          WidgetFactory.showAlert(stage, "Invalid Filename", "The specified file name contains invalid characters.");
          return;
        }

        try {
          if (!newName.endsWith(".directb2s")) {
            newName = newName + ".directb2s";
          }
          client.getBackglassServiceClient().renameBackglass(selectedItem.getEmulatorId(), selectedItem.getName(), newName);
        } catch (Exception ex) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to dupliate backglass: " + ex.getMessage());
        }
        onReload();
      }
    }
  }

  @FXML
  private void onDuplicate(ActionEvent e) {
    DirectB2S selectedItem = directb2sList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Duplicate Backglass", "Duplicate backglass file \"" + selectedItem.getName() + ".directb2s\"?", null, "Duplicate");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.getBackglassServiceClient().duplicateBackglass(selectedItem.getEmulatorId(), selectedItem.getName());
        } catch (Exception ex) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to dupliate backglass: " + ex.getMessage());
        }
        onReload();
      }
    }
  }

  @FXML
  private void onDelete(ActionEvent e) {
    DirectB2S selectedItem = directb2sList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete Backglass", "Delete backglass file \"" + selectedItem.getName() + ".directb2s\"?", null, "Delete");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getBackglassServiceClient().deleteBackglass(selectedItem.getEmulatorId(), selectedItem.getName());
        onReload();
      }
    }
  }

  @FXML
  private void onCancel(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onReload() {
    setSaveEnabled(false);
    backglasses = client.getBackglassServiceClient().getBackglasses();
    List<DirectB2S> filtered = filterEntries(backglasses);
    directb2sList.setItems(FXCollections.observableList(filtered));
    directb2sList.refresh();
    setSaveEnabled(true);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.dataManagerBtn.setDisable(true);
    this.renameBtn.setDisable(true);
    this.duplicateBtn.setDisable(true);
    this.deleteBtn.setDisable(true);

    this.directb2sList.setCellFactory(new Callback<>() {
      @Override
      public ListCell<DirectB2S> call(ListView<DirectB2S> param) {
        return new ListCell<>() {
          @Override
          public void updateItem(DirectB2S backglass, boolean empty) {
            super.updateItem(backglass, empty);
            if (backglass == null || empty) {
              setText(null);
              setGraphic(null);
            }
            else {
              setText(backglass.getName());

              FontIcon fontIcon = backglass.isVpxAvailable() ? null : WidgetFactory.createExclamationIcon();
              setGraphic(fontIcon);
            }
          }
        };
      }
    });

    List<GameEmulatorRepresentation> emulators = new ArrayList<>(client.getPinUPPopperService().getGameEmulators());
    emulators.add(0, null);
    ObservableList<GameEmulatorRepresentation> data = FXCollections.observableList(emulators);
    this.emulatorCombo.setItems(data);
    this.emulatorCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      backglasses = client.getBackglassServiceClient().getBackglasses();
      List<DirectB2S> filtered = filterEntries(backglasses);
      directb2sList.setItems(FXCollections.observableList(filtered));
    });

    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
      List<DirectB2S> filtered = filterEntries(backglasses);
      directb2sList.setItems(FXCollections.observableList(filtered));
    });

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

    this.backglasses = client.getBackglassServiceClient().getBackglasses();
    this.directb2sList.setItems(FXCollections.observableList(backglasses));
    this.directb2sList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      setSaveEnabled(false);
      deleteBtn.setDisable(newValue == null);

      Platform.runLater(() -> {
        refresh(newValue);
        setSaveEnabled(true);
      });
    });
  }

  private List<DirectB2S> filterEntries(List<DirectB2S> backglasses) {
    int emulatorId = -1;
    if (this.emulatorCombo.getValue() != null) {
      emulatorId = this.emulatorCombo.getValue().getId();
    }
    List<DirectB2S> filtered = new ArrayList<>();
    for (DirectB2S backglass : backglasses) {
      if (emulatorId != -1 && backglass.getEmulatorId() != emulatorId) {
        continue;
      }

      if (!StringUtils.isEmpty(searchField.getText().trim()) && !backglass.getName().toLowerCase().contains(searchField.getText().toLowerCase())) {
        continue;
      }

      filtered.add(backglass);
    }

    return filtered;
  }

  @Override
  public void onDialogCancel() {
  }

  private void refresh(@Nullable DirectB2S newValue) {
    setSaveEnabled(false);
    this.dataManagerBtn.setDisable(true);

    this.renameBtn.setDisable(newValue == null);
    this.duplicateBtn.setDisable(newValue == null);
    this.deleteBtn.setDisable(newValue == null);

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
    gameLabel.setText("-");
    gameFilenameLabel.setText("-");
    game = null;

    if (newValue != null) {
      this.tableData = client.getBackglassServiceClient().getDirectB2SData(newValue.getEmulatorId(), newValue.getName());
      if (this.tableData.getGameId() > 0) {
        this.tableSettings = client.getBackglassServiceClient().getTableSettings(this.tableData.getGameId());
        game = client.getGame(this.tableData.getGameId());
        gameLabel.setText(game.getGameDisplayName());
        gameFilenameLabel.setText(game.getGameFileName());
        dataManagerBtn.setDisable(false);
      }
      else {
        this.tableSettings = null;
      }


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

      hideGrill.setDisable(tableSettings == null);
      hideB2SDMD.setSelected(false);
      hideB2SDMD.setDisable(tableSettings == null);
      hideDMD.setDisable(tableSettings == null);
      skipLampFrames.getValueFactory().setValue(0);
      skipLampFrames.setDisable(tableSettings == null || tableData.getIlluminations() == 0);
      skipGIFrames.getValueFactory().setValue(0);
      skipGIFrames.setDisable(tableSettings == null || tableData.getIlluminations() == 0);
      skipSolenoidFrames.getValueFactory().setValue(0);
      skipSolenoidFrames.setDisable(tableSettings == null || tableData.getIlluminations() == 0);
      skipLEDFrames.getValueFactory().setValue(0);
      skipLEDFrames.setDisable(tableSettings == null || tableData.getIlluminations() == 0 || usedLEDType.getValue() == null || usedLEDType.getValue().getId() == 2);
      lightBulbOn.setSelected(false);
      lightBulbOn.setDisable(tableSettings == null || (usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 2));
      glowing.setDisable(tableSettings == null || (usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 2));
      usedLEDType.setDisable(tableSettings == null);
      startBackground.setSelected(false);
      startBackground.setDisable(tableSettings == null);
      bringBGFromTop.setSelected(false);
      bringBGFromTop.setDisable(tableSettings == null);

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

      setSaveEnabled(true);
    }
  }

  public void setTableSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  private void save() {
    if (!saveEnabled) {
      return;
    }

    if (this.game != null) {
      try {
        if (this.saveEnabled) {
          client.getBackglassServiceClient().saveTableSettings(game.getId(), this.tableSettings);
          this.refresh(this.directb2sList.getSelectionModel().getSelectedItem());
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
        Thread.sleep(DEBOUNCE_MS + 10);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    this.saveEnabled = b;
  }
}
