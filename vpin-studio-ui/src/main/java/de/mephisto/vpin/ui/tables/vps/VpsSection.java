package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import javafx.geometry.Insets;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

/**
 * Collapsible card of one VPS download category (table versions, backglasses, ...):
 * header with icon, title, entry count and chevron, followed by the {@link VpsEntry} rows.
 * Uses the common collapsible look (sm-collapsible, see UI-STYLEGUIDE.md "Collapsible panels").
 */
public class VpsSection extends VBox {
  private static final PseudoClass EXPANDED = PseudoClass.getPseudoClass("expanded");

  private final Label countLabel = new Label("…");
  private final FontIcon chevron = new FontIcon("mdi2c-chevron-up");
  private final VBox rows = new VBox();

  public VpsSection(String title, VpsDiffTypes diffType) {
    getStyleClass().add("sm-collapsible");

    FontIcon icon = new FontIcon(getIcon(diffType));
    icon.setIconSize(18);
    icon.getStyleClass().add("c-icon-muted");

    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("sm-collapsible-title");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    countLabel.getStyleClass().add("vps-count");

    chevron.setIconSize(16);
    chevron.getStyleClass().add("c-icon-subtle");

    HBox header = new HBox(10, icon, titleLabel, spacer, countLabel, chevron);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getStyleClass().add("sm-collapsible-header");
    header.setOnMouseClicked(e -> setExpanded(!rows.isVisible()));

    rows.managedProperty().bind(rows.visibleProperty());
    setLoading();

    getChildren().addAll(header, rows);
    setExpanded(true);
  }

  public void setExpanded(boolean expanded) {
    rows.setVisible(expanded);
    pseudoClassStateChanged(EXPANDED, expanded);
    chevron.setIconLiteral(expanded ? "mdi2c-chevron-up" : "mdi2c-chevron-down");
  }

  /**
   * Shows two skeleton bars until the entries are available.
   */
  public void setLoading() {
    countLabel.setText("…");
    Region bar1 = new Region();
    bar1.getStyleClass().add("vps-skeleton");
    bar1.setMaxWidth(300);
    Region bar2 = new Region();
    bar2.getStyleClass().add("vps-skeleton");
    bar2.setMaxWidth(190);
    VBox skeleton = new VBox(10, bar1, bar2);
    skeleton.setPadding(new Insets(12, 14, 14, 14));
    skeleton.getStyleClass().add("vps-entry");
    rows.getChildren().setAll(skeleton);
  }

  public void setEntries(List<Node> entries) {
    rows.getChildren().setAll(entries);
    countLabel.setText(String.valueOf(entries.stream().filter(n -> n instanceof VpsEntry).count()));
  }

  public static String getIcon(VpsDiffTypes diffType) {
    if (diffType == null) {
      return "mdi2f-file-outline";
    }
    return switch (diffType) {
      case tableNewVPX, tableNewVersion, tableNewVersionVPX, tableVersionUpdate -> "mdi2t-table-large";
      case b2s -> "mdi2m-monitor";
      case altSound -> "mdi2v-volume-high";
      case altColor -> "mdi2p-palette";
      case pupPack -> "mdi2m-movie-open";
      case rom -> "mdi2c-chip";
      case sound -> "mdi2m-music";
      case topper -> "mdi2m-monitor-small";
      case tutorial -> "mdi2s-school";
      case pov -> "mdi2c-camera";
      case wheel -> "mdi2c-circle-outline";
      default -> "mdi2f-file-outline";
    };
  }
}
