package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.commons.utils.ZipUtil;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class PopperMediaService {
  private final static Logger LOG = LoggerFactory.getLogger(PopperMediaService.class);

  @Autowired
  private GameService gameService;

  public void installMediaPack(@NonNull UploadDescriptor uploadDescriptor, @Nullable UploaderAnalysis analysis) throws Exception {
    File tempFile = new File(uploadDescriptor.getTempFilename());
    if (analysis == null) {
      analysis = new UploaderAnalysis(tempFile);
      analysis.analyze();
    }

    Game game = gameService.getGame(uploadDescriptor.getGameId());
    PopperScreen[] values = PopperScreen.values();
    for (PopperScreen value : values) {
      List<String> filesForScreen = analysis.getPopperMediaFiles(value);

      for (String popperMediaFile : filesForScreen) {
        if (popperMediaFile.toLowerCase().contains("macosx")) {
          continue;
        }

        String suffix = FilenameUtils.getExtension(popperMediaFile);
        File out = uniquePopperAsset(game, value, suffix);
        ZipUtil.unzipTargetFile(tempFile, out, popperMediaFile);
        LOG.info("Created \"" + out.getAbsolutePath() + "\" for popper screen \"" + value.name() + "\"");
      }
    }
  }


  public File uniquePopperAsset(Game game, PopperScreen screen) {
    String suffix = "mp4";
    if (screen.equals(PopperScreen.AudioLaunch) || screen.equals(PopperScreen.Audio)) {
      suffix = "mp3";
    }
    return uniquePopperAsset(game, screen, suffix);
  }

  public File uniquePopperAsset(Game game, PopperScreen screen, String suffix) {
    File out = new File(game.getPinUPMediaFolder(screen), game.getGameName() + "." + suffix);
    if (out.exists()) {
      String nameIndex = "01";
      out = new File(out.getParentFile(), game.getGameName() + nameIndex + "." + suffix);
    }

    int index = 1;
    while (out.exists()) {
      index++;
      String nameIndex = index <= 9 ? "0" + index : String.valueOf(index);
      out = new File(out.getParentFile(), game.getGameName() + nameIndex + "." + suffix);
    }
    return out;
  }
}
