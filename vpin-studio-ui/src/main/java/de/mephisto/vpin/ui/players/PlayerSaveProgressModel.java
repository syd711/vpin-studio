package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetRepresentation;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class PlayerSaveProgressModel extends ProgressModel<PlayerRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(PlayerSaveProgressModel.class);
  private List<PlayerRepresentation> players;
  private File avatarFile;
  private final Pane avatarStack;

  private final Iterator<PlayerRepresentation> playerIterator;

  public PlayerSaveProgressModel(PlayerRepresentation playerRepresentation, File avatarFile, Pane avatarStack) {
    super("Saving Player");
    this.players = Arrays.asList(playerRepresentation);
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
        avatarFile = WidgetFactory.snapshot(avatarStack);
      }

      player = client.getPlayerService().savePlayer(player);

      if (this.avatarFile != null) {
        this.uploadAvatar(player, this.avatarFile);
      }
      client.getPlayerService().savePlayer(player);
      client.clearCache();
      progressResultModel.getResults().add(player);
    } catch (Exception ex) {
      WidgetFactory.showAlert(Studio.stage, ex.getMessage());
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
