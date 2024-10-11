package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.ui.WaitOverlay;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.util.Keys;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

  protected ObservableList<M> models;

  protected FilteredList<M> filteredModels;

  private BaseColumnSorter<M> columnSorter;

  private WaitOverlay loadingOverlay;

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

  //----------------------
  // Key Pressed

  private long lastKeyInputTime = System.currentTimeMillis();
  private String lastKeyInput = "";

  //----------------------

  public void setRootController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

  protected void initialize(String name, String names, BaseColumnSorter<M> columnSorter) {
    this.columnSorter = columnSorter;
    this.name = name;
    this.names = names;

    loadingOverlay = new WaitOverlay(loaderStack, null);
    this.clearBtn.setVisible(false);

    registerKeyPressed();
  }

  protected void loadFilterPanel(String resource) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
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

  @FXML
  protected void onDelete(Event e) {
  }

  protected void registerKeyPressed() {

    tableView.setOnKeyPressed(event -> {
      if (Keys.isSpecial(event)) {
        KeyCode code = event.getCode();
        switch (code) {
          case DELETE:
            //onDelete(event);
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

      for (M model : filteredModels) {
        if (model.getName().toLowerCase().startsWith(text.toLowerCase())) {
          setSelection(model, true);
          break;
        }
      }
    });
  }

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

  public void startReload(String message) {
    loadingOverlay.setBusy(message, true);
    this.searchTextField.setDisable(true);
    this.filterBtn.setDisable(true);
    this.reloadBtn.setDisable(true);
    this.labelCount.setText(null);
  }

  public void endReload() {
    this.searchTextField.setDisable(false);
    this.filterBtn.setDisable(false);
    this.reloadBtn.setDisable(false);
    loadingOverlay.setBusy("", false);

    tableView.requestFocus();
  }

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
}
