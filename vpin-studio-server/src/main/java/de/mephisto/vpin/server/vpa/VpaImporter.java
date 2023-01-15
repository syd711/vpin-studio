package de.mephisto.vpin.server.vpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.ExportDescriptor;
import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreVersion;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class VpaImporter {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  private final Game game;
  private final ExportDescriptor exportDescriptor;
  private final List<HighscoreVersion> scoreHistory;
  private final File target;
  private final VpaExportListener listener;
  private final ObjectMapper objectMapper;

  public VpaImporter(@NonNull Game game, @NonNull ExportDescriptor exportDescriptor, @NonNull List<HighscoreVersion> scoreHistory, @NonNull File target, @NonNull VpaExportListener listener) {
    this.game = game;
    this.exportDescriptor = exportDescriptor;
    this.scoreHistory = scoreHistory;
    this.target = target;
    this.listener = listener;
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  public void startImport() {

  }
}
