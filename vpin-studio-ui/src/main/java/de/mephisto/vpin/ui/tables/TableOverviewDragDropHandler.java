package de.mephisto.vpin.ui.tables;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.mephisto.vpin.restclient.games.GameRepresentation;

public class TableOverviewDragDropHandler extends BaseDragDropHandler {
  
  private final TablesController tablesController;

  private final List<String> suffixes = Arrays.asList("vpx", "zip", "rar", "res", "ini", "pov", "directb2s", "vni", "pal", "pac", "crz", "cfg", "nv");

  public TableOverviewDragDropHandler(TablesController tablesController) {
    super(tablesController.getTableOverviewController().getTableView(), 
      tablesController.getTableOverviewController().getLoaderStack(), 
      false);
    this.tablesController = tablesController;
  }

  //@Override
  protected GameRepresentation getSelectedGame() {
    return tablesController.getTableOverviewController().getSelection();
  }

  @Override
  protected boolean acceptFile(File file) {
    String extension = FilenameUtils.getExtension(file.getName());
    return suffixes.contains(extension);
  }

  @Override
  protected void processDroppedFile(File file) {
    GameRepresentation selection = getSelectedGame();
    UploadAnalysisDispatcher.dispatch(file, selection);
  }
}
