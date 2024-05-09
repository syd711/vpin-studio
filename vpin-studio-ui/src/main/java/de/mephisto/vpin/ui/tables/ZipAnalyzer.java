package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

public class ZipAnalyzer extends UploadDispatchAnalysisZipProgressModel {
  public ZipAnalyzer(GameRepresentation game, File file) throws IOException {
    super(game, file);
  }

  @Override
  protected void analyze(ZipEntry entry) {

  }
}
