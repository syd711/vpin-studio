package de.mephisto.vpin.ui.players.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.AvatarImageUtil;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import eu.hansolo.tilesfx.Tile;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.FutureTask;

public class AvatarGeneratorProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(AvatarGeneratorProgressModel.class);

  private final Tile avatar;
  private List<File> files;
  private File avatarFile;
  private final Iterator<File> avatarIterator;

  public AvatarGeneratorProgressModel(Tile avatar, File avatarFile) {
    super("Generating Avatar");
    this.avatar = avatar;
    this.files = Arrays.asList(avatarFile);
    this.avatarIterator = files.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return files.size();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public boolean hasNext() {
    return this.avatarIterator.hasNext();
  }

  @Override
  public File getNext() {
    return avatarIterator.next();
  }

  @Override
  public String nextToString(File f) {
    return "";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File file) {
    try {
      FutureTask<Object> futureTask = new FutureTask<>(() -> {
        try {
          this.avatarFile = AvatarImageUtil.createAvatar(file);
          FileInputStream fileInputStream = new FileInputStream(avatarFile);
          Image image = new Image(fileInputStream);
          avatar.setImage(image);
          fileInputStream.close();
        } catch (Exception e) {
          LOG.error("Failed to crop avatar image: " + e.getMessage());
        }
      }, null);
      Platform.runLater(futureTask);
      futureTask.get();

      progressResultModel.getResults().add(this.avatarFile);
    } catch (Exception ex) {
      progressResultModel.getResults().add(ex.getMessage());
      WidgetFactory.showAlert(Studio.stage, ex.getMessage());
    }
  }

}
