package de.mephisto.vpin.server.hooks;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.restclient.hooks.HookCommand;
import de.mephisto.vpin.restclient.hooks.HookList;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
public class HooksService {
  private final static Logger LOG = LoggerFactory.getLogger(HooksService.class);

  @Autowired
  private GameService gameService;

  public HookList getHooks() {
    HookList result = new HookList();

    File hooksFolder = getHooksFolder();
    if (hooksFolder.exists()) {
      File[] files = hooksFolder.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.toLowerCase().endsWith(".exe") ||
              name.toLowerCase().endsWith(".bat") ||
//              name.toLowerCase().endsWith(".ps1") ||
              name.toLowerCase().endsWith(".vbs");
        }
      });

      if (files != null) {
        for (File file : files) {
          result.getHooks().add(file.getName());
        }
      }
    }
    return result;
  }

  @NonNull
  private static File getHooksFolder() {
    return new File(SystemInfo.RESOURCES, "hooks");
  }

  public HookCommand execute(HookCommand cmd) {
    try {
      List<String> commands = new ArrayList<>();
      if (cmd.getName().toLowerCase().endsWith(".vps")) {
        cmd.getCommands().add("cscript");
      }
//      else if (cmd.getName().toLowerCase().endsWith(".ps1")) {
//        cmd.getCommands().add("powershell.exe");
//      }

      commands.add(cmd.getName());
      commands.addAll(cmd.getCommands());

      Game game = gameService.getGame(cmd.getGameId());
      if (game != null) {
        String rom = game.getRom();
        String fileName = game.getGameFileName();

        if (!StringUtils.isEmpty(fileName)) {
          commands.add(fileName);
        }

        if (!StringUtils.isEmpty(rom)) {
          commands.add(rom);
        }
      }

      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(getHooksFolder());
      executor.executeCommandAsync();
    }
    catch (Exception e) {
      LOG.error("Hook command execution failed: {}", e.getMessage(), e);
    }
    return cmd;
  }
}
