package de.mephisto.vpin.ui.tables.drophandler;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.AssetCopy;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TableMediaCopyProgressModel extends ProgressModel<FrontendMediaItemRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(TableMediaCopyProgressModel.class);

  private final Iterator<FrontendMediaItemRepresentation> iterator;
  private final VPinScreen screen;
  private final List<FrontendMediaItemRepresentation> items = new ArrayList<>();

  public TableMediaCopyProgressModel(VPinScreen screen, FrontendMediaItemRepresentation mediaItem) {
    super("Copying " + mediaItem.getName());
    this.screen = screen;
    this.items.add(mediaItem);
    this.iterator = this.items.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return this.items.size();
  }

  @Override
  public FrontendMediaItemRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return this.items.size() == 1;
  }

  @Override
  public String nextToString(FrontendMediaItemRepresentation item) {
    return "Copying \"" + item.getName() + "\" to screen \"" + screen.name() + "\"";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, FrontendMediaItemRepresentation media) {
    try {
      client.getGameMediaService().copyMedia(screen, media);
    }
    catch (Exception e) {
      LOG.error("Copy failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
