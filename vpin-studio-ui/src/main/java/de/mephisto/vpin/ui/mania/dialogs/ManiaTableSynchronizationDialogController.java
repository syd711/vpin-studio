package de.mephisto.vpin.ui.mania.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.restclient.mania.ManiaTableSyncResult;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaTableSynchronizationDialogController implements DialogController, Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaTableSynchronizationDialogController.class);

  @FXML
  private Label statsLabel;

  @FXML
  private TableView<ManiaTableSynchronizationDialogController.ManiaTableSyncResultModel> tableView;

  @FXML
  private TableColumn<ManiaTableSynchronizationDialogController.ManiaTableSyncResultModel, String> nameColumn;

  @FXML
  private TableColumn<ManiaTableSynchronizationDialogController.ManiaTableSyncResultModel, String> accountColumn;

  @FXML
  private TableColumn<ManiaTableSynchronizationDialogController.ManiaTableSyncResultModel, String> scoreColumn;

  @FXML
  private TableColumn<ManiaTableSynchronizationDialogController.ManiaTableSyncResultModel, String> statusColumn;

  @Override
  public void onDialogCancel() {

  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    tableView.setPlaceholder(new Label("No synchronization data found."));
    tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    nameColumn.setCellValueFactory(cellData -> {
      ManiaTableSyncResultModel value = cellData.getValue();
      return new SimpleObjectProperty(value.getTableName());
    });

    accountColumn.setCellValueFactory(cellData -> {
      ManiaTableSyncResultModel value = cellData.getValue();
      return new SimpleObjectProperty(value.getAccount());
    });

    scoreColumn.setCellValueFactory(cellData -> {
      ManiaTableSyncResultModel value = cellData.getValue();
      return new SimpleObjectProperty(value.getScore());
    });

    statusColumn.setCellValueFactory(cellData -> {
      ManiaTableSyncResultModel value = cellData.getValue();

      FontIcon statusIcon = WidgetFactory.createCheckIcon(null);
      Label statusLabel = new Label();
      if (value.isDenied()) {
        statusIcon = WidgetFactory.createExclamationIcon(null);
        statusLabel.setTooltip(new Tooltip("This highscore is on the deny list."));
      }
      else {
        statusLabel.setTooltip(new Tooltip("Synchronization successful."));
      }
      statusLabel.setGraphic(statusIcon);
      return new SimpleObjectProperty(statusLabel);
    });

  }

  public void setSynchronizationResult(List<ManiaTableSyncResult> result) {
    List<ManiaTableSyncResultModel> models = result.stream().map(r -> new ManiaTableSyncResultModel(r)).collect(Collectors.toList());
    tableView.setItems(FXCollections.observableList(models));

    statsLabel.setText(result.stream().filter(r -> !r.isDenied()).count() + " highscore have been submitted to vpin-mania.net.");
  }

  static class ManiaTableSyncResultModel {

    private final ManiaTableSyncResult result;

    public ManiaTableSyncResultModel(ManiaTableSyncResult result) {
      this.result = result;
    }

    public String getTableName() {
      return result.getTableScore().getTableName();
    }

    public String getScore() {
      return ScoreFormatUtil.formatScore(result.getTableScore().getScore(), Locale.getDefault());
    }

    public boolean isDenied() {
      return result.isDenied();
    }

    public String getAccount() {
      Account account = maniaClient.getAccountClient().getAccount(result.getTableScore().getAccountId());
      return account.getDisplayName() + " [" + account.getInitials() + "]";
    }
  }
}
