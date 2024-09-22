package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.video.VideoConversionCommand;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;

import static de.mephisto.vpin.ui.Studio.client;

public class VideoConversionProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(VideoConversionProgressModel.class);

  private final Iterator<String> iterator;
  private final int gameId;
  private final VPinScreen screen;
  private final String name;
  private final VideoConversionCommand command;

  public VideoConversionProgressModel(String title, int gameId, VPinScreen screen, String name, VideoConversionCommand command) {
    super(title);
    this.gameId = gameId;
    this.screen = screen;
    this.name = name;
    this.command = command;
    this.iterator = Arrays.asList(name).iterator();
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
  public String getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(String term) {
    return "Converting video '" + term + "'";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String term) {
    try {
      client.getVideoConversionService().convert(gameId, screen, name, command);
    }
    catch (Exception e) {
      LOG.error("Video conversion failed: " + e.getMessage(), e);
      progressResultModel.getResults().add("Video conversion failed: " + e.getMessage());
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
