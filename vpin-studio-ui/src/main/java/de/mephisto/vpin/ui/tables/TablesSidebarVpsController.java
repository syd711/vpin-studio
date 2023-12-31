package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.tables.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.tables.GameMediaRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
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

public class TablesSidebarVpsController implements Initializable, AutoCompleteTextFieldChangeListener, ChangeListener<VpsTableVersion> {
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
  private Button openBtn;

  @FXML
  private Button copyTableBtn;

  @FXML
  private Button copyTableVersionBtn;

  @FXML
  private SplitMenuButton autoFillBtn;

  @FXML
  private Button openTableBtn;

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
  private Hyperlink ipdbLink;
  private AutoCompleteTextField autoCompleteNameField;

  private ValidationState validationState;


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

//      Notifications.create()
//        .darkStyle()
//        .position(Pos.BOTTOM_LEFT)
//        .title("Copied VPS Table URL " + vpsTableUrl)
////        .graphic(new Rectangle(600, 400, Color.GREEN))
//        .hideAfter(Duration.seconds(3))
//        .show();
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
  private void onAutoFill() {
    ProgressDialog.createProgressDialog(new TableVpsDataAutoFillProgressModel(Arrays.asList(this.game.get()), true));
    EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
  }

  @FXML
  private void onAutoFillAll() {
    if (this.game.isPresent()) {
      ConfirmationResult result = WidgetFactory.showAlertOptionWithCheckbox(Studio.stage, "Auto-fill table type and version for all " + client.getGameService().getGamesCached().size() + " tables?",
        "Cancel", "Continue", "The tablename and display name is used to find the matching table.", "You may have to adept the result manually.", "Overwrite existing assignments", false);
      if (!result.isApplied()) {
        ProgressDialog.createProgressDialog(new TableVpsDataAutoFillProgressModel(client.getGameService().getGamesCached(), result.isChecked()));
        EventManager.getInstance().notifyTablesChanged();
      }
    }
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

  @Override
  public void onChange(String value) {
    this.tableVersionsCombo.valueProperty().removeListener(this);
    List<VpsTable> tables = VPS.getInstance().getTables();
    Optional<VpsTable> selectedEntry = tables.stream().filter(t -> t.getDisplayName().equalsIgnoreCase(value)).findFirst();
    if (selectedEntry.isPresent()) {
      VpsTable vpsTable = selectedEntry.get();
      client.getVpsService().saveTable(this.game.get().getId(), vpsTable.getId());
    }
    this.tableVersionsCombo.valueProperty().addListener(this);
    EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
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
    openBtn.setDisable(true);
    copyTableBtn.setDisable(true);
    copyTableVersionBtn.setDisable(true);
    autoFillBtn.setDisable(g.isEmpty());

    if (g.isPresent()) {
      GameRepresentation game = g.get();

      if (StringUtils.isEmpty(game.getExtTableId()) || StringUtils.isEmpty(game.getExtTableVersionId())) {
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

      openBtn.setDisable(StringUtils.isEmpty(game.getExtTableId()));
      openTableBtn.setDisable(StringUtils.isEmpty(game.getExtTableVersionId()));
      copyTableBtn.setDisable(StringUtils.isEmpty(game.getExtTableId()));
      copyTableVersionBtn.setDisable(StringUtils.isEmpty(game.getExtTableVersionId()));

      if (!StringUtils.isEmpty(game.getExtTableId())) {
        VpsTable tableById = VPS.getInstance().getTableById(game.getExtTableId());
        if (tableById != null) {
          refreshTableView(tableById);
          if (!StringUtils.isEmpty(game.getExtTableVersionId())) {
            VpsTableVersion version = tableById.getVersion(game.getExtTableVersionId());
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
            if(tableVersion.getAuthors() != null) {
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

    autoCompleteNameField.setText(vpsTable.getDisplayName());
    yearLabel.setText(String.valueOf(vpsTable.getYear()));
    manufacturerLabel.setText(vpsTable.getManufacturer());
    playersLabel.setText(String.valueOf(vpsTable.getPlayers()));
    ipdbLink.setText(vpsTable.getIpdbUrl());
    ipdbLink.setDisable(StringUtils.isEmpty(vpsTable.getIpdbUrl()) || !vpsTable.getIpdbUrl().startsWith("http"));
    updatedLabel.setText(DateFormat.getDateInstance().format(new Date(vpsTable.getUpdatedAt())));

    boolean doFilter = filterCheckbox.isSelected();

    if (!doFilter || !game.get().isPupPackAvailable()) {
      addSection(dataRoot, "PUP Pack", vpsTable.getPupPackFiles());
    }

    if (!doFilter || !game.get().isDirectB2SAvailable()) {
      addSection(dataRoot, "Backglasses", vpsTable.getB2sFiles());
    }

    if (!doFilter || !game.get().isAltSoundAvailable()) {
      addSection(dataRoot, "ALT Sound", vpsTable.getAltSoundFiles());
    }

    addSection(dataRoot, "ALT Color", vpsTable.getAltColorFiles());

    if (!doFilter || !game.get().isRomExists()) {
      addSection(dataRoot, "ROM", vpsTable.getRomFiles());
    }

    addSection(dataRoot, "Sound", vpsTable.getSoundFiles());

    GameMediaRepresentation gameMedia = game.get().getGameMedia();
    List<GameMediaItemRepresentation> items = gameMedia.getMediaItems(PopperScreen.Topper);
    if (!doFilter || items.isEmpty()) {
      addSection(dataRoot, "Topper", vpsTable.getTopperFiles());
    }

    items = gameMedia.getMediaItems(PopperScreen.Wheel);
    if (!doFilter || items.isEmpty()) {
      addSection(dataRoot, "Wheel Art", vpsTable.getWheelArtFiles());
    }

    if (!doFilter || !game.get().isPovAvailable()) {
      addSection(dataRoot, "POV", vpsTable.getPovFiles());
    }
  }

  public static void addSection(VBox dataRoot, String title, List<? extends VpsAuthoredUrls> urls) {
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
          if (vpsUrl.isBroken()) {
            url = "";
          }
          entries.add(new VpsEntry(version, authors, url, updatedAt));
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
    openTableBtn.setDisable(newValue == null || newValue.getUrls().isEmpty());
    copyTableVersionBtn.setDisable(newValue == null);
    if (newValue != null) {
      copyTableVersionBtn.setDisable(false);
      String existingValueId = this.game.get().getExtTableVersionId();
      String newValueId = newValue.getId();
      if (existingValueId == null || !existingValueId.equals(newValueId)) {
        client.getVpsService().saveVersion(this.game.get().getId(), newValueId);
        EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
      }
    }
    else {
      client.getVpsService().saveVersion(this.game.get().getId(), null);
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    detailsBox.managedProperty().bindBidirectional(detailsBox.visibleProperty());
    dataRoot.managedProperty().bindBidirectional(dataRoot.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());

    openTableBtn.setDisable(true);
    copyTableBtn.setDisable(true);
    copyTableVersionBtn.setDisable(true);

    tableVersionsCombo.setCellFactory(c -> new VpsTableVersionCell());
    tableVersionsCombo.setButtonCell(new VpsTableVersionCell());

    filterCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshView(game));

    List<VpsTable> tables = VPS.getInstance().getTables();
    refreshSheetData(tables);
    TreeSet<String> collect = new TreeSet<>(tables.stream().map(t -> t.getDisplayName()).collect(Collectors.toSet()));
    autoCompleteNameField = new AutoCompleteTextField(this.nameField, this, collect);
  }
}