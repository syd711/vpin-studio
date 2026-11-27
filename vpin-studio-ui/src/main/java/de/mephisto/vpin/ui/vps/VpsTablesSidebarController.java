package de.mephisto.vpin.ui.vps;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsAuthoredUrls;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.tables.TablesSidebarVpsController;
import de.mephisto.vpin.ui.tables.panels.BaseSideBarController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.mephisto.vpin.ui.Studio.client;

public class VpsTablesSidebarController extends BaseSideBarController<VpsTable> implements Initializable {
  private static final Pattern IPDB_ID_PATTERN = Pattern.compile("[?&]id=(\\d+)");

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private VBox root;

  @FXML
  private Label nameLabel;

  @FXML
  private Label subtitleLabel;

  @FXML
  private Button openBtn;

  @FXML
  private Button copyBtn;

  @FXML
  private FlowPane features;

  @FXML
  private Label manufacturer;

  @FXML
  private Label year;

  @FXML
  private Label typeLabel;

  @FXML
  private Label players;

  @FXML
  private Label theme;

  @FXML
  private Label updated;

  @FXML
  private HBox ipdbBox;

  @FXML
  private Hyperlink ipdbLink;

  @FXML
  private TextArea commentsArea;

  @FXML
  private VBox dataRoot;

  private Optional<VpsTable> selection = Optional.empty();
  private CommentChangeListener commentChangeListener;

  @FXML
  private void onVpsBtn() {
    selection.ifPresent(table -> Studio.browse(VPS.getVpsTableUrl(table.getId())));
  }

  @FXML
  private void onHideSidebar() {
    TablesController.INSTANCE.toggleSidebar();
  }

  @FXML
  private void onCopyVpsLink() {
    selection.ifPresent(table -> {
      ClipboardContent content = new ClipboardContent();
      content.putString(VPS.getVpsTableUrl(table.getId()));
      Clipboard.getSystemClipboard().setContent(content);
    });
  }

