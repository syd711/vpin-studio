package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TabOverviewController implements Initializable, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TabOverviewController.class);

  private static final int COLUMNS = 3;
  private static final String[] STATUS_STYLES = {"sm-pill-ok", "sm-pill-warn", "sm-pill-unknown"};

  @FXML
  private HBox hint;

  @FXML
  private Label statusLabel;

  @FXML
  private FontIcon statusIcon;

  @FXML
  private Label lastCheckedLabel;

  @FXML
  private Button refreshBtn;

  @FXML
  private GridPane componentGrid;

  private final List<ComponentShortSummaryController> cards = new ArrayList<>();

  @FXML
  private void onVersionRefresh() {
    ComponentChecksProgressModel model = new ComponentChecksProgressModel(true);
    ProgressDialog.createProgressDialog(model);
  }

  @FXML
  private void onDiscord() {
    Studio.browse(Messages.get("components.components.https_discord_gg_ampwvazhvy"));
  }

  @FXML
  private void onGitHub() {
    Studio.browse(Messages.get("components.components.https_github_com_syd711_vpin_studio_issues"));
  }

  @FXML
  private void onDismiss() {
    ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckbox(stage, Messages.get("dialog.hide_this_warning"), Messages.get("dialog.hide_warning"), Messages.get("dialog.select_the_checkbox_below_if_you_do_2"), null, Messages.get("dialog.do_not_show_again"), false);
    if (!confirmationResult.isApplyClicked()) {
      hint.setVisible(false);
    }

    if (!confirmationResult.isApplyClicked() && confirmationResult.isChecked()) {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      uiSettings.setHideComponentWarning(true);
      client.getPreferenceService().setJsonPreference(uiSettings);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    hint.setVisible(false);
    hint.managedProperty().bindBidirectional(hint.visibleProperty());
    client.getPreferenceService().addListener(this);
    preferencesChanged(PreferenceNames.UI_SETTINGS, null);

    JFXFuture.supplyAsync(() -> client.getComponentService().getComponents())
        .thenAcceptLater(components -> {
          for (ComponentRepresentation component : components) {
            try {
              FXMLLoader loader = new FXMLLoader(ComponentShortSummaryController.class.getResource("component-short-summary-panel.fxml"));
              loader.setResources(Messages.getBundle());
              loader.load();
              ComponentShortSummaryController controller = loader.getController();
              controller.setOnChange(this::refreshSummary);
              cards.add(controller);
              controller.refresh(component);
            }
            catch (IOException e) {
              LOG.error("Failed to load component card: " + e.getMessage(), e);
            }
          }
          refreshSummary();
        });
  }

  /**
   * Lays out the visible cards and updates the status row.
   */
  private void refreshSummary() {
    componentGrid.getChildren().clear();
    List<ComponentShortSummaryController> visible = cards.stream().filter(c -> c.getRoot().isVisible()).toList();
    for (int i = 0; i < visible.size(); i++) {
      componentGrid.add(visible.get(i).getRoot(), i % COLUMNS, i / COLUMNS);
    }

    long updates = visible.stream().filter(c -> c.getStatus() == ComponentShortSummaryController.Status.UPDATE_AVAILABLE).count();
    long unknown = visible.stream().filter(c -> c.getStatus() == ComponentShortSummaryController.Status.UNKNOWN).count();

    statusLabel.getStyleClass().removeAll(STATUS_STYLES);
    if (visible.isEmpty()) {
      statusLabel.setText(Messages.get("components.components.loading_component_information"));
      statusLabel.getStyleClass().add("sm-pill-unknown");
      statusIcon.setIconLiteral("mdi2h-help-circle-outline");
      statusIcon.setIconColor(Paint.valueOf("#BDBDBD"));
    }
    else if (updates > 0) {
      statusLabel.setText(updates == 1 ? Messages.get("components.overview.update_available") : Messages.get("components.overview.updates_available", updates));
      statusLabel.getStyleClass().add("sm-pill-warn");
      statusIcon.setIconLiteral("mdi2a-arrow-up-bold-circle-outline");
      statusIcon.setIconColor(Paint.valueOf("#DD9646"));
    }
    else if (unknown > 0) {
      statusLabel.setText(Messages.get("components.overview.versions_unknown", unknown));
      statusLabel.getStyleClass().add("sm-pill-unknown");
      statusIcon.setIconLiteral("mdi2h-help-circle-outline");
      statusIcon.setIconColor(Paint.valueOf("#BDBDBD"));
    }
    else {
      statusLabel.setText(Messages.get("components.overview.all_up_to_date", visible.size()));
      statusLabel.getStyleClass().add("sm-pill-ok");
      statusIcon.setIconLiteral("mdi2c-check-bold");
      statusIcon.setIconColor(Paint.valueOf("#5CC46A"));
    }

    OffsetDateTime lastCheck = visible.stream()
        .map(ComponentShortSummaryController::getComponent)
        .filter(Objects::nonNull)
        .map(ComponentRepresentation::getLastCheck)
        .filter(Objects::nonNull)
        .max(OffsetDateTime::compareTo)
        .orElse(null);
    if (lastCheck != null) {
      String formatted = lastCheck.atZoneSameInstant(ZoneId.systemDefault())
          .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
      lastCheckedLabel.setText(Messages.get("components.overview.last_checked", formatted));
    }
    else {
      lastCheckedLabel.setText("");
    }
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.UI_SETTINGS)) {
      JFXFuture.supplyAsync(() -> client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class))
          .thenAcceptLater(uiSettings -> hint.setVisible(!uiSettings.isHideComponentWarning()));
    }
  }
}
