package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.backglassmanager.dialogs.BackglassManagerDialogs;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.tables.validation.BackglassValidationTexts;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.SystemUtil;
import de.mephisto.vpin.ui.util.WaitProgressModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.*;

/**
 *
 */
public class BackglassManagerController extends BaseTableController<DirectB2S, DirectB2SModel>
    implements Initializable, StudioFXController, StudioEventListener {

  private final static Logger LOG = LoggerFactory.getLogger(BackglassManagerController.class);
  public static Debouncer debouncer = new Debouncer();


  @FXML
  private Button resBtn;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button openBtn;

  @FXML
  private Button renameBtn;

  @FXML
  private Button vpsOpenBtn;

  @FXML
  private Button dmdPositionBtn;

  @FXML
  private SplitPane splitPane;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> statusColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> displayNameColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> numberDirectB2SColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> fullDmdColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> grillColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> scoreColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> resColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> frameColumn;

  //-------------

  @FXML
  private BorderPane validationError;
  @FXML
  private Label validationErrorLabel;
  @FXML
  private Label validationErrorText;

  //-------------

  @FXML
  private BackglassManagerSidebarController backglassManagerSideBarController; //fxml magic! Not unused

  //-------------
  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
        onDMDPosition();
      }
    }
  }

  @FXML
  private void onDMDPosition() {
    GameRepresentation game = getGameFromSelection();
    if (game != null) {
      LOG.info("open DMD Position dialog for game " + game.getId());
      TableDialogs.openDMDPositionDialog(game, this);
    }
  }

  @FXML
  private void onUpload(ActionEvent e) {
    GameRepresentation game = getGameFromSelection();
    if (game != null) {
      TableDialogs.openDirectb2sUploads(game, null, this::reloadSelection);
    }
  }

  @FXML
  public void onOpenTable(ActionEvent e) {
    GameRepresentation game = getGameFromSelection();
    if (game != null) {
      NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(game.getId()));
    }
  }

  @FXML
  public void onBackglassReload(ActionEvent e) {
    DirectB2SModel selectedModel = getSelectedModel();
    DirectB2S versions = client.getBackglassServiceClient().reloadDirectB2S(selectedModel.getEmulatorId(), selectedModel.getFileName());
    selectedModel.setBean(versions);
    reloadSelection();
  }

  @FXML
  private void onResEdit(ActionEvent e) {
    DirectB2S selection = getSelection();
    if (selection != null) {
      BackglassManagerDialogs.openResGenerator(selection.getEmulatorId(), selection.getFileName());
    }
  }

  @FXML
  protected void onTableDataManager(ActionEvent e) {
    GameRepresentation game = getGameFromSelection();
    if (game != null) {
      TableDialogs.openTableDataDialog(tablesController.getTableOverviewController(), game);
    }
  }

  @FXML
  protected void onRename(ActionEvent e) {
    DirectB2SModel selection = getSelectedModel();
    if (selection != null) {
      //Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      String newName = WidgetFactory.showInputDialog(Studio.stage, "Rename Backglass", "Enter new name for backglass file \"" + selection.getFileName() + "\"", "The renaming will include all versions of the backglass too.", null, selection.getName());
      if (newName != null) {
        if (!FileUtils.isValidFilename(newName)) {
          WidgetFactory.showAlert(stage, "Invalid Filename", "The specified file name contains invalid characters.");
          return;
        }

        try {
          if (!newName.endsWith(".directb2s")) {
            newName = newName + ".directb2s";
          }
          DirectB2S b2s = client.getBackglassServiceClient().renameBackglass(selection.getEmulatorId(), selection.getFileName(), newName);
          if (b2s != null) {
            selection.setBean(b2s);
            reloadSelection();
            applyFilter();

            // then notify changes
            if (selection.getGameId() > 0) {
              EventManager.getInstance().notifyTableChange(selection.getGameId(), null);
            }
          }
        }
        catch (Exception ex) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to dupliate backglass: " + ex.getMessage());
        }
      }
    }
  }

  @FXML
  private void onOpen() {
    DirectB2S selectedItem = getSelection();
    if (selectedItem != null) {
      GameEmulatorRepresentation emulatorRepresentation = client.getEmulatorService().getGameEmulator(selectedItem.getEmulatorId());
      File folder = new File(emulatorRepresentation.getGamesDirectory());
      File file = new File(folder, selectedItem.getFileName());
      if (file.exists()) {
        SystemUtil.openFile(file);
      }
      else {
        SystemUtil.openFolder(file.getParentFile());
      }
    }
  }

  @FXML
  private void onVpsOpen() {
    GameRepresentation game = getGameFromSelection();
    if (game != null) {
      VpsTable tableById = client.getVpsService().getTableById(game.getExtTableId());
      if (tableById != null) {
        Studio.browse(VPS.getVpsTableUrl(tableById.getId()));
      }
    }
  }

  @FXML
  private void onReload() {
    ProgressDialog.createProgressDialog(new WaitProgressModel<>("Invalidate Cache",
        "Invalidating Backglasses Cache...", () -> {
      client.getBackglassServiceClient().clearCache();
    }));
    doReload();
  }

  public void doReload() {
    startReload("Loading Backglasses...");

    refreshPlaylists();

    JFXFuture.supplyAsync(() -> client.getBackglassServiceClient().getBackglasses()).thenAcceptLater(data -> {
      setItems(data);
      endReload();
    });
  }

  @FXML
  private void onValidationSettings() {
    PreferencesController.open("validators_backglass");
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("backglass", "backglasses", new BackglassManagerColumnSorter(this));

    resBtn.managedProperty().bindBidirectional(resBtn.visibleProperty());

    // reference the controllers
    backglassManagerSideBarController.setBackglassManagerController(this);
    backglassManagerSideBarController.setRootController(tablesController);

    resBtn.setVisible(Features.RES_EDITOR);
    EventManager.getInstance().addListener(this);

    this.clearBtn.setVisible(false);

    this.openBtn.setVisible(client.getSystemService().isLocal());

    bindTable();

    super.loadFilterPanel("scene-directb2s-admin-filter.fxml");

    super.loadPlaylistCombo();

    // Install the handler for backglass selection
    this.tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      refreshView(newValue);
    });

    // add the overlay for drag and drop
    new BackglassManagerDragDropHandler(this, tableView, tableStack);
  }

  @Override
  public void onViewDeactivated() {
    double position = splitPane.getDividers().get(0).getPosition();
    LocalUISettings.saveProperty("backglassesDivider", String.valueOf(position));
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList("Backglasses"));

    // first time activation
    if (models == null || models.isEmpty()) {
      doReload();

      if (this.tableView.getItems().isEmpty()) {
        clearSelection();
      }
      else {
        this.tableView.getSelectionModel().select(0);
      }
      refreshView(null);
    }
    reloadSelection();

    String backglassesDivider = LocalUISettings.getString("backglassesDivider");
    if (!StringUtils.isEmpty(backglassesDivider)) {
      splitPane.getDividers().get(0).setPosition(Double.parseDouble(backglassesDivider));
    }
  }

  private void bindTable() {
    BaseLoadingColumn.configureLoadingColumn(statusColumn, "", (value, model) -> {
      int validationCode = model.getValidationCode();
      if (validationCode > 0) {
        Label icon = new Label();
        icon.setTooltip(new Tooltip("The backglass file \"" + model.getName() + "\n has configuration issue(s)."));
        icon.setGraphic(WidgetFactory.createExclamationIcon(getIconColor(value)));
        return icon;
      }
      else if (validationCode == 0) {
        return WidgetFactory.createCheckIcon(getIconColor(value));
      }
      else {
        return null;
      }
    });

    BaseLoadingColumn.configureColumn(displayNameColumn, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(numberDirectB2SColumn, (value, model) -> {
      int nbVersions = value.getNbVersions();
      String iconname = null;
      if (nbVersions > 9) {
        iconname = "mdi2n-numeric-9-plus-box-multiple-outline";
      }
      else if (nbVersions > 1) {
        iconname = "mdi2n-numeric-" + nbVersions + "-box-multiple-outline";
      }
      if (iconname != null) {
        return WidgetFactory.createIcon(iconname, value.isEnabled() ? null : WidgetFactory.DISABLED_COLOR);
      }
      else {
        return new Label("");
      }
    }, this, true);

    BaseLoadingColumn.configureLoadingColumn(fullDmdColumn, cell -> new LoadingCheckTableCell() {
      @Override
      protected String getLoading(DirectB2SModel model) {
        return "loading...";
      }

      @Override
      protected CheckStyle isChecked(DirectB2SModel model) {
        return !model.hasDmd() ? CheckStyle.NONE :
            model.isFullDmd() ? CheckStyle.CHECKED :
                model.hasWrongFullDMDRatioError() ? CheckStyle.ERROR : CheckStyle.WARNING;
      }

      @Override
      protected String getTooltip(DirectB2SModel model) {
        return (model.isFullDmd() ?
            "Full DMD backglass" : "DMD backglass present, but not Full-DMD Aspect Ratio")
            + ", resolution " + model.getDmdWidth() + "x" + model.getDmdHeight();
      }
    });

    BaseLoadingColumn.configureLoadingColumn(grillColumn, cell -> new LoadingCheckTableCell() {
      @Override
      protected CheckStyle isChecked(DirectB2SModel model) {
        return model.getGrillHeight() > 0 ? CheckStyle.CHECKED : null;
      }

      @Override
      protected String getTooltip(DirectB2SModel model) {
        return "Grill Height set to " + model.getGrillHeight();
      }
    });

    BaseLoadingColumn.configureLoadingColumn(scoreColumn, cell -> new LoadingCheckTableCell() {
      @Override
      protected CheckStyle isChecked(DirectB2SModel model) {
        return model.getNbScores() > 0 ? CheckStyle.CHECKED : null;
      }

      @Override
      protected String getTooltip(DirectB2SModel model) {
        return model.getNbScores() > 0 ? "Backglass contains " + model.getNbScores() + " scores" : "";
      }
    });

    BaseLoadingColumn.configureLoadingColumn(resColumn, cell -> new LoadingCheckTableCell() {
      @Override
      protected CheckStyle isChecked(DirectB2SModel model) {
        return model.getResPath() != null ? CheckStyle.CHECKED : null;
      }

      @Override
      protected String getTooltip(DirectB2SModel model) {
        return model.getResPath() != null ? "Backglass uses a specific .res file: " + model.getResPath() : "";
      }
    });
    resColumn.setVisible(Features.RES_EDITOR);

    BaseLoadingColumn.configureLoadingColumn(frameColumn, cell -> new LoadingCheckTableCell() {
      @Override
      protected CheckStyle isChecked(DirectB2SModel model) {
        return model.getFramePath() != null ? CheckStyle.CHECKED : null;
      }

      @Override
      protected String getTooltip(DirectB2SModel model) {
        return model.getFramePath() != null ? "Backglass uses a background frame: " + model.getFramePath() : "";
      }
    });
    frameColumn.setVisible(Features.RES_EDITOR);
  }

  @Override
  protected void refreshView(@Nullable DirectB2SModel model) {
    if (model != null) {
      NavigationController.setBreadCrumb(Arrays.asList("Backglasses", model.getName()));
    }
    else {
      NavigationController.setBreadCrumb(Arrays.asList("Backglasses"));
    }

    setValidationVisible(false);
    validationErrorLabel.setText("");
    validationErrorText.setText("");

    this.dmdPositionBtn.setDisable(true);
    this.resBtn.setDisable(true);
    this.uploadBtn.setDisable(true);
    this.openBtn.setDisable(true);
    this.renameBtn.setDisable(true);
    this.vpsOpenBtn.setDisable(true);

    // load the sidebar
    backglassManagerSideBarController.setData(model != null ? model.getBean() : null);

    // empty game sidebar section while loading game
    backglassManagerSideBarController.resetGame();

    if (model != null) {
      JFXFuture
          .supplyAsync(() -> client.getGameService().getGame(model.getGameId()))
          .thenAcceptLater(game -> {
            // Ignore old answer when a new backglass has been selected
            if (game == null || model.getGameId() != game.getId()) {
              return;
            }

            if (game != null) {
              dmdPositionBtn.setDisable(false);
              resBtn.setDisable(false);
              uploadBtn.setDisable(false);
              openBtn.setDisable(false);
              renameBtn.setDisable(false);
              vpsOpenBtn.setDisable(client.getVpsService().getTableById(game.getExtTableId()) == null);
            }
            // Pass Game to sidebar so that it also updates the Game section
            backglassManagerSideBarController.setGame(game, model.isGameAvailable());
          });

      int validationCode = model.getValidationCode();
      if (validationCode > 0) {
        setValidationVisible(true);
        LocalizedValidation validationMessage = BackglassValidationTexts.validate(validationCode, model);
        validationErrorLabel.setText(validationMessage.getLabel());
        validationErrorText.setText(validationMessage.getText());
      }
      else {
        setValidationVisible(false);
      }
    }
  }

  public void selectGame(@Nullable GameRepresentation game) {
    // try to select first backglass of the game, do the selection by name
    if (game != null) {
      String gameBaseName = FilenameUtils.getBaseName(game.getGameFileName());

      // at calling time, the list may not have been populated so register a listener in that case
      if (models != null && !models.isEmpty()) {
        selectGame(gameBaseName);
      }
      else {
        ChangeListener<ObservableList<DirectB2SModel>> listener = new ChangeListener<>() {
          @Override
          public void changed(ObservableValue<? extends ObservableList<DirectB2SModel>> observable,
                              ObservableList<DirectB2SModel> oldValue, ObservableList<DirectB2SModel> newValue) {
            selectGame(gameBaseName);
            tableView.itemsProperty().removeListener(this);
          }
        };
        this.tableView.itemsProperty().addListener(listener);
      }
    }
  }

  private void selectGame(String gameBaseName) {
    for (DirectB2SModel backglass : models) {
      if (StringUtils.startsWithIgnoreCase(backglass.getFileName(), gameBaseName)) {
        tableView.scrollTo(backglass);
        tableView.getSelectionModel().select(backglass);
        break;
      }
    }
  }

  public void setValidationVisible(boolean visible) {
    validationError.setVisible(visible);
  }

  //------------------------------------------------
  // Implementation of StudioEventListener

  @Override
  public void preferencesChanged(PreferenceType preferenceType) {
    if (preferenceType.equals(PreferenceType.serverSettings)) {
      Platform.runLater(() -> {
        this.doReload();
      });
    }
  }

  //------------------------------------------------
  // Implementation of StudioEventListener

  @Override
  public void tableChanged(int id, String rom, String gameName) {
    // tab should have been initialized to support reload
    if (id > 0 && models != null) {
      // When a game is updated or added, the associated backglass in table + view should be updated
      // If it is a new game, this backglass sis discovered and added into the table as a new row
      JFXFuture.supplyAsync(() -> client.getBackglassServiceClient().getDirectB2S(id))
          .thenAcceptLater(b2s -> {
            if (b2s != null) {
              reloadItem(b2s);
            }
            else {
              // detection of deletion for a table
              Optional<DirectB2SModel> model = models.stream().filter(m -> m.getGameId() == id).findFirst();
              if (model.isPresent()) {
                models.remove(model.get());
              }
            }
          });
    }
  }

  @Override
  public void backglassChanged(int emulatorId, String b2sFileName) {
    Optional<DirectB2SModel> model = models.stream().filter(m -> m.getEmulatorId() == emulatorId && StringUtils.equals(m.getFileName(), b2sFileName)).findFirst();
    if (model.isPresent()) {
      reloadItem(model.get().getBean());
    }
  }

  //------------------------------------------------

  private String getIconColor(DirectB2S value) {
    return value.isEnabled() ? null : WidgetFactory.DISABLED_COLOR;
  }

  private String getLabelCss(DirectB2S value) {
    return value.isEnabled() ? "" : WidgetFactory.DISABLED_TEXT_STYLE;
  }

  public void delete(DirectB2SModel selection) {
    // remove from the list if successfully deleted
    models.remove(selection);
    applyFilter();
    // then notify changes
    if (selection.getGameId() > 0) {
      EventManager.getInstance().notifyTableChange(selection.getGameId(), null);
    }
  }

  @Override
  protected DirectB2SModel toModel(DirectB2S b2s) {
    return new DirectB2SModel(b2s);
  }

  protected GameRepresentation getGameFromSelection() {
    DirectB2SModel selection = getSelectedModel();
    if (selection != null && selection.getGameId() > 0) {
      return client.getGameService().getGame(selection.getGameId());
    }
    return null;
  }
}
