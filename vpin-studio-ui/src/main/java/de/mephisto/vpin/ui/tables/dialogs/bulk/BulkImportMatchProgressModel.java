package de.mephisto.vpin.ui.tables.dialogs.bulk;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportMatcher.MatchMode;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Computes the table suggestions of the bulk importer items, one step per file.
 */
public class BulkImportMatchProgressModel extends ProgressModel<BulkImportItem> {
  private final BulkImportMatcher matcher;
  private final List<BulkImportItem> items;
  private final File root;
  private final MatchMode mode;
  private final int limit;
  private final Iterator<BulkImportItem> iterator;

  public BulkImportMatchProgressModel(BulkImportMatcher matcher, List<BulkImportItem> items, @Nullable File root, MatchMode mode, int limit) {
    super(Messages.get("bulk_importer.matching"));
    this.matcher = matcher;
    this.items = items;
    this.root = root;
    this.mode = mode;
    this.limit = limit;
    this.iterator = items.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return items.size();
  }

  @Override
  public BulkImportItem getNext() {
    return iterator.next();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public String nextToString(BulkImportItem item) {
    return item.getRelativePath();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, BulkImportItem item) {
    item.setSuggestions(matcher.suggest(item.getFile(), root, mode, limit));
    progressResultModel.addProcessed();
  }
}
