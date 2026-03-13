package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import static de.mephisto.vpin.ui.Studio.*;

public class AvatarUploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(AvatarUploadProgressModel.class);

  private final Iterator<File> iterator;

  public AvatarUploadProgressModel(File file) {
    super("Uploading Avatar");
    this.iterator = Arrays.asList(file).iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public File getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(File f) {
    return "Uploading \"" + f.getName() + "\"";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File f) {
    try {
      client.getPreferenceService().uploadVPinAvatar(f);
      if (Features.MANIA_ENABLED && maniaClient.getCabinetClient().getDefaultCabinetCached() != null) {
        maniaClient.getCabinetClient().updateAvatar(maniaClient.getCabinetClient().getDefaultCabinetCached().getId(), f, null, true);
      }
    } catch (Exception e) {
      LOG.error("Error waiting for server: " + e.getMessage(), e);
    }
  }
}
