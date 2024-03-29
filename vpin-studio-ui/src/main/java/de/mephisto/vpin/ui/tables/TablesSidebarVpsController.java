package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.tables.vps.VpsDBDownloadProgressModel;
import de.mephisto.vpin.ui.tables.vps.VpsEntry;
import de.mephisto.vpin.ui.tables.vps.VpsEntryComment;
import de.mephisto.vpin.ui.tables.vps.VpsTableVersionCell;
import de.mephisto.vpin.ui.util.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarVpsController implements Initializable, AutoCompleteTextFieldChangeListener, ChangeListener<VpsTableVersion>, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarVpsController.class);

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  @FXML
  private VBox detailsBox;

  @FXML
  private VBox dataRoot;

  @FXML
  private TextField nameField;

  @FXML
  private Label yearLabel;

  @FXML
  private Label manufacturerLabel;

  @FXML
  private Label playersLabel;

  @FXML
  private Label updatedLabel;

  @FXML
  private ComboBox<VpsTableVersion> tableVersionsCombo;

  @FXML
  private CheckBox filterCheckbox;

  @FXML
  private Button openTableBtn;

  @FXML
  private Button copyTableBtn;

  @FXML
  private Button copyTableVersionBtn;

  @FXML
  private Button openTableVersionBtn;

  @FXML
  private SplitMenuButton autoFillBtn;

  @FXML
  private Label entriesLabel;

  @FXML
  private Label versionAuthorsLabel;

  @FXML
  private Label updateDateLabel;

  @FXML
  private VBox errorBox;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorText;

  @FXML
  private Button vpsResetBtn;

  @FXML
  private Hyperlink ipdbLink;
  private AutoCompleteTextField autoCompleteNameField;

  private ValidationState validationState;
  private ServerSettings serverSettings;


  // Add a public no-args constructor
  public TablesSidebarVpsController() {
  }


  @FXML
  private void onCopyTable() {
    if (!this.game.isEmpty()) {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      ClipboardContent content = new ClipboardContent();
      String vpsTableUrl = VPS.getVpsTableUrl(this.game.get().getExtTableId());
      content.putString(vpsTableUrl);
      clipboard.setContent(content);
    }
  }

  @FXML
  private void onCopyTableVersion() {
    if (!this.game.isEmpty()) {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      ClipboardContent content = new ClipboardContent();
      content.putString(VPS.getVpsTableUrl(this.game.get().getExtTableId(), this.game.get().getExtTableVersionId()));
      clipboard.setContent(content);
    }
  }


  @FXML
  private void onAutoMatch() {
    if (this.game.isPresent()) {
      TableDialogs.openAutoMatch(this.game.get());
    }
  }

  @FXML
  private void onAutoMatchAll() {
    TableDialogs.openAutoMatchAll();
  }

  @FXML
  private void onDismiss() {
    if (validationState != null) {
      GameRepresentation g = game.get();
      DismissalUtil.dismissValidation(g, this.validationState);
    }
  }

  @FXML
  private void onOpen() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI(VPS.getVpsTableUrl(game.get().getExtTableId())));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
        ipdbLink.setDisable(true);
      }
    }
  }

  @FXML
  private void onTableOpen() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        VpsTableVersion value = this.tableVersionsCombo.getValue();
        VpsUrl vpsUrl = value.getUrls().get(0);
        desktop.browse(new URI(vpsUrl.getUrl()));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
        ipdbLink.setDisable(true);
      }
    }
  }

  @FXML
  private void onVpsReset() {
    if (!this.game.isEmpty()) {
      GameRepresentation gameRepresentation = this.game.get();
      TableActions.onVpsReset(Arrays.asList(gameRepresentation));
    }
  }

  @FXML
  private void onIpdbLink() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI(ipdbLink.getText()));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
        ipdbLink.setDisable(true);
      }
    }
  }

  @FXML
  private void onUpdate() {
    ProgressDialog.createProgressDialog(new VpsDBDownloadProgressModel("Download VPS Database", Arrays.asList(VPS.getInstance().getVpsDbFile())));
    List<VpsTable> tables = VPS.getInstance().getTables();
    refreshSheetData(tables);
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.tableVersionsCombo.valueProperty().removeListener(this);
    this.game = game;
    this.refreshView(game);
    this.tableVersionsCombo.valueProperty().addListener(this);
  }

  /**
   * Table name change
   *
   * @param value
   */
  @Override
  public void onChange(String value) {
    try {
      this.tableVersionsCombo.valueProperty().removeListener(this);
      List<VpsTable> tables = VPS.getInstance().getTables();
      Optional<VpsTable> selectedEntry = tables.stream().filter(t -> t.getDisplayName().equalsIgnoreCase(value)).findFirst();
      if (selectedEntry.isPresent()) {
        GameRepresentation gameRepresentation = this.game.get();
        VpsTable vpsTable = selectedEntry.get();

        TableDetails tableDetails = client.getPinUPPopperService().getTableDetails(gameRepresentation.getId());
        tableDetails.setMappedValue(serverSettings.getMappingVpsTableId(), vpsTable.getId());
        client.getPinUPPopperService().saveTableDetails(tableDetails, gameRepresentation.getId());
      }
      this.tableVersionsCombo.valueProperty().addListener(this);
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
    } catch (Exception e) {
      LOG.error("Failed to save updated VPS data: " + e.getMessage(), e);
    }
  }


  public void refreshView(Optional<GameRepresentation> g) {
    errorBox.setVisible(false);

    dataRoot.getChildren().removeAll(dataRoot.getChildren());

    autoCompleteNameField.reset();
    autoCompleteNameField.setDisable(g.isEmpty());
    tableVersionsCombo.setDisable(g.isEmpty());
    tableVersionsCombo.setValue(null);


    versionAuthorsLabel.setText("-");
    versionAuthorsLabel.setTooltip(null);
    yearLabel.setText("-");
    manufacturerLabel.setText("-");
    playersLabel.setText("-");
    updatedLabel.setText("-");
    ipdbLink.setText("");
    openTableVersionBtn.setDisable(true);
    copyTableBtn.setDisable(true);
    copyTableVersionBtn.setDisable(true);
    autoFillBtn.setDisable(g.isEmpty());
    vpsResetBtn.setDisable(true);

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      vpsResetBtn.setDisable(game.getVpsUpdates().isEmpty());

      String vpsTableId = game.getExtTableId();
      String vpsTableVersionId = game.getExtTableVersionId();

      if (StringUtils.isEmpty(vpsTableId) || StringUtils.isEmpty(vpsTableVersionId)) {
        PreferenceEntryRepresentation entry = Studio.client.getPreference(PreferenceNames.IGNORED_VALIDATIONS);
        List<String> csvValue = entry.getCSVValue();
        if (!csvValue.contains(String.valueOf(GameValidationCode.CODE_VPS_MAPPING_MISSING))) {
          errorBox.setVisible(true);
          validationState = new ValidationState();
          validationState.setCode(GameValidationCode.CODE_VPS_MAPPING_MISSING);
          LocalizedValidation validationResult = GameValidationTexts.getValidationResult(game, validationState);
          errorTitle.setText(validationResult.getLabel());
          errorText.setText(validationResult.getText());
        }
      }

      openTableVersionBtn.setDisable(StringUtils.isEmpty(vpsTableId));
      openTableVersionBtn.setDisable(StringUtils.isEmpty(vpsTableVersionId));
      copyTableBtn.setDisable(StringUtils.isEmpty(vpsTableId));
      copyTableVersionBtn.setDisable(StringUtils.isEmpty(vpsTableVersionId));

      if (!StringUtils.isEmpty(vpsTableId)) {
        VpsTable tableById = VPS.getInstance().getTableById(vpsTableId);
        if (tableById != null) {
          refreshTableView(tableById);
          if (!StringUtils.isEmpty(vpsTableVersionId)) {
            VpsTableVersion version = tableById.getVersion(vpsTableVersionId);
            tableVersionsCombo.setValue(version);
          }
        }
      }
    }
  }

  private void refreshTableView(VpsTable vpsTable) {
    versionAuthorsLabel.setText("-");
    versionAuthorsLabel.setTooltip(new Tooltip(null));
    List<VpsTableVersion> tableFiles = new ArrayList<>(vpsTable.getTableFilesForFormat(VpsFeatures.VPX));
    if (!tableFiles.isEmpty()) {
      tableVersionsCombo.setItems(FXCollections.emptyObservableList());
      tableFiles.add(0, null);
      tableVersionsCombo.setItems(FXCollections.observableList(tableFiles));
      String extTableVersionId = game.get().getExtTableVersionId();

      if (!StringUtils.isEmpty(extTableVersionId)) {
        for (VpsTableVersion tableVersion : tableFiles) {
          if (tableVersion != null && tableVersion.getId().equals(extTableVersionId)) {
            tableVersionsCombo.setValue(tableVersion);
            if (tableVersion.getAuthors() != null) {
              versionAuthorsLabel.setText(String.join(", ", tableVersion.getAuthors()));
              versionAuthorsLabel.setTooltip(new Tooltip(String.join(", ", tableVersion.getAuthors())));
            }
            break;
          }
        }
      }

      Platform.runLater(() -> {
        tableVersionsCombo.show();
        tableVersionsCombo.hide();
      });
    }
    else {
      tableVersionsCombo.setItems(FXCollections.emptyObservableList());
    }

    autoCompleteNameField.setText(vpsTable.getDisplayName());
    yearLabel.setText(String.valueOf(vpsTable.getYear()));
    manufacturerLabel.setText(vpsTable.getManufacturer());
    playersLabel.setText(String.valueOf(vpsTable.getPlayers()));
    ipdbLink.setText(vpsTable.getIpdbUrl());
    ipdbLink.setDisable(StringUtils.isEmpty(vpsTable.getIpdbUrl()) || !vpsTable.getIpdbUrl().startsWith("http"));
    updatedLabel.setText(DateFormat.getDateInstance().format(new Date(vpsTable.getUpdatedAt())));

    boolean doFilter = filterCheckbox.isSelected();

    if (!doFilter || game.get().getPupPackName() != null) {
      addSection(dataRoot, "PUP Pack", game.get(), VpsDiffTypes.pupPack, vpsTable.getPupPackFiles());
    }

    if (!doFilter || !game.get().isDirectB2SAvailable()) {
      addSection(dataRoot, "Backglasses", game.get(), VpsDiffTypes.b2s, vpsTable.getB2sFiles());
    }

    if (!doFilter || !game.get().isAltSoundAvailable()) {
      addSection(dataRoot, "ALT Sound", game.get(), VpsDiffTypes.altSound, vpsTable.getAltSoundFiles());
    }

    addSection(dataRoot, "ALT Color", game.get(), VpsDiffTypes.altColor, vpsTable.getAltColorFiles());

    if (!doFilter || !game.get().isRomExists()) {
      addSection(dataRoot, "ROM", game.get(), VpsDiffTypes.rom, vpsTable.getRomFiles());
    }

    addSection(dataRoot, "Sound", game.get(), VpsDiffTypes.sound, vpsTable.getSoundFiles());

    GameMediaRepresentation gameMedia = game.get().getGameMedia();
    List<GameMediaItemRepresentation> items = gameMedia.getMediaItems(PopperScreen.Topper);
    if (!doFilter || items.isEmpty()) {
      addSection(dataRoot, "Topper", game.get(), VpsDiffTypes.topper, vpsTable.getTopperFiles());
    }

    items = gameMedia.getMediaItems(PopperScreen.Wheel);
    if (!doFilter || items.isEmpty()) {
      addSection(dataRoot, "Wheel Art", game.get(), VpsDiffTypes.wheel, vpsTable.getWheelArtFiles());
    }

    if (!doFilter || !game.get().isPovAvailable()) {
      addSection(dataRoot, "POV", game.get(), VpsDiffTypes.pov, vpsTable.getPovFiles());
    }

    TablesSidebarVpsController.addTutorialsSection(dataRoot, "Tutorials", game.get(), vpsTable.getTutorialFiles());
  }

  public static void addSection(VBox dataRoot, String title, GameRepresentation game, VpsDiffTypes diffTypes, List<? extends VpsAuthoredUrls> urls) {
    if (urls == null || urls.isEmpty()) {
      return;
    }

    List<Node> entries = new ArrayList<>();
    for (VpsAuthoredUrls authoredUrl : urls) {
      List<VpsUrl> authoredUrlUrls = authoredUrl.getUrls();
      if (authoredUrlUrls != null && !authoredUrlUrls.isEmpty()) {
        String version = authoredUrl.getVersion();
        long updatedAt = authoredUrl.getUpdatedAt();
        List<String> authors = authoredUrl.getAuthors();

        for (VpsUrl vpsUrl : authoredUrlUrls) {
          String url = vpsUrl.getUrl();
          if (StringUtils.isEmpty(url)) {
            continue;
          }

          if (vpsUrl.isBroken()) {
            continue;
          }

          String updateText = null;
          if(game != null) {
            List<VPSChange> changes = game.getVpsUpdates().getChanges();
            for (VPSChange change : changes) {
              if (change.getId() != null && authoredUrl.getId() != null && change.getId().equals(authoredUrl.getId())) {
                updateText = change.toString(game.getExtTableId());
                break;
              }
            }
          }
          entries.add(new VpsEntry(version, authors, url, updatedAt, updateText));
        }

        if (authoredUrl instanceof VpsBackglassFile) {
          VpsBackglassFile backglassFile = (VpsBackglassFile) authoredUrl;
          if (!StringUtils.isEmpty(backglassFile.getComment())) {
            entries.add(new VpsEntryComment(backglassFile.getComment()));
          }
        }
      }
    }

    if (!entries.isEmpty()) {
      addSectionHeader(dataRoot, title);
      dataRoot.getChildren().addAll(entries);
    }
  }

  public static void addTutorialsSection(VBox dataRoot, String title, GameRepresentation game, List<VpsTutorialUrls> urls) {
    if (urls == null || urls.isEmpty()) {
      return;
    }

    List<Node> entries = new ArrayList<>();
    for (VpsTutorialUrls authoredUrl : urls) {
      String youtubeId = authoredUrl.getYoutubeId();
      if (youtubeId != null) {
        String version = authoredUrl.getVersion();
        long updatedAt = authoredUrl.getUpdatedAt();
        List<String> authors = authoredUrl.getAuthors();

        String url = "https://www.youtube.com/watch?v=" + youtubeId;

        String updateText = null;
        if(game != null) {
          List<VPSChange> changes = game.getVpsUpdates().getChanges();
          for (VPSChange change : changes) {
            if (change.getId() != null && authoredUrl.getId() != null && change.getId().equals(authoredUrl.getId())) {
              updateText = change.toString(game.getExtTableId());
              break;
            }
          }
        }
        entries.add(new VpsEntry(version, authors, url, updatedAt, updateText));
      }
    }


    if (!entries.isEmpty()) {
      addSectionHeader(dataRoot, title);
      dataRoot.getChildren().addAll(entries);
    }
  }

  private static void addSectionHeader(VBox dataRoot, String title) {
    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarVpsController.class.getResource("section-vps.fxml"));
      Pane section = loader.load();
      Label label = (Label) section.getChildren().get(0);
      label.setText(title);
      dataRoot.getChildren().add(section);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  private void refreshSheetData(List<VpsTable> tables) {
    entriesLabel.setText(String.valueOf(tables.size()));
    Date changeDate = VPS.getInstance().getChangeDate();
    updateDateLabel.setText(DateFormat.getDateTimeInstance().format(changeDate));
  }

  @Override
  public void changed(ObservableValue<? extends VpsTableVersion> observable, VpsTableVersion oldValue, VpsTableVersion newValue) {
    openTableVersionBtn.setDisable(newValue == null || newValue.getUrls().isEmpty());
    copyTableVersionBtn.setDisable(newValue == null);

    if (this.game.isPresent()) {
      try {
        GameRepresentation gameRepresentation = this.game.get();
        TableDetails tableDetails = client.getPinUPPopperService().getTableDetails(gameRepresentation.getId());
        copyTableVersionBtn.setDisable(newValue == null);

        String updatedId = null;
        if (newValue != null) {
          updatedId = newValue.getId();
        }
        String oldVersionValue = tableDetails.getMappedValue(serverSettings.getMappingVpsTableVersionId());
        if (!String.valueOf(oldValue).equals(oldVersionValue)) {
          tableDetails.setMappedValue(serverSettings.getMappingVpsTableVersionId(), updatedId);
          client.getPinUPPopperService().saveTableDetails(tableDetails, gameRepresentation.getId());
          EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
        }
      } catch (Exception e) {
        LOG.error("Failed to save VPS version: " + e.getMessage(), e);
      }
    }

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    preferencesChanged(PreferenceNames.SERVER_SETTINGS, null);

    vpsResetBtn.managedProperty().bindBidirectional(vpsResetBtn.visibleProperty());
    detailsBox.managedProperty().bindBidirectional(detailsBox.visibleProperty());
    dataRoot.managedProperty().bindBidirectional(dataRoot.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());

    vpsResetBtn.setVisible(false);
    openTableVersionBtn.setDisable(true);
    copyTableBtn.setDisable(true);
    copyTableVersionBtn.setDisable(true);

    tableVersionsCombo.setCellFactory(c -> new VpsTableVersionCell());
    tableVersionsCombo.setButtonCell(new VpsTableVersionCell());

    filterCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      this.tableVersionsCombo.valueProperty().removeListener(this);
      refreshView(game);
      this.tableVersionsCombo.valueProperty().addListener(this);
    });

    List<VpsTable> tables = VPS.getInstance().getTables();
    refreshSheetData(tables);
    TreeSet<String> collect = new TreeSet<>(tables.stream().map(t -> t.getDisplayName()).collect(Collectors.toSet()));
    autoCompleteNameField = new AutoCompleteTextField(this.nameField, this, collect);

    Image image2 = new Image(Studio.class.getResourceAsStream("vps-checked.png"));
    ImageView view2 = new ImageView(image2);
    view2.setFitWidth(18);
    view2.setFitHeight(18);
    vpsResetBtn.setGraphic(view2);

    preferencesChanged(PreferenceNames.UI_SETTINGS, null);
    client.getPreferenceService().addListener(this);
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.UI_SETTINGS)) {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      this.vpsResetBtn.setVisible(!uiSettings.isHideVPSUpdates());
    }
    else if (key.equals(PreferenceNames.SERVER_SETTINGS)) {
      serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    }
  }
}