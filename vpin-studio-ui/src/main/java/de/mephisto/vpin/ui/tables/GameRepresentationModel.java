package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.validation.*;
import de.mephisto.vpin.ui.tables.panels.BaseGameModel;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;

import static de.mephisto.vpin.commons.utils.WidgetFactory.DISABLED_COLOR;
import static de.mephisto.vpin.ui.Studio.client;

public class GameRepresentationModel extends BaseLoadingModel<GameRepresentation, GameRepresentationModel> implements BaseGameModel {

  VpsTable vpsTable;

  GameEmulatorRepresentation gameEmulator;

  FrontendMediaRepresentation frontendMedia;

  public GameRepresentationModel(GameRepresentation game) {
    super(game);
  }

  public GameRepresentation getGame() {
    return getBean();
  }

  @Override     // BaseGameModel
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

  public String getStatusColor(VPinScreen screen, ValidationSettings validationSettings) {
    FrontendMediaItemRepresentation defaultMediaItem = getFrontendMedia().getDefaultMediaItem(screen);
    ValidationProfile defaultProfile = validationSettings.getDefaultProfile();
    ValidationConfig config = defaultProfile.getOrCreateConfig(screen.getValidationCode());
    boolean ignored = bean.getIgnoredValidations().contains(screen.getValidationCode());

    if (defaultMediaItem != null) {
      String mimeType = defaultMediaItem.getMimeType();
      if (mimeType.contains("audio")) {
        if (!ignored && !config.getMedia().equals(ValidatorMedia.audio)) {
          return WidgetFactory.ERROR_COLOR;
        }
      }
      else if (mimeType.contains("image")) {
        if (!ignored && !config.getMedia().equals(ValidatorMedia.image) && !config.getMedia().equals(ValidatorMedia.imageOrVideo)) {
          return WidgetFactory.ERROR_COLOR;
        }
      }
      else if (mimeType.contains("video")) {
        if (!ignored && !config.getMedia().equals(ValidatorMedia.video) && !config.getMedia().equals(ValidatorMedia.imageOrVideo)) {
          return WidgetFactory.ERROR_COLOR;
        }
      }

      if (!ignored && config.getOption().equals(ValidatorOption.empty)) {
        return WidgetFactory.ERROR_COLOR;
      }
    }
    else {
      if (!ignored) {
        if (config.getOption().equals(ValidatorOption.empty)) {
          return DISABLED_COLOR;
        }
        else if (config.getOption().equals(ValidatorOption.optional)) {
          return DISABLED_COLOR;
        }
        else if (config.getOption().equals(ValidatorOption.mandatory)) {
          return WidgetFactory.ERROR_COLOR;
        }
      }
    }

    return "#FFFFFF";
  }

  @Override
  public String getName() {
    return bean.getGameDisplayName();
  }

  @Override
  public void load() {
    this.frontendMedia = null;
    if (bean != null) {
      this.vpsTable = client.getVpsService().getTableById(bean.getExtTableId());
      this.gameEmulator = client.getEmulatorService().getGameEmulator(bean.getEmulatorId());
    }
  }
}