package de.mephisto.vpin.server.steam;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class SteamService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(SteamService.class);

  private final static String ZACCARIA_LAUNCH = "-applaunch 444930 -rotate right -skipmenu \"%s\" -skipmenu_player 1 -skipmenu_gamemode classic_simulation";
  private final static String FX_LAUNCH = "-applaunch 2328760 -Table %s -GameMode Classic";
  private final static String FX3_LAUNCH = "-applaunch 442120 -class -table_%s";
  private final static String M_LAUNCH = "-applaunch 2337640 -Table %s -GameMode Classic";

  @Nullable
  public File getGameFolder(@NonNull EmulatorType emulatorType) {
    Map<String, File> gameFolders = SteamUtil.getGameFolders();
    if (!StringUtils.isEmpty(emulatorType.folderName()) && gameFolders.containsKey(emulatorType.folderName())) {
      File file = gameFolders.get(emulatorType.folderName());
      LOG.info("SteamService resolved game folder {}", file.getAbsolutePath());
      return file;
    }
    return null;
  }

  public boolean play(@NonNull Game game) {
    try {
      File gameFile = game.getGameFile();
      GameEmulator emulator = game.getEmulator();
      EmulatorType type = emulator.getType();

      File steamExe = new File(SteamUtil.getSteamFolder(), "steam.exe");
      List<String> params = new ArrayList<>();
      params.add(steamExe.getAbsolutePath());

      switch (type) {
        case ZenFX: {
          String cmd = String.format(FX_LAUNCH, game.getRom());
          params.addAll(Arrays.asList(cmd.split(" ")));
          break;
        }
        case ZenFX3: {
          String cmd = String.format(FX3_LAUNCH, game.getGameName());
          params.addAll(Arrays.asList(cmd.split(" ")));
          break;
        }
        case PinballM: {
          String cmd = String.format(M_LAUNCH, game.getRom());
          params.addAll(Arrays.asList(cmd.split(" ")));
          break;
        }
        case Zaccaria: {
          String cmd = String.format(ZACCARIA_LAUNCH, game.getGameName());
          params.addAll(Arrays.asList(cmd.split(" ")));
          break;
        }
      }


      SystemCommandExecutor executor = new SystemCommandExecutor(params, false);
      executor.setDir(SteamUtil.getSteamFolder());
      executor.executeCommandAsync();

      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (standardErrorFromCommand != null && !StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("Steam command failed:\n" + standardErrorFromCommand);
        return false;
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("Error executing Steam command: " + e.getMessage(), e);
    }
    return false;
  }

  public File getSteamFolder() {
    return SteamUtil.getSteamFolder();
  }

  @Override
  public void afterPropertiesSet() throws Exception {

  }
}
