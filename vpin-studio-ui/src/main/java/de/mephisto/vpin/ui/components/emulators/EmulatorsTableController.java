package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.dialogs.MediaUploaderColumnSorter;
import de.mephisto.vpin.ui.tables.models.MediaUploadArchiveItem;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class EmulatorsTableController extends BaseTableController<GameEmulatorRepresentation, EmulatorModel> implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorsTableController.class);

  @FXML
  private Node root;

  @FXML
  TableColumn<EmulatorModel, EmulatorModel> columnSelection;

  @FXML
  TableColumn<EmulatorModel, EmulatorModel> columnName;

  @FXML
  TableColumn<EmulatorModel, EmulatorModel> columnDescription;

  private List<EmulatorModel> filteredData;

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {

      }
    }
  }

  private void doReload() {
    startReload("Loading Emulators...");

    // run later to let the splash render properly
    JFXFuture.runAsync(() -> {
          List<GameEmulatorRepresentation> gameEmulators = client.getFrontendService().getGameEmulators();
          filteredData = gameEmulators.stream().map(e -> toModel(e)).collect(Collectors.toList());
        })
        .thenLater(() -> {
          try {
            tableView.setItems(FXCollections.observableList(filteredData));
            this.labelCount.setText(filteredData.size() + " emulators");
            tableView.refresh();

            endReload();
          }
          catch (Exception e) {
            LOG.error("Emulator refresh failed: {}", e.getMessage(), e);
          }
        });
  }

  public void reload() {
    doReload();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("emulator", "emulators", new EmulatorsTableColumnSorter(this));

    BaseLoadingColumn.configureColumn(columnSelection, (value, model) -> {
      CheckBox columnCheckbox = new CheckBox();
      columnCheckbox.setUserData(value);
      columnCheckbox.setSelected(model.isEnabled());
      columnCheckbox.getStyleClass().add("default-text");
      columnCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          model.setEnabled(newValue);
          model.save();
          tableView.refresh();
        }
      });
      return columnCheckbox;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnName, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(model));
      label.setTooltip(new Tooltip(model.getName()));
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnDescription, (value, model) -> {
      Label label = new Label(model.getDescription());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(model));
      label.setTooltip(new Tooltip(model.getDescription()));
      return label;
    }, this, true);

    reload();
  }

  private static String getLabelCss(EmulatorModel value) {
    String status = "";
    if (!value.isEnabled()) {
      status = WidgetFactory.DISABLED_TEXT_STYLE;
    }
    return status;
  }

  @Override
  protected EmulatorModel toModel(GameEmulatorRepresentation emulatorRepresentation) {
    return new EmulatorModel(emulatorRepresentation);
  }
}
