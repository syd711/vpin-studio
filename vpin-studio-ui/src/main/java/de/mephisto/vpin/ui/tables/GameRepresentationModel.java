package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;

import static de.mephisto.vpin.ui.Studio.client;

public class GameRepresentationModel extends BaseLoadingModel<GameRepresentation, GameRepresentationModel> {

  VpsTable vpsTable;

  GameEmulatorRepresentation gameEmulator;

  FrontendMediaRepresentation frontendMedia;

  public GameRepresentationModel(GameRepresentation game) {
    super(game);
  }

  public GameRepresentation getGame() {
    return getBean();
  }

  public int getGameId() {
    return bean.getId();
  }

  @Override
  public boolean sameBean(GameRepresentation other) {
    return bean.getId() == other.getId();
  }

  public FrontendMediaRepresentation getFrontendMedia() {
    if (frontendMedia == null) {
      frontendMedia = client.getFrontendMedia(bean.getId());
    }
    return frontendMedia;
  }

  public VpsTable getVpsTable() {
    return vpsTable;
  }

  public GameEmulatorRepresentation getGameEmulator() {
    return gameEmulator;
  }

  @Override
  public String getName() {
    return bean.getGameDisplayName();
  }

  @Override
  public void load() {
    this.vpsTable = client.getVpsService().getTableById(bean.getExtTableId());
    this.gameEmulator = client.getFrontendService().getGameEmulator(bean.getEmulatorId());
  }
}