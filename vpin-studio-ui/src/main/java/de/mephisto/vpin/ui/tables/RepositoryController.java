package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.ImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class RepositoryController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(RepositoryController.class);

  @FXML
  private Button deleteBtn;

  @FXML
  private Button importBtn;

  @FXML
  private Button uploadBtn;

  @FXML
  private TextField searchTextField;

  @FXML
  private TableView<VpaDescriptorRepresentation> tableView;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> iconColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> nameColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> directB2SColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> pupPackColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> romColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> popperColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> sizeColumn;

  @FXML
  private TableColumn<VpaDescriptorRepresentation, String> createdAtColumn;

  @FXML
  private StackPane tableStack;

  private Parent loadingOverlay;


  private ObservableList<VpaDescriptorRepresentation> data;
  private List<VpaDescriptorRepresentation> archives;
  private TablesController tablesController;

  // Add a public no-args constructor
  public RepositoryController() {
  }

  @FXML
  private void onImport() {
    VpaDescriptorRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if(selection != null) {
      if (client.isPinUPPopperRunning()) {
        Optional<ButtonType> buttonType = Dialogs.openPopperRunningWarning(Studio.stage);
        if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
          Studio.client.terminatePopper();
          Dialogs.openVpaImportDialog(this, selection);
        }
      }
      else {
        Dialogs.openVpaImportDialog(this, selection);
      }
    }

  }

  @FXML
  private void onUpload() {

  }

  @FXML
  private void onReload() {
    this.searchTextField.setDisable(true);

    VpaDescriptorRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    tableView.getSelectionModel().clearSelection();
    boolean disable = selection == null;
    deleteBtn.setDisable(disable);
    importBtn.setDisable(disable);

    tableView.setVisible(false);
    tableStack.getChildren().add(loadingOverlay);

    new Thread(() -> {
      archives = client.getVpaDescriptors();

      Platform.runLater(() -> {
        data = FXCollections.observableList(filterArchives(archives));
        tableView.setItems(data);
        tableView.refresh();
        if (data.contains(selection)) {
          tableView.getSelectionModel().select(selection);
          deleteBtn.setDisable(false);
          importBtn.setDisable(false);
        }
        else if (!data.isEmpty()) {
          tableView.getSelectionModel().select(0);
        }

        this.searchTextField.setDisable(false);

        tableStack.getChildren().remove(loadingOverlay);
        tableView.setVisible(true);

      });
    }).start();
  }

  @FXML
  private void onDelete() {
    VpaDescriptorRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Archive '" + selection.getFilename() + "'?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.deleteVpa(selection);
        tableView.getSelectionModel().clearSelection();
        onReload();
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Table Repository"));
    tableView.setPlaceholder(new Label("The list of archived tables is shown here."));


    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Archives...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    iconColumn.setCellValueFactory(cellData -> {
      VpaDescriptorRepresentation value = cellData.getValue();
      String thumbnail = value.getManifest().getThumbnail();
      if (thumbnail == null) {
        Image wheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
        ImageView view = new ImageView(wheel);
        view.setPreserveRatio(true);
        view.setFitWidth(70);
        view.setFitHeight(70);
        return new SimpleObjectProperty(view);
      }

      byte[] decode = Base64.getDecoder().decode(thumbnail);
      Image wheel = new Image(new ByteArrayInputStream(decode));
      ImageView view = new ImageView(wheel);
      view.setPreserveRatio(true);
      view.setFitWidth(80);
      view.setFitHeight(80);
      ImageUtil.setClippedImage(view, (int) (wheel.getWidth() / 2));
      return new SimpleObjectProperty(view);
    });

    nameColumn.setCellValueFactory(cellData -> {
      VpaDescriptorRepresentation value = cellData.getValue();
      return new SimpleStringProperty(value.getManifest().getGameDisplayName());
    });

    directB2SColumn.setCellValueFactory(cellData -> {
      VpaDescriptorRepresentation value = cellData.getValue();
      boolean directb2s = value.getManifest().getPackageInfo().isDirectb2s();
      if (directb2s) {
        return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
      }
      return new SimpleStringProperty("");
    });

    pupPackColumn.setCellValueFactory(cellData -> {
      VpaDescriptorRepresentation value = cellData.getValue();
      boolean packaged = value.getManifest().getPackageInfo().isPupPack();
      if (packaged) {
        return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
      }
      return new SimpleStringProperty("");
    });

    popperColumn.setCellValueFactory(cellData -> {
      VpaDescriptorRepresentation value = cellData.getValue();
      boolean packaged = value.getManifest().getPackageInfo().isPopperMedia();
      if (packaged) {
        return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
      }
      return new SimpleStringProperty("");
    });

    romColumn.setCellValueFactory(cellData -> {
      VpaDescriptorRepresentation value = cellData.getValue();
      boolean packaged = value.getManifest().getPackageInfo().isPopperMedia();
      if (packaged) {
        return new SimpleObjectProperty(WidgetFactory.createCheckIcon());
      }
      return new SimpleStringProperty("");
    });

    sizeColumn.setCellValueFactory(cellData -> {
      VpaDescriptorRepresentation value = cellData.getValue();
      return new SimpleStringProperty(FileUtils.readableFileSize(value.getSize()));
    });

    createdAtColumn.setCellValueFactory(cellData -> {
      VpaDescriptorRepresentation value = cellData.getValue();
      return new SimpleStringProperty(DateFormat.getInstance().format(value.getCreatedAt()));
    });

    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean disable = newSelection == null;
      deleteBtn.setDisable(disable);
      importBtn.setDisable(disable);

      if (oldSelection == null || !oldSelection.equals(newSelection)) {
        updateSelection(Optional.ofNullable(newSelection));
      }
    });

    tableView.setRowFactory(tv -> {
      TableRow<VpaDescriptorRepresentation> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {

        }
      });
      return row;
    });

    searchTextField.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();

      List<VpaDescriptorRepresentation> filtered = filterArchives(this.archives);
      tableView.setItems(FXCollections.observableList(filtered));
    });

    deleteBtn.setDisable(true);
    importBtn.setDisable(true);
    this.onReload();
  }

  private void updateSelection(Optional<VpaDescriptorRepresentation> newSelection) {
    NavigationController.setBreadCrumb(Arrays.asList("Table Repository"));
    if (newSelection.isPresent()) {
      VpaDescriptorRepresentation descriptorRepresentation = newSelection.get();
      NavigationController.setBreadCrumb(Arrays.asList("Table Repository", descriptorRepresentation.getFilename()));
    }
    tablesController.getRepositorySideBarController().setVpaDescriptor(newSelection);
  }

  private List<VpaDescriptorRepresentation> filterArchives(List<VpaDescriptorRepresentation> archives) {
    List<VpaDescriptorRepresentation> filtered = new ArrayList<>();
    String filterValue = searchTextField.textProperty().getValue();
    if (filterValue == null) {
      filterValue = "";
    }

    for (VpaDescriptorRepresentation archive : archives) {
      if (archive.getFilename().toLowerCase().contains(filterValue.toLowerCase())) {
        filtered.add(archive);
      }
    }
    return filtered;
  }

  public Optional<VpaDescriptorRepresentation> getSelection() {
    VpaDescriptorRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      return Optional.of(selection);
    }
    return Optional.empty();
  }

  public int getCount() {
    return this.archives != null ? this.archives.size() : 0;
  }

  public void setRootController(TablesController tablesController) {
    this.tablesController = tablesController;
  }
}