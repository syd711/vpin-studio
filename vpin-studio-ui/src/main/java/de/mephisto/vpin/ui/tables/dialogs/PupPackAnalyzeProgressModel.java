package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.PupPackAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class PupPackAnalyzeProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackAnalyzeProgressModel.class);

  private final Iterator<File> iterator;
  private final String rom;
  private final String tableName;
  private final File file;
  private PupPackAnalyzer analyzer;

  public PupPackAnalyzeProgressModel(String rom, String tableName, String title, File file) {
    super(title);
    this.rom = rom;
    this.tableName = tableName;
    this.file = file;
    this.iterator = Collections.singletonList(this.file).iterator();
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
  public File getNext() {
    return iterator.next();
  }

  @Override
  public void cancel() {
    super.cancel();
    this.analyzer.cancel();
  }

  @Override
  public String nextToString(File file) {
    return "Analyzing " + file.getName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      analyzer = new PupPackAnalyzer();
      List<String> roms = new ArrayList<>();
      if (rom != null) {
        roms.add(rom);
      }
      if (tableName != null && !roms.contains(tableName)) {
        roms.add(tableName);
      }
      analyzer.analyze(next, roms, progressResultModel);
      progressResultModel.addProcessed();
    } catch (Exception e) {
      LOG.error("PUP pack upload failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
