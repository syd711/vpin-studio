package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.localsettings.BaseTableSettings;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class BaseLoadingColumn {
  private static final Debouncer debouncer = new Debouncer();

  public static <T, M extends BaseLoadingModel<T, M>> void configureColumn(
      TableColumn<M, M> column, BaseLoadingColumnRenderer<T, M> renderer, BaseTableController<T, M> baseTableController, boolean visible) {
    column.setVisible(visible);
    BaseTableSettings tableSettings = baseTableController.getTableSettings();
    double columnWidth = tableSettings != null ? tableSettings.getColumnWidth(column.getId()) : 0;
    if (columnWidth > 0) {
      column.setPrefWidth(columnWidth);
    }
    column.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        debouncer.debounce(column.getId(), () -> {
          tableSettings.getColumnWith().put(column.getId(), newValue.doubleValue());
          tableSettings.save();
        }, 300);
      }
    });

    column.setCellValueFactory(cellData -> {
      M model = cellData.getValue();
      return model;
    });
    column.setCellFactory(cellData -> {
      TableCell<M, M> cell = new TableCell<>();
      cell.itemProperty().addListener((obs, old, model) -> {
        if (model != null) {
          Node node = renderer.render(model.getBean(), model);
          cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).then((Node) null).otherwise(node));
        }
      });
      return cell;
    });
  }

  public static <T, M extends BaseLoadingModel<T, M>> void configureLoadingColumn(
      TableColumn<M, M> column, Callback<TableColumn<M, M>, TableCell<M, M>> factory) {

    column.setCellValueFactory(cellData -> cellData.getValue());
    column.setCellFactory(factory);
  }

  public static <T, M extends BaseLoadingModel<T, M>> void configureLoadingColumn(
      TableColumn<M, M> column,
      String loading, BaseLoadingColumnRenderer<T, M> renderer) {

    //if (true) { configureColumn(column, renderer); return; }

    column.setCellValueFactory(cellData -> cellData.getValue());
    column.setCellFactory(cellData -> new BaseLoadingTableCell<M>() {

      @Override
      protected String getLoading(M model) {
        return loading;
      }

      @Override
      protected void renderItem(M model) {
        Node node = renderer.render(model.getBean(), model);
        setGraphic(node);
      }
    });
  }
}
