package de.mephisto.vpin.ui.players.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.restclient.assets.AssetRepresentation;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.FutureTask;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class PlayerSaveProgressModel extends ProgressModel<PlayerRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(PlayerSaveProgressModel.class);
  private List<PlayerRepresentation> players;
  private final boolean tournamentPlayer;
  private File avatarFile;
  private final Pane avatarStack;

  private final Iterator<PlayerRepresentation> playerIterator;

  public PlayerSaveProgressModel(PlayerRepresentation playerRepresentation, boolean tournamentPlayer, File avatarFile, Pane avatarStack) {
    super("Saving Player");
    this.players = Arrays.asList(playerRepresentation);
    this.tournamentPlayer = tournamentPlayer;
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
      if (player.getAvatar() == null && this.avatarFile == null) {
        FutureTask<Object> futureTask = new FutureTask<>(() -> {
          try {
            avatarFile = WidgetFactory.snapshot(avatarStack);
          } catch (IOException e) {
            LOG.error("Failed to crop avatar image: " + e.getMessage());
          }
        }, null);
        Platform.runLater(futureTask);
        futureTask.get();
      }

      player = client.getPlayerService().savePlayer(player);

      if (this.avatarFile != null) {
        this.uploadAvatar(player, this.avatarFile);
      }

      if (tournamentPlayer) {
        Account maniaAccount = player.toManiaAccount();
        if (player.isRegistered()) {
          Account update = maniaClient.getAccountClient().update(maniaAccount);
          if (update == null) {
            update = maniaClient.getAccountClient().create(maniaAccount, this.avatarFile, null);
            player.setTournamentUserUuid(update.getUuid());
            client.getPlayerService().savePlayer(player);
          }
          else {
            if (this.avatarFile != null) {
              maniaClient.getAccountClient().updateAvatar(maniaAccount, this.avatarFile, null);
            }
          }

        }
        else {
          Account register = maniaClient.getAccountClient().create(maniaAccount, this.avatarFile, null);
          player.setTournamentUserUuid(register.getUuid());
        }
      }
      else {
        if (!StringUtils.isEmpty(player.getTournamentUserUuid())) {
          try {
            String accountUuId = player.getTournamentUserUuid();
            List<Account> accounts = maniaClient.getAccountClient().getAccounts();
            Optional<Account> first = accounts.stream().filter(a -> a.getUuid().equals(accountUuId)).findFirst();
            if (first.isPresent()) {
              maniaClient.getAccountClient().deleteAccount(first.get().getId());
            }
          } catch (Exception e) {
            LOG.error("VPin Mania account deletion failed: " + e.getMessage(), e);
          }
          player.setTournamentUserUuid(null);
        }
      }

      client.getPlayerService().savePlayer(player);
      client.clearCache();

      progressResultModel.getResults().add(player);
    } catch (Exception ex) {
      LOG.error("Failed to save player: " + ex.getMessage(), ex);
      progressResultModel.getResults().add(ex.getMessage());
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
