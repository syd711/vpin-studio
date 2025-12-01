package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.converter.MediaConversionCommand;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class MediaConversionProgressModel extends ProgressModel<FrontendMediaItemRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Iterator<FrontendMediaItemRepresentation> iterator;
  private final int gameId;
  private final VPinScreen screen;
  private final List<FrontendMediaItemRepresentation> items;
  private final MediaConversionCommand command;

  public MediaConversionProgressModel(String title, int gameId, VPinScreen screen, List<FrontendMediaItemRepresentation> items, MediaConversionCommand command) {
    super(title);
    this.gameId = gameId;
    this.screen = screen;
    this.items = items;
    this.command = command;
    this.iterator = items.iterator();
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
  public FrontendMediaItemRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return items.size() == 1;
  }

  @Override
  public String nextToString(FrontendMediaItemRepresentation item) {
    return "Converting video \"" + item.getName() + "\"";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, FrontendMediaItemRepresentation item) {
    try {
      client.getMediaConversionService().convert(gameId, screen, item.getName(), command);
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
