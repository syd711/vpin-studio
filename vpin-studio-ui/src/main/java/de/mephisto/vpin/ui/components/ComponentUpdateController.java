package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.ui.util.FilterPills;
import de.mephisto.vpin.ui.util.ThemeColors;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentDiffEntryRepresentation;
import de.mephisto.vpin.restclient.components.ComponentInstallation;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.components.GithubReleaseRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * The split view of a component tab: version, installer and installation settings on the left,
 * the log of the last comparison, simulation or installation on the right.
 */
public class ComponentUpdateController implements Initializable, ChangeListener<GithubReleaseRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentUpdateController.class);

  private static final String[] BANNER_STYLES = {"sm-banner-ok", "sm-banner-warn", "sm-banner-error", "sm-banner-info"};
  private static final Pattern SHORT_VERSION_PATTERN = Pattern.compile("^(v?\\d+(?:\\.\\d+)+).*");

  /** display order, label and color of the diff states */
  enum DiffKind {
    SIZE_DIFF(ComponentDiffEntryRepresentation.SIZE_DIFF, "components.tab.diff_size", ThemeColors.WARN),
    TARGET_FILE_NOT_EXIST(ComponentDiffEntryRepresentation.TARGET_FILE_NOT_EXIST, "components.tab.diff_missing", ThemeColors.LINK),
    TARGET_FOLDER_NOT_EXIST(ComponentDiffEntryRepresentation.TARGET_FOLDER_NOT_EXIST, "components.tab.diff_folder_missing", ThemeColors.ERROR),
    FILE_MATCH(ComponentDiffEntryRepresentation.FILE_MATCH, "components.tab.diff_match", ThemeColors.OK),
    SKIPPED(ComponentDiffEntryRepresentation.SKIPPED, "components.tab.diff_skipped", ThemeColors.SUBTLE),
    OTHER("", "components.tab.diff_other", ThemeColors.MUTED);

    final String state;
    final String labelKey;
    final String color;

    DiffKind(String state, String labelKey, String color) {
      this.state = state;
      this.labelKey = labelKey;
      this.color = color;
    }

    static DiffKind of(@Nullable String state) {
      for (DiffKind kind : values()) {
        if (kind.state.equals(state)) {
          return kind;
        }
      }
      return OTHER;
    }
  }

  enum LogType {
    COMPARISON, SIMULATION, INSTALLATION
  }

  /** a row of the diff list: a group header or a file */
  private record GroupItem(DiffKind kind, int count, boolean open) {
  }

  @FXML
  private VBox leftContent;

  @FXML
  private VBox installerSection;

  @FXML
  private Button installBtn;

  @FXML
  private FontIcon installIcon;

  @FXML
  private Button checkBtn;

  @FXML
  private Button simBtn;

  @FXML
  private TextArea textArea;

  @FXML
  private TextArea releaseNotes;

  @FXML
  private Label releaseNotesState;

  @FXML
  private FontIcon releaseNotesChevron;

  @FXML
  private ComboBox<String> artifactCombo;

  @FXML
  private ComboBox<GithubReleaseRepresentation> releasesCombo;

  @FXML
  private HBox hintRow;

  @FXML
  private VBox hintBox;

  @FXML
  private Label logMetaLabel;

  @FXML
  private HBox filterBox;

  @FXML
  private TextField searchField;

  @FXML
  private ToggleButton groupToggle;

  @FXML
  private Button copyBtn;

  @FXML
  private Button clearBtn;

  @FXML
  private HBox banner;

  @FXML
  private FontIcon bannerIcon;

  @FXML
  private Label bannerTitle;

  @FXML
  private Label bannerText;

  @FXML
  private ListView<Object> diffList;

  @FXML
  private Label emptyLabel;

  private AbstractComponentTab componentTab;
  private ComponentRepresentation component;

  private boolean localInstallOnly = true;

  /**
   * Overrides the component folder as installation target, e.g. the folder of another VPX emulator
   */
  @Nullable
  private String targetFolder;

  /** the currently shown log */
  @Nullable
  private ComponentActionLogRepresentation log;
  @Nullable
  private String logArtifact;
  private LogType logType = LogType.COMPARISON;

  /** null for all kinds */
  @Nullable
  private DiffKind filter;
  private final Set<DiffKind> collapsed = EnumSet.of(DiffKind.FILE_MATCH, DiffKind.SKIPPED);

  @FXML
  private void onFetch() {
    ComponentType type = component.getType();
    ComponentInstallation install = new ComponentInstallation();
    install.setComponent(type);
    install.setReleaseTag("-latest-");
    install.setArtifactName("-latest-");
    install.setTargetFolder(getTargetFolder());

    ComponentCheckProgressModel model = new ComponentCheckProgressModel(Messages.get("dialog.checking_status_for", type), install, true);
    ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);
    if (!resultModel.getResults().isEmpty()) {
      showLog((ComponentActionLogRepresentation) resultModel.getResults().getFirst(), LogType.COMPARISON, null);
      EventManager.getInstance().notify3rdPartyVersionUpdate(type);
    }
    else {
      showError(LogType.COMPARISON, Messages.get("components.component_update_panel.check_failed_see_log_for_details"));
    }
  }

  @FXML
  private void onInstall() {
    if (client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        runInstall();
      }
    }
    else {
      runInstall();
    }
  }

  @FXML
  private void onInstallSimulate() {
    run(true, Collections.emptyList());
  }

  @FXML
  private void onCheck() {
    Platform.runLater(() -> {
      try {
        ComponentType type = component.getType();
        GithubReleaseRepresentation release = releasesCombo.getValue();
        String artifact = artifactCombo.getValue();

        ComponentInstallation install = new ComponentInstallation();
        install.setComponent(type);
        install.setReleaseTag(release.getTag());
        install.setArtifactName(artifact);
        install.setTargetFolder(getTargetFolder());

        ComponentCheckProgressModel model = new ComponentCheckProgressModel(Messages.get("dialog.component_check_for", type), install, false);
        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);

        if (!resultModel.getResults().isEmpty()) {
          showLog((ComponentActionLogRepresentation) resultModel.getResults().getFirst(), LogType.COMPARISON, artifact);
        }
        else {
          showError(LogType.COMPARISON, Messages.get("components.component_update_panel.check_failed_see_log_for_details"));
        }

        EventManager.getInstance().notify3rdPartyVersionUpdate(type);
      }
      catch (Exception e) {
        LOG.error("Failed to execute component check: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.failed_to_execute_component_check", e.getMessage()));
      }
    });
  }

  @FXML
  private void onToggleReleaseNotes() {
    setReleaseNotesOpen(!releaseNotes.isVisible());
  }

  @FXML
  private void onCopyLog() {
    if (log != null) {
      ClipboardContent content = new ClipboardContent();
      content.putString(log.toString());
      Clipboard.getSystemClipboard().setContent(content);
    }
  }

  @FXML
  private void onClearLog() {
    showLog(null, LogType.COMPARISON, null);
  }

  private void runInstall() {
    String artifact = artifactCombo.getValue();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("msg.install_update", String.valueOf(artifact)), Messages.get("dialog.existing_files_will_be_overwritten"), Messages.get("dialog.make_sure_to_follow_the_additional_instructions"), Messages.get("dialog.continue_button"));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      run(false, Collections.emptyList());
    }
  }

  //TODO exclusion dialog for selection files not finished, e.g. skip or not to skip mame mappings
  private void run(boolean simulate, List<String> exclusions) {
    LogType type = simulate ? LogType.SIMULATION : LogType.INSTALLATION;
    Platform.runLater(() -> {
      try {
        ComponentType componentType = component.getType();
        GithubReleaseRepresentation release = releasesCombo.getValue();
        String artifact = artifactCombo.getValue();

        ComponentInstallation install = new ComponentInstallation();
        install.setComponent(componentType);
        install.setReleaseTag(release.getTag());
        install.setArtifactName(artifact);
        install.setTargetFolder(getTargetFolder());

        ComponentInstallProgressModel model = new ComponentInstallProgressModel(install, simulate);
        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(model);

        if (!resultModel.getResults().isEmpty()) {
          showLog((ComponentActionLogRepresentation) resultModel.getResults().getFirst(), type, artifact);
        }

        componentTab.postProcessing(simulate);

        EventManager.getInstance().notify3rdPartyVersionUpdate(componentType);
      }
      catch (Exception ex) {
        LOG.error("Failed to run component update: " + ex.getMessage(), ex);
        showError(type, Messages.get("components.component_update_panel.action_failed", ex.getMessage()));
      }
    });
  }

  public void setComponent(AbstractComponentTab tab, ComponentRepresentation component) {
    this.componentTab = tab;
    this.component = component;
    refresh(null, null);
  }

  public void refreshComponent(ComponentRepresentation component) {
    this.component = component;
    refresh(releasesCombo.getValue(), artifactCombo.getValue());
  }

  public void setTargetFolder(@Nullable String targetFolder) {
    if (!Objects.equals(this.targetFolder, targetFolder)) {
      this.targetFolder = targetFolder;
      // the log belongs to the previous folder
      showLog(null, LogType.COMPARISON, null);
    }
  }

  private String getTargetFolder() {
    return targetFolder != null ? targetFolder : component.getTargetFolder();
  }

  public void setLocalInstallOnly(boolean localInstallOnly) {
    this.localInstallOnly = localInstallOnly;
  }

  /**
   * The left column, the tab adds the version and installation sections around the installer section.
   */
  public VBox getLeftContent() {
    return leftContent;
  }

  public VBox getInstallerSection() {
    return installerSection;
  }

  /**
   * Sets the component specific instructions shown below the install buttons.
   */
  public void setHint(@Nullable VBox hint) {
    hintBox.getChildren().clear();
    if (hint != null) {
      for (Node node : new ArrayList<>(hint.getChildren())) {
        if (node instanceof Label label) {
          label.getStyleClass().setAll("label", "sm-small");
          label.setWrapText(true);
          label.setMinHeight(Region.USE_PREF_SIZE);
        }
        hintBox.getChildren().add(node);
      }
    }
    hintRow.setVisible(!hintBox.getChildren().isEmpty());
  }

  public void refresh(@Nullable GithubReleaseRepresentation release, @Nullable String artifactId) {
    releasesCombo.setItems(FXCollections.observableList(component.getReleases()));
    releasesCombo.setDisable(component.getReleases().isEmpty());

    boolean remoteBlocked = localInstallOnly && !client.getSystemService().isLocal();

    checkBtn.setVisible(component.isInstalled());
    checkBtn.setDisable(component.getReleases().isEmpty() || artifactCombo.getValue() == null);
    simBtn.setDisable(component.getReleases().isEmpty() || artifactCombo.getValue() == null);
    installBtn.setDisable(component.getReleases().isEmpty() || artifactCombo.getValue() == null || remoteBlocked);
    installBtn.setTooltip(installBtn.isDisabled() ? new Tooltip(Messages.get("dialog.component_not_updatable_remote")) : null);

    releaseNotes.setText("");
    if (!component.getReleases().isEmpty()) {
      if (release != null) {
        releasesCombo.setValue(release);
      }
      else {
        releasesCombo.setValue(component.getReleases().getFirst());
      }

      release = releasesCombo.getValue();
      releaseNotes.setText(release.getReleaseNotes() != null ? release.getReleaseNotes().trim() : "");

      List<String> artifacts = release.getArtifacts();
      artifactCombo.setItems(FXCollections.observableList(artifacts));
      if (!artifacts.isEmpty()) {
        artifactCombo.setValue(artifacts.getFirst());
      }
      if (artifactId != null) {
        artifactCombo.setValue(artifactId);
      }

      artifactCombo.setDisable(component.getReleases().isEmpty());
    }
    refreshReleaseNotesState();
    refreshInstallButton();
  }

  private void refreshInstallButton() {
    GithubReleaseRepresentation release = releasesCombo.getValue();
    if (localInstallOnly && !client.getSystemService().isLocal()) {
      installBtn.setText(Messages.get("components.component_update_panel.start_installation_on_cabinet_only"));
    }
    else if (release != null && !StringUtils.isEmpty(release.getTag())) {
      installBtn.setText(Messages.get("components.tab.install_version", toShortVersion(release.getTag())));
    }
    else {
      installBtn.setText(Messages.get("components.component_update_panel.start_installation"));
    }
  }

  private void refreshReleaseNotesState() {
    boolean empty = StringUtils.isBlank(releaseNotes.getText());
    releaseNotesState.setText(empty ? Messages.get("components.tab.release_notes_none") : "");
    if (empty) {
      setReleaseNotesOpen(false);
    }
  }

  private void setReleaseNotesOpen(boolean open) {
    boolean canOpen = open && !StringUtils.isBlank(releaseNotes.getText());
    releaseNotes.setVisible(canOpen);
    releaseNotesChevron.setIconLiteral(canOpen ? "mdi2c-chevron-down" : "mdi2c-chevron-right");
  }

  @Override
  public void changed(ObservableValue<? extends GithubReleaseRepresentation> observable, GithubReleaseRepresentation oldValue, GithubReleaseRepresentation newValue) {
    this.releasesCombo.valueProperty().removeListener(this);
    refresh(newValue, null);
    this.releasesCombo.valueProperty().addListener(this);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    simBtn.setDisable(true);
    installBtn.setDisable(true);
    artifactCombo.setDisable(true);
    releasesCombo.setDisable(true);
    checkBtn.setDisable(true);

    checkBtn.managedProperty().bind(checkBtn.visibleProperty());
    releaseNotes.managedProperty().bind(releaseNotes.visibleProperty());
    hintRow.managedProperty().bind(hintRow.visibleProperty());
    banner.managedProperty().bind(banner.visibleProperty());
    hintRow.setVisible(false);
    setReleaseNotesOpen(false);

    releasesCombo.valueProperty().addListener(this);

    artifactCombo.valueProperty().addListener((observableValue, s, t1) -> {
      checkBtn.setDisable(!component.isInstalled() || t1 == null);
      installBtn.setDisable(t1 == null || (localInstallOnly && !client.getSystemService().isLocal()));
      simBtn.setDisable(t1 == null);
    });

    diffList.setCellFactory(list -> new DiffCell());
    diffList.setFocusTraversable(false);
    searchField.textProperty().addListener((obs, o, n) -> refreshDiffList());
    groupToggle.selectedProperty().addListener((obs, o, n) -> refreshDiffList());

    showLog(null, LogType.COMPARISON, null);
  }

  //------------------------------------------- log ------------------------------------------------------------------

  private void showError(LogType type, String message) {
    ComponentActionLogRepresentation error = new ComponentActionLogRepresentation();
    error.setStatus(message);
    error.setLogsSummary(message);
    error.setDiff(type == LogType.COMPARISON);
    showLog(error, type, null);
  }

  /**
   * Shows the result of an action, null clears the log.
   */
  private void showLog(@Nullable ComponentActionLogRepresentation log, LogType type, @Nullable String artifact) {
    this.log = log;
    this.logType = type;
    this.logArtifact = artifact;
    this.filter = null;

    copyBtn.setDisable(log == null);
    clearBtn.setDisable(log == null);

    if (log == null) {
      logMetaLabel.setText("");
      banner.setVisible(false);
      setLogView(false, false);
      refreshFilters();
      return;
    }

    String time = LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM));
    logMetaLabel.setText(Messages.get(switch (type) {
      case COMPARISON -> "components.tab.log_comparison";
      case SIMULATION -> "components.tab.log_simulation";
      case INSTALLATION -> "components.tab.log_installation";
    }, time));

    refreshBanner();

    boolean structured = log.isDiff() && log.getDiffEntries() != null && !log.getDiffEntries().isEmpty();
    if (structured) {
      refreshFilters();
      refreshDiffList();
    }
    else {
      refreshFilters();
      textArea.setText(log.toString());
      Platform.runLater(() -> textArea.setScrollTop(0));
    }
    setLogView(true, structured);
  }

  private void setLogView(boolean hasLog, boolean structured) {
    emptyLabel.setVisible(!hasLog);
    diffList.setVisible(hasLog && structured);
    textArea.setVisible(hasLog && !structured);
    searchField.setVisible(hasLog && structured);
    groupToggle.setVisible(hasLog && structured);
    filterBox.setVisible(hasLog && structured);
    searchField.setManaged(searchField.isVisible());
    groupToggle.setManaged(groupToggle.isVisible());
    filterBox.setManaged(filterBox.isVisible());
  }

  private void refreshBanner() {
    banner.getStyleClass().removeAll(BANNER_STYLES);
    banner.setVisible(true);
    String artifact = logArtifact != null ? logArtifact : Messages.get("components.tab.latest_artifact");

    if (log != null && !StringUtils.isEmpty(log.getStatus())) {
      setBanner("sm-banner-error", "mdi2c-close-circle-outline", ThemeColors.ERROR,
          Messages.get(switch (logType) {
            case COMPARISON -> "components.tab.banner_comparison_failed";
            case SIMULATION -> "components.tab.banner_simulation_failed";
            case INSTALLATION -> "components.tab.banner_installation_failed";
          }), log.getStatus());
      return;
    }

    switch (logType) {
      case COMPARISON -> {
        List<ComponentDiffEntryRepresentation> entries = log != null && log.getDiffEntries() != null ? log.getDiffEntries() : List.of();
        long sizeDiffs = entries.stream().filter(e -> DiffKind.of(e.getState()) == DiffKind.SIZE_DIFF).count();
        long missing = entries.stream().filter(e -> DiffKind.of(e.getState()) == DiffKind.TARGET_FILE_NOT_EXIST
            || DiffKind.of(e.getState()) == DiffKind.TARGET_FOLDER_NOT_EXIST).count();
        String details = Messages.get("components.tab.banner_diff_details", sizeDiffs, missing, entries.size());
        if (entries.isEmpty()) {
          // older servers do not send the compared files and the result, the plain log tells
          setBanner("sm-banner-info", "mdi2i-information-outline", ThemeColors.INFO, Messages.get("components.tab.banner_compared", artifact), "");
        }
        else if (log != null && log.isDiffering()) {
          setBanner("sm-banner-warn", "mdi2a-alert-outline", ThemeColors.WARN, Messages.get("components.tab.banner_outdated", artifact), details);
        }
        else {
          setBanner("sm-banner-ok", "mdi2c-check-circle-outline", ThemeColors.OK, Messages.get("components.tab.banner_matches", artifact), details);
        }
      }
      case SIMULATION -> setBanner("sm-banner-info", "mdi2f-flask-outline", ThemeColors.INFO,
          Messages.get("components.tab.banner_simulated", artifact), Messages.get("components.tab.banner_simulated_details"));
      case INSTALLATION -> setBanner("sm-banner-ok", "mdi2c-check-circle-outline", ThemeColors.OK,
          Messages.get("components.tab.banner_installed", artifact), Messages.get("components.tab.banner_installed_details"));
    }
  }

  private void setBanner(String style, String icon, String color, String title, String text) {
    banner.getStyleClass().add(style);
    bannerIcon.setIconLiteral(icon);
    ThemeColors.setIconColor(bannerIcon, color);
    bannerTitle.setText(title);
    bannerText.setText(text);
    bannerText.setVisible(!StringUtils.isEmpty(text));
    bannerText.setManaged(bannerText.isVisible());
  }

  private List<ComponentDiffEntryRepresentation> getDiffEntries() {
    return log != null && log.getDiffEntries() != null ? log.getDiffEntries() : List.of();
  }

  private void refreshFilters() {
    filterBox.getChildren().clear();
    List<ComponentDiffEntryRepresentation> entries = getDiffEntries();
    if (entries.isEmpty()) {
      return;
    }

    ToggleGroup group = new ToggleGroup();
    filterBox.getChildren().add(createFilterPill(group, null, Messages.get("components.screens.all"), null, entries.size()));
    for (DiffKind kind : DiffKind.values()) {
      long count = entries.stream().filter(e -> DiffKind.of(e.getState()) == kind).count();
      if (count > 0) {
        filterBox.getChildren().add(createFilterPill(group, kind, Messages.get(kind.labelKey), kind.color, count));
      }
    }
  }

  private ToggleButton createFilterPill(ToggleGroup group, @Nullable DiffKind kind, String label, @Nullable String color, long count) {
    ToggleButton pill = FilterPills.create(group, label, color, count);
    pill.setSelected(filter == kind);
    pill.setOnAction(e -> {
      filter = kind;
      refreshDiffList();
    });
    return pill;
  }

  private void refreshDiffList() {
    List<ComponentDiffEntryRepresentation> entries = getDiffEntries();
    String query = StringUtils.trimToEmpty(searchField.getText()).toLowerCase();

    List<ComponentDiffEntryRepresentation> visible = entries.stream()
        .filter(e -> filter == null || DiffKind.of(e.getState()) == filter)
        .filter(e -> query.isEmpty() || new File(e.getFile()).getName().toLowerCase().contains(query))
        .toList();

    List<Object> items = new ArrayList<>();
    if (groupToggle.isSelected()) {
      for (DiffKind kind : DiffKind.values()) {
        List<ComponentDiffEntryRepresentation> rows = visible.stream().filter(e -> DiffKind.of(e.getState()) == kind).toList();
        if (rows.isEmpty()) {
          continue;
        }
        // an explicitly filtered group is always open
        boolean open = filter == kind || !collapsed.contains(kind);
        items.add(new GroupItem(kind, rows.size(), open));
        if (open) {
          items.addAll(rows);
        }
      }
    }
    else {
      items.addAll(visible);
    }
    diffList.setItems(FXCollections.observableList(items));
    diffList.setPlaceholder(new Label(Messages.get("components.tab.log_no_match")));
  }

  private void toggleGroup(DiffKind kind) {
    if (!collapsed.remove(kind)) {
      collapsed.add(kind);
    }
    refreshDiffList();
  }

  /**
   * "v3.7.0-477-6589fc5" becomes "v3.7.0", tags without a version number are kept.
   */
  static String toShortVersion(String tag) {
    Matcher m = SHORT_VERSION_PATTERN.matcher(tag.trim());
    return m.matches() ? m.group(1) : tag;
  }

  static String formatSize(long bytes) {
    if (bytes < 0) {
      return "?";
    }
    if (bytes >= 1024 * 1024) {
      return String.format("%.2f MB", bytes / (1024d * 1024d));
    }
    return String.format("%.1f KB", bytes / 1024d);
  }

  /**
   * Renders group headers and file rows.
   */
  private class DiffCell extends ListCell<Object> {
    @Override
    protected void updateItem(Object item, boolean empty) {
      super.updateItem(item, empty);
      setText(null);
      if (empty || item == null) {
        setGraphic(null);
        setOnMouseClicked(null);
        return;
      }

      if (item instanceof GroupItem groupItem) {
        setGraphic(createGroupHeader(groupItem));
        setOnMouseClicked(e -> toggleGroup(groupItem.kind()));
      }
      else if (item instanceof ComponentDiffEntryRepresentation entry) {
        setGraphic(createRow(entry));
        setOnMouseClicked(null);
      }
    }

    private Node createGroupHeader(GroupItem item) {
      FontIcon chevron = new FontIcon(item.open() ? "mdi2c-chevron-down" : "mdi2c-chevron-right");
      chevron.setIconSize(16);
      ThemeColors.setIconColor(chevron, ThemeColors.MUTED);

      Label title = new Label(Messages.get(item.kind().labelKey));
      title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + ThemeColors.WHITE + ";");
      Label count = new Label(String.valueOf(item.count()));
      count.getStyleClass().add("sm-small");

      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      Label code = new Label(item.kind() == DiffKind.OTHER ? "" : item.kind().state);
      code.getStyleClass().add("sm-small");

      HBox header = new HBox(10, chevron, FilterPills.createDot(item.kind().color), title, count, spacer, code);
      header.setAlignment(Pos.CENTER_LEFT);
      header.setPadding(new Insets(7, 14, 7, 14));
      header.getStyleClass().add("sm-log-group");
      header.prefWidthProperty().bind(diffList.widthProperty().subtract(18));
      return header;
    }

    private Node createRow(ComponentDiffEntryRepresentation entry) {
      DiffKind kind = DiffKind.of(entry.getState());
      File file = new File(entry.getFile());

      Label name = new Label(file.getName());
      name.setStyle("-fx-font-size: 14px; -fx-text-fill: " + ThemeColors.WHITE + ";");
      Label path = new Label(entry.getFile());
      path.getStyleClass().add("sm-small");
      path.setTooltip(new Tooltip(entry.getFile()));
      VBox names = new VBox(1, name, path);
      names.setMinWidth(0);
      HBox.setHgrow(names, Priority.ALWAYS);

      HBox sizes = new HBox(10);
      sizes.setAlignment(Pos.CENTER_RIGHT);
      sizes.setMinWidth(Region.USE_PREF_SIZE);
      switch (kind) {
        case SIZE_DIFF -> {
          long delta = entry.getReleaseSize() - entry.getInstalledSize();
          Label from = small(formatSize(entry.getInstalledSize()), ThemeColors.SUBTLE);
          Label arrow = small("→", ThemeColors.DISABLED);
          Label to = small(formatSize(entry.getReleaseSize()), ThemeColors.WHITE);
          Label diff = small((delta >= 0 ? "+" : "−") + formatSize(Math.abs(delta)), kind.color);
          diff.setMinWidth(90);
          diff.setAlignment(Pos.CENTER_RIGHT);
          sizes.getChildren().addAll(from, arrow, to, diff);
        }
        case TARGET_FILE_NOT_EXIST, TARGET_FOLDER_NOT_EXIST -> sizes.getChildren().add(small(Messages.get("components.tab.diff_will_be_added"), kind.color));
        case FILE_MATCH -> sizes.getChildren().add(small(formatSize(entry.getInstalledSize()), ThemeColors.SUBTLE));
        default -> {
        }
      }

      HBox row = new HBox(14, FilterPills.createDot(kind.color), names, sizes);
      row.setAlignment(Pos.CENTER_LEFT);
      row.setPadding(new Insets(8, 14, 8, 14));
      row.getStyleClass().add("sm-log-row");
      // keep the row inside the list, otherwise long paths add a horizontal scrollbar
      row.prefWidthProperty().bind(diffList.widthProperty().subtract(18));
      row.setMaxWidth(Region.USE_PREF_SIZE);
      return row;
    }

    private Label small(String text, String color) {
      Label label = new Label(text);
      label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
      label.setMinWidth(Region.USE_PREF_SIZE);
      return label;
    }
  }
}
