package de.mephisto.vpin.ui.tournaments.view;

import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.ui.util.AvatarFactory;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentCellContainer extends HBox {
  private final static int TITLE_WIDTH = 60;

  public TournamentCellContainer(ManiaTournamentRepresentation tournament) {
    super(6);

    String badgeUrl = maniaClient.getTournamentClient().getBadgeUrl(tournament);
    this.getChildren().add(AvatarFactory.create(client.getCachedUrlImage(badgeUrl)));

    VBox entries = new VBox(3);

    Label name = new Label(tournament.getDisplayName());
    name.getStyleClass().add("default-headline");
    entries.getChildren().add(name);

    HBox row = new HBox(6);
    Label titleLabel = new Label("Owner:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.getStyleClass().add("default-headline");
    Label valueLabel = new Label("TODO");
    valueLabel.getStyleClass().add("default-text");
    row.getChildren().addAll(titleLabel, valueLabel);
    entries.getChildren().add(row);

    row = new HBox(6);
    titleLabel = new Label("Tables:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.getStyleClass().add("default-headline");
    valueLabel = new Label(String.valueOf(tournament.getTableIdList().size()));
    valueLabel.getStyleClass().add("default-text");
    row.getChildren().addAll(titleLabel, valueLabel);
    entries.getChildren().add(row);

    this.getChildren().add(entries);
  }
}