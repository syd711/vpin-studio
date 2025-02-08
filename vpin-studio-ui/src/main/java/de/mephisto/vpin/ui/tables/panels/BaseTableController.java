package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.localsettings.BaseTableSettings;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.WaitOverlay;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.util.Keys;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public abstract class BaseTableController<T, M extends BaseLoadingModel<T, M>> {
  private final static Logger LOG = LoggerFactory.getLogger(BaseTableController.class);

  @FXML
  protected StackPane loaderStack;

  @FXML
  protected StackPane tableStack;

  @FXML
  protected TableView<M> tableView;

  @FXML
  protected Label labelCount;

  private String name;
  private String names;

  protected TablesController tablesController;

  protected ObservableList<M> models = FXCollections.observableArrayList();

  protected FilteredList<M> filteredModels;

  private BaseColumnSorter<M> columnSorter;

  protected WaitOverlay loadingOverlay;

  @FXML
  protected Button reloadBtn;

  //----------------------
  // Filters

  @FXML
  protected Button filterBtn;

  @FXML
  protected TextField searchTextField;

  @FXML
  protected Button clearBtn;

  protected BaseFilterController<T, M> filterController;

  @FXML
  protected ComboBox<PlaylistRepresentation> playlistCombo;

  //----------------------
  // Key Pressed

  private long lastKeyInputTime = System.currentTimeMillis();
  private String lastKeyInput = "";

  //----------------------
  // UI Settings
  private BaseTableSettings baseTableSettings;

  //----------------------

  public void setRootController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

  public BaseTableSettings getTableSettings() {
    if (this.baseTableSettings == null) {
      baseTableSettings = LocalUISettings.getTablePreference(this.getClass());
    }
    return baseTableSettings;
  }

  protected void initialize(String name, String names, BaseColumnSorter<M> columnSorter) {
    this.columnSorter = columnSorter;
    this.name = name;
    this.names = names;

    loadingOverlay = new WaitOverlay(loaderStack, null);
    if (this.clearBtn != null) {
      this.clearBtn.setVisible(false);
    }

    List<String> columnOrder = getTableSettings().getColumnOrder();
    if (!columnOrder.isEmpty()) {
      for (String columnName : columnOrder) {
        Optional<TableColumn<M, ?>> first = tableView.getColumns().stream().filter(c -> c.getId().equals(columnName)).findFirst();
        if (first.isPresent()) {
          tableView.getColumns().remove(first.get());
          tableView.getColumns().add(first.get());
        }
        else {
          LOG.warn("{} has no column {} for restoring the order", this.getClass().getSimpleName(), columnName);
        }
      }
    }

    tableView.getColumns().addListener(new ListChangeListener<TableColumn<M, ?>>() {
      @Override
      public void onChanged(Change<? extends TableColumn<M, ?>> c) {
        List<String> reOrderedColumns = c.getList().stream().map(column -> column.getId()).collect(Collectors.toList());
        baseTableSettings.setColumnOrder(reOrderedColumns);
        baseTableSettings.save();
      }
    });

    registerKeyPressed();
  }

  protected void loadFilterPanel(Class<?> clazz, String resource) {
    try {
      FXMLLoader loader = new FXMLLoader(clazz.getResource(resource));
      loader.load();
      filterController = loader.getController();
      filterController.setTableController(this);
      filterController.setupDrawer(filterBtn, tableStack, tableView);
      filterController.bindSearchField(searchTextField, clearBtn);
    }
    catch (IOException e) {
      LOG.error("Failed to load loading filter: " + e.getMessage(), e);
    }
  }

  protected void loadFilterPanel(String resource) {
    loadFilterPanel(this.getClass(), resource);
  }

  @FXML
  protected void onDelete(Event e) {
  }

  protected void loadPlaylistCombo() {
    if (this.playlistCombo != null) {
      this.playlistCombo.managedProperty().bindBidirectional(this.playlistCombo.visibleProperty());

      FrontendType frontendType = client.getFrontendService().getFrontendType();
      if (frontendType.supportPlaylists()) {
        playlistCombo.setCellFactory(c -> new PlaylistBackgroundImageListCell());
        playlistCombo.setButtonCell(new PlaylistBackgroundImageListCell());
        filterController.bindPlaylistField(playlistCombo);
      }
      else {
        playlistCombo.setVisible(false);
      }
    }
  }

  protected void registerKeyPressed() {
    tableView.setOnKeyPressed(event -> {
      if (Keys.isSpecial(event)) {
        KeyCode code = event.getCode();
        switch (code) {
          case DELETE:
            onDelete(event);
            event.consume();
            break;
          default:
        }
        return;
      }

      String text = event.getText();

      long timeDiff = System.currentTimeMillis() - lastKeyInputTime;
      if (timeDiff > 800) {
        lastKeyInputTime = System.currentTimeMillis();
        lastKeyInput = text;
      }
      else {
        lastKeyInputTime = System.currentTimeMillis();
        lastKeyInput = lastKeyInput + text;
        text = lastKeyInput;
      }

      if (filteredModels != null) {
        for (M model : filteredModels) {
          if (model.getName().toLowerCase().startsWith(text.toLowerCase())) {
            setSelection(model, true);
            break;
          }
        }
      }
    });
  }

  //----------------------
  // Filtering

  @FXML
  private void onSearchKeyPressed(KeyEvent e) {
    if (e.getCode().equals(KeyCode.ENTER)) {
      tableView.getSelectionModel().select(0);
      tableView.requestFocus();
    }
  }

  @FXML
  private void onClear() {
    searchTextField.setText("");
  }

  @FXML
  private void onFilter() {
    filterController.toggle();
  }

  public void applyFilter() {
    // mind that it can be called by threads before data is even initialized
    if (this.filteredModels != null) {
      this.filteredModels.setPredicate(filterController.buildPredicate());
      // update data count
      applyTableCount();
    }
  }

  protected void applyTableCount() {
    labelCount.setText(filteredModels.size() + " " + (filteredModels.size() > 1 ? names : name));
  }

  //----------------------
  // Reload management (on tables)

  public void startReload(String message) {
    loadingOverlay.setBusy(message, true);

    if (searchTextField != null) {
      this.searchTextField.setDisable(true);
    }
    if (filterBtn != null) {
      this.filterBtn.setDisable(true);
    }
    if (reloadBtn != null) {
      this.reloadBtn.setDisable(true);
    }

    this.labelCount.setText(null);
  }

  /**
   * Reload just the selected item in the table
   */
  public void reloadSelection() {
    reloadItem(getSelection());
  }

  /**
   * Reload just one item in the table
   *
   * @param The item in the table to be reloaded
   */
  public void reloadItem(T bean) {
    if (bean != null) {
      try {
        M model = getModel(bean);
        if (model != null) {
          model.setBean(bean);
          model.reload();

          // refresh views too if the game is selected
          T selected = getSelection();
          if (selected != null && model.sameBean(selected)) {
            refreshView(model.getBean());
          }
        }
        else {
          model = toModel(bean);
          if (model != null) {
            models.add(0, model);
          }
        }
        // force refresh the view for elements not observed by the table
        tableView.refresh();
      }
      catch (Exception ex) {
        LOG.error("Reload of item failed: " + ex.getMessage(), ex);
      }
    }
  }

  public void endReload() {
    if (searchTextField != null) {
      this.searchTextField.setDisable(false);
    }
    if (filterBtn != null) {
      this.filterBtn.setDisable(false);
    }
    if (reloadBtn != null) {
      this.reloadBtn.setDisable(false);
    }
    loadingOverlay.setBusy("", false);
    tableView.requestFocus();
  }

  /**
   * Refresh the sidebar view
   */
  protected void refreshView(T bean) {
  }

  //----------------------

  public void setBusy(String message, boolean b) {
    loadingOverlay.setBusy(message, b);
  }

  //----------------------

  protected void setItems(List<T> data) {
    this.models = FXCollections.observableArrayList();
    for (T bean : data) {
      models.add(toModel(bean));
    }

    // Wrap games in a FilteredList
    if (filterController != null) {
      this.filteredModels = new FilteredList<>(models, filterController.buildPredicate());

      // Wrap the FilteredList in a SortedList
      SortedList<M> sortedData = new SortedList<>(this.filteredModels);
      // Bind the SortedList comparator to the TableView comparator.
      sortedData.comparatorProperty().bind(Bindings.createObjectBinding(
          () -> columnSorter.buildComparator(tableView),
          tableView.comparatorProperty()));
      // Set a dummy SortPolicy to tell the TableView data is successfully sorted
      tableView.setSortPolicy(tableView -> true);

      // Set the items in the TableView
      tableView.setItems(sortedData);

      // filter the list and refresh number of items
      applyFilter();
    }
    else {
      tableView.setItems(models);
    }
  }

  protected abstract M toModel(T bean);

  //----------------------

  public M getModel(T bean) {
    return models.stream().filter(m -> m.sameBean(bean)).findFirst().orElse(null);
  }

  public List<T> getData() {
    return models.stream().map(m -> m.getBean()).collect(Collectors.toList());
  }

  public M getSelectedModel() {
    return tableView.getSelectionModel().getSelectedItem();
  }

  public T getSelection() {
    M selection = getSelectedModel();
    return selection != null ? selection.getBean() : null;
  }

  public List<T> getSelections() {
    List<M> models = tableView.getSelectionModel().getSelectedItems();
    return models.stream().map(model -> model.getBean()).collect(Collectors.toList());
  }

  public void setSelection(T game) {
    clearSelection();
    selectBeanInModel(game, true);
  }

  public void selectBeanInModel(T bean, boolean scrollToModel) {
    Optional<M> model = models.stream().filter(m -> m.sameBean(bean)).findFirst();
    setSelection(model.orElse(null), scrollToModel);
  }

  public void setSelection(M model, boolean scrollToModel) {
    if (model == null) {
      clearSelection();
    }
    else {
      int index = tableView.getItems().indexOf(model);
      tableView.getSelectionModel().clearAndSelect(index);
      if (scrollToModel) {
        tableView.scrollTo(model);
      }
    }
  }

  public void clearSelection() {
    tableView.getSelectionModel().clearSelection();
  }

  /**
   * Select items of a Combo base on Predicate applied on its items
   */
  public <I> void selectItem(ComboBox<I> combo, Predicate<I> p) {
    for (I item : combo.getItems()) {
      if (item != null && p.test(item)) {
        combo.getSelectionModel().select(item);
      }
    }
  }


  //----------------------
  // Playlists

  public List<PlaylistRepresentation> getPlaylists() {
    return this.playlistCombo.getItems();
  }

  public void updatePlaylist(PlaylistRepresentation playlist) {
    int idx = ListUtils.indexOf(this.playlistCombo.getItems(), p -> p != null && p.getId() == playlist.getId());
    if (idx >= 0) {
      boolean selected = this.playlistCombo.getSelectionModel().isSelected(idx);

      this.playlistCombo.getItems().remove(idx);
      this.playlistCombo.getItems().add(idx, playlist);

      if (selected) {
        this.playlistCombo.getSelectionModel().select(playlist);
      }
    }
  }

  public void refreshPlaylists() {
    PlaylistRepresentation selected = this.playlistCombo.getSelectionModel().getSelectedItem();
    this.playlistCombo.setDisable(true);
    JFXFuture.supplyAsync(() -> client.getPlaylistsService().getPlaylists()).thenAcceptLater(playlists -> {
      List<PlaylistRepresentation> pl = new ArrayList<>(playlists);
      pl.add(0, null);
      playlistCombo.setItems(FXCollections.observableList(pl));

      // reselect same playlist
      if (selected != null) {
        selectItem(playlistCombo, p -> p.getId() == selected.getId());
      }
      this.playlistCombo.setDisable(false);
    });
  }

  //----------------------

  public void onKeyEvent(KeyEvent event) {
    if (event.getCode() == KeyCode.F && event.isControlDown()) {
      searchTextField.requestFocus();
      searchTextField.selectAll();
      event.consume();
    }
    else if (event.getCode() == KeyCode.ESCAPE) {
      if (searchTextField.isFocused()) {
        searchTextField.setText("");
      }
      event.consume();
    }
  }


  //---------------------------------------

  public class PlaylistBackgroundImageListCell extends ListCell<PlaylistRepresentation> {
    public PlaylistBackgroundImageListCell() {
    }

    protected void updateItem(PlaylistRepresentation item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(null);
      setText(null);
      if (item != null) {
        UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
        Label playlistIcon = WidgetFactory.createPlaylistIcon(item, uiSettings);
        setGraphic(playlistIcon);

        setText(" " + item.toString());
      }
    }
  }

  public GameEmulatorRepresentation getEmulatorSelection() {
    return null;
  }
}
