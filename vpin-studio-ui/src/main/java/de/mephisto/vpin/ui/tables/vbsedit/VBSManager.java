package de.mephisto.vpin.ui.tables.vbsedit;

import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import static de.mephisto.vpin.ui.Studio.client;

public class VBSManager {
  private final static Logger LOG = LoggerFactory.getLogger(VBSManager.class);

  private static final VBSManager instance = new VBSManager();
  private final VbsMonitoringService monitoringService = new VbsMonitoringService();


  private boolean embeddedEditing = false;//!System.getProperty("os.name").contains("Windows");

  private VBSManager() {
    monitoringService.startMonitoring(getVbsFolder());
    LOG.info("VPS Monitor started.");
  }

  public static VBSManager getInstance() {
    return instance;
  }


  public void edit(Optional<GameRepresentation> game) {
    edit(game, false);
  }

  public void edit(Optional<GameRepresentation> game, boolean embeddedEditor) {
    try {
      TextFile textFile = new TextFile(VPinFile.VBScript);
      textFile.setFileId(game.get().getId());

      if (game.isPresent()) {
        if (embeddedEditor) {
          boolean b = Dialogs.openTextEditor(textFile, game.get().getGameFileName());
          if (b) {
            client.getMameService().clearCache();
            EventManager.getInstance().notifyTablesChanged();
          }
        }
        else {
          TextFile value = client.getTextEditorService().getText(textFile);
          File vbsFile = writeVbsFile(game.get(), value.getContent());

          openFile(vbsFile);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open table VPS: " + e.getMessage(), e);
      String msg = e.getMessage();
      if(msg != null) {
        msg = msg.replaceAll("\\\\n", "\n");
      }
      WidgetFactory.showOutputDialog(Studio.stage, "Error",  "The extraction of the .vbs file failed.", "Please report this issue to: https://github.com/syd711/vpin-studio/issues", msg);
    }
  }

  private static void openFile(File vbsFile) {
    Studio.edit(vbsFile);
  }

  private static File getVbsFolder() {
    File basePath = Updater.getWriteableBaseFolder();
    File vpsFolder = new File(basePath, "./resources/vbs/");
    if (!vpsFolder.exists()) {
      vpsFolder.mkdirs();
    }
    return vpsFolder;
  }

  private File writeVbsFile(GameRepresentation game, String content) throws IOException {
    monitoringService.setPaused(true);
    String name = game.getGameName() + "[" + game.getId() + "].vbs";
    File vbsFile = new File(getVbsFolder(), name);
    if (vbsFile.exists()) {
      if (!vbsFile.delete()) {
        throw new IOException("Failed to delete " + vbsFile.getAbsolutePath());
      }
      LOG.info("Deleted existing " + vbsFile.getAbsolutePath());
    }

    Files.write(vbsFile.toPath(), content.getBytes());
    LOG.info("Written .vbs file '" + vbsFile.getAbsolutePath() + "'");
    monitoringService.setPaused(false);
    return vbsFile;
  }
}
