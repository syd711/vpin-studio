package de.mephisto.vpin.ui.backglassmanager.dialogs;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * Searches the tables that have a backglass or DMD image but no corresponding media asset.
 */
public class BackglassBulkImportSearchModel extends ProgressModel<DirectB2S> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final List<DirectB2S> candidates;
  private final BackglassBulkImportType type;
  private final Iterator<DirectB2S> iterator;
  private final List<BackglassBulkImportItem> result = Collections.synchronizedList(new ArrayList<>());

  public BackglassBulkImportSearchModel(List<DirectB2S> candidates, BackglassBulkImportType type) {
    super(Messages.get("backglass.bulk_import.search_title"));
    this.candidates = candidates;
    this.type = type;
    this.iterator = candidates.iterator();
  }

  public List<BackglassBulkImportItem> getResult() {
    synchronized (result) {
      return new ArrayList<>(result);
    }
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return candidates.size();
  }

  @Override
  public DirectB2S getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(DirectB2S backglass) {
    return Messages.get("backglass.bulk_import.checking") + backglass.getName();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, DirectB2S backglass) {
    try {
      int gameId = backglass.getGameId();
      if (!client.getGameMediaService().getGameMedia(gameId).getMediaItems(type.getScreen()).isEmpty()) {
        progressResultModel.addSkipped();
        return;
      }

      DirectB2SData data = client.getBackglassServiceClient().getDirectB2SData(gameId);
      boolean imageAvailable = data != null && (type == BackglassBulkImportType.DMD ? data.isDmdImageAvailable() : data.isBackgroundAvailable());
      if (!imageAvailable) {
        progressResultModel.addSkipped();
        return;
      }

      GameRepresentation game = client.getGameService().getGame(gameId);
      if (game == null) {
        progressResultModel.addSkipped();
        return;
      }

      result.add(new BackglassBulkImportItem(gameId, game.getGameDisplayName(), backglass));
      progressResultModel.addProcessed();
    }
    catch (Exception e) {
      LOG.error("Failed to check backglass \"{}\": {}", backglass.getName(), e.getMessage(), e);
      progressResultModel.addError();
    }
  }
}
