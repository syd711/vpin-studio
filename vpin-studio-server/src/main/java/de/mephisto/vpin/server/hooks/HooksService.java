package de.mephisto.vpin.server.hooks;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.restclient.hooks.HookCommand;
import de.mephisto.vpin.restclient.hooks.HookList;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  public HookList getHooks() {
    HookList result = new HookList();

    File hooksFolder = getHooksFolder();
    if (hooksFolder.exists()) {
      File[] files = hooksFolder.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.toLowerCase().endsWith(".exe") || name.toLowerCase().endsWith(".bat");
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
      commands.add(cmd.getHooks());
      commands.addAll(cmd.getCommands());
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
