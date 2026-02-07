package de.mephisto.vpin.ui.mania.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.Contact;
import de.mephisto.vpin.connectors.mania.model.User;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.util.ManiaHelper;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
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

    Cabinet selectedItem = cabinetsTable.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Cabinet myCabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();

      if(myCabinet.getUuid().equalsIgnoreCase(selectedItem.getUuid())) {
        WidgetFactory.showInformation(stage, "Invalid Cabinet Id", "You can not invite yourself.");
        return;
      }

      try {
        Contact invite = maniaClient.getContactClient().createInvite(myCabinet.getId(), selectedItem.getUuid());
        if (invite != null) {
          User userByCabinetUuid = maniaClient.getUserClient().getUserByCabinetUuid(selectedItem.getUuid());
          WidgetFactory.showAlert(stage, "Invite Send", "The invite has been sent to " + userByCabinetUuid.getEmail());
        }
      }
      catch (Exception ex) {
        LOG.error("Failed to send invite: {}", ex.getMessage(), ex);
        WidgetFactory.showAlert(stage, "Error", "Failed to send invite: " + ex.getMessage());
      }
    }

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
      InputStream cachedUrlImage = client.getCachedUrlImage(ManiaHelper.getCabinetAvatarUrl(value));
      if (cachedUrlImage == null) {
        cachedUrlImage = Studio.class.getResourceAsStream("avatar-blank.png");
      }
      Image image = new Image(cachedUrlImage);
      ImageView view = new ImageView(image);
      view.setPreserveRatio(true);
      view.setFitWidth(UIDefaults.DEFAULT_AVATARSIZE);
      view.setFitHeight(UIDefaults.DEFAULT_AVATARSIZE);
      CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
      return new SimpleObjectProperty(view);
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

    cabinetsTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Cabinet>() {
      @Override
      public void changed(ObservableValue<? extends Cabinet> observable, Cabinet oldValue, Cabinet newValue) {
        okButton.setDisable(newValue == null);
      }
    });
  }
}