  @FXML
  private void onIpdbLink() {
    selection.ifPresent(table -> {
      if (!StringUtils.isEmpty(table.getIpdbUrl())) {
        Studio.browse(table.getIpdbUrl());
      }
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    root.managedProperty().bindBidirectional(root.visibleProperty());
    ipdbBox.managedProperty().bindBidirectional(ipdbBox.visibleProperty());
    features.managedProperty().bind(features.visibleProperty());
    WidgetFactory.addToTextListener(nameLabel);
    commentChangeListener = new CommentChangeListener();
  }

  @Override
  public void setVisible(boolean b) {
    this.root.setVisible(b);
  }

  public void setTable(Optional<VpsTable> selection, VpsTablesPredicateFactory predicate) {
    this.selection = selection;
    this.openBtn.setDisable(selection.isEmpty());
    this.copyBtn.setDisable(selection.isEmpty());

    this.commentsArea.textProperty().removeListener(commentChangeListener);
    this.commentsArea.setDisable(selection.isEmpty());
    this.commentsArea.setText("");

    nameLabel.setText("-");
    subtitleLabel.setText("");
    manufacturer.setText("-");
    year.setText("-");
    typeLabel.setText("-");
    players.setText("-");
    theme.setText("-");
    updated.setText("-");
    ipdbLink.setText("-");
    ipdbBox.setVisible(false);
    features.getChildren().clear();
    features.setVisible(false);
    dataRoot.getChildren().clear();

    if (selection.isEmpty()) {
      return;
    }

    VpsTable table = selection.get();
    nameLabel.setText(StringUtils.defaultIfEmpty(table.getName(), StringUtils.defaultIfEmpty(table.getDisplayName(), "-")));

    List<String> subtitle = new ArrayList<>();
    if (!StringUtils.isEmpty(table.getManufacturer())) {
      subtitle.add(table.getManufacturer());
    }
    if (table.getYear() != null && table.getYear() > 0) {
      subtitle.add(String.valueOf(table.getYear()));
    }
    if (!StringUtils.isEmpty(table.getType())) {
      subtitle.add(typeName(table.getType()));
    }
    subtitleLabel.setText(String.join(" · ", subtitle));

    manufacturer.setText(StringUtils.defaultIfEmpty(table.getManufacturer(), "-"));
    year.setText(table.getYear() != null && table.getYear() > 0 ? String.valueOf(table.getYear()) : "-");
    typeLabel.setText(StringUtils.defaultIfEmpty(table.getType(), "-"));
    players.setText(table.getPlayers() != null && table.getPlayers() > 0 ? String.valueOf(table.getPlayers()) : "-");
    theme.setText(table.getTheme() == null || table.getTheme().isEmpty() ? "-" : String.join(", ", table.getTheme()));
    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    if (table.getUpdatedAt() != null) {
      updated.setText(formatter.format(Instant.ofEpochMilli(table.getUpdatedAt()).atZone(ZoneId.systemDefault()).toLocalDate()));
    }

    if (!StringUtils.isEmpty(table.getIpdbUrl())) {
      ipdbBox.setVisible(true);
      ipdbLink.setText(formatIpdbUrl(table.getIpdbUrl()));
      ipdbLink.setTooltip(new Tooltip(table.getIpdbUrl()));
    }

    for (String feature : table.getAllFeatures()) {
      Label badge = new Label(feature);
      badge.setTooltip(new Tooltip(VpsUtil.getFeatureColorTooltip(feature)));
      badge.getStyleClass().add("sm-pill");
      boolean active = predicate.isFeaturesFilterEmpty() || predicate.isSelectedFeature(feature);
      badge.getStyleClass().add(active ? "sm-pill-ok" : "sm-pill-unknown");
      features.getChildren().add(badge);
    }
    features.setVisible(!features.getChildren().isEmpty());

    this.commentsArea.setText(table.getComment());
    this.commentsArea.textProperty().addListener(commentChangeListener);

    TablesSidebarVpsController.addTablesSection(dataRoot, Messages.get("dialog.vps_section_table_version"), null, VpsDiffTypes.tableNewVersionVPX, table, false, predicate.buildTableVersionPredicate());

    Predicate<VpsAuthoredUrls> authoredUrlsPredicate = predicate.buildAuthoredUrlsPredicate();
    TablesSidebarVpsController.addSection(dataRoot, Messages.get("dialog.vps_section_backglasses"), null, VpsDiffTypes.b2s, table.getB2sFiles(), false, authoredUrlsPredicate);
    TablesSidebarVpsController.addSection(dataRoot, Messages.get("dialog.vps_section_alt_sound"), null, VpsDiffTypes.altSound, table.getAltSoundFiles(), false, authoredUrlsPredicate);
    TablesSidebarVpsController.addSection(dataRoot, Messages.get("dialog.vps_section_alt_color"), null, VpsDiffTypes.altColor, table.getAltColorFiles(), false, authoredUrlsPredicate);
    TablesSidebarVpsController.addSection(dataRoot, Messages.get("dialog.vps_section_pup_pack"), null, VpsDiffTypes.pupPack, table.getPupPackFiles(), false, authoredUrlsPredicate);
    TablesSidebarVpsController.addSection(dataRoot, "ROM", null, VpsDiffTypes.rom, table.getRomFiles(), false, authoredUrlsPredicate);
    TablesSidebarVpsController.addSection(dataRoot, Messages.get("dialog.vps_section_sound"), null, VpsDiffTypes.sound, table.getSoundFiles(), false, authoredUrlsPredicate);
    TablesSidebarVpsController.addSection(dataRoot, Messages.get("dialog.vps_section_topper"), null, VpsDiffTypes.topper, table.getTopperFiles(), false, authoredUrlsPredicate);
    TablesSidebarVpsController.addSection(dataRoot, Messages.get("dialog.vps_section_tutorials"), null, VpsDiffTypes.tutorial, table.getTutorialFiles(), false, authoredUrlsPredicate);
    TablesSidebarVpsController.addSection(dataRoot, "POV", null, VpsDiffTypes.pov, table.getPovFiles(), false, authoredUrlsPredicate);
    TablesSidebarVpsController.addSection(dataRoot, Messages.get("dialog.vps_section_wheel_art"), null, VpsDiffTypes.wheel, table.getWheelArtFiles(), false, authoredUrlsPredicate);
  }

  private static String typeName(String type) {
    return switch (type) {
      case "SS" -> Messages.get("vps.vps_tables_sidebar.type_ss");
      case "EM" -> Messages.get("vps.vps_tables_sidebar.type_em");
      case "PM" -> Messages.get("vps.vps_tables_sidebar.type_pm");
      default -> type;
    };
  }

  private static String formatIpdbUrl(String url) {
    Matcher matcher = IPDB_ID_PATTERN.matcher(url);
    if (url.contains("ipdb.org") && matcher.find()) {
      return Messages.get("vps.vps_tables_sidebar.ipdb_machine", matcher.group(1));
    }
    return url.replaceFirst("^https?://(www\\.)?", "");
  }

  class CommentChangeListener implements ChangeListener<String> {
    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
      debouncer.debounce("vpsComment", () -> {
        if (selection.isPresent()) {
          VpsTable vpsTable = selection.get();
          vpsTable.setComment(newValue);
          client.getVpsService().saveVpsData(vpsTable);
          EventManager.getInstance().notifyVpsTableChange(vpsTable.getId());
        }
      }, 300);
    }
  }
}
