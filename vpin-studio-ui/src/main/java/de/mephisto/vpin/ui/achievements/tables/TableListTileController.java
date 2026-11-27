package de.mephisto.vpin.ui.achievements.tables;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.TableAchievements;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.ui.Studio;
import javafx.beans.property.ReadOnlyObjectProperty;
import de.mephisto.vpin.ui.util.SegmentedToggles;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;
import static de.mephisto.vpin.ui.achievements.tables.TablesTabController.GLOBAL;

/**
 * Tables tile: searchable and filterable list of the tables with their achievement progress.
 */
public class TableListTileController implements Initializable {

  @FXML
  private TextField searchField;
  @FXML
  private Button clearBtn;
  @FXML
  private ToggleGroup filterGroup;
  @FXML
  private ToggleButton allBtn;
  @FXML
  private ToggleButton inProgressBtn;
  @FXML
  private ToggleButton completedBtn;
  @FXML
  private ToggleButton notStartedBtn;
  @FXML
  private ToggleButton noMapBtn;
  @FXML
  private ListView<TableAchievements> tableList;

  private List<TableAchievements> tables = new ArrayList<>();
  private String selectRom;
  private Consumer<TableAchievements> onSelect = t -> {
  };

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    searchField.textProperty().addListener((obs, o, n) -> {
      clearBtn.setVisible(n != null && !n.isEmpty());
      applyFilter();
    });
    SegmentedToggles.keepSelected(allBtn, inProgressBtn, completedBtn, notStartedBtn, noMapBtn);
    filterGroup.selectedToggleProperty().addListener((obs, o, n) -> {
      if (n == null) {
        o.setSelected(true);
      }
      applyFilter();
    });
    tableList.setCellFactory(lv -> new TableCell());
    tableList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
      if (n != null) {
        onSelect.accept(n);
      }
    });
    applyFilter();
  }

  public void setOnSelect(Consumer<TableAchievements> onSelect) {
    this.onSelect = onSelect;
  }

  public ReadOnlyObjectProperty<TableAchievements> selectedTableProperty() {
    return tableList.getSelectionModel().selectedItemProperty();
  }

  /**
   * Sets the tables and selects the pending ROM or the first table with achievements.
   */
  public void setTables(List<TableAchievements> tables) {
    TableAchievements selected = tableList.getSelectionModel().getSelectedItem();
    this.tables = tables;
    applyFilter();
    if (selectRom == null && selected != null) {
      // the list items are new instances after a reload, keep the selection by ROM
      selectRom = selected.rom;
    }
    if (!selectPending() && !tableList.getItems().isEmpty()) {
      TableAchievements first = tableList.getItems().stream().filter(t -> !GLOBAL.equals(t.rom) && t.hasMap && t.total > 0).findFirst()
          .orElse(tableList.getItems().get(0));
      tableList.getSelectionModel().select(first);
    }
  }

  public void selectRom(String rom) {
    this.selectRom = rom != null ? rom.trim() : null;
    if (selectRom != null && tables.stream().anyMatch(t -> selectRom.equalsIgnoreCase(t.rom))
        && tableList.getItems().stream().noneMatch(t -> selectRom.equalsIgnoreCase(t.rom))) {
      // the requested table is hidden by the current filter, reset it
      searchField.setText("");
      filterGroup.selectToggle(allBtn);
    }
    selectPending();
  }

  private boolean selectPending() {
    if (selectRom == null) {
      return false;
    }
    for (TableAchievements t : tableList.getItems()) {
      if (selectRom.equalsIgnoreCase(t.rom)) {
        tableList.getSelectionModel().select(t);
        tableList.scrollTo(t);
        selectRom = null;
        return true;
      }
    }
    return false;
  }

  @FXML
  private void onClear() {
    searchField.setText("");
  }

  private void applyFilter() {
    int inProgress = 0, completed = 0, notStarted = 0, noMap = 0;
    for (TableAchievements t : tables) {
      if (GLOBAL.equals(t.rom)) continue;
      switch (t.status) {
        case TableAchievements.STATUS_IN_PROGRESS -> inProgress++;
        case TableAchievements.STATUS_COMPLETED -> completed++;
        case TableAchievements.STATUS_NOT_STARTED -> notStarted++;
        default -> noMap++;
      }
    }
    allBtn.setText(Messages.get("achievements.filter.all") + " · " + (tables.isEmpty() ? 0 : tables.size() - 1));
    inProgressBtn.setText(Messages.get("achievements.filter.in_progress") + " · " + inProgress);
    completedBtn.setText(Messages.get("achievements.filter.completed") + " · " + completed);
    notStartedBtn.setText(Messages.get("achievements.filter.not_started") + " · " + notStarted);
    noMapBtn.setText(Messages.get("achievements.filter.no_map") + " · " + noMap);

    String term = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
    String status = filterGroup.getSelectedToggle() == inProgressBtn ? TableAchievements.STATUS_IN_PROGRESS
        : filterGroup.getSelectedToggle() == completedBtn ? TableAchievements.STATUS_COMPLETED
        : filterGroup.getSelectedToggle() == notStartedBtn ? TableAchievements.STATUS_NOT_STARTED
        : filterGroup.getSelectedToggle() == noMapBtn ? TableAchievements.STATUS_NO_MAP : null;
    TableAchievements selected = tableList.getSelectionModel().getSelectedItem();
    String selectedRom = selected != null ? selected.rom : null;
    List<TableAchievements> filtered = new ArrayList<>();
    for (TableAchievements t : tables) {
      boolean global = GLOBAL.equals(t.rom);
      if (status != null && (global || !status.equals(t.status))) continue;
      if (!term.isEmpty() && !(t.gameName != null && t.gameName.toLowerCase().contains(term)) && !(t.rom != null && t.rom.toLowerCase().contains(term))) continue;
      filtered.add(t);
    }
    tableList.getItems().setAll(filtered);
    if (selectedRom != null) {
      filtered.stream().filter(t -> selectedRom.equals(t.rom)).findFirst().ifPresent(t -> tableList.getSelectionModel().select(t));
    }
  }

  private static final int WHEEL_SIZE = 36;
  private static Image blankWheel;
  /** Loaded wheel thumbnails by game id; the blank image marks games without a wheel. */
  private static final Map<Integer, Image> wheelCache = new ConcurrentHashMap<>();

  private static Image getBlankWheel() {
    if (blankWheel == null) {
      blankWheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"), WHEEL_SIZE, WHEEL_SIZE, true, true);
    }
    return blankWheel;
  }

  private static class TableCell extends ListCell<TableAchievements> {
    @Override
    protected void updateItem(TableAchievements t, boolean empty) {
      super.updateItem(t, empty);
      if (empty || t == null) {
        setGraphic(null);
        setText(null);
        setCursor(Cursor.DEFAULT);
        return;
      }
      HBox row = new HBox(10);
      row.setAlignment(Pos.CENTER_LEFT);
      VBox box = new VBox(5);
      HBox line = new HBox(8);
      Label name = bold(t.gameName != null ? t.gameName : t.rom);
      name.setMaxWidth(210);
      name.underlineProperty().bind(hoverProperty());
      setCursor(Cursor.HAND);
      box.setMaxWidth(284);
      box.setPrefWidth(284);
      String count = !t.hasMap ? Messages.get("achievements.no_map_short")
          : GLOBAL.equals(t.rom) ? "" : t.total == 0 ? Messages.get("achievements.no_rules_short") : t.unlocked + "/" + t.total;
      line.getChildren().addAll(name, spacer(), muted(count));
      box.getChildren().add(line);
      if (t.hasMap && t.total > 0 && !GLOBAL.equals(t.rom)) {
        box.getChildren().add(progress(Math.max(t.getProgressPct(), t.unlocked > 0 ? 2 : 0), isSelected() ? ORANGE : BLUE, 5));
      }
      row.getChildren().addAll(wheel(t), box);
      setGraphic(row);
      setText(null);
    }

    private StackPane wheel(TableAchievements t) {
      StackPane wheelBox = new StackPane();
      wheelBox.setMinSize(WHEEL_SIZE, WHEEL_SIZE);
      wheelBox.setPrefSize(WHEEL_SIZE, WHEEL_SIZE);
      if (GLOBAL.equals(t.rom)) {
        wheelBox.getChildren().add(icon("mdi2t-trophy-variant", ORANGE, 28));
        return wheelBox;
      }
      Image cached = t.gameId > 0 ? wheelCache.get(t.gameId) : getBlankWheel();
      ImageView view = new ImageView(cached != null ? cached : getBlankWheel());
      wheelBox.getChildren().add(view);
      if (cached == null) {
        int gameId = t.gameId;
        JFXFuture.supplyAsync(() -> {
              try (InputStream in = client.getWheelIcon(gameId, true)) {
                Image img = in != null ? new Image(in, WHEEL_SIZE, WHEEL_SIZE, true, true) : null;
                return img != null && !img.isError() ? img : getBlankWheel();
              }
              catch (Exception e) {
                return getBlankWheel();
              }
            })
            .thenAcceptLater(img -> {
              wheelCache.put(gameId, img);
              if (getItem() == t) {
                view.setImage(img);
              }
            });
      }
      return wheelBox;
    }
  }
}
