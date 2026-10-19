package de.mephisto.vpin.ui.tables.dialogs.bulk;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.ui.tables.dialogs.bulk.BulkImportMatcher.MatchMode;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Reads the media files of the bulk importer source folder, a single indeterminate step reporting the files found.
 * When a matcher is available, the table suggestions of the found files are computed afterwards.
 */
public class BulkImportScanProgressModel extends ProgressModel<File> {
  private static final int STATUS_INTERVAL = 25;
  private static final int MATCH_STATUS_INTERVAL = 10;

  private final File folder;
  private final boolean recursive;
  private final Set<String> extensions;
  private final BulkImportMatcher matcher;
  private final MatchMode mode;
  private final int limit;
  private final List<BulkImportItem> items = new ArrayList<>();
  private final Iterator<File> iterator;
  private volatile boolean cancelled = false;
  private boolean matched = false;

  public BulkImportScanProgressModel(File folder, boolean recursive, Set<String> extensions,
                                     @Nullable BulkImportMatcher matcher, MatchMode mode, int limit) {
    super(Messages.get("bulk_importer.scanning"));
    this.folder = folder;
    this.recursive = recursive;
    this.extensions = extensions;
    this.matcher = matcher;
    this.mode = mode;
    this.limit = limit;
    this.iterator = Collections.singletonList(folder).iterator();
  }

  public List<BulkImportItem> getItems() {
    return items;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  /**
   * True when the suggestions of all items have been computed.
   */
  public boolean isMatched() {
    return matched;
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public boolean isShowSteps() {
    return false;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public File getNext() {
    return iterator.next();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public String nextToString(File folder) {
    return folder.getAbsolutePath();
  }

  @Override
  public void cancel() {
    cancelled = true;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File folder) throws Exception {
    List<File> found = new ArrayList<>();
    Predicate<File> isMedia = f -> extensions.contains(FilenameUtils.getExtension(f.getName()).toLowerCase());
    try (Stream<Path> walk = recursive ? Files.walk(folder.toPath()) : Files.list(folder.toPath())) {
      Iterator<Path> paths = walk.iterator();
      while (paths.hasNext() && !cancelled) {
        Path path = paths.next();
        if (!Files.isRegularFile(path)) {
          continue;
        }
        File file = path.toFile();
        if (isMedia.test(file)) {
          found.add(file);
          if (found.size() % STATUS_INTERVAL == 0) {
            progressResultModel.setStatusMessage(Messages.get("bulk_importer.scanning_found", found.size()));
          }
        }
      }
    }
    if (cancelled) {
      return;
    }
    found.sort(Comparator.comparing(f -> f.getAbsolutePath().toLowerCase()));
    for (File file : found) {
      items.add(new BulkImportItem(file, this.folder));
    }

    if (matcher != null) {
      progressResultModel.setProgress(0);
      for (int i = 0; i < items.size(); i++) {
        if (cancelled) {
          return;
        }
        BulkImportItem item = items.get(i);
        item.setSuggestions(matcher.suggest(item.getFile(), this.folder, mode, limit));
        if (i % MATCH_STATUS_INTERVAL == 0 || i == items.size() - 1) {
          progressResultModel.setStatusMessage(Messages.get("bulk_importer.matching_progress", i + 1, items.size()));
          progressResultModel.setProgress((i + 1d) / items.size());
        }
      }
      matched = true;
    }
    progressResultModel.addProcessed();
  }
}
