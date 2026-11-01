package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.ui.util.UploadProgressModel;

public class CfgUploadController extends BaseUploadController {

  public CfgUploadController() {
    super(AssetType.CFG, true, true, "cfg", "zip", "7z", "rar");
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new CfgUploadProgressModel(Messages.get("dialog.upload_cfg"), getSelections(), getSelectedEmulatorId(), getSelectedGameId());
  }
}
