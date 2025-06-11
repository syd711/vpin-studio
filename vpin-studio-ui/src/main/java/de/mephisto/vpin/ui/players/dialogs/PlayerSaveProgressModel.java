package de.mephisto.vpin.ui.players.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.AccountVisibility;
import de.mephisto.vpin.restclient.assets.AssetRepresentation;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.FutureTask;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class PlayerSaveProgressModel extends ProgressModel<PlayerRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(PlayerSaveProgressModel.class);
  private final List<PlayerRepresentation> players;
  private final boolean maniaPlayer;
  private final String maniaName;
  private final AccountVisibility visibility;
  private File avatarFile;
  private final Pane avatarStack;

  private final Iterator<PlayerRepresentation> playerIterator;

  public PlayerSaveProgressModel(Stage stage, PlayerRepresentation playerRepresentation, boolean maniaPlayer, String maniaName, AccountVisibility visibility, File avatarFile, Pane avatarStack) {
    super("Saving Player");
    this.players = Arrays.asList(playerRepresentation);
    this.maniaPlayer = maniaPlayer;
    this.maniaName = maniaName;
    this.visibility = visibility;
    this.avatarFile = avatarFile;
    this.avatarStack = avatarStack;
    this.playerIterator = players.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return players.size();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public boolean hasNext() {
    return this.playerIterator.hasNext();
  }

  @Override
  public PlayerRepresentation getNext() {
    return playerIterator.next();
  }

  @Override
  public String nextToString(PlayerRepresentation game) {
    return "";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, PlayerRepresentation player) {
    try {
//      if (player.getAvatar() == null && this.avatarFile == null) {
        generateAvatarFile();
//      }

      player = client.getPlayerService().savePlayer(player);

      if (this.avatarFile != null) {
        this.uploadAvatar(player, this.avatarFile);
      }

      player = client.getPlayerService().savePlayer(player);
      client.getManiaService().clearCache();
      client.getDiscordService().clearCache();
      client.getImageCache().clearCache();
      client.getGameService().clearCache();
      client.getFrontendService().clearCache();

      progressResultModel.getResults().add(player);

      if (Features.MANIA_ENABLED) {
        updateTournamentPlayer(player, avatarFile);
      }
    }
    catch (Exception ex) {
      LOG.error("Failed to save player: " + ex.getMessage(), ex);
      progressResultModel.getResults().add(ex.getMessage());
    }
  }

  private void generateAvatarFile() throws Exception {
    FutureTask<Object> futureTask = new FutureTask<>(() -> {
      try {
        avatarFile = WidgetFactory.snapshot(avatarStack);
      }
      catch (IOException e) {
        LOG.error("Failed to crop avatar image: " + e.getMessage());
      }
    }, null);
    Platform.runLater(futureTask);
    futureTask.get();
  }

  private void updateTournamentPlayer(PlayerRepresentation player, File avatarFile) throws Exception {
    //post process tournament player creation
    if (maniaPlayer) {
      Account maniaAccount = null;
      if (!StringUtils.isEmpty(player.getTournamentUserUuid())) {
        maniaAccount = maniaClient.getAccountClient().getAccountByUuid(player.getTournamentUserUuid());
      }

      //the user is already registered
      if (maniaAccount != null) {
        String accountName = player.getName();
        if(!StringUtils.isEmpty(maniaName)) {
          accountName = maniaName;
        }

        maniaAccount.setVisibility(visibility);
        maniaAccount.setDisplayName(accountName);
        maniaAccount.setInitials(player.getInitials());

        Account update = maniaClient.getAccountClient().update(maniaAccount);
        if (update == null) {
          update = maniaClient.getAccountClient().create(maniaAccount, this.avatarFile, null);
          player.setTournamentUserUuid(update.getUuid());
          client.getPlayerService().savePlayer(player);
        }
        else {
          if (avatarFile != null) {
            maniaClient.getAccountClient().updateAvatar(maniaAccount, avatarFile, null);
          }
        }
      }
      else {
        //register new account
        generateAvatarFile();
        maniaAccount = player.toManiaAccount();
        String accountName = player.getName();
        if(!StringUtils.isEmpty(maniaName)) {
          maniaAccount.setDisplayName(maniaName);
        }
        Account register = maniaClient.getAccountClient().create(maniaAccount, avatarFile, null);
        player.setTournamentUserUuid(register.getUuid());
        client.getPlayerService().savePlayer(player);
      }
    }
    else {
      if (!StringUtils.isEmpty(player.getTournamentUserUuid())) {
        try {
          String accountUuId = player.getTournamentUserUuid();
          Account acc = maniaClient.getAccountClient().getAccountByUuid(accountUuId);
          if (acc != null) {
            maniaClient.getAccountClient().deleteAccount(acc.getId());
          }
        }
        catch (Exception e) {
          LOG.error("VPin Mania account deletion failed: " + e.getMessage(), e);
        }
        player.setTournamentUserUuid(null);
        client.getPlayerService().savePlayer(player);
      }
    }
  }


  private void uploadAvatar(PlayerRepresentation player, File file) throws Exception {
    long assetId = 0;
    if (player.getAvatar() != null) {
      assetId = player.getAvatar().getId();
    }
    AssetRepresentation assetRepresentation = client.getAssetService().uploadAsset(file, assetId, 300, AssetType.AVATAR, null);
    player.setAvatar(assetRepresentation);
  }

}
