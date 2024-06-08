package de.mephisto.vpin.ui.tournaments.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentSearchResult;
import de.mephisto.vpin.connectors.mania.model.TournamentSearchResultItem;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tournaments.view.TournamentSearchTableSummary;
import de.mephisto.vpin.ui.tournaments.view.TournamentSearchText;
import de.mephisto.vpin.ui.util.AvatarFactory;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentBrowserDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentBrowserDialogController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private VBox avatarPane;

  @FXML
  private Button saveBtn;

  @FXML
  private TextField searchText;

  @FXML
  private Label ownerLabel;

  @FXML
  private Label nameLabel;

  @FXML
  private Label startLabel;

  @FXML
  private Label remainingLabel;

  @FXML
  private Label endLabel;

  @FXML
  private Hyperlink discordLink;

  @FXML
  private Hyperlink websiteLink;

  @FXML
  private Label descriptionText;

  @FXML
  private Button nextBtn;

  @FXML
  private Button previousBtn;

  @FXML
  private TableView<TournamentSearchResultItem> tableView;

  @FXML
  private TableColumn<TournamentSearchResultItem, Object> avatarColumn;

  @FXML
  private TableColumn<TournamentSearchResultItem, String> nameColumn;

  @FXML
  private TableColumn<TournamentSearchResultItem, String> playersColumn;

  @FXML
  private TableColumn<TournamentSearchResultItem, String> tablesColumn;

  @FXML
  private StackPane viewStack;

  @FXML
  private Label pagingInfo;

  private TournamentSearchResultItem tournament;
  private Optional<TournamentSearchResultItem> selection = Optional.empty();
  private Parent loadingOverlay;
  private TournamentSearchResult searchResult;
  private int page = 0;
  private PlayerRepresentation defaultPlayer;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.tournament = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    if(this.selection.isPresent()) {
      this.tournament = selection.get();
    }

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onNext() {
    int page = searchResult.getPage();
    doSearch(searchText.getText(), page + 1);
  }

  @FXML
  private void onPrevious() {
    int page = searchResult.getPage();
    doSearch(searchText.getText(), page - 1);
  }

  @FXML
  private void onWebsiteOpen() {
    if (this.selection.isPresent()) {
      TournamentSearchResultItem item = selection.get();
      String link = item.getWebsite();
      if (!StringUtils.isEmpty(link)) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
          try {
            desktop.browse(new URI(link));
          } catch (Exception e) {
            LOG.error("Failed to open website link: " + e.getMessage(), e);
          }
        }
      }
    }
  }

  @FXML
  private void onDiscordLink() {
    if (this.selection.isPresent()) {
      TournamentSearchResultItem item = selection.get();
      String link = item.getDiscordLink();
      if (!StringUtils.isEmpty(link)) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
          try {
            desktop.browse(new URI(link));
          } catch (Exception e) {
            LOG.error("Failed to open discord link: " + e.getMessage(), e);
          }
        }
      }
    }
  }

  @Override
  public void onDialogCancel() {
    this.tournament = null;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("                  No tournaments found!\nUse the search field to filter tournaments."));
    saveBtn.setDisable(true);
    searchText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("nameField", () -> {
      this.page = 0;
      Platform.runLater(() -> {
        doSearch(t1, 0);
      });
    }, 300));

    avatarColumn.setCellValueFactory(cellData -> {
      TournamentSearchResultItem value = cellData.getValue();
      String avatarUrl = maniaClient.getAccountClient().getAvatarUrl(value.getOwnerUuid());
      ImageView imageView = AvatarFactory.create(client.getCachedUrlImage(avatarUrl));
      Tooltip.install(imageView, new Tooltip(value.getOwnerName()));
      return new SimpleObjectProperty<>(imageView);
    });

    nameColumn.setCellValueFactory(cellData -> {
      TournamentSearchResultItem value = cellData.getValue();
      return new SimpleObjectProperty(new TournamentSearchText(value));
    });

    playersColumn.setCellValueFactory(cellData -> {
      TournamentSearchResultItem value = cellData.getValue();
      return new SimpleObjectProperty(value.getPlayerCount());
    });

    tablesColumn.setCellValueFactory(cellData -> {
      TournamentSearchResultItem value = cellData.getValue();
      TournamentSearchTableSummary summary = new TournamentSearchTableSummary(value);
      return new SimpleObjectProperty(summary);
    });

    tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TournamentSearchResultItem>) c -> {
      if (c.getList().isEmpty()) {
        selection = Optional.empty();
      }
      else {
        selection = Optional.of(c.getList().get(0));
      }

      updateSelection(selection);
    });

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Tournaments...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    defaultPlayer = client.getPlayerService().getDefaultPlayer();

    Platform.runLater(() -> {
      doSearch("", 0);
    });
  }

  private void doSearch(String t1, int page) {
    Platform.runLater(() -> {
      try {
        viewStack.getChildren().add(loadingOverlay);
        searchResult = maniaClient.getTournamentClient().search(t1, page, UIDefaults.TOURNAMENT_BROWSER_PAGE_SIZE);
        List<TournamentSearchResultItem> results = searchResult.getResults();

        int from = searchResult.getPage() * UIDefaults.TOURNAMENT_BROWSER_PAGE_SIZE;
        int to = from + UIDefaults.TOURNAMENT_BROWSER_PAGE_SIZE;
        if (to > searchResult.getTotal()) {
          to = searchResult.getTotal();
        }

        pagingInfo.setText((from+1) + " to " + to + " of " + searchResult.getTotal());
        pagingInfo.setVisible(searchResult.getTotal() > 0);
        previousBtn.setDisable(true);
        nextBtn.setDisable(true);

        if (!results.isEmpty()) {
          boolean hasNext = to < searchResult.getTotal();
          boolean hasPrevious = searchResult.getPage() > 0;
          nextBtn.setDisable(!hasNext);
          previousBtn.setDisable(!hasPrevious);
        }

        new Thread(() -> {
          Platform.runLater(() -> {
            for (TournamentSearchResultItem result : results) {
              String avatarUrl = maniaClient.getAccountClient().getAvatarUrl(result.getOwnerUuid());
              client.getCachedUrlImage(avatarUrl);
            }
            tableView.setItems(FXCollections.observableList(results));
            if (!results.isEmpty()) {
              tableView.getSelectionModel().select(0);
            }
            tableView.refresh();
            viewStack.getChildren().remove(loadingOverlay);
          });
        }).start();
      } catch (Exception e) {
        viewStack.getChildren().remove(loadingOverlay);
        WidgetFactory.showAlert(Studio.stage, "Error", "Search failed: " + e.getMessage());
      }
    });
  }

  private void updateSelection(Optional<TournamentSearchResultItem> selection) {
    nameLabel.setText("-");
    startLabel.setText("-");
    endLabel.setText("-");
    remainingLabel.setText("-");
    discordLink.setText("-");
    websiteLink.setText("-");
    ownerLabel.setText("-");
    descriptionText.setText("-");
    avatarPane.getChildren().removeAll(avatarPane.getChildren());
    saveBtn.setDisable(true);


    if (selection.isPresent()) {
      TournamentSearchResultItem item = selection.get();

      saveBtn.setDisable(defaultPlayer != null && defaultPlayer.getTournamentUserUuid() != null && defaultPlayer.getTournamentUserUuid().equals(item.getOwnerUuid()));

      nameLabel.setText(item.getDisplayName());
      startLabel.setText(DateFormat.getDateTimeInstance().format(item.getStartDate()));
      endLabel.setText(DateFormat.getDateTimeInstance().format(item.getEndDate()));
      remainingLabel.setText(DateUtil.formatDuration(item.getStartDate(), item.getEndDate()));
      if (!StringUtils.isEmpty(item.getDiscordLink())) {
        discordLink.setText(item.getDiscordLink());
      }
      if (!StringUtils.isEmpty(item.getWebsite())) {
        websiteLink.setText(item.getWebsite());
      }
      if (!StringUtils.isEmpty(item.getDescription())) {
        descriptionText.setText(item.getDescription());
      }

      ownerLabel.setText(item.getOwnerName());
      String avatarUrl = maniaClient.getAccountClient().getAvatarUrl(item.getOwnerUuid());
      ImageView imageView = AvatarFactory.create(client.getCachedUrlImage(avatarUrl));
      avatarPane.getChildren().add(imageView);
    }
  }

  public Tournament getTournament() {
    return tournament;
  }

}
