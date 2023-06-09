package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsAuthoredUrls;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsUrl;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarVpsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarVpsController.class);

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  @FXML
  private VBox detailsBox;

  @FXML
  private VBox dataRoot;

  @FXML
  private Label nameLabel;

  @FXML
  private Label yearLabel;

  @FXML
  private Label manufacturerLabel;

  @FXML
  private Label playersLabel;

  @FXML
  private Label updatedLabel;

  @FXML
  private Hyperlink ipdbLink;

  // Add a public no-args constructor
  public TablesSidebarVpsController() {
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

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    detailsBox.managedProperty().bindBidirectional(detailsBox.visibleProperty());
    dataRoot.managedProperty().bindBidirectional(dataRoot.visibleProperty());
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    dataRoot.getChildren().removeAll(dataRoot.getChildren());

    nameLabel.setText("-");
    yearLabel.setText("-");
    manufacturerLabel.setText("-");
    playersLabel.setText("-");
    updatedLabel.setText("-");
    ipdbLink.setText("");

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      String term = game.getGameDisplayName();
      List<VpsTable> vpsTables = VPS.getInstance().find(term);
      if (!vpsTables.isEmpty()) {
        VpsTable vpsTable = vpsTables.get(0);

        nameLabel.setText(vpsTable.getName());
        yearLabel.setText(String.valueOf(vpsTable.getYear()));
        manufacturerLabel.setText(vpsTable.getManufacturer());
        playersLabel.setText(String.valueOf(vpsTable.getPlayers()));
        ipdbLink.setText(vpsTable.getIpdbUrl());
        ipdbLink.setDisable(StringUtils.isEmpty(vpsTable.getIpdbUrl()) || !vpsTable.getIpdbUrl().startsWith("http"));
        updatedLabel.setText(DateFormat.getDateInstance().format(new Date(vpsTable.getUpdatedAt())));

        addSection("PUP Pack", vpsTable.getPupPackFiles());
        addSection("ALT Sound", vpsTable.getAltSoundFiles());
        addSection("ALT Color", vpsTable.getAltColorFiles());
        addSection("ROM", vpsTable.getRomFiles());
        addSection("Sound", vpsTable.getSoundFiles());
        addSection("Topper", vpsTable.getTopperFiles());
        addSection("Wheel Art", vpsTable.getWheelArtFiles());
        addSection("POV", vpsTable.getPovFiles());
      }
    }
  }

  private void addSection(String title, List<VpsAuthoredUrls> urls) {
    if (urls == null || urls.isEmpty()) {
      return;
    }

    addSectionHeader(title);

    for (VpsAuthoredUrls authoredUrl : urls) {
      List<VpsUrl> urls1 = authoredUrl.getUrls();
      String version = authoredUrl.getVersion();
      long updatedAt = authoredUrl.getUpdatedAt();
      List<String> authors = authoredUrl.getAuthors();

      for (VpsUrl vpsUrl : urls1) {
        String url = vpsUrl.getUrl();
        if (vpsUrl.isBroken()) {
          url = "";
        }
        dataRoot.getChildren().add(new VpsEntry(version, authors, url, updatedAt));
      }
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
}