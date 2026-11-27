package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mcp.McpAuditEntry;
import de.mephisto.vpin.restclient.mcp.McpSettings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * Settings of the MCP endpoint of the server, which lets AI assistants like Claude query and change the cabinet,
 * and the log of the changes they made.
 */
public class McpPreferencesController implements Initializable {

  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM);
  private static final int MAX_CELL_TEXT = 200;

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private CheckBox readOnlyCheckbox;

  @FXML
  private Pane connectionPanel;

  @FXML
  private Pane groupsPanel;

  @FXML
  private CheckBox tablesCheckbox;

  @FXML
  private CheckBox competitionsCheckbox;

  @FXML
  private CheckBox playlistsCheckbox;

  @FXML
  private CheckBox mediaCheckbox;

  @FXML
  private CheckBox settingsCheckbox;

  @FXML
  private CheckBox systemCheckbox;

  @FXML
  private TextField urlText;

  @FXML
  private TextField commandText;

  @FXML
  private TableView<McpAuditEntry> auditTable;

  @FXML
  private TableColumn<McpAuditEntry, String> auditTimeColumn;

  @FXML
  private TableColumn<McpAuditEntry, String> auditToolColumn;

  @FXML
  private TableColumn<McpAuditEntry, McpAuditEntry> auditStatusColumn;

  @FXML
  private TableColumn<McpAuditEntry, String> auditArgumentsColumn;

  @FXML
  private TableColumn<McpAuditEntry, String> auditResultColumn;

  @FXML
  private Button auditClearBtn;

  private McpSettings settings;

  @FXML
  private void onCopyUrl() {
    copy(urlText.getText());
  }

  @FXML
  private void onCopyCommand() {
    copy(commandText.getText());
  }

  @FXML
  private void onAuditRefresh() {
    loadAuditLog();
  }

  @FXML
  private void onAuditClear() {
    JFXFuture.supplyAsync(() -> client.getMcpService().clearAuditLog())
        .thenAcceptLater(cleared -> loadAuditLog());
  }

  private void save() {
    client.getPreferenceService().setJsonPreference(settings);
    refresh();
  }

  private void refresh() {
    boolean enabled = settings.isEnabled();
    connectionPanel.setDisable(!enabled);
    groupsPanel.setDisable(settings.isReadOnly());

    String url = getMcpUrl();
    urlText.setText(url);
    commandText.setText("claude mcp add --transport http vpin-studio " + url);
  }

  private void loadAuditLog() {
    JFXFuture.supplyAsync(() -> client.getMcpService().getAuditLog())
        .thenAcceptLater(this::showAuditLog);
  }

  private void showAuditLog(List<McpAuditEntry> entries) {
    auditTable.setItems(FXCollections.observableArrayList(entries));
    auditClearBtn.setDisable(entries.isEmpty());
  }

  private static String getMcpUrl() {
    String baseUrl = client.getRestClient().getBaseUrl();
    return StringUtils.appendIfMissing(baseUrl, "/") + "mcp";
  }

  private static void copy(String text) {
    ClipboardContent content = new ClipboardContent();
    content.putString(text);
    Clipboard.getSystemClipboard().setContent(content);
  }

  private void bind(CheckBox checkBox, boolean value, BiConsumer<McpSettings, Boolean> setter) {
    checkBox.setSelected(value);
    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      setter.accept(settings, newValue);
      save();
    });
  }

  private void initAuditTable() {
    auditTable.setPlaceholder(new Label(Messages.get("pref.mcp.audit.empty")));
    auditTimeColumn.setCellValueFactory(cellData -> {
      McpAuditEntry entry = cellData.getValue();
      String time = entry.getTimestamp() != null ? TIME_FORMAT.format(entry.getTimestamp().atZone(ZoneId.systemDefault())) : "";
      return new SimpleStringProperty(time);
    });
    auditToolColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTool()));
    auditArgumentsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(StringUtils.abbreviate(cellData.getValue().getArguments(), MAX_CELL_TEXT)));
    auditResultColumn.setCellValueFactory(cellData -> new SimpleStringProperty(StringUtils.abbreviate(cellData.getValue().getResult(), MAX_CELL_TEXT)));
    auditArgumentsColumn.setCellFactory(column -> new TooltipCell());
    auditResultColumn.setCellFactory(column -> new TooltipCell());
    auditArgumentsColumn.getStyleClass().add("sm-table-mono");
    auditResultColumn.getStyleClass().add("sm-table-mono");

    auditStatusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
    auditStatusColumn.setCellFactory(column -> new TableCell<>() {
      @Override
      protected void updateItem(McpAuditEntry entry, boolean empty) {
        super.updateItem(entry, empty);
        if (empty || entry == null) {
          setGraphic(null);
          setText(null);
          return;
        }
        Label pill = new Label();
        pill.getStyleClass().add("sm-pill");
        if (entry.isError()) {
          pill.setText(Messages.get("pref.mcp.audit.error"));
          pill.getStyleClass().add("sm-pill-error");
        }
        else if (entry.isDryRun()) {
          pill.setText(Messages.get("pref.mcp.audit.dry_run"));
          pill.getStyleClass().add("sm-pill-info");
        }
        else {
          pill.setText(Messages.get("pref.mcp.audit.ok"));
          pill.getStyleClass().add("sm-pill-ok");
        }
        setText(null);
        setGraphic(pill);
      }
    });
  }

  /**
   * Shows the cut text in the cell and the whole text as tooltip.
   */
  private static class TooltipCell extends TableCell<McpAuditEntry, String> {
    @Override
    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
        setTooltip(null);
        return;
      }
      setText(item);
      McpAuditEntry entry = getTableRow() != null ? getTableRow().getItem() : null;
      String full = entry == null ? item
          : getTableColumn() != null && "auditArgumentsColumn".equals(getTableColumn().getId()) ? entry.getArguments() : entry.getResult();
      Tooltip tooltip = new Tooltip(full);
      tooltip.setWrapText(true);
      tooltip.setMaxWidth(600);
      setTooltip(tooltip);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MCP_SETTINGS, McpSettings.class);
    if (settings == null) {
      //servers older than this client don't know the MCP settings
      settings = new McpSettings();
    }

    bind(enabledCheckbox, settings.isEnabled(), McpSettings::setEnabled);
    bind(readOnlyCheckbox, settings.isReadOnly(), McpSettings::setReadOnly);
    bind(tablesCheckbox, settings.isAllowTables(), McpSettings::setAllowTables);
    bind(competitionsCheckbox, settings.isAllowCompetitions(), McpSettings::setAllowCompetitions);
    bind(playlistsCheckbox, settings.isAllowPlaylists(), McpSettings::setAllowPlaylists);
    bind(mediaCheckbox, settings.isAllowMedia(), McpSettings::setAllowMedia);
    bind(settingsCheckbox, settings.isAllowSettings(), McpSettings::setAllowSettings);
    bind(systemCheckbox, settings.isAllowSystem(), McpSettings::setAllowSystem);

    initAuditTable();
    refresh();
    loadAuditLog();
  }
}
