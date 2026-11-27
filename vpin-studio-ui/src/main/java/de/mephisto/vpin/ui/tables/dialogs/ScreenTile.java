package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.tables.dialogs.TableAssetManagerPane.MediaPane;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Screen tile of the screen map (UI-STYLEGUIDE.md, "Asset manager"): used by the asset manager dialog, the asset view
 * of the tables section and the media panel of the tables sidebar.
 * <ul>
 *   <li>Empty = {@code am-screen-tile}, with media = {@code am-screen-tile-filled}, selected = {@code am-screen-tile-selected}</li>
 *   <li>Name + "Empty" / "N files" bottom left. Tiles lower than {@link #COMPACT_HEIGHT} show both on one line.</li>
 *   <li>Without a media preview a check badge marks a screen with media (top right).</li>
 *   <li>Media tiles ({@link #setMedia(Node)}) show the preview above the label line, overlays (e.g. hover actions)
 *   are added with {@link #addOverlay(Node, Pos)}.</li>
 * </ul>
 * Content never draws outside the tile.
 */
public class ScreenTile extends MediaPane {
  /**
   * Below this height the name and the status share one line and the check badge is hidden (e.g. the Logo tile)
   */
  private static final double COMPACT_HEIGHT = 64;

  private final boolean mediaTile;
  private final Label statusLabel = new Label();
  private final Label separatorLabel = new Label("·");
  private final StackPane checkBadge;
  private final VBox labels;
  private final HBox nameRow;
  private final StackPane content;
  private final BorderPane mediaHolder = new BorderPane();

  private boolean compact = false;
  private int fileCount = 0;

  /**
   * @param mediaTile true for tiles showing a media preview: the label line sits below the preview and there is no
   *                  check badge
   */
  public ScreenTile(TableAssetManagerPane<?> rootPane, String text, VPinScreen screen, boolean mediaTile, String... suffixes) {
    super(rootPane, text, screen, suffixes);
    this.mediaTile = mediaTile;
    getStyleClass().add("am-screen-tile");

    Label nameLabel = new Label(text);
    nameLabel.getStyleClass().add("sm-text-bold");
    nameLabel.setMinWidth(0);
    statusLabel.getStyleClass().add("am-screen-status");
    statusLabel.setMinWidth(Region.USE_PREF_SIZE);
    separatorLabel.getStyleClass().add("am-screen-status");
    separatorLabel.setMinWidth(Region.USE_PREF_SIZE);
    separatorLabel.managedProperty().bind(separatorLabel.visibleProperty());

    // the status label moves between the second line (regular) and the name row (compact / media tiles)
    nameRow = new HBox(6, nameLabel, separatorLabel);
    nameRow.setAlignment(Pos.BASELINE_LEFT);
    nameRow.setMinWidth(0);
    labels = new VBox(2, nameRow, statusLabel);
    labels.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    labels.setMinWidth(0);

    FontIcon check = new FontIcon("mdi2c-check");
    check.setIconSize(16);
    checkBadge = new StackPane(check);
    checkBadge.getStyleClass().add("am-screen-check");
    StackPane.setAlignment(checkBadge, Pos.TOP_RIGHT);
    StackPane.setMargin(checkBadge, new Insets(10, 10, 0, 0));

    content = new StackPane();
    content.setMinSize(0, 0);
    if (mediaTile) {
      // preview above, one label line below
      mediaHolder.setMinSize(0, 0);
      mediaHolder.setPadding(new Insets(6, 6, 0, 6));
      content.getChildren().add(mediaHolder);
      setCenter(content);
      setBottom(labels);
    }
    else {
      // badge and labels are layered, so the badge takes no height from the labels
      content.getChildren().addAll(checkBadge, labels);
      setCenter(content);
    }

    // never draw outside of the tile, however small it gets
    Rectangle clip = new Rectangle();
    clip.widthProperty().bind(widthProperty());
    clip.heightProperty().bind(heightProperty());
    clip.setArcWidth(12);
    clip.setArcHeight(12);
    setClip(clip);

    heightProperty().addListener((obs, oldValue, newValue) -> {
      boolean newCompact = newValue.doubleValue() < COMPACT_HEIGHT;
      if (newCompact != compact) {
        compact = newCompact;
        // not during the layout pass
        Platform.runLater(this::applyLayout);
      }
    });

    applyLayout();
    setFileCount(0);
  }

  private void applyLayout() {
    boolean singleLine = compact || mediaTile;
    if (singleLine && labels.getChildren().contains(statusLabel)) {
      labels.getChildren().remove(statusLabel);
      nameRow.getChildren().add(statusLabel);
    }
    else if (!singleLine && nameRow.getChildren().contains(statusLabel)) {
      nameRow.getChildren().remove(statusLabel);
      labels.getChildren().add(statusLabel);
    }
    separatorLabel.setVisible(singleLine);

    if (mediaTile) {
      labels.setPadding(new Insets(5, 10, 6, 10));
      BorderPane.setAlignment(labels, Pos.CENTER_LEFT);
    }
    else if (compact) {
      labels.setPadding(new Insets(0, 10, 0, 14));
      StackPane.setAlignment(labels, Pos.CENTER_LEFT);
    }
    else {
      labels.setPadding(new Insets(0, 10, 10, 14));
      StackPane.setAlignment(labels, Pos.BOTTOM_LEFT);
    }
    updateBadge();
  }

  private void updateBadge() {
    checkBadge.setVisible(!mediaTile && fileCount > 0 && !compact);
  }

  public void setFileCount(int count) {
    fileCount = count;
    if (count == 0) {
      statusLabel.setText(Messages.get("tables.table_asset_manager.empty"));
    }
    else if (count == 1) {
      statusLabel.setText(Messages.get("tables.table_asset_manager.one_file"));
    }
    else {
      statusLabel.setText(Messages.get("tables.table_asset_manager.n_files", count));
    }
    separatorLabel.setVisible(compact || mediaTile);
    updateBadge();
    setStyleClass("am-screen-tile-filled", count > 0);
  }

  /**
   * Clears the status line, e.g. when no table is selected.
   */
  public void clearFileCount() {
    setFileCount(0);
    statusLabel.setText("");
    separatorLabel.setVisible(false);
  }

  public void setSelected(boolean selected) {
    setStyleClass("am-screen-tile-selected", selected);
  }

  /**
   * Marks the tile with the info border, e.g. the screen the highscore cards are generated for.
   */
  public void setHighlighted(boolean highlighted) {
    setStyleClass("am-screen-tile-highlight", highlighted);
  }

  /**
   * The container of the media preview of a media tile, its center holds the preview.
   */
  public BorderPane getMediaHolder() {
    return mediaHolder;
  }

  public void setMedia(@Nullable Node media) {
    mediaHolder.setCenter(media);
  }

  public void addOverlay(Node node, Pos alignment) {
    StackPane.setAlignment(node, alignment);
    content.getChildren().add(node);
  }

  private void setStyleClass(String styleClass, boolean enabled) {
    getStyleClass().remove(styleClass);
    if (enabled) {
      getStyleClass().add(styleClass);
    }
  }
}
