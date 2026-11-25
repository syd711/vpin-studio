package de.mephisto.vpin.ui.components.screens;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.FrontendScreenSummary;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.restclient.system.SystemSummary;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * System Manager / Screens: list of all configured screens, a map of the monitor layout and an inspector
 * for the selected screen or monitor.
 */
public class ScreensController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ScreensController.class);

  private static final double ZOOM_MIN = 0.5;
  private static final double ZOOM_MAX = 3;
  private static final double ZOOM_STEP = 0.25;

  /** padding around the monitors on the map */
  private static final double PAD = 24;
  /** space below the monitors for their captions */
  private static final double CAPTION = 40;

  private static final String COLOR_MONITOR_BORDER = "#4A4A4A";
  private static final String COLOR_MONITOR_WARN = "#B04A50";
  private static final String COLOR_MAP_BACKGROUND = "#161616";

  enum Source {
    FRONTEND("#8C8CE0", "components.screens.managed_screen.frontend_displays", "components.screens.source_frontend"),
    SCREENRES("#5CC46A", "components.screens.managed_screen.screenres_txt_displays", "components.screens.source_screenres"),
    VPX("#DD9646", "components.screens.managed_screen.vpx_display", "components.screens.source_vpx");

    final String color;
    final String labelKey;
    final String chipKey;

    Source(String color, String labelKey, String chipKey) {
      this.color = color;
      this.labelKey = labelKey;
      this.chipKey = chipKey;
    }
  }

  record ScreenEntry(String id, Source source, FrontendPlayerDisplay display) {
    String name() {
      return display.getName();
    }

    long area() {
      return (long) display.getWidth() * display.getHeight();
    }

    String positionKey() {
      return display.getX() + "," + display.getY();
    }
  }

  @FXML
  private HBox errorBox;

  @FXML
  private VBox errorList;

  @FXML
  private Label countLabel;

  @FXML
  private VBox groupsBox;

  @FXML
  private HBox legendBox;

  @FXML
  private Label zoomLabel;

  @FXML
  private Button zoomInBtn;

  @FXML
  private Button zoomOutBtn;

  @FXML
  private ScrollPane mapScroll;

  @FXML
  private Pane canvas;

  @FXML
  private VBox inspector;

  private final List<ScreenEntry> entries = new ArrayList<>();
  private final List<MonitorInfo> monitors = new ArrayList<>();
  private final List<String> errors = new ArrayList<>();
  private final Set<String> hidden = new HashSet<>();
  private String frontendName;

  /** the id of the selected screen entry or "monitor:<id>" */
  @Nullable
  private String selection;
  private double zoom = 1;
  private boolean renderScheduled = false;

  @FXML
  private void onReload() {
    load(true);
  }

  @FXML
  private void onZoomOut() {
    setZoom(zoom - ZOOM_STEP);
  }

  @FXML
  private void onZoomIn() {
    setZoom(zoom + ZOOM_STEP);
  }

  @FXML
  private void onZoomFit() {
    setZoom(1);
  }

  private void setZoom(double value) {
    zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, value));
    zoomLabel.setText(Math.round(zoom * 100) + "%");
    zoomOutBtn.setDisable(zoom <= ZOOM_MIN);
    zoomInBtn.setDisable(zoom >= ZOOM_MAX);
    scheduleRenderMap();
  }

  /**
   * Rebuilds the map on the next pulse. The map must never be rebuilt during a layout pass (e.g. from a size listener):
   * nodes added there miss the CSS pass of that pulse and stay unstyled and invisible.
   */
  private void scheduleRenderMap() {
    if (!renderScheduled) {
      renderScheduled = true;
      Platform.runLater(() -> {
        renderScheduled = false;
        renderMap();
      });
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    errorBox.setVisible(false);
    mapScroll.setStyle("-fx-background: " + COLOR_MAP_BACKGROUND + "; -fx-background-color: " + COLOR_MAP_BACKGROUND + ";");

    // the scroll pane must not grow with the canvas, otherwise the canvas size feeds back into the layout
    mapScroll.setMinSize(0, 0);
    mapScroll.setPrefSize(100, 100);
    // fit the map into the visible area
    mapScroll.widthProperty().addListener((obs, o, n) -> {
      if (Math.abs(n.doubleValue() - o.doubleValue()) >= 1) {
        scheduleRenderMap();
      }
    });
    mapScroll.heightProperty().addListener((obs, o, n) -> {
      if (Math.abs(n.doubleValue() - o.doubleValue()) >= 1) {
        scheduleRenderMap();
      }
    });

    setZoom(1);
    load(false);
  }

  private void load(boolean reload) {
    JFXFuture.supplyAsync(() -> {
      SystemSummary systemSummary = client.getSystemService().getSystemSummary(reload);
      FrontendScreenSummary screenSummary = client.getFrontendService().getScreenSummary(reload);
      Frontend frontend = client.getFrontendService().getFrontendCached();
      return new Object[]{systemSummary, screenSummary, frontend};
    }).thenAcceptLater(result -> {
      SystemSummary systemSummary = (SystemSummary) result[0];
      FrontendScreenSummary screenSummary = (FrontendScreenSummary) result[1];
      Frontend frontend = (Frontend) result[2];
      frontendName = frontend != null ? frontend.getName() : null;

      monitors.clear();
      if (systemSummary != null && systemSummary.getMonitorInfos() != null) {
        monitors.addAll(systemSummary.getMonitorInfos());
      }

      entries.clear();
      errors.clear();
      if (screenSummary != null) {
        addEntries(Source.FRONTEND, screenSummary.getFrontendDisplays());
        addEntries(Source.SCREENRES, screenSummary.getScreenResDisplays());
        addEntries(Source.VPX, screenSummary.getVpxDisplaysDisplays());
        errors.addAll(screenSummary.getErrors());
      }

      if (selection != null && findEntry(selection) == null && findMonitor(selection) == null) {
        selection = null;
      }
      refresh();
    }).onErrorLater(e -> LOG.error("Failed to load screen information: {}", e.getMessage(), e));
  }

  private void addEntries(Source source, List<FrontendPlayerDisplay> displays) {
    if (displays == null) {
      return;
    }
    Set<String> ids = new HashSet<>();
    for (FrontendPlayerDisplay display : displays) {
      String id = source.name() + ":" + display.getName();
      // the names should be unique, but make sure the ids are
      while (!ids.add(id)) {
        id = id + "'";
      }
      entries.add(new ScreenEntry(id, source, display));
    }
  }

  private void refresh() {
    renderErrors();
    renderList();
    renderLegend();
    scheduleRenderMap();
    renderInspector();
  }

  private void select(@Nullable String id) {
    this.selection = id;
    renderList();
    scheduleRenderMap();
    renderInspector();
  }

  private void toggleVisibility(ScreenEntry entry) {
    if (!hidden.remove(entry.id())) {
      hidden.add(entry.id());
    }
    renderList();
    scheduleRenderMap();
    renderInspector();
  }

  //------------------------------------------- errors -----------------------------------------------------------------

  private void renderErrors() {
    errorList.getChildren().clear();
    for (String error : errors) {
      Label label = new Label(error);
      label.setWrapText(true);
      label.getStyleClass().add("sm-alert-text");
      errorList.getChildren().add(label);
    }
    errorBox.setVisible(!errors.isEmpty());
  }

  //------------------------------------------- list -------------------------------------------------------------------

  private void renderList() {
    groupsBox.getChildren().clear();
    long visibleCount = entries.stream().filter(e -> !hidden.contains(e.id())).count();
    countLabel.setText(Messages.get("components.screens.shown_count", visibleCount, entries.size()));

    for (Source source : Source.values()) {
      List<ScreenEntry> items = entries.stream().filter(e -> e.source() == source).toList();
      if (items.isEmpty()) {
        continue;
      }
      VBox group = new VBox(2);

      HBox header = new HBox(10);
      header.setAlignment(Pos.CENTER_LEFT);
      header.setPadding(new Insets(6, 14, 6, 14));
      VBox titles = new VBox(1);
      Label title = new Label(Messages.get(source.labelKey));
      title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
      Label sub = new Label(getSourceDescription(source));
      sub.getStyleClass().add("sm-small");
      titles.getChildren().addAll(title, sub);
      HBox.setHgrow(titles, Priority.ALWAYS);

      boolean allOn = items.stream().noneMatch(e -> hidden.contains(e.id()));
      CheckBox allCheckbox = new CheckBox(Messages.get("components.screens.all"));
      allCheckbox.getStyleClass().add("sm-hint");
      allCheckbox.setSelected(allOn);
      allCheckbox.setOnAction(e -> {
        items.forEach(item -> {
          if (allOn) {
            hidden.add(item.id());
          }
          else {
            hidden.remove(item.id());
          }
        });
        renderList();
        scheduleRenderMap();
        renderInspector();
      });
      header.getChildren().addAll(createSwatch(source.color), titles, allCheckbox);
      group.getChildren().add(header);

      for (ScreenEntry entry : items) {
        group.getChildren().add(createListRow(entry));
      }
      groupsBox.getChildren().add(group);
    }

    if (entries.isEmpty()) {
      Label empty = new Label(Messages.get("components.screens.no_screens"));
      empty.getStyleClass().add("sm-hint");
      empty.setWrapText(true);
      empty.setPadding(new Insets(8, 14, 8, 14));
      groupsBox.getChildren().add(empty);
    }
  }

  private HBox createListRow(ScreenEntry entry) {
    boolean visible = !hidden.contains(entry.id());
    boolean selected = entry.id().equals(selection);

    HBox row = new HBox(10);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setMinHeight(30);
    row.setPadding(new Insets(0, 8, 0, 36));
    VBox.setMargin(row, new Insets(0, 6, 0, 6));
    row.getStyleClass().add("sm-list-row");
    if (selected) {
      row.getStyleClass().add("sm-list-row-selected");
    }

    CheckBox checkBox = new CheckBox();
    checkBox.setSelected(visible);
    checkBox.setOnAction(e -> toggleVisibility(entry));
    // toggling the visibility must not select the row
    checkBox.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

    Label name = new Label(entry.name());
    name.setMaxWidth(Double.MAX_VALUE);
    name.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (!visible ? "#7A7A7A" : "white") + ";" + (selected ? " -fx-font-weight: bold;" : ""));
    HBox.setHgrow(name, Priority.ALWAYS);

    Label resolution = new Label(entry.display().getWidth() + " × " + entry.display().getHeight());
    resolution.getStyleClass().add("sm-small");
    resolution.setMinWidth(Region.USE_PREF_SIZE);

    row.getChildren().addAll(checkBox, name, resolution);
    row.setOnMouseClicked(e -> select(entry.id()));
    return row;
  }

  private String getSourceDescription(Source source) {
    return switch (source) {
      case FRONTEND -> StringUtils.isEmpty(frontendName) ? Messages.get("components.screens.source_frontend_description")
          : Messages.get("components.screens.source_frontend_description_named", frontendName);
      case SCREENRES -> Messages.get("components.screens.source_screenres_description");
      case VPX -> Messages.get("components.screens.source_vpx_description");
    };
  }

  private Region createSwatch(String color) {
    Region swatch = new Region();
    swatch.setMinSize(12, 12);
    swatch.setPrefSize(12, 12);
    swatch.setMaxSize(12, 12);
    swatch.setStyle("-fx-background-color: " + color + "33; -fx-background-radius: 3; -fx-border-color: " + color + "; -fx-border-width: 1.5; -fx-border-radius: 3;");
    return swatch;
  }

  //------------------------------------------- legend -----------------------------------------------------------------

  private void renderLegend() {
    legendBox.getChildren().clear();
    legendBox.getChildren().add(createLegendItem(Messages.get("components.screens.monitor"), "-fx-border-color: #7A7A7A;"));
    for (Source source : Source.values()) {
      if (entries.stream().anyMatch(e -> e.source() == source)) {
        legendBox.getChildren().add(createLegendItem(Messages.get(source.chipKey),
            "-fx-border-color: " + source.color + "; -fx-background-color: " + source.color + "2E;"));
      }
    }
    legendBox.getChildren().add(createLegendItem(Messages.get("components.screens.stacked"),
        "-fx-border-color: #8C8CE0; -fx-effect: dropshadow(one-pass-box, #8C8CE0, 0, 0, 2, 2);"));
  }

  private HBox createLegendItem(String text, String swatchStyle) {
    Region swatch = new Region();
    swatch.setMinSize(12, 9);
    swatch.setMaxSize(12, 9);
    swatch.setStyle("-fx-border-width: 1.5; -fx-border-radius: 2; -fx-background-radius: 2; " + swatchStyle);
    Label label = new Label(text);
    label.getStyleClass().add("sm-small");
    HBox item = new HBox(6, swatch, label);
    item.setAlignment(Pos.CENTER_LEFT);
    return item;
  }

  //------------------------------------------- map --------------------------------------------------------------------

  private void renderMap() {
    canvas.getChildren().clear();
    if (monitors.isEmpty()) {
      return;
    }

    double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
    for (MonitorInfo monitor : monitors) {
      minX = Math.min(minX, monitor.getX());
      minY = Math.min(minY, monitor.getY());
      maxX = Math.max(maxX, monitor.getX() + monitor.getWidth());
      maxY = Math.max(maxY, monitor.getY() + monitor.getHeight());
    }
    double totalWidth = Math.max(1, maxX - minX);
    double totalHeight = Math.max(1, maxY - minY);

    // leave some room for the scrollbars
    double availableWidth = Math.max(100, mapScroll.getWidth() - 2 * PAD - 16);
    double availableHeight = Math.max(100, mapScroll.getHeight() - 2 * PAD - CAPTION - 16);
    double scale = Math.min(availableWidth / totalWidth, availableHeight / totalHeight) * zoom;

    double canvasWidth = totalWidth * scale + 2 * PAD;
    double canvasHeight = totalHeight * scale + 2 * PAD + CAPTION;
    canvas.setMinSize(canvasWidth, canvasHeight);
    canvas.setPrefSize(canvasWidth, canvasHeight);
    canvas.setMaxSize(canvasWidth, canvasHeight);

    final double offsetX = minX;
    final double offsetY = minY;

    for (MonitorInfo monitor : monitors) {
      double x = (monitor.getX() - offsetX) * scale + PAD;
      double y = (monitor.getY() - offsetY) * scale + PAD;
      double w = monitor.getWidth() * scale;
      double h = monitor.getHeight() * scale;
      boolean warn = hasError(monitor);
      boolean selected = monitorId(monitor).equals(selection);

      Region rect = new Region();
      rect.relocate(x, y);
      rect.setPrefSize(w, h);
      rect.resize(w, h);
      String border = warn ? COLOR_MONITOR_WARN : COLOR_MONITOR_BORDER;
      rect.setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-width: 1.5; -fx-border-color: "
          + (selected ? "white" : border) + ";");
      rect.setOnMouseClicked(e -> select(monitorId(monitor)));
      rect.setCursor(Cursor.HAND);
      canvas.getChildren().add(rect);

      // two lines limited to the monitor width, so captions of narrow monitors do not overlap their neighbours
      Label name = new Label(monitor.getFormattedName());
      name.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");
      if (warn) {
        FontIcon icon = new FontIcon("mdi2a-alert-outline");
        icon.setIconSize(14);
        icon.setIconColor(Paint.valueOf("#FF6B70"));
        name.setGraphic(icon);
      }
      Label meta = new Label(getMonitorMeta(monitor));
      meta.getStyleClass().add("sm-small");
      VBox caption = new VBox(1, name, meta);
      caption.setMaxWidth(Math.max(w, 60));
      caption.setPrefWidth(Math.max(w, 60));
      caption.relocate(x, y + h + 6);
      canvas.getChildren().add(caption);
    }

    // screens sharing the same position are drawn as one stacked box
    Map<String, List<ScreenEntry>> stacks = getStacks();
    List<List<ScreenEntry>> sorted = new ArrayList<>(stacks.values());
    sorted.sort(Comparator.comparingLong((List<ScreenEntry> s) -> s.get(0).area()).reversed());

    for (List<ScreenEntry> members : sorted) {
      ScreenEntry biggest = members.get(0);
      ScreenEntry lead = members.stream().filter(m -> m.id().equals(selection)).findFirst().orElse(biggest);
      boolean selected = lead.id().equals(selection);
      boolean stacked = members.size() > 1;
      String color = lead.source().color;

      FrontendPlayerDisplay display = biggest.display();
      double x = (display.getX() - offsetX) * scale + PAD;
      double y = (display.getY() - offsetY) * scale + PAD;
      double w = Math.max(18, display.getWidth() * scale);
      double h = Math.max(14, display.getHeight() * scale);

      if (stacked) {
        Region shadow = new Region();
        shadow.relocate(x + 4, y + 4);
        shadow.resize(w, h);
        shadow.setPrefSize(w, h);
        shadow.setMouseTransparent(true);
        shadow.setStyle("-fx-background-color: " + COLOR_MAP_BACKGROUND + "; -fx-border-color: " + color + "99; -fx-border-width: 1.5; -fx-border-radius: 3; -fx-background-radius: 3;");
        canvas.getChildren().add(shadow);
      }

      HBox box = new HBox(6);
      box.setAlignment(Pos.TOP_LEFT);
      box.setPadding(new Insets(3, 6, 3, 6));
      box.relocate(x, y);
      box.setMinSize(w, h);
      box.setPrefSize(w, h);
      box.setMaxSize(w, h);
      box.resize(w, h);
      // stacked boxes need an opaque base, otherwise the stack outline behind shines through
      box.setStyle("-fx-background-color: " + (stacked ? COLOR_MAP_BACKGROUND : "transparent") + ", " + color + (selected ? "40" : "1C") + ";"
          + " -fx-background-radius: 3, 3;"
          + " -fx-border-radius: 3" + (selected ? ", 5" : "") + ";"
          + " -fx-border-color: " + color + (selected ? ", white" : "") + ";"
          + " -fx-border-width: 1.5" + (selected ? ", 2" : "") + ";"
          + (selected ? " -fx-border-insets: 0, -3;" : ""));
      box.setCursor(Cursor.HAND);

      Label label = new Label(lead.name());
      label.setMinWidth(Region.USE_PREF_SIZE);
      label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
      box.getChildren().add(label);
      if (stacked) {
        Label badge = new Label("+" + (members.size() - 1));
        badge.setMinWidth(Region.USE_PREF_SIZE);
        badge.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_MAP_BACKGROUND + "; -fx-background-color: " + color
            + "; -fx-background-radius: 8; -fx-padding: 0 5 0 5;");
        box.getChildren().add(badge);
      }

      Tooltip.install(box, new Tooltip(String.join(", ", members.stream().map(ScreenEntry::name).toList())));
      box.setOnMouseClicked(e -> {
        // clicking a selected stack cycles through its members
        if (selected && stacked) {
          int index = members.indexOf(lead);
          select(members.get((index + 1) % members.size()).id());
        }
        else {
          select(lead.id());
        }
      });
      canvas.getChildren().add(box);
    }
  }

  /**
   * Groups the visible screens by their position, every group is sorted by size, biggest first.
   */
  private Map<String, List<ScreenEntry>> getStacks() {
    Map<String, List<ScreenEntry>> stacks = new LinkedHashMap<>();
    for (ScreenEntry entry : entries) {
      if (!hidden.contains(entry.id())) {
        stacks.computeIfAbsent(entry.positionKey(), k -> new ArrayList<>()).add(entry);
      }
    }
    stacks.values().forEach(list -> list.sort(Comparator.comparingLong(ScreenEntry::area).reversed()));
    return stacks;
  }

  private boolean hasError(MonitorInfo monitor) {
    String name = monitor.getFormattedName();
    return !StringUtils.isEmpty(name) && errors.stream().anyMatch(e -> e.contains(name));
  }

  private String getMonitorMeta(MonitorInfo monitor) {
    String resolution = monitor.getWidth() + " × " + monitor.getHeight();
    if (monitor.isPrimary()) {
      return Messages.get("components.screens.monitor_meta_primary", monitor.getId(), resolution);
    }
    return Messages.get("components.screens.monitor_meta", monitor.getId(), resolution);
  }

  private static String monitorId(MonitorInfo monitor) {
    return "monitor:" + monitor.getId();
  }

  @Nullable
  private ScreenEntry findEntry(@Nullable String id) {
    return entries.stream().filter(e -> e.id().equals(id)).findFirst().orElse(null);
  }

  @Nullable
  private MonitorInfo findMonitor(@Nullable String id) {
    return monitors.stream().filter(m -> monitorId(m).equals(id)).findFirst().orElse(null);
  }

  /**
   * The monitor containing the top left corner of the display, the primary one as fallback.
   */
  @Nullable
  private MonitorInfo getMonitorOf(FrontendPlayerDisplay display) {
    for (MonitorInfo monitor : monitors) {
      if (display.getX() >= monitor.getX() && display.getX() < monitor.getX() + monitor.getWidth()
          && display.getY() >= monitor.getY() && display.getY() < monitor.getY() + monitor.getHeight()) {
        return monitor;
      }
    }
    return monitors.stream().filter(MonitorInfo::isPrimary).findFirst().orElse(monitors.isEmpty() ? null : monitors.get(0));
  }

  //------------------------------------------- inspector --------------------------------------------------------------

  private void renderInspector() {
    inspector.getChildren().clear();

    ScreenEntry entry = findEntry(selection);
    MonitorInfo selectedMonitor = findMonitor(selection);
    if (entry == null && selectedMonitor == null) {
      Label hint = new Label(Messages.get("components.screens.select_hint"));
      hint.getStyleClass().add("sm-hint");
      hint.setWrapText(true);
      inspector.getChildren().add(hint);
      return;
    }

    VBox head = new VBox(6);
    Label caption = new Label(Messages.get("components.screens.selected").toUpperCase());
    caption.getStyleClass().add("sm-caption");
    head.getChildren().add(caption);

    GridPane details = new GridPane();
    details.setHgap(14);
    details.setVgap(10);
    ColumnConstraints c0 = new ColumnConstraints();
    c0.setMinWidth(Region.USE_PREF_SIZE);
    ColumnConstraints c1 = new ColumnConstraints();
    c1.setHgrow(Priority.ALWAYS);
    details.getColumnConstraints().addAll(c0, c1);

    if (entry != null) {
      FrontendPlayerDisplay display = entry.display();
      Label name = new Label(entry.name());
      name.getStyleClass().add("sm-title");
      head.getChildren().addAll(name, createSourceChip(Messages.get(entry.source().chipKey), entry.source().color));

      MonitorInfo monitor = getMonitorOf(display);
      int row = 0;
      if (monitor != null) {
        String monitorText = monitor.getFormattedName();
        if (hasError(monitor)) {
          monitorText = Messages.get("components.screens.unsupported_position", monitorText);
        }
        addDetail(details, row++, Messages.get("components.screens.monitor"), monitorText);
        addDetail(details, row++, Messages.get("components.screens.position"),
            "x " + Math.round(display.getX() - monitor.getX()) + " · y " + Math.round(display.getY() - monitor.getY()));
      }
      else {
        addDetail(details, row++, Messages.get("components.screens.position"), "x " + display.getX() + " · y " + display.getY());
      }
      addDetail(details, row++, Messages.get("components.screens.size"), display.getWidth() + " × " + display.getHeight());
      if (monitor != null && monitor.getWidth() > 0 && monitor.getHeight() > 0) {
        long coverage = Math.round(entry.area() * 100.0 / ((long) monitor.getWidth() * monitor.getHeight()));
        addDetail(details, row++, Messages.get("components.screens.coverage"), Messages.get("components.screens.coverage_value", coverage));
      }
      if (display.getRotation() != 0) {
        addDetail(details, row, Messages.get("components.screens.rotation"), display.getRotation() + "°");
      }
      inspector.getChildren().addAll(head, details);

      List<ScreenEntry> others = new ArrayList<>(getStacks().getOrDefault(entry.positionKey(), Collections.emptyList()));
      others.removeIf(o -> o.id().equals(entry.id()));
      if (!others.isEmpty() && !hidden.contains(entry.id())) {
        VBox stackBox = new VBox(8);
        stackBox.getStyleClass().add("sm-inset");
        stackBox.setPadding(new Insets(12));
        Label title = new Label(others.size() == 1 ? Messages.get("components.screens.same_position_one")
            : Messages.get("components.screens.same_position", others.size()));
        title.getStyleClass().add("sm-text");
        title.setWrapText(true);
        FlowPane chips = new FlowPane(6, 6);
        for (ScreenEntry other : others) {
          Button chip = new Button(other.name());
          chip.getStyleClass().add("sm-chip");
          chip.setOnAction(e -> select(other.id()));
          chips.getChildren().add(chip);
        }
        stackBox.getChildren().addAll(title, chips);
        inspector.getChildren().add(stackBox);
      }
    }
    else {
      Label name = new Label(selectedMonitor.getFormattedName());
      name.getStyleClass().add("sm-title");
      head.getChildren().addAll(name, createSourceChip(Messages.get("components.screens.monitor"), "#BDBDBD"));

      int row = 0;
      addDetail(details, row++, Messages.get("components.screens.id"), String.valueOf(selectedMonitor.getId()));
      addDetail(details, row++, Messages.get("components.screens.position"),
          "x " + Math.round(selectedMonitor.getX()) + " · y " + Math.round(selectedMonitor.getY()));
      addDetail(details, row++, Messages.get("components.screens.size"), selectedMonitor.getWidth() + " × " + selectedMonitor.getHeight());
      addDetail(details, row++, Messages.get("components.screens.primary"),
          Messages.get(selectedMonitor.isPrimary() ? "components.screens.yes" : "components.screens.no"));
      if (selectedMonitor.getScaling() > 0) {
        addDetail(details, row++, Messages.get("components.screens.scaling"), Math.round(selectedMonitor.getScaling() * 100) + "%");
      }
      long screenCount = entries.stream().filter(e -> !hidden.contains(e.id()) && selectedMonitor.equals(getMonitorOf(e.display()))).count();
      addDetail(details, row, Messages.get("components.screens.screens_on_monitor"), String.valueOf(screenCount));
      inspector.getChildren().addAll(head, details);

      if (hasError(selectedMonitor)) {
        String monitorName = selectedMonitor.getFormattedName();
        VBox errorBox = new VBox(4);
        errorBox.getStyleClass().add("sm-alert");
        errorBox.setPadding(new Insets(10, 12, 10, 12));
        errors.stream().filter(e -> e.contains(monitorName)).forEach(e -> {
          Label l = new Label(e);
          l.setWrapText(true);
          l.getStyleClass().add("sm-alert-text");
          errorBox.getChildren().add(l);
        });
        inspector.getChildren().add(errorBox);
      }
    }
  }

  private void addDetail(GridPane grid, int row, String key, String value) {
    Label keyLabel = new Label(key);
    keyLabel.getStyleClass().add("sm-hint");
    Label valueLabel = new Label(value);
    valueLabel.getStyleClass().add("sm-text");
    valueLabel.setWrapText(true);
    grid.add(keyLabel, 0, row);
    grid.add(valueLabel, 1, row);
  }

  private Label createSourceChip(String text, String color) {
    Label chip = new Label(text);
    chip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-background-color: " + color
        + "26; -fx-background-radius: 11; -fx-padding: 3 10 3 10;");
    return chip;
  }
}
