package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.UploadProgressModel;

public class AltColorUploadController extends BaseUploadController {

  private GameRepresentation game;

  public AltColorUploadController() {
    super(AssetType.ALT_COLOR, false, false, "zip", "pac", "rar", "vni", "pal", "cRZ");
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new AltColorUploadProgressModel(this.game.getId(), "ALT Color Upload", getSelection(), "altcolor");
  }

}
