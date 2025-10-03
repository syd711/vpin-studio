package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.tables.validation.GameEmulatorValidationTexts;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.utils.WidgetFactory.DISABLED_COLOR;
import static de.mephisto.vpin.ui.Studio.client;

public class EmulatorsTableController extends BaseTableController<GameEmulatorRepresentation, EmulatorModel> implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorsTableController.class);

  @FXML
  private Node root;

  @FXML
  private Pane validationError;

  @FXML
  private Label validationErrorLabel;

  @FXML
  private Label validationErrorText;

  @FXML
  TableColumn<EmulatorModel, EmulatorModel> columnSelection;

  @FXML
  TableColumn<EmulatorModel, EmulatorModel> columnName;

  @FXML
  TableColumn<EmulatorModel, EmulatorModel> columnDescription;

  @FXML
  TableColumn<EmulatorModel, EmulatorModel> columnGamesDir;

  @FXML
  TableColumn<EmulatorModel, EmulatorModel> columnExtension;

  private List<EmulatorModel> filteredData;
  private EmulatorsController emulatorsController;

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
          List<GameEmulatorRepresentation> gameEmulators = client.getEmulatorService().getValidatedGameEmulators();
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
    validationError.setVisible(false);

    BaseLoadingColumn.configureColumn(columnSelection, (value, model) -> {
      List<ValidationState> validationState = value.getValidationStates();
      FontIcon statusIcon = WidgetFactory.createCheckIcon(getIconColor(model));
      if (!validationState.isEmpty()) {
        ValidationState v = validationState.get(0);
        statusIcon = WidgetFactory.createExclamationIcon(getIconColor(model));
      }

      HBox hbox = new HBox(6);
      hbox.setAlignment(Pos.CENTER);

      Label label = new Label();
      label.getStyleClass().add("default-text");
      label.setGraphic(statusIcon);

      hbox.getChildren().add(label);
      return hbox;
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

    BaseLoadingColumn.configureColumn(columnGamesDir, (value, model) -> {
      Label label = new Label(model.getGamesDirectory());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(model));
      label.setTooltip(new Tooltip(model.getGamesDirectory()));
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnExtension, (value, model) -> {
      Label label = new Label(model.getGameExt());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(model));
      label.setTooltip(new Tooltip(model.getGameExt()));
      return label;
    }, this, true);

    tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EmulatorModel>() {
      @Override
      public void changed(ObservableValue<? extends EmulatorModel> observable, EmulatorModel oldValue, EmulatorModel newValue) {
        if (newValue != null && !newValue.isValid()) {
          validationError.setVisible(true);
          LocalizedValidation validationMessage = GameEmulatorValidationTexts.validate(newValue.getBean());
          validationErrorLabel.setText(validationMessage.getLabel());
          validationErrorText.setText(validationMessage.getText());
        }
        else {
          validationError.setVisible(false);
        }
        refresh(newValue == null ? Optional.empty() : Optional.of(newValue.getBean()));
      }
    });

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    columnDescription.setVisible(frontendType.equals(FrontendType.Popper));

    reload();

    List<EmulatorModel> items = tableView.getItems();
    if(!items.isEmpty()) {
      tableView.getSelectionModel().select(0);
    }
  }

  private void refresh(Optional<GameEmulatorRepresentation> emulator) {
    emulatorsController.setSelection(emulator);
  }

  private static String getIconColor(EmulatorModel value) {
    if (!value.isEnabled()) {
      return DISABLED_COLOR;
    }
    return null;
  }

  private static String getLabelCss(EmulatorModel value) {
    String status = "";
    if (!value.isEnabled()) {
      status = WidgetFactory.DISABLED_TEXT_STYLE;
    }
    else if (!value.isValid()) {
      return WidgetFactory.ERROR_STYLE;
    }
    return status;
  }

  public void select(GameEmulatorRepresentation gameEmulatorRepresentation) {
    EmulatorModel model = toModel(gameEmulatorRepresentation);
    tableView.getSelectionModel().select(model);
  }

  @Override
  protected EmulatorModel toModel(GameEmulatorRepresentation emulatorRepresentation) {
    return new EmulatorModel(emulatorRepresentation);
  }

  public void setEmulatorController(EmulatorsController emulatorsController) {
    this.emulatorsController = emulatorsController;
  }
}
