package de.mephisto.vpin.ui.tables.panels;

import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class BaseLoadingColumn {

  public static <T, M extends BaseLoadingModel<T, M>> void configureColumn(
        TableColumn<M, M> column, BaseLoadingColumnRenderer<T, M> renderer, boolean visible) {
    column.setVisible(visible);
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
