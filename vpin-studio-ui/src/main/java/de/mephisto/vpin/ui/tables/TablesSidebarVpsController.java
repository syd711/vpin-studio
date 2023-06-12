package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.*;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.vps.VpsDBDownloadProgressModel;
import de.mephisto.vpin.ui.tables.vps.VpsEntry;
import de.mephisto.vpin.ui.tables.vps.VpsEntryComment;
import de.mephisto.vpin.ui.util.AutoCompleteTextField;
import de.mephisto.vpin.ui.util.AutoCompleteTextFieldChangeListener;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class TablesSidebarVpsController implements Initializable, AutoCompleteTextFieldChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarVpsController.class);

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  @FXML
  private VBox detailsBox;

  @FXML
  private VBox dataRoot;

  @FXML
  private TextField nameField;

  @FXML
  private Label yearLabel;

  @FXML
  private Label manufacturerLabel;

  @FXML
  private Label playersLabel;

  @FXML
  private Label updatedLabel;

  @FXML
  private ComboBox<VpsTableFile> tablesCombo;

  @FXML
  private HBox featureBox;

  @FXML
  private CheckBox filterCheckbox;

  @FXML
  private Button openBtn;

  @FXML
  private Hyperlink ipdbLink;
  private AutoCompleteTextField autoCompleteNameField;

  // Add a public no-args constructor
  public TablesSidebarVpsController() {
  }

  @FXML
  private void onOpen() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI("https://virtual-pinball-spreadsheet.web.app/game/" + game.get().getExtTableId() + "/"));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
        ipdbLink.setDisable(true);
      }
    }
  }

  @FXML
  private void onIpdbLink() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI(ipdbLink.getText()));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
        ipdbLink.setDisable(true);
      }
    }
  }

  @FXML
  private void onUpdate() {
    Date changeDate = VPS.getInstance().getChangeDate();
    String msg = "Download latest version of the Visual Pinball Spreadsheet database?";
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Virtual Pinball Spreadsheet Update", msg, "Your database last update was on " + DateFormat.getDateInstance().format(changeDate) + ".");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      Dialogs.createProgressDialog(new VpsDBDownloadProgressModel("Download VPS Database", Arrays.asList(VPS.getInstance().getVpsDbFile())));
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    detailsBox.managedProperty().bindBidirectional(detailsBox.visibleProperty());
    dataRoot.managedProperty().bindBidirectional(dataRoot.visibleProperty());

    filterCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshView(game));

    List<VpsTable> tables = VPS.getInstance().getTables();
    TreeSet<String> collect = new TreeSet<>(tables.stream().map(t -> t.getName()).collect(Collectors.toSet()));
    autoCompleteNameField = new AutoCompleteTextField(this.nameField, this, collect);

    tablesCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      featureBox.getChildren().removeAll(featureBox.getChildren());
      if (newValue != null) {
        List<String> features = newValue.getFeatures();
        if (features != null) {
          for (String feature : features) {
            Label badge = new Label(feature);
            badge.getStyleClass().add("vps-badge");
            badge.setStyle("-fx-background-color: " + VpsUtil.getFeatureColor(feature) + ";");
            featureBox.getChildren().add(badge);
          }
        }

        saveExternalTableVersionId(newValue.getId());
      }
    });
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  @Override
  public void onChange(String value) {
    List<VpsTable> tables = VPS.getInstance().getTables();
    Optional<VpsTable> selectedEntry = tables.stream().filter(t -> t.getName().equals(value)).findFirst();
    if (selectedEntry.isPresent()) {
      VpsTable vpsTable = selectedEntry.get();
      refreshTableView(vpsTable);
      saveExternalTableId(vpsTable.getId());
    }
  }


  public void refreshView(Optional<GameRepresentation> g) {
    dataRoot.getChildren().removeAll(dataRoot.getChildren());
    tablesCombo.setItems(FXCollections.emptyObservableList());

    autoCompleteNameField.reset();
    autoCompleteNameField.setDisable(g.isEmpty());
    tablesCombo.setDisable(g.isEmpty());

    yearLabel.setText("-");
    manufacturerLabel.setText("-");
    playersLabel.setText("-");
    updatedLabel.setText("-");
    ipdbLink.setText("");
    openBtn.setDisable(true);

    if (g.isPresent()) {
      GameRepresentation game = g.get();

      openBtn.setDisable(StringUtils.isEmpty(game.getExtTableId()));

      if (!StringUtils.isEmpty(game.getExtTableId())) {
        VpsTable tableById = VPS.getInstance().getTableById(game.getExtTableId());
        if (tableById != null) {
          refreshTableView(tableById);
          return;
        }
      }

      String term = game.getGameDisplayName();
      List<VpsTable> vpsTables = VPS.getInstance().find(term);
      if (!vpsTables.isEmpty()) {
        VpsTable vpsTable = vpsTables.get(0);
        refreshTableView(vpsTable);

        saveExternalTableId(vpsTable.getId());
      }
    }
  }

  private void refreshTableView(VpsTable vpsTable) {
    List<VpsTableFile> tableFiles = vpsTable.getTableFilesForFormat(VpsFeatures.VPX);
    if (tableFiles != null && !tableFiles.isEmpty()) {
      tablesCombo.setItems(FXCollections.observableList(tableFiles));
      String extTableVersionId = game.get().getExtTableVersionId();

      if (!StringUtils.isEmpty(extTableVersionId)) {
        for (VpsTableFile tableFile : tableFiles) {
          if (tableFile.getId().equals(extTableVersionId)) {
            tablesCombo.setValue(tableFile);
            break;
          }
        }
      }
    }

    autoCompleteNameField.setText(vpsTable.getName());
    yearLabel.setText(String.valueOf(vpsTable.getYear()));
    manufacturerLabel.setText(vpsTable.getManufacturer());
    playersLabel.setText(String.valueOf(vpsTable.getPlayers()));
    ipdbLink.setText(vpsTable.getIpdbUrl());
    ipdbLink.setDisable(StringUtils.isEmpty(vpsTable.getIpdbUrl()) || !vpsTable.getIpdbUrl().startsWith("http"));
    updatedLabel.setText(DateFormat.getDateInstance().format(new Date(vpsTable.getUpdatedAt())));

    boolean doFilter = filterCheckbox.isSelected();

    if (!doFilter || !game.get().isPupPackAvailable()) {
      addSection("PUP Pack", vpsTable.getPupPackFiles());
    }

    if (!doFilter || !game.get().isDirectB2SAvailable()) {
      addSection("Backglasses", vpsTable.getB2sFiles());
    }

    if (!doFilter || !game.get().isAltSoundAvailable()) {
      addSection("ALT Sound", vpsTable.getAltSoundFiles());
    }

    addSection("ALT Color", vpsTable.getAltColorFiles());

    if (!doFilter || !game.get().isRomExists()) {
      addSection("ROM", vpsTable.getRomFiles());
    }

    addSection("Sound", vpsTable.getSoundFiles());

    GameMediaRepresentation gameMedia = game.get().getGameMedia();
    GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.Topper);
    if (!doFilter || item == null) {
      addSection("Topper", vpsTable.getTopperFiles());
    }

    item = gameMedia.getItem(PopperScreen.Wheel);
    if (!doFilter || item == null) {
      addSection("Wheel Art", vpsTable.getWheelArtFiles());
    }

    if (!doFilter || !game.get().isPovAvailable()) {
      addSection("POV", vpsTable.getPovFiles());
    }
  }

  private void addSection(String title, List<? extends VpsAuthoredUrls> urls) {
    if (urls == null || urls.isEmpty()) {
      return;
    }

    List<Node> entries = new ArrayList<>();
    for (VpsAuthoredUrls authoredUrl : urls) {
      List<VpsUrl> authoredUrlUrls = authoredUrl.getUrls();
      if (authoredUrlUrls != null && !authoredUrlUrls.isEmpty()) {
        String version = authoredUrl.getVersion();
        long updatedAt = authoredUrl.getUpdatedAt();
        List<String> authors = authoredUrl.getAuthors();

        for (VpsUrl vpsUrl : authoredUrlUrls) {
          String url = vpsUrl.getUrl();
          if (vpsUrl.isBroken()) {
            url = "";
          }
          entries.add(new VpsEntry(version, authors, url, updatedAt));
        }

        if (authoredUrl instanceof VpsBackglassFile) {
          VpsBackglassFile backglassFile = (VpsBackglassFile) authoredUrl;
          if (!StringUtils.isEmpty(backglassFile.getComment())) {
            entries.add(new VpsEntryComment(backglassFile.getComment()));
          }
        }
      }
    }

    if (!entries.isEmpty()) {
      addSectionHeader(title);
      dataRoot.getChildren().addAll(entries);
    }
  }

  private void addSectionHeader(String title) {
    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarVpsController.class.getResource("section-vps.fxml"));
      Pane section = loader.load();
      Label label = (Label) section.getChildren().get(0);
      label.setText(title);
      dataRoot.getChildren().add(section);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  private void saveExternalTableId(String id) {
    try {
      GameRepresentation g = game.get();
      if (!id.equals(g.getExtTableId())) {
        g.setExtTableId(id);
        Studio.client.getGameService().saveGame(g);
      }
    } catch (Exception e) {
      LOG.error("Failed to set external game id: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Error updating external game reference: " + e.getMessage());
    }
  }

  private void saveExternalTableVersionId(String id) {
    try {
      GameRepresentation g = game.get();
      if (!id.equals(g.getExtTableVersionId())) {
        g.setExtTableVersionId(id);
        Studio.client.getGameService().saveGame(g);
      }
    } catch (Exception e) {
      LOG.error("Failed to set external game id: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Error updating external game reference: " + e.getMessage());
    }
  }
}