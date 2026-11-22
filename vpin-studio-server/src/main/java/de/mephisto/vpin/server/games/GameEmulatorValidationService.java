package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.games.ValidationStateFactory;
import de.mephisto.vpin.restclient.validation.GameEmulatorValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class GameEmulatorValidationService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Max depth used when looking for game files: files directly in the games folder (1),
   * files in a table subfolder (2) and files one nested level deeper (3).
   */
  private static final int GAMES_SEARCH_DEPTH = 3;

  public List<ValidationState> validate(@NonNull FrontendType frontendType, @NonNull GameEmulator emulator, boolean findFirst) {
    List<ValidationState> result = new ArrayList<>();

    if (emulator.isFpEmulator() || emulator.isVpxEmulator() || emulator.isMameEmulator() || EmulatorType.OTHER.equals(emulator.getType())) {
      if (StringUtils.isEmpty(emulator.getInstallationDirectory()) || !emulator.getInstallationFolder().exists()) {
        result.add(ValidationStateFactory.create(GameEmulatorValidationCode.CODE_NO_INSTALLATION_DIRECTORY));
        if (findFirst) {
          return result;
        }
      }
    }

    if (frontendType.equals(FrontendType.Popper) && StringUtils.isEmpty(emulator.getGameExt()) && !emulator.isZaccariaEmulator()) {
      result.add(ValidationStateFactory.create(GameEmulatorValidationCode.CODE_NO_GAME_EXTENSION));
      if (findFirst) {
        return result;
      }
    }

    if (StringUtils.isEmpty(emulator.getGamesDirectory()) || !new File(emulator.getGamesDirectory()).exists()) {
      if (frontendType.equals(FrontendType.PinballX) && !emulator.isVpxEmulator() & !emulator.isFpEmulator() && !emulator.isZenEmulator()) {
        result.add(ValidationStateFactory.create(GameEmulatorValidationCode.CODE_NO_GAMES_FOLDER));
        if (findFirst) {
          return result;
        }
      }
    }

    if (!StringUtils.isEmpty(emulator.getRomDirectory()) && !new File(emulator.getRomDirectory()).exists()) {
      result.add(ValidationStateFactory.create(GameEmulatorValidationCode.CODE_INVALID_ROMS_FOLDER));
      if (findFirst) {
        return result;
      }
    }

    if ((emulator.isFpEmulator() || emulator.isVpxEmulator() || emulator.isMameEmulator()) && !StringUtils.isEmpty(emulator.getGamesDirectory()) && !StringUtils.isEmpty(emulator.getGameExt())) {
      File folder = new File(emulator.getGamesDirectory());
      if (folder.isDirectory() && !containsGameFiles(folder.toPath(), emulator.getGameExt())) {
        result.add(ValidationStateFactory.create(GameEmulatorValidationCode.CODE_NO_GAMES_FOUND));
        if (findFirst) {
          return result;
        }
      }
    }

    if (!StringUtils.isEmpty(emulator.getMediaDirectory()) && !new File(emulator.getMediaDirectory()).exists()) {
      result.add(ValidationStateFactory.create(GameEmulatorValidationCode.CODE_INVALID_MEDIA_FOLDER));
      if (findFirst) {
        return result;
      }
    }

    return result;
  }

  /**
   * Checks whether the games folder contains at least one file with the given extension.
   * Subfolders are searched as well, since VPX 10.8.1+ stores every table in its own
   * subfolder of the tables folder. The walk stops at the first match.
   */
  private boolean containsGameFiles(@NonNull Path gamesFolder, @NonNull String gameExt) {
    String ext = gameExt.toLowerCase();
    try (Stream<Path> paths = Files.find(gamesFolder, GAMES_SEARCH_DEPTH,
        (path, attrs) -> attrs.isRegularFile() && path.getFileName().toString().toLowerCase().endsWith(ext))) {
      return paths.findAny().isPresent();
    }
    catch (IOException | UncheckedIOException e) {
      // e.g. an inaccessible subfolder; do not report a false "no games found" in that case
      LOG.warn("Failed to scan games folder {}: {}", gamesFolder, e.getMessage());
      return true;
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
