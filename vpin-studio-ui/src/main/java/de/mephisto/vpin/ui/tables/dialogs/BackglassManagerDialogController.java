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
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

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
  private Label fullDmdLabel;

  @FXML
  private Label modificationDateLabel;

  @FXML
  private ImageView thumbnailImage;

  @FXML
  private ImageView dmdThumbnailImage;

  @FXML
  private Button downloadBackglassBtn;

  @FXML
  private Button downloadDMDBtn;

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
  private CheckBox startAsExe;

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
  private MenuButton filterButton;

  @FXML
  private TableView<DirectB2SEntryModel> directb2sList;

  @FXML
  private TableColumn<DirectB2SEntryModel, Object> statusColumn;

  @FXML
  private TableColumn<DirectB2SEntryModel, Object> displayNameColumn;

  @FXML
  private TableColumn<DirectB2SEntryModel, DirectB2SEntryModel> grillColumn;

  @FXML
  private TableColumn<DirectB2SEntryModel, DirectB2SEntryModel> fullDmdColumn;

  private DirectB2SData tableData;
  private DirectB2STableSettings tableSettings;
  private boolean saveEnabled;

  private TablesSidebarController tablesSidebarController;
  private List<DirectB2SEntryModel> backglasses;
  private GameRepresentation game;

  @FXML
  private void onUpload(ActionEvent e) {
    if (game != null) {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      TableDialogs.directBackglassUpload(stage, game);
    }
  }

  @FXML
  private void onBackglassDownload() {
    byte[] bytesEncoded = org.apache.commons.codec.binary.Base64.decodeBase64(tableData.getBackgroundBase64());
    export(bytesEncoded);
  }

  @FXML
  private void onDMDDownload() {
    byte[] bytesEncoded = org.apache.commons.codec.binary.Base64.decodeBase64(tableData.getDmdBase64());
    export(bytesEncoded);
  }

  private void export(byte[] bytesEncoded) {
    if (bytesEncoded != null) {
      StudioFolderChooser chooser = new StudioFolderChooser();
      chooser.setTitle("Select Target Folder");
      File targetFolder = chooser.showOpenDialog(stage);

      if (targetFolder != null) {
        try {
          File targetFile = new File(targetFolder, this.tableData.getName() + ".png");
          targetFile = FileUtils.uniqueFile(targetFile);
          FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
          IOUtils.write(bytesEncoded, fileOutputStream);
          fileOutputStream.close();

          WidgetFactory.showInformation(stage, "Export Finished", "Written \"" + targetFile.getName() + "\".");
        } catch (IOException e) {
          LOG.error("Failed to download backglass image: " + e.getMessage(), e);
          WidgetFactory.showAlert(stage, "Error", "Failed to download backglass image: " + e.getMessage());
        }
      }
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
    DirectB2SEntryModel selectedItem = directb2sList.getSelectionModel().getSelectedItem();
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
          client.getBackglassServiceClient().renameBackglass(selectedItem.backglass, newName);
        } catch (Exception ex) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to dupliate backglass: " + ex.getMessage());
        }
        onReload();
      }
    }
  }

  @FXML
  private void onDuplicate(ActionEvent e) {
    DirectB2SEntryModel selectedItem = directb2sList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Duplicate Backglass", "Duplicate backglass file \"" + selectedItem.getName() + ".directb2s\"?", null, "Duplicate");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.getBackglassServiceClient().duplicateBackglass(selectedItem.backglass);
        } catch (Exception ex) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to dupliate backglass: " + ex.getMessage());
        }
        onReload();
      }
    }
  }

  @FXML
  private void onDelete(ActionEvent e) {
    try {
      DirectB2SEntryModel selectedItem = directb2sList.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete Backglass", "Delete backglass file \"" + selectedItem.getName() + ".directb2s\"?", null, "Delete");
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
          client.getBackglassServiceClient().deleteBackglass(selectedItem.backglass);
          onReload();
        }
      }
    } catch (Exception ex) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to delete backglass file: " + ex.getMessage());
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
    backglasses = toModels(client.getBackglassServiceClient().getBackglasses());
    List<DirectB2SEntryModel> filtered = filterEntries(backglasses);
    directb2sList.setItems(FXCollections.observableList(filtered));
    directb2sList.refresh();
    setSaveEnabled(true);
  }

  private List<DirectB2SEntryModel> toModels(List<DirectB2S> backglasses) {
    List<DirectB2SEntryModel> models = new ArrayList<>(backglasses.size());
    for (DirectB2S b2s: backglasses) {
      models.add(new DirectB2SEntryModel(b2s));
    }
    return models;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.dataManagerBtn.setDisable(true);
    this.renameBtn.setDisable(true);
    this.duplicateBtn.setDisable(true);
    this.deleteBtn.setDisable(true);

    bindTable();

    List<GameEmulatorRepresentation> gameEmulators = client.getPinUPPopperService().getVpxGameEmulators();
    for (GameEmulatorRepresentation gameEmulator : gameEmulators) {
      CustomMenuItem item = new CustomMenuItem();
      CheckBox checkBox = new CheckBox(gameEmulator.getName());
      checkBox.setStyle("-fx-font-size: 14px;-fx-padding: 0 6 0 6;");
      checkBox.setPrefHeight(30);
      checkBox.setSelected(true);
      checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          backglasses = toModels(client.getBackglassServiceClient().getBackglasses());
          List<DirectB2SEntryModel> filtered = filterEntries(backglasses);
          directb2sList.setItems(FXCollections.observableList(filtered));
        }
      });
      checkBox.setUserData(gameEmulator);
      item.setContent(checkBox);
      filterButton.getItems().add(item);
    }

    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
      List<DirectB2SEntryModel> filtered = filterEntries(backglasses);
      directb2sList.setItems(FXCollections.observableList(filtered));
    });

    hideGrill.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.VISIBILITIES));
    hideGrill.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setHideGrill(t1.getId());
      save();
    });

    hideB2SDMD.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setHideB2SDMD(newValue);
      save();
    });

    hideDMD.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.VISIBILITIES));
    hideDMD.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setHideDMD(t1.getId());
      save();
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipLampFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("skipLampFrames", () -> {
        if (tableSettings == null) {
          return;
        }
        tableSettings.setLampsSkipFrames(t1);
        save();
      }, DEBOUNCE_MS);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipGIFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      debouncer.debounce("skipGIFrames", () -> {
        if (tableSettings == null) {
          return;
        }
        tableSettings.setGiStringsSkipFrames(t1);
        save();
      }, DEBOUNCE_MS);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipSolenoidFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (tableSettings == null) {
        return;
      }
      debouncer.debounce("skipSolenoidFrames", () -> {
        tableSettings.setSolenoidsSkipFrames(t1);
        save();
      }, DEBOUNCE_MS);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipLEDFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (tableSettings == null) {
        return;
      }

      debouncer.debounce("skipLEDFrames", () -> {
        tableSettings.setLedsSkipFrames(t1);
        save();
      }, DEBOUNCE_MS);
    });


    glowing.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.GLOWINGS));
    glowing.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setGlowIndex(t1.getId());
      save();
    });

    startAsExe.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setStartAsEXE(newValue);
      save();
    });

    lightBulbOn.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setGlowBulbOn(newValue);
      save();
    });

    usedLEDType.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.LED_TYPES));
    usedLEDType.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setUsedLEDType(t1.getId());
      glowing.setDisable(t1.getId() == 1);
      lightBulbOn.setDisable(t1.getId() == 1);
      lightBulbOn.setSelected(false);
      save();
    });

    startBackground.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setStartBackground(newValue);
      save();
    });

    bringBGFromTop.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (tableSettings == null) {
        return;
      }
      tableSettings.setFormToFront(newValue);
      save();
    });

    this.backglasses = toModels(client.getBackglassServiceClient().getBackglasses());
    this.directb2sList.setItems(FXCollections.observableList(backglasses));
    this.directb2sList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      setSaveEnabled(false);
      this.deleteBtn.setDisable(newValue == null);
      this.glowing.setDisable(newValue == null);
      this.startAsExe.setDisable(newValue == null);
      this.dataManagerBtn.setDisable(newValue == null);
      this.duplicateBtn.setDisable(newValue == null);
      this.hideB2SDMD.setDisable(newValue == null);
      this.hideGrill.setDisable(newValue == null);
      this.hideDMD.setDisable(newValue == null);
      this.startBackground.setDisable(newValue == null);
      this.bringBGFromTop.setDisable(newValue == null);
      this.skipGIFrames.setDisable(newValue == null);
      this.skipLampFrames.setDisable(newValue == null);
      this.skipLampFrames.setDisable(newValue == null);
      this.skipSolenoidFrames.setDisable(newValue == null);
      this.bulbsLabel.setDisable(newValue == null);
      this.usedLEDType.setDisable(newValue == null);

      Platform.runLater(() -> {
        refresh(newValue.backglass);
      });
    });

    if (this.directb2sList.getItems().isEmpty()) {
      this.directb2sList.getSelectionModel().clearSelection();
    }
    else {
      this.directb2sList.getSelectionModel().select(0);
    }
  }

  private void bindTable() {
  
    statusColumn.setCellValueFactory(cellData -> {
      DirectB2SEntryModel backglass = cellData.getValue();
      if (!backglass.isVpxAvailable()) {
        Label icon = new Label();
        icon.setTooltip(new Tooltip("The backglass file \"" + backglass.getName() + "\n has no matching VPX file."));
        icon.setGraphic(WidgetFactory.createExclamationIcon());
        return new SimpleObjectProperty<Object>(icon);
      }
      // else
      return new SimpleObjectProperty<Object>(WidgetFactory.createCheckIcon());
    });

    displayNameColumn.setCellValueFactory(cellData -> {
      DirectB2SEntryModel backglass = cellData.getValue();
      Label label = new Label(backglass.getName());
      label.getStyleClass().add("default-text");
      return new SimpleObjectProperty<Object>(label);
    });

    fullDmdColumn.setCellValueFactory(cellData -> {
      return cellData.getValue();
    });
    fullDmdColumn.setCellFactory(cellData -> {
      return new LoadingCheckTableCell() {
        @Override
        protected String getLoading(DirectB2SEntryModel model) {
          return "loading...";
        }
        @Override
        protected boolean isChecked(DirectB2SEntryModel model) {
          return isFullDmd(model.dmdWidth, model.dmdHeight);
        }
        @Override
        protected String getTooltip(DirectB2SEntryModel model) {
          return "Full DMD backglass, resolution " +model.dmdWidth + "x" + model.dmdHeight;
        }
      };
    });

    grillColumn.setCellValueFactory(cellData -> {
      return cellData.getValue();
    });
    grillColumn.setCellFactory(cellData -> {
      return new LoadingCheckTableCell() {
        @Override
        protected String getLoading(DirectB2SEntryModel model) {
          return "";
        }
        @Override
        protected boolean isChecked(DirectB2SEntryModel model) {
          return model.grillHeight>0;
        }
        @Override
        protected String getTooltip(DirectB2SEntryModel model) {
          return "Grill Height set to " +model.grillHeight;
        }
      };
    });

  }

  private boolean isFullDmd(double imageWidth, double imageHeight) {
    double ratio = imageWidth / imageHeight;
    return ratio < 3.0;
  }

  private List<DirectB2SEntryModel> filterEntries(List<DirectB2SEntryModel> backglasses) {
    List<Integer> emuIds = new ArrayList<>();
    ObservableList<MenuItem> items = this.filterButton.getItems();
    for (MenuItem item : items) {
      CheckBox checkBox = (CheckBox) ((CustomMenuItem) item).getContent();
      GameEmulatorRepresentation emulatorRepresentation = (GameEmulatorRepresentation) checkBox.getUserData();
      if (checkBox.isSelected()) {
        emuIds.add(emulatorRepresentation.getId());
      }
    }

    filterButton.getStyleClass().remove("filter-button-selected");
    if (emuIds.size() != client.getPinUPPopperService().getVpxGameEmulators().size()) {
      filterButton.getStyleClass().add("filter-button-selected");
      filterButton.setGraphic(WidgetFactory.createIcon("mdi2f-filter-menu"));
    }
    else {
      filterButton.setGraphic(WidgetFactory.createIcon("mdi2f-filter-menu-outline"));
    }

    List<DirectB2SEntryModel> filtered = new ArrayList<>();
    for (DirectB2SEntryModel backglass : backglasses) {
      if (!emuIds.contains(backglass.getEmulatorId())) {
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
    downloadBackglassBtn.setDisable(true);
    downloadDMDBtn.setDisable(true);
    resolutionLabel.setText("");
    dmdResolutionLabel.setText("");
    fullDmdLabel.setText("");
    gameLabel.setText("-");
    gameFilenameLabel.setText("-");
    game = null;


    DirectB2STableSettings tmpTableSettings;
    if (newValue != null) {
      try {
        this.tableData = client.getBackglassServiceClient().getDirectB2SData(newValue);
      } catch (Exception e) {
        this.tableData = new DirectB2SData();
      }

      if (this.tableData.getGameId() > 0) {
        tmpTableSettings = client.getBackglassServiceClient().getTableSettings(this.tableData.getGameId());
        game = client.getGame(this.tableData.getGameId());
        gameLabel.setText(game.getGameDisplayName());
        gameFilenameLabel.setText(game.getGameFileName());
        dataManagerBtn.setDisable(false);
        modificationDateLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tableData.getModificationDate()));
      }
      else {
        //VPX is not installed, but available!
        if (newValue.isVpxAvailable()) {
          gameLabel.setText("?");
          gameFilenameLabel.setText("(Available, but not installed)");
        }
        tmpTableSettings = null;
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

      hideGrill.setDisable(tableData.getGrillHeight() == 0);

      byte[] bytesEncoded = org.apache.commons.codec.binary.Base64.decodeBase64(tableData.getBackgroundBase64());
      if (bytesEncoded != null) {
        Image image = new Image(new ByteArrayInputStream(bytesEncoded));
        if (tableData.getGrillHeight() > 0 && tmpTableSettings != null && tmpTableSettings.getHideGrill() == 1) {
          PixelReader reader = image.getPixelReader();
          image = new WritableImage(reader, 0, 0, (int) image.getWidth(), (int) (image.getHeight() - tableData.getGrillHeight()));
        }
        thumbnailImage.setImage(image);
        downloadBackglassBtn.setDisable(false);
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
        downloadDMDBtn.setDisable(false);
        dmdResolutionLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
        fullDmdLabel.setText(isFullDmd(image.getWidth(), image.getHeight()) ? "Yes": "No");
      }
      else {
        dmdResolutionLabel.setText("No DMD background available.");
        fullDmdLabel.setText("No");
      }

      hideGrill.setDisable(tmpTableSettings == null);
      hideB2SDMD.setSelected(false);
      hideB2SDMD.setDisable(tmpTableSettings == null);
      hideDMD.setDisable(tmpTableSettings == null);
      skipLampFrames.getValueFactory().setValue(0);
      skipLampFrames.setDisable(tmpTableSettings == null || tableData.getIlluminations() == 0);
      skipGIFrames.getValueFactory().setValue(0);
      skipGIFrames.setDisable(tmpTableSettings == null || tableData.getIlluminations() == 0);
      skipSolenoidFrames.getValueFactory().setValue(0);
      skipSolenoidFrames.setDisable(tmpTableSettings == null || tableData.getIlluminations() == 0);
      skipLEDFrames.getValueFactory().setValue(0);
      skipLEDFrames.setDisable(tmpTableSettings == null || tableData.getIlluminations() == 0 || usedLEDType.getValue() == null || usedLEDType.getValue().getId() == 2);
      lightBulbOn.setSelected(false);
      lightBulbOn.setDisable(tmpTableSettings == null || (usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 2));
      glowing.setDisable(tmpTableSettings == null || (usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 2));
      usedLEDType.setDisable(tmpTableSettings == null);
      startBackground.setSelected(false);
      startBackground.setDisable(tmpTableSettings == null);
      bringBGFromTop.setSelected(false);
      bringBGFromTop.setDisable(tmpTableSettings == null);

      if (tmpTableSettings != null) {
        startAsExe.setSelected(tmpTableSettings.isStartAsEXE());
        hideGrill.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tmpTableSettings.getHideGrill()).findFirst().get());
        hideB2SDMD.selectedProperty().setValue(tmpTableSettings.isHideB2SDMD());
        hideDMD.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tmpTableSettings.getHideDMD()).findFirst().get());
        skipLampFrames.getValueFactory().valueProperty().set(tmpTableSettings.getLampsSkipFrames());
        skipGIFrames.getValueFactory().valueProperty().set(tmpTableSettings.getGiStringsSkipFrames());
        skipSolenoidFrames.getValueFactory().valueProperty().set(tmpTableSettings.getSolenoidsSkipFrames());
        skipLEDFrames.getValueFactory().valueProperty().set(tmpTableSettings.getLedsSkipFrames());
        lightBulbOn.selectedProperty().setValue(tmpTableSettings.isGlowBulbOn());
        glowing.setValue(TablesSidebarDirectB2SController.GLOWINGS.stream().filter(v -> v.getId() == tmpTableSettings.getGlowIndex()).findFirst().get());
        usedLEDType.setValue(TablesSidebarDirectB2SController.LED_TYPES.stream().filter(v -> v.getId() == tmpTableSettings.getUsedLEDType()).findFirst().get());
        startBackground.selectedProperty().setValue(tmpTableSettings.isStartBackground());
        bringBGFromTop.selectedProperty().setValue(tmpTableSettings.isFormToFront());
      }

      this.tableSettings = tmpTableSettings;

      setSaveEnabled(true);
    }
    else {
      tmpTableSettings = null;
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
          Platform.runLater(() -> {
            this.refresh(this.directb2sList.getSelectionModel().getSelectedItem().backglass);
          });
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

  //------------------------------------------------

  /**
   * Background loader
   */
  private final Executor executor = Executors.newFixedThreadPool(10, runnable -> {
      Thread t = new Thread(runnable);
      t.setDaemon(true);
      return t ;
  });

  private class DirectB2SEntryModel extends ObjectPropertyBase<DirectB2SEntryModel> {

    DirectB2S backglass;

    private boolean loadRequested;
    private boolean loaded;

    private int dmdWidth;
    private int dmdHeight;
    private int grillHeight;

    private DirectB2SEntryModel(DirectB2S backglass) {
      this.backglass = backglass;
      // 
      set(this);
    }

    public void load() throws Exception {
      DirectB2SData backglassData = client.getBackglassServiceClient().getDirectB2SData(backglass);
  
      this.grillHeight = backglassData.getGrillHeight();

      if (StringUtils.isNotEmpty(backglassData.getDmdBase64())) {
        byte[] bytesEncoded = org.apache.commons.codec.binary.Base64.decodeBase64(backglassData.getDmdBase64());
        if (bytesEncoded != null) {
          Image image = new Image(new ByteArrayInputStream(bytesEncoded));
          this.dmdWidth = (int) image.getWidth();
          this.dmdHeight = (int) image.getHeight();
        }
      }
    }

    @Override
    public DirectB2SEntryModel getBean() {
      return null;
    }

    /**
     * Whether or not the value has been loaded. 
     */
    public final boolean isLoaded() {
      if (!loadRequested) {
        loadRequested = true ;
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
              load();
              return true;
            }
        };
        task.setOnSucceeded(e -> {
            loaded = true;
            fireValueChangedEvent();
        });
        // do something more sensible here..
        task.setOnFailed(e -> {
          loaded = true;
          fireValueChangedEvent();
      });
        executor.execute(task);
      }
      return loaded;
    }

    public int getEmulatorId() {
      return backglass.getEmulatorId();
    }
    public String getName() {
      return backglass.getName();
    }
    public boolean isVpxAvailable() {
      return backglass.isVpxAvailable();
    }

  }

  private static abstract class LoadingCheckTableCell extends TableCell<DirectB2SEntryModel, DirectB2SEntryModel> {

    protected abstract String getLoading(DirectB2SEntryModel model);

    /** should return true if the checked mark is visible. Model is never null */
    protected abstract boolean isChecked(DirectB2SEntryModel model);

    /** should return a contextualized tooltip for given model. Model is never null */
    protected abstract String getTooltip(DirectB2SEntryModel model);

    @Override
    protected void updateItem(DirectB2SEntryModel model, boolean empty) {
      super.updateItem(model, empty);
      if (empty || model==null) {
          setText("");
          setTooltip(null);
          setGraphic(null);
      } 
      else if (model.isLoaded()) {
        if (isChecked(model)) {
          setText(null);
          setTooltip(new Tooltip(getTooltip(model)));
          setGraphic(WidgetFactory.createCheckboxIcon());
        }
        else {
          setText("");
          setTooltip(null);
          setGraphic(null);
        }
      }
      else {
        setText(getLoading(model));
        setTooltip(null);
        setGraphic(null);
      }
    }
  };

}
