package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.ImportDescriptor;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class TableImportProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(TableImportProgressModel.class);

  private final ImportDescriptor descriptor;
  private final Iterator<GameRepresentation> iterator;
  private final List<GameRepresentation> games;

  public TableImportProgressModel(String title, ImportDescriptor descriptor, List<GameRepresentation> games) {
    super(title);
    this.descriptor = descriptor;
    this.iterator = games.iterator();
    this.games = games;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return games.size();
  }

  @Override
  public String processNext(ProgressResultModel progressResultModel) {
    try {
      GameRepresentation next = iterator.next();
      descriptor.setGameId(next.getId());

//      Studio.client.import(descriptor);
      progressResultModel.addProcessed();
      return next.getGameDisplayName();
    } catch (Exception e) {
      LOG.error("Table export failed: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
