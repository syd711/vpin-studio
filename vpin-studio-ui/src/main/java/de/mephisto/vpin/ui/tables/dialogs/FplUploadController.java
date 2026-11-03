package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.ui.util.UploadProgressModel;

public class FplUploadController extends BaseUploadController {

  public FplUploadController() {
    super(AssetType.FPL, true, true, "fpl");
  }

  @Override
  protected boolean isFpOnly() {
    return true;
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new FplUploadProgressModel(Messages.get("dialog.upload_fpl"), getSelections(), getSelectedEmulatorId());
  }
}
