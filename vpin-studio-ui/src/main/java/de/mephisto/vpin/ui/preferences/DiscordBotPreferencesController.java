package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.discord.DiscordClient;
import de.mephisto.vpin.connectors.discord.GuildInfo;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordCategory;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
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

public class DiscordBotPreferencesController implements Initializable {
  @FXML
  private Label botTokenLabel;

  @FXML
  private Button resetBtn;

  @FXML
  private ComboBox<DiscordServer> serverCombo;

  @FXML
  private ComboBox<DiscordChannel> channelCombo;

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
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Token Reset", "Reset this Discord bot token?",
        "If you haven't stored it elsewhere, you have to re-generate a new one using the Discord developer portal.",
        "Yes, delete token");
    if (result.get().equals(ButtonType.OK)) {
      client.getPreferenceService().setPreference(PreferenceNames.DISCORD_BOT_TOKEN, "");

      botTokenLabel.setText("-");
      this.serverCombo.setDisable(true);
      this.serverCombo.setValue(null);
      this.channelCombo.setDisable(true);
      this.channelCombo.setValue(null);
      this.categoryCombo.setDisable(true);
      this.categoryCombo.setValue(null);
      this.dynamicSubscriptions.setDisable(true);
      this.validateBtn.setDisable(true);
    }
  }

  @FXML
  private void onValidate() {
    validateBtn.setDisable(true);
    Platform.runLater(()-> {
      Studio.client.getDiscordService().clearCache();
      boolean b = client.getDiscordService().validateSettings();
      if(!b) {
        validateDefaultSettings();
        WidgetFactory.showAlert(Studio.stage, "Issues Detected", "There have been issues detected with you Discord settings.", "One or more values have been resetted.");
      }
      else {
        WidgetFactory.showInformation(Studio.stage, "Information", "No issues found.");
      }
      validateBtn.setDisable(false);
    });
  }

  @FXML
  private void onUserSelect() {
    Dialogs.openBotWhitelistDialog(this);
  }

  @FXML
  private void onTokenEdit() {
    String value = this.botTokenLabel.getText();
    if (value.length() == 1) {
      value = "";
    }

    String token = WidgetFactory.showInputDialog(Studio.stage, "Discord Bot Token", "Discord Bot Token",
        "Paste the bot token copied from the Discord developer portal here.", null, value);
    if (StringUtils.isEmpty(token)) {
      return;
    }

    Studio.stage.getScene().setCursor(Cursor.WAIT);
    new Thread(() -> {
      DiscordClient discordClient = null;
      try {
        discordClient = new DiscordClient(token.trim(), null);
        List<GuildInfo> guilds = discordClient.getGuilds();
        discordClient.shutdown();

        Platform.runLater(() -> {
          client.clearDiscordCache();
          Studio.stage.getScene().setCursor(Cursor.DEFAULT);
          client.getPreferenceService().setPreference(PreferenceNames.DISCORD_BOT_TOKEN, token.trim());
          botTokenLabel.setText(token.trim());
          serverCombo.setDisable(false);
          validateBtn.setDisable(false);
          channelCombo.setDisable(true);
          categoryCombo.setDisable(true);
          dynamicSubscriptions.setDisable(false);
          resetBtn.setDisable(false);
          validateDefaultSettings();
        });
      } catch (Exception e) {
        if (discordClient != null) {
          discordClient.shutdown();
        }

        Platform.runLater(() -> {
          Studio.stage.getScene().setCursor(Cursor.DEFAULT);
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        });
      }
    }).start();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Studio.client.getDiscordService().clearCache();

    serverCombo.setDisable(true);
    channelCombo.setDisable(true);
    categoryCombo.setDisable(true);
    resetBtn.setDisable(true);

    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.DISCORD_BOT_TOKEN);
    String token = !StringUtils.isEmpty(preference.getValue()) ? preference.getValue() : "-";
    botTokenLabel.setText(token);
    if (!StringUtils.isEmpty(token)) {
      serverCombo.setDisable(false);
      resetBtn.setDisable(false);
    }

    preference = client.getPreference(PreferenceNames.DISCORD_BOT_COMMANDS_ENABLED);
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

    categoryCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_CATEGORY_ID, newValue.getId());
      }
      else {
        client.getPreferenceService().setPreference(PreferenceNames.DISCORD_CATEGORY_ID, "");
      }
    });

    PreferenceEntryRepresentation dynamicSubscriptionsPreference = client.getPreference(PreferenceNames.DISCORD_DYNAMIC_SUBSCRIPTIONS);
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

    List<DiscordServer> servers = client.getDiscordService().getAdministratedDiscordServers();
    ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
    serverCombo.setItems(FXCollections.observableList(discordServers));

    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.DISCORD_GUILD_ID);
    PreferenceEntryRepresentation channelPreference = client.getPreference(PreferenceNames.DISCORD_CHANNEL_ID);
    PreferenceEntryRepresentation categoryPreference = client.getPreference(PreferenceNames.DISCORD_CATEGORY_ID);

    long serverId = preference.getLongValue();
    if (serverId > 0) {
      DiscordServer discordServer = client.getDiscordServer(serverId);
      if (discordServer != null) {
        channelCombo.setDisable(false);
        categoryCombo.setDisable(false);

        serverCombo.setValue(discordServer);

        List<DiscordChannel> discordChannels = client.getDiscordService().getDiscordChannels(discordServer.getId());
        channelCombo.setItems(FXCollections.observableArrayList(discordChannels));
        long channelId = channelPreference.getLongValue();
        if (channelId > 0) {
          Optional<DiscordChannel> first = discordChannels.stream().filter(channel -> channel.getId() == channelId).findFirst();
          first.ifPresent(discordChannel -> channelCombo.setValue(discordChannel));
        }

        List<DiscordCategory> discordCategories = discordServer.getCategories();
        categoryCombo.setItems(FXCollections.observableArrayList(discordCategories));
        long categoryId = categoryPreference.getLongValue();
        if (categoryId > 0) {
          Optional<DiscordCategory> first = discordCategories.stream().filter(category -> category.getId() == categoryId).findFirst();
          first.ifPresent(category -> categoryCombo.setValue(category));
        }
      }
    }
  }
}
