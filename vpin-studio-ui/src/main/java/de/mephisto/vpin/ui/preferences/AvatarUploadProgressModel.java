package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

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
      if (Features.MANIA_ENABLED && maniaClient.getCabinetClient() != null) {
        maniaClient.getCabinetClient().updateAvatar(f, null, true);

        BufferedImage avtr = ImageIO.read(f);
        List<Tournament> tournaments = maniaClient.getTournamentClient().getTournaments();
        for (Tournament tournament : tournaments) {
          maniaClient.getTournamentClient().updateBadge(avtr, tournament);
          LOG.info("Updated tournament badge for " + tournament);
        }
      }
    } catch (Exception e) {
      LOG.error("Error waiting for server: " + e.getMessage(), e);
    }
  }
}
