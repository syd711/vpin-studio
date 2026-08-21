package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordCategory;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class DiscordBotPreferencesController implements Initializable {
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private Label botTokenLabel;

  @FXML
  private Label botNameLabel;

  @FXML
  private Button resetBtn;

  @FXML
  private ComboBox<DiscordServer> serverCombo;

  @FXML
  private ComboBox<DiscordChannel> channelCombo;

  @FXML
  private ComboBox<DiscordChannel> tableUpdatesCombo;

  @FXML
  private ComboBox<DiscordCategory> categoryCombo;

  @FXML
  private VBox allowListPane;

  @FXML
  private CheckBox commandsEnabledCheckbox;

  @FXML
  private CheckBox dynamicSubscriptions;

  @FXML
  private Button selectUsersBtn;

  @FXML
  private Button validateBtn;

  @FXML
  private void onReset() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.delete_token"), Messages.get("dialog.delete_this_discord_bot_token"),
        Messages.get("dialog.if_you_haven_t_stored_it_elsewhere"),
        Messages.get("dialog.delete_token"));
    if (result.get().equals(ButtonType.OK)) {
      resetToken();
    }
  }

  @FXML
  private void onValidate() {
    validateBtn.setDisable(true);
    Platform.runLater(() -> {
      Studio.client.getDiscordService().clearCache();
      DiscordBotStatus status = client.getDiscordService().validateSettings();
      if (status.getError() != null) {
        validateDefaultSettings();
        WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.issues_detected"), Messages.get("dialog.there_have_been_issues_detected_with_you"), status.getError());
      }
      else {
        if (!status.isCanManageCategories()) {
          WidgetFactory.showInformation(Studio.stage, "Information", Messages.get("dialog.the_bot_configuration_is_valid_but_your"), Messages.get("dialog.discord_allows_a_maximum_of_50_channels") +
              Messages.get("dialog.with_the_channel_permission_the_bot_can"));
        }
        else {
          WidgetFactory.showInformation(Studio.stage, "Information", Messages.get("dialog.no_issues_found"));
        }

      }
      validateBtn.setDisable(false);
    });
  }

  @FXML
  private void onUserSelect() {
    PreferencesDialogs.openBotWhitelistDialog(this);
  }

  @FXML
  private void onTokenEdit() {
    String existingToken = this.botTokenLabel.getText();
    if (existingToken.length() == 1) {
      existingToken = "";
    }

    String token = WidgetFactory.showInputDialog(Studio.stage, Messages.get("dialog.discord_bot_token"), Messages.get("dialog.discord_bot_token"),
        Messages.get("dialog.paste_the_bot_token_copied_from_the"), null, existingToken);
    if (StringUtils.isEmpty(token) || existingToken.equals(token.trim())) {
      return;
    }

    try {
      client.clearDiscordCache();
      resetToken();
      client.getPreferenceService().setPreference(PreferenceNames.DISCORD_BOT_TOKEN, token.trim());
      DiscordBotStatus status = client.getDiscordService().validateSettings();
      if (status.getError() != null) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.invalid_bot_configuration_found_check_your_token"), Messages.get("dialog.error") + status.getError());
      }
      else {
        botTokenLabel.setText(token.trim());
        validateDefaultSettings();
      }
    }
    catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, e.getMessage());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Studio.client.getDiscordService().clearCache();

    serverCombo.setDisable(true);
    channelCombo.setDisable(true);
    categoryCombo.setDisable(true);
    tableUpdatesCombo.setDisable(true);
    resetBtn.setDisable(true);

    PreferenceEntryRepresentation preference = client.getPreferenceService().getPreference(PreferenceNames.DISCORD_BOT_TOKEN);
    String token = !StringUtils.isEmpty(preference.getValue()) ? preference.getValue() : "-";
    botTokenLabel.setText(token);
    if (!StringUtils.isEmpty(token)) {
      serverCombo.setDisable(false);
      resetBtn.setDisable(false);

      List<DiscordServer> servers = client.getDiscordService().getAdministratedDiscordServers();
      ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
      serverCombo.setItems(FXCollections.observableList(discordServers));
    }

    preference = client.getPreferenceService().getPreference(PreferenceNames.DISCORD_BOT_COMMANDS_ENABLED);
    selectUsersBtn.setDisable(!preference.getBooleanValue());
    commandsEnabledCheckbox.setSelected(preference.getBooleanValue());
    commandsEnabledCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_BOT_COMMANDS_ENABLED, t1);
        selectUsersBtn.setDisable(!t1);
      }
    });

    validateDefaultSettings();

    serverCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_GUILD_ID, newValue.getId());
        validateDefaultSettings();
      }
      else {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_GUILD_ID, "");
      }
    });

    channelCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_CHANNEL_ID, newValue.getId());
      }
      else {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_CHANNEL_ID, "");
      }
    });

    tableUpdatesCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_UPDATES_CHANNEL_ID, newValue.getId());
      }
      else {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_UPDATES_CHANNEL_ID, "");
      }
    });

    categoryCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_CATEGORY_ID, newValue.getId());
      }
      else {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_CATEGORY_ID, "");
      }
    });

    PreferenceEntryRepresentation dynamicSubscriptionsPreference = client.getPreferenceService().getPreference(PreferenceNames.DISCORD_DYNAMIC_SUBSCRIPTIONS);
    dynamicSubscriptions.setSelected(dynamicSubscriptionsPreference.getBooleanValue());
    dynamicSubscriptions.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_DYNAMIC_SUBSCRIPTIONS, t1);
      }
    });

    refreshAllowList();
  }

  public void refreshAllowList() {
    allowListPane.getChildren().removeAll(allowListPane.getChildren());

    List<PlayerRepresentation> allowList = new ArrayList<>(client.getDiscordService().getAllowList());
    for (PlayerRepresentation user : allowList) {
      HBox root = new HBox();
      root.setStyle("-fx-padding: 3 0 3 0;");
      root.setAlignment(Pos.BASELINE_LEFT);
      root.setSpacing(3);
      Label label = new Label("- " + user.getName());
      label.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");
      root.getChildren().add(label);
      allowListPane.getChildren().add(root);
    }

    if (allowList.isEmpty()) {
      Label label = new Label("No users are filtered. All server members can execute bot commands.");
      label.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");
      allowListPane.getChildren().add(label);
    }
  }

  private void validateDefaultSettings() {
    client.clearDiscordCache();

    PreferenceEntryRepresentation preference = client.getPreferenceService().getPreference(PreferenceNames.DISCORD_GUILD_ID);
    PreferenceEntryRepresentation defaultChannelPreference = client.getPreferenceService().getPreference(PreferenceNames.DISCORD_CHANNEL_ID);
    PreferenceEntryRepresentation categoryPreference = client.getPreferenceService().getPreference(PreferenceNames.DISCORD_CATEGORY_ID);
    PreferenceEntryRepresentation updatesPreference = client.getPreferenceService().getPreference(PreferenceNames.DISCORD_UPDATES_CHANNEL_ID);

    botNameLabel.setText("-");
    DiscordBotStatus status = client.getDiscordService().validateSettings();
    if (status.getError() == null) {
      List<DiscordServer> servers = client.getDiscordService().getAdministratedDiscordServers();
      ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
      serverCombo.setItems(FXCollections.observableList(discordServers));
      serverCombo.setDisable(false);
      validateBtn.setDisable(false);

      if (!StringUtils.isEmpty(status.getName())) {
        botNameLabel.setText(status.getName());
      }
    }

    long serverId = preference.getLongValue();
    if (serverId > 0) {
      DiscordServer discordServer = client.getDiscordService().getDiscordServer(serverId);
      if (discordServer != null) {
        channelCombo.setDisable(false);
        tableUpdatesCombo.setDisable(false);
        categoryCombo.setDisable(false);

        serverCombo.setValue(discordServer);

        List<DiscordChannel> discordChannels = client.getDiscordService().getDiscordChannels(discordServer.getId());
        List<DiscordChannel> updatedList = new ArrayList<>(discordChannels);
        updatedList.addFirst( null);
        channelCombo.setItems(FXCollections.observableArrayList(updatedList));
        tableUpdatesCombo.setItems(FXCollections.observableArrayList(updatedList));

        long defaultChannelId = defaultChannelPreference.getLongValue();
        if (defaultChannelId > 0) {
          Optional<DiscordChannel> first = discordChannels.stream().filter(channel -> channel.getId() == defaultChannelId).findFirst();
          if (first.isPresent()) {
            DiscordChannel selectedValue = channelCombo.getValue();
            if (selectedValue == null || !selectedValue.equals(first.get())) {
              channelCombo.setValue(first.get());
            }
          }
        }

        long updatedChannelId = updatesPreference.getLongValue();
        if (updatedChannelId > 0) {
          Optional<DiscordChannel> first = discordChannels.stream().filter(channel -> channel.getId() == updatedChannelId).findFirst();
          if (first.isPresent()) {
            DiscordChannel selectedValue = tableUpdatesCombo.getValue();
            if (selectedValue == null || !selectedValue.equals(first.get())) {
              tableUpdatesCombo.setValue(first.get());
            }
          }
        }

        List<DiscordCategory> discordCategories = discordServer.getCategories();
        categoryCombo.setItems(FXCollections.observableArrayList(discordCategories));
        long categoryId = categoryPreference.getLongValue();
        if (categoryId > 0) {
          Optional<DiscordCategory> first = discordCategories.stream().filter(category -> category.getId() == categoryId).findFirst();
          if (first.isPresent()) {
            DiscordCategory existingValue = categoryCombo.getValue();
            if (existingValue == null || !first.get().equals(existingValue)) {
              first.ifPresent(category -> categoryCombo.setValue(category));
            }
          }
        }
      }
    }
  }

  private void resetToken() {
    client.getPreferenceService().setPreference(PreferenceNames.DISCORD_BOT_TOKEN, "");

    botTokenLabel.setText("-");
    botNameLabel.setText("-");
    this.serverCombo.setDisable(true);
    this.serverCombo.setValue(null);
    this.channelCombo.setDisable(true);
    this.channelCombo.setValue(null);
    this.tableUpdatesCombo.setDisable(true);
    this.tableUpdatesCombo.setValue(null);
    this.categoryCombo.setDisable(true);
    this.categoryCombo.setValue(null);
    this.validateBtn.setDisable(true);
  }
}
