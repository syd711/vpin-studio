package de.mephisto.vpin.server;

import de.mephisto.vpin.server.util.SystemCommandExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class StateManager {
  private final static Logger LOG = LoggerFactory.getLogger(StateManager.class);
  private static final String VPIN_EXTENSIONS_JAR = "vpin-extensions.jar";

  public boolean isInstalled() {
    return getAutostartFile().exists();
  }

  public boolean isRunning() {
//    return RequestUtil.doGet("http://localhost:" + HttpServer.PORT + "/service" + AsyncServlet.PATH_PING);
    return false;
  }

  public void waitForRunState() {
    try {
      int attempts = 0;
      while(!isRunning()) {
        Thread.sleep(1000);
        attempts++;

        if(attempts > 30) {
          break;
        }
      }
    } catch (InterruptedException e) {
      //ignore
    }
  }

  public void start() throws Exception {
    List<String> commands = Arrays.asList(("jdk/bin/java -jar " + VPIN_EXTENSIONS_JAR).split(" "));
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
      executor.setDir(new File("./"));
      executor.executeCommandAsync();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        throw new Exception(standardErrorFromCommand.toString());
      }
    } catch (Exception e) {
      String message = "Failed start service via command: " + e.getMessage();
      LOG.info(message, e);
      throw new Exception(message);
    }
  }

  public boolean shutdown() {
//    return RequestUtil.doGet("http://localhost:" + HttpServer.PORT + "/service" + AsyncServlet.PATH_SYSTEM_EXIT);
    return true;
  }

  public void install() throws IOException {
    File root = new File("./");
    String script = "cd /D " + root.getAbsolutePath() +
        "\nstart jdk/bin/javaw -jar " + new File(root, VPIN_EXTENSIONS_JAR).getAbsolutePath();
    FileUtils.writeStringToFile(getAutostartFile(), script, Charset.forName("UTF-8"));
    LOG.info("Written autostart file " + getAutostartFile().getAbsolutePath());
  }

  public boolean uninstall() throws Exception {
    try {
      shutdown();
      Thread.sleep(1000);
      if (!getAutostartFile().delete()) {
        throw new Exception("Failed to delete autostart file " + getAutostartFile().getAbsolutePath());
      }
      LOG.info("Deleted " + getAutostartFile().getAbsolutePath());
    } catch (InterruptedException e) {
      LOG.error("Uninstall failed: " + e.getMessage());
    }
    return false;
  }

  private File getAutostartFile() {
    String path = "C:/Users/%s/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/vpin-service.bat";
    String userName = System.getProperty("user.name");

    String formattedPath = String.format(path, userName);
    return new File(formattedPath);
  }
}
