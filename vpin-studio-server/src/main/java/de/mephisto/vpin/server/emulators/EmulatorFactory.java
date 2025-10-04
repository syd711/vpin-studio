package de.mephisto.vpin.server.emulators;

import de.mephisto.vpin.restclient.emulators.EmulatorValidation;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
import de.mephisto.vpin.server.frontend.popper.PUPGameImporter;
import de.mephisto.vpin.server.steam.SteamService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class EmulatorFactory implements ApplicationContextAware {

  private ApplicationContext applicationContext;

  @NonNull
  public EmulatorValidation create(@NonNull EmulatorType emulatorType) {
    EmulatorValidation validation = new EmulatorValidation();

    switch (emulatorType) {
      case ZenFX: {
        validateZenFX(emulatorType, validation);
        break;
      }
      case ZenFX2: {
        validateDefault(emulatorType, validation);
        break;
      }
      case ZenFX3: {
        break;
      }
      case PinballM: {
        break;
      }
      case Zaccaria: {
        break;
      }
      case PinballArcade: {
        validateDefault(emulatorType, validation);
        break;
      }
      case VisualPinball9: {
        break;
      }
      case VisualPinball: {
        break;
      }
      case FuturePinball: {
        break;
      }
      case MAME: {
        validateDefault(emulatorType, validation);
        break;
      }
      case OTHER: {
        validateDefault(emulatorType, validation);
        break;
      }
    }
    return validation;
  }

  private void validateDefault(EmulatorType emulatorType, EmulatorValidation validation) {
    GameEmulatorRepresentation emu = new GameEmulatorRepresentation();
    emu.setType(emulatorType);
    validation.setGameEmulator(emu);
  }

  private void validateZenFX(EmulatorType emulatorType, EmulatorValidation validation) {
    File gameFolder = getSteamService().getGameFolder(emulatorType);
    if (gameFolder == null) {
      validation.setErrorTitle("No installation directory found for this emulator type.");
      validation.setErrorText("The automatic setup for this emulator is supported, but the installation directory was not found. Please contact support.");
      return;
    }

    List<TableDetails> read = PUPGameImporter.read(emulatorType);
    if (read.isEmpty()) {
      validation.setErrorTitle("The PUP game import failed.");
      validation.setErrorText("The automatic setup for this emulator is supported, but the automatic game import failed. Please contact support.");
      return;
    }

    GameEmulatorRepresentation emu = new GameEmulatorRepresentation();
    emu.setType(emulatorType);
    emu.setInstallationDirectory(gameFolder.getAbsolutePath());
    emu.setExeName("");
    emu.setGameExt("pxp");
    emu.setSafeName(emulatorType.folderName());
    emu.setDescription(emulatorType.folderName());
    emu.setGamesDirectory("");
    emu.setExitScript(createScript(false, "\"[STARTDIR]LAUNCH\\PUPCLOSER.EXE\" WINTIT \"PinballFX\" 4 1"));
    emu.setLaunchScript(createScript(true, "SET ALTPARAM=\n" +
        "if \"[ALTMODE]\"==\"Classic\"  (SET ALTPARAM=Classic)\n" +
        "if \"[ALTMODE]\"==\"Hotseat2\" (SET ALTPARAM=Hotseat_2 )\n" +
        "if \"[ALTMODE]\"==\"Hotseat3\" (SET ALTPARAM=Hotseat_3 )\n" +
        "if \"[ALTMODE]\"==\"Hotseat4\" (SET ALTPARAM=Hotseat_4 )\n" +
        "if \"[ALTMODE]\"==\"Pro\" (SET ALTPARAM=Pro )\n" +
        "if \"[ALTMODE]\"==\"Practice\" (SET ALTPARAM=Practice )\n" +
        "\n" +
        "START \"\" \"[STARTDIR]Launch\\VPXSTARTER.exe\" 5 5 20 \"PinballFX\"\n" +
        "cd /d \"[DIREMU]\"\n" +
        "START \"\" \"steam.exe\" -applaunch 2328760 -Table [?ROM?] -GameMode %ALTPARAM%"));

    MediaAccessStrategy mediaStrategy = getFrontendService().getFrontendConnector().getMediaAccessStrategy();
    File folder = mediaStrategy.getEmulatorMediaFolder(emulatorType);
    if (folder != null) {
      emu.setMediaDirectory(folder.getAbsolutePath());
    }
    validation.setGameEmulator(emu);
  }

  private GameEmulatorScript createScript(boolean startScript, String script) {
    GameEmulatorScript s = new GameEmulatorScript();
    s.setScript(script);
    s.setStartScript(startScript);
    return s;
  }

  private SteamService getSteamService() {
    return applicationContext.getBean(SteamService.class);
  }

  private FrontendService getFrontendService() {
    return applicationContext.getBean(FrontendService.class);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
