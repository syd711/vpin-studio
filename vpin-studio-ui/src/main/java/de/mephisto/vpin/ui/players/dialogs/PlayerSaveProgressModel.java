package de.mephisto.vpin.ui.players.dialogs;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetRepresentation;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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

      player = client.getPlayerService().savePlayer(player);
      client.clearCache();

      progressResultModel.getResults().add(player);

      if (Features.TOURNAMENTS_ENABLED) {
        updateTournamentPlayer(player);
      }
    } catch (Exception ex) {
      LOG.error("Failed to save player: " + ex.getMessage(), ex);
      progressResultModel.getResults().add(ex.getMessage());
    }
  }

  private void updateTournamentPlayer(PlayerRepresentation player) throws Exception {
    //post process tournament player creation
    if (tournamentPlayer) {
      if (player.isRegistered()) {
        Account maniaAccount = maniaClient.getAccountClient().getAccountByUuid(player.getTournamentUserUuid());
        if (maniaAccount != null) {
          maniaAccount.setDisplayName(player.getName());
          maniaAccount.setInitials(player.getInitials());

          Account update = maniaClient.getAccountClient().update(maniaAccount);
          if (update == null) {
            update = maniaClient.getAccountClient().create(maniaAccount, this.avatarFile, null);
            player.setTournamentUserUuid(update.getUuid());
            client.getPlayerService().savePlayer(player);
          }
          else {
            if (this.avatarFile != null) {
              maniaClient.getAccountClient().updateAvatar(maniaAccount, ImageIO.read(this.avatarFile), null);
            }
          }
        }
        else {
          Platform.runLater(() -> {
            WidgetFactory.showAlert(Studio.stage, "Error", "Failed to update VPin Mania account: account not found.");
          });
        }
      }
      else {
        Account maniaAccount = player.toManiaAccount();
        PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
        Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
        if (!StringUtils.isEmpty(avatarEntry.getValue())) {
          image = new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
        }
        BufferedImage avatarImage = SwingFXUtils.fromFXImage(image, null);
        Account register = maniaClient.getAccountClient().create(maniaAccount, avatarImage, null);
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
        } catch (Exception e) {
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
