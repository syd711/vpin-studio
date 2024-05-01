package de.mephisto.vpin.ui.tournaments.view;

import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tournaments.TournamentHelper;
import de.mephisto.vpin.ui.util.AvatarFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentCellContainer extends HBox {
  private final static int TITLE_WIDTH = 60;

  public TournamentCellContainer(Tournament tournament) {
    super(6);

    String badgeUrl = maniaClient.getTournamentClient().getBadgeUrl(tournament);
    this.getChildren().add(AvatarFactory.create(client.getCachedUrlImage(badgeUrl)));

    VBox entries = new VBox(3);

    Label name = new Label(tournament.getDisplayName());
    name.getStyleClass().add("default-headline");
    name.setStyle(TournamentHelper.getLabelCss(tournament));
    entries.getChildren().add(name);

    HBox row = new HBox(6);
    Label titleLabel = new Label("Owner:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.getStyleClass().add("default-headline");
    titleLabel.setStyle(TournamentHelper.getLabelCss(tournament));
    Label valueLabel = new Label("TODO");
    valueLabel.getStyleClass().add("default-text");
    valueLabel.setStyle(TournamentHelper.getLabelCss(tournament));
    row.getChildren().addAll(titleLabel, valueLabel);
    entries.getChildren().add(row);

    row = new HBox(6);
    titleLabel = new Label("Tables:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.getStyleClass().add("default-headline");
    titleLabel.setStyle(TournamentHelper.getLabelCss(tournament));

    List<TournamentTable> tournamentTables = maniaClient.getTournamentClient().getTournamentTables(tournament.getId());
    valueLabel = new Label(String.valueOf(tournamentTables.size()));
    valueLabel.getStyleClass().add("default-text");
    valueLabel.setStyle(TournamentHelper.getLabelCss(tournament));
    row.getChildren().addAll(titleLabel, valueLabel);
    entries.getChildren().add(row);

    this.getChildren().add(entries);
  }
}
