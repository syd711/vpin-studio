package de.mephisto.vpin.ui.mania.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.User;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class FriendSearchDialogController implements DialogController, Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(FriendSearchDialogController.class);
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private TextField nameField;

  @FXML
  private Button okButton;

  @FXML
  private TableView<Cabinet> cabinetsTable;

  @FXML
  private TableColumn<Cabinet, Object> avatarColumn;
  @FXML
  private TableColumn<Cabinet, Object> nameColumn;
  @FXML
  private TableColumn<Cabinet, Object> ownerColumn;


  private Stage stage;
  private User user;

  @Override
  public void onDialogCancel() {

  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDialogSubmit(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
//    maniaClient.getContactClient().createInvite(myCabinet.getId(), search.get(0));
    stage.close();
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    this.nameField.requestFocus();
  }

  private void search(@NonNull String text) {
    if (!StringUtils.isEmpty(text)) {
      try {
        List<User> search = maniaClient.getUserClient().search(text);
        if (search.size() == 1) {
          user = search.get(0);
          List<Cabinet> cabinetsByUserUuid = maniaClient.getCabinetClient().getCabinetsByUserUuid(user.getUuid());
          cabinetsTable.setItems(FXCollections.observableList(cabinetsByUserUuid));
          cabinetsTable.refresh();
        }
        else {
          Platform.runLater(() -> {
            cabinetsTable.setItems(FXCollections.emptyObservableList());
            cabinetsTable.setPlaceholder(new Label("No exact user match found."));
            cabinetsTable.refresh();
          });
        }
      }
      catch (Exception e) {
        LOG.error("User search failed: {}", e.getMessage(), e);
        Platform.runLater(() -> {
          WidgetFactory.showInformation(stage, "User Search Failed", e.getMessage());
        });
      }
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    cabinetsTable.setPlaceholder(new Label("No users searched yet."));
    okButton.setDisable(true);

    nameColumn.setCellValueFactory(cellData -> {
      Cabinet value = cellData.getValue();
      Label label = new Label(value.getDisplayName());
      label.getStyleClass().add("default-text");
      return new SimpleObjectProperty<>(label);
    });
    ownerColumn.setCellValueFactory(cellData -> {
      Cabinet value = cellData.getValue();
      Label label = new Label(user.getEmail());
      label.getStyleClass().add("default-text");
      return new SimpleObjectProperty<>(label);
    });
    avatarColumn.setCellValueFactory(cellData -> {
      Cabinet value = cellData.getValue();
      Label label = new Label("");
      return new SimpleObjectProperty<>(label);
    });

    nameField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        debouncer.debounce("search", () -> {
          if (newValue.length() >= 3) {
            search(newValue);
          }
        }, 500);

      }
    });
  }
}
