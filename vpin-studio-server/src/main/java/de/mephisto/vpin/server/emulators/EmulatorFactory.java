package de.mephisto.vpin.server.emulators;

import de.mephisto.vpin.commons.SystemInfoWindows;
import de.mephisto.vpin.restclient.emulators.EmulatorValidation;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
import de.mephisto.vpin.server.frontend.popper.PUPGameImporter;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.steam.SteamService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.Nullable;
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
        validateZenFX3(emulatorType, validation);
        break;
      }
      case PinballM: {
        validateZenM(emulatorType, validation);
        break;
      }
      case Zaccaria: {
        validateZaccaria(emulatorType, validation);
        break;
      }
      case PinballArcade: {
        validateDefault(emulatorType, validation);
        break;
      }
      case VisualPinball9: {
        validateVPX9(emulatorType, validation);
        break;
      }
      case VisualPinball: {
        validateVPX(emulatorType, validation);
        break;
      }
      case FuturePinball: {
        validateFuturePinball(emulatorType, validation);
        break;
      }
      case MAME: {
        validateMAME(emulatorType, validation);
        break;
      }
      case OTHER: {
        validatePC(emulatorType, validation);
        break;
      }
    }
    return validation;
  }

  private void validateDefault(EmulatorType emulatorType, EmulatorValidation validation) {
    GameEmulatorRepresentation emu = new GameEmulatorRepresentation();
    emu.setType(emulatorType);
    emu.setEnabled(true);
    validation.setGameEmulator(emu);
  }

  private void validateVPX(EmulatorType emulatorType, EmulatorValidation validation) {
    GameEmulatorRepresentation emu = new GameEmulatorRepresentation();
    emu.setType(emulatorType);
    emu.setEnabled(true);
    emu.setGameExt("vpx");
    emu.setInstallationDirectory("C:\\vPinball\\VisualPinball");
    emu.setGamesDirectory("C:\\vPinball\\VisualPinball");

    File vpxExe = getSystemService().resolveVpx64Exe();
    if (vpxExe.exists()) {
      emu.setInstallationDirectory(vpxExe.getParentFile().getAbsolutePath());
      emu.setGamesDirectory(new File(vpxExe.getParentFile(), "Tables").getAbsolutePath());
    }

    emu.setLaunchScript(createScript(true, "@echo off\n" +
        "START \"\" \"[STARTDIR]Launch\\VPXSTARTER.exe\" 30 10 60 \"Visual Pinball Player\" 2\n" +
        "cd /d \"[DIREMU]\"\n" +
        "\n" +
        "rem Change the following to EnableTrueFullScreen to default FullScreen Exclusive!\n" +
        "SET FSMODE=DisableTrueFullScreen\n" +
        "\n" +
        "SET VPXEXE=vpinballx.exe\n" +
        "\n" +
        "if \"[RECMODE]\"==\"1\" (SET FSMODE=DisableTrueFullScreen )\n" +
        "if /I \"[CUSTOM1]\"==\"NOFSX\" (SET FSMODE=DisableTrueFullScreen )\n" +
        "if NOT \"[ALTEXE]\"==\"\" (SET VPXEXE=[ALTEXE] )\n" +
        "\n" +
        "START /min \"\" %VPXEXE% -%FSMODE% -primary -LessCPUthreads -minimized -play \"[GAMEFULLNAME]\"\n" +
        "\n" +
        "if %FSMODE%==DisableTrueFullScreen (START \"\" \"[STARTDIR]Launch\\PopperKeepFocus.exe\" \"Visual Pinball Player\" 10)\n"));
    emu.setExitScript(createScript(false, "\"[STARTDIR]LAUNCH\\PUPCLOSER.EXE\" WINTIT \"Visual Pinball\" 4 1\n"));

    MameService mameService = getMameService();
    File romsFolder = mameService.getRomsFolder();
    if (romsFolder.exists()) {
      emu.setRomDirectory(romsFolder.getAbsolutePath());
    }

    setMediaDir(emulatorType, emu);
    validation.setGameEmulator(emu);
  }

  private void validateVPX9(EmulatorType emulatorType, EmulatorValidation validation) {
    GameEmulatorRepresentation emu = new GameEmulatorRepresentation();
    emu.setType(emulatorType);
    emu.setEnabled(true);
    emu.setGameExt("vpt");
    emu.setDescription("Visual Pinball 9");
    emu.setInstallationDirectory("C:\\vPinball\\VisualPinball");
    emu.setGamesDirectory("C:\\vPinball\\VisualPinball");

    File vpxExe = getSystemService().resolveVpx64Exe();
    if (vpxExe.exists()) {
      emu.setInstallationDirectory(vpxExe.getParentFile().getAbsolutePath());
      emu.setGamesDirectory(new File(vpxExe.getParentFile(), "Tables").getAbsolutePath());
    }

    emu.setLaunchScript(createScript(true, "START \"\" \"[STARTDIR]Launch\\VPXSTARTER.exe\" 10 10 60\n" +
        "cd /d \"[DIREMU]\"\n" +
        "if \"[ALTEXE]\" == \"\" (\n" +
        "      START /min \"\" vpinball995.exe \"[DIREMU]\" -play \"[GAMEFULLNAME]\"\n" +
        ") else  (\n" +
        "      START /min \"\" [ALTEXE].exe \"[DIREMU]\" -play \"[GAMEFULLNAME]\"\n" +
        ")"));
    emu.setExitScript(createScript(false, "\"[STARTDIR]LAUNCH\\PUPCLOSER.EXE\" WINTIT \"Visual Pinball\" 10 1"));

    MameService mameService = getMameService();
    File romsFolder = mameService.getRomsFolder();
    if (romsFolder.exists()) {
      emu.setRomDirectory(romsFolder.getAbsolutePath());
    }

    setMediaDir(emulatorType, emu);
    validation.setGameEmulator(emu);
  }

  private void validateMAME(EmulatorType emulatorType, EmulatorValidation validation) {
    GameEmulatorRepresentation emu = new GameEmulatorRepresentation();
    emu.setType(emulatorType);
    emu.setEnabled(true);
    emu.setGameExt("zip");
    emu.setDescription("MAME Emulator");
    emu.setInstallationDirectory("C:\\MAME");
    emu.setGamesDirectory("C:\\MAME\\roms");
    emu.setLaunchScript(createScript(true, "START \"\" \"[STARTDIR]Launch\\VPXSTARTER.exe\" 2 2 10 \"Multiple Arcade Machine Emulator\"\n" +
        "\n" +
        "C:\n" +
        "\n" +
        "cd \"C:\\MAME\"\n" +
        "\n" +
        "START \"\" \"mamelayplus.exe\" \"[GAMEFULLNAME]\"\n"));
    emu.setExitScript(createScript(false, "\"[STARTDIR]LAUNCH\\PUPCLOSER.EXE\" PROC \"mamelayplus\" 3 1\n"));

    setMediaDir(emulatorType, emu);
    validation.setGameEmulator(emu);
  }

  private void validateFuturePinball(EmulatorType emulatorType, EmulatorValidation validation) {
    GameEmulatorRepresentation emu = new GameEmulatorRepresentation();
    emu.setType(emulatorType);
    emu.setGameExt("fpt");
    emu.setEnabled(true);
    emu.setInstallationDirectory("C:\\vPinball\\FuturePinball");
    emu.setGamesDirectory("C:\\vPinball\\FuturePinall");

    File fpExe = getSystemService().resolveFpExe();
    if (fpExe.exists()) {
      emu.setInstallationDirectory(fpExe.getParentFile().getAbsolutePath());
      emu.setGamesDirectory(new File(fpExe.getParentFile(), "Tables").getAbsolutePath());
    }

    String dmdDeviceIni = "C:\\vPinball\\VisualPinball\\VPinMAME\\DmdDevice.ini";
    String mamePath = "C:\\vPinball\\VisualPinball\\VPinMAME";

    MameService mameService = getMameService();
    File dmdDeviceIniFile = mameService.getDmdDeviceIni();
    if (dmdDeviceIniFile.exists()) {
      dmdDeviceIni = dmdDeviceIniFile.getAbsolutePath();
      mamePath = dmdDeviceIniFile.getParentFile().getAbsolutePath();
    }


    emu.setLaunchScript(createScript(true, "Rem :Enable FP Backglass and Arcade Mode\n" +
        "\n" +
        "REG ADD \"HKCU\\Software\\Future Pinball\\GamePlayer\" /v \"ArcadeMode\" /t REG_DWORD /d 1 /f\n" +
        "REG ADD \"HKCU\\Software\\Future Pinball\\GamePlayer\" /v \"SecondMonitorEnable\" /t REG_DWORD /d 1 /f\n" +
        "\n" +
        "Rem :Disable FP Backglass and Arcade Mode if \"Custom Launch Param\" = PinEvent\n" +
        "\n" +
        "if \"[custom1]\"==\"PinEvent\" (REG ADD \"HKCU\\Software\\Future Pinball\\GamePlayer\" /v \"ArcadeMode\" /t REG_DWORD /d 0 /f)\n" +
        "if \"[custom1]\"==\"PinEvent\" (REG ADD \"HKCU\\Software\\Future Pinball\\GamePlayer\" /v \"SecondMonitorEnable\" /t REG_DWORD /d 0 /f)\n" +
        "\n" +
        "Rem :If you use DOFLinx, Remove>rem<from the next 2 Lines) and Add **PinEvent** to **Custom Launch Param** in Game Manager\n" +
        "rem if \"[custom1]\"==\"PinEvent\" (cd /d \"C:\\directoutput\")\n" +
        "rem if \"[custom1]\"==\"PinEvent\" (DOFLinxMsg \"PROCESSES=\")\n" +
        "\n" +
        "Rem :Run DMDExt only if \"Custom Var #2\" DOES NOT = NO_DMDExt\n" +
        "\n" +
        "if NOT \"[custom2]\"==\"NO_DMDExt\" (c:)\n" +
        "if NOT \"[custom2]\"==\"NO_DMDExt\" (cd \"" + mamePath + "\")\n" +
        "if NOT \"[custom2]\"==\"NO_DMDExt\" (start /min \"\" \"dmdext.exe\" mirror --source=futurepinball -q --virtual-stay-on-top --fps 60 -g \"[GAMENAME]\" --use-ini=\"" + dmdDeviceIni + "\")\n" +
        "if NOT \"[custom2]\"==\"NO_DMDExt\" (timeout /t 1)\n" +
        "\n" +
        "Rem :Launch Future Pinball\n" +
        "\n" +
        "START \"\" \"[STARTDIR]Launch\\VPXSTARTER.exe\" 10 5 60 \"BSP Software*\"\n" +
        "START \"\" \"[DIREMU]\\BAM\\FPLoader.exe\" /open \"[GAMEFULLNAME]\" /play /exit /arcaderender /STAYINRAM\n"));
    emu.setExitScript(createScript(false, "\"[STARTDIR]LAUNCH\\PUPCLOSER.EXE\" WINTIT \"Future Pinball\" 2 1\n" +
        "\n" +
        "REG ADD \"HKCU\\Software\\Future Pinball\\GamePlayer\" /v \"ArcadeMode\" /t REG_DWORD /d 1 /f\n" +
        "REG ADD \"HKCU\\Software\\Future Pinball\\GamePlayer\" /v \"SecondMonitorEnable\" /t REG_DWORD /d 1 /f\n" +
        "taskkill /f /im \"dmdext.exe\"\n" +
        "\n" +
        "Rem :If you use DOFLinx, Remove>rem<from the next 3 Lines (Have the PROCESSES= Match the one in your DOFLinx.INI But leave \"\n" +
        "\n" +
        "rem timeout /t 1\n" +
        "rem cd /d \"C:\\directoutput\"\n" +
        "rem DOFLinxMsg \"PROCESSES=\"\n"));

    setMediaDir(emulatorType, emu);
    validation.setGameEmulator(emu);
  }

  private void validatePC(EmulatorType emulatorType, EmulatorValidation validation) {
    GameEmulatorRepresentation emu = new GameEmulatorRepresentation();
    emu.setType(emulatorType);
    emu.setEnabled(true);
    emu.setGameExt("lnk");
    emu.setExitScript(createScript(false, "\"[STARTDIR]LAUNCH\\PUPCLOSER.EXE\" WINTIT \"[CUSTOM1]\" 4 1"));
    emu.setLaunchScript(createScript(true, "START \"\" \"[STARTDIR]Launch\\VPXSTARTER.exe\" 5 1 5 \"timeout\"\n" +
        "cd /d \"[DIRGAME]\"\n" +
        "start \"\" \"[GAMEFULLNAME]\""));

    setMediaDir(emulatorType, emu);
    validation.setGameEmulator(emu);
  }


  private void validateZenFX(EmulatorType emulatorType, EmulatorValidation validation) {
    GameEmulatorRepresentation emu = setupDefaultEmulatorWithGameList(emulatorType, validation);
    if (emu == null) {
      return;
    }

    emu.setGameExt("pxp");
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

    validation.setGameEmulator(emu);
  }

  private void validateZenFX3(EmulatorType emulatorType, EmulatorValidation validation) {
    GameEmulatorRepresentation emu = setupDefaultEmulatorWithGameList(emulatorType, validation);
    if (emu == null) {
      return;
    }

    emu.setGameExt("pxp");
    emu.setExitScript(createScript(false, "\"[STARTDIR]LAUNCH\\PUPCLOSER.EXE\" WINTIT \"Pinball FX3\" 4 1\n"));
    emu.setLaunchScript(createScript(true, "@echo off\n" +
        "\n" +
        "rem remove next two rem to use or setup DMDEXT for FX3\n" +
        "rem cd /d \"%PopperInstDir%\\VisualPinball\\VPinMAME\"\n" +
        "rem start /min \"\" \"dmdext.exe\" mirror --source=pinballfx3 -q -d auto --virtual-hide-grip --virtual-position=x y w -o PinUP\\[GAMENAME]\n" +
        "\n" +
        "rem remove the next two rem lines to setup PUpDMD for FX3\n" +
        "rem cd /d \"%PopperInstDir%\\VisualPinball\\VPinMAME\"\n" +
        "rem start /min \"\" \"PUPDMDControl.exe\" FX3 PINUP\\[GAMENAME]\n" +
        "\n" +
        "REM SET ALTPARAM=-class\n" +
        "REM if \"[ALTMODE]\"==\"arcade\"  (SET ALTPARAM=)\n" +
        "REM If you prefer arcade Change the 1st 2 lines below to look like ones above\n" +
        "REM Default is classic and arcade\n" +
        "\n" +
        "SET ALTPARAM=\n" +
        "if \"[ALTMODE]\"==\"classic\"  (SET ALTPARAM=-class )\n" +
        "if \"[ALTMODE]\"==\"hotseat2\" (SET ALTPARAM=-hotseat_2 )\n" +
        "if \"[ALTMODE]\"==\"hotseat3\" (SET ALTPARAM=-hotseat_3 )\n" +
        "if \"[ALTMODE]\"==\"hotseat4\" (SET ALTPARAM=-hotseat_4 )\n" +
        "\n" +
        "START \"\" \"[STARTDIR]Launch\\VPXSTARTER.exe\" 5 5 20 \"Pinball FX3\"\n" +
        "\n" +
        "START \"\" \"[DIREMU]\\steam.exe\" -applaunch 442120 %ALTPARAM% -table_[GAMENAME]"));

    validation.setGameEmulator(emu);
  }


  private void validateZenM(EmulatorType emulatorType, EmulatorValidation validation) {
    GameEmulatorRepresentation emu = setupDefaultEmulatorWithGameList(emulatorType, validation);
    if (emu == null) {
      return;
    }

    emu.setGameExt("pxp");
    emu.setExitScript(createScript(false, "\"[STARTDIR]LAUNCH\\PUPCLOSER.EXE\" WINTIT \"PinballM\" 4 1"));
    emu.setLaunchScript(createScript(true, "SET ALTPARAM=\n" +
        "if \"[ALTMODE]\"==\"Classic\"  (SET ALTPARAM=Classic)\n" +
        "if \"[ALTMODE]\"==\"Hotseat2\" (SET ALTPARAM=Hotseat_2 )\n" +
        "if \"[ALTMODE]\"==\"Hotseat3\" (SET ALTPARAM=Hotseat_3 )\n" +
        "if \"[ALTMODE]\"==\"Hotseat4\" (SET ALTPARAM=Hotseat_4 )\n" +
        "if \"[ALTMODE]\"==\"Pro\" (SET ALTPARAM=Pro )\n" +
        "if \"[ALTMODE]\"==\"Practice\" (SET ALTPARAM=Practice )\n" +
        "\n" +
        "START \"\" \"[STARTDIR]Launch\\VPXSTARTER.exe\" 5 5 20 \"PinballM\"\n" +
        "cd /d \"[DIREMU]\"\n" +
        "START \"\" \"steam.exe\" -applaunch 2337640 -Table [?ROM?] -GameMode %ALTPARAM%"));

    validation.setGameEmulator(emu);
  }


  private void validateZaccaria(EmulatorType emulatorType, EmulatorValidation validation) {
    GameEmulatorRepresentation emu = setupDefaultEmulatorWithGameList(emulatorType, validation);
    if (emu == null) {
      return;
    }

    emu.setDescription("Zaccaria Pinball");
    emu.setGameExt("");
    emu.setKeepDisplays("0,1,9,10");
    emu.setExitScript(createScript(false, "\"[STARTDIR]LAUNCH\\PUPCLOSER.EXE\" WINTIT \"Zaccaria_Pinball\" 10 5 2"));
    emu.setLaunchScript(createScript(true, "START \"\" \"[STARTDIR]Launch\\VPXSTARTER.exe\" 5 5 5 \"Zaccaria Pinball\"\n" +
        "SET ALTPARAM=_player 1\n" +
        "if \"[ALTMODE]\"==\"zplayers2\" (SET ALTPARAM=_player 2 )\n" +
        "if \"[ALTMODE]\"==\"zplayers3\" (SET ALTPARAM=_player 3 )\n" +
        "if \"[ALTMODE]\"==\"zplayers4\" (SET ALTPARAM=_player 4 )\n" +
        "START \"\" \"[DIREMU]\\steam.exe\" -applaunch 444930 -rotate right -skipmenu \"[GAMENAME]\" -skipmenu%ALTPARAM% -skipmenu_gamemode classic_simulation\n"));

    validation.setGameEmulator(emu);
  }

  @Nullable
  private GameEmulatorRepresentation setupDefaultEmulatorWithGameList(EmulatorType emulatorType, EmulatorValidation validation) {
    File gameFolder = getSteamService().getGameFolder(emulatorType);
    if (gameFolder == null) {
      validation.setErrorTitle("No installation directory found for this emulator type.");
      validation.setErrorText("The automatic setup for this emulator is supported, but the installation directory was not found. Please contact support.");
      return null;
    }

    List<TableDetails> read = PUPGameImporter.read(emulatorType);
    if (read.isEmpty()) {
      validation.setErrorTitle("The PUP game import failed.");
      validation.setErrorText("The automatic setup for this emulator is supported, but the automatic game import failed. Please contact support.");
      return null;
    }

    validation.setGameCount(read.size());

    GameEmulatorRepresentation emu = new GameEmulatorRepresentation();

    setMediaDir(emulatorType, emu);

    emu.setType(emulatorType);
    emu.setEnabled(true);
    emu.setInstallationDirectory(gameFolder.getAbsolutePath());
    emu.setExeName("");
    emu.setSafeName(emulatorType.folderName());
    emu.setDescription(emulatorType.folderName());
    emu.setGamesDirectory("");
    return emu;
  }

  private void setMediaDir(EmulatorType emulatorType, GameEmulatorRepresentation emu) {
    MediaAccessStrategy mediaStrategy = getFrontendService().getFrontendConnector().getMediaAccessStrategy();
    File folder = mediaStrategy.getEmulatorMediaFolder(emulatorType);
    if (folder != null) {
      emu.setMediaDirectory(folder.getAbsolutePath());
    }
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

  private MameService getMameService() {
    return applicationContext.getBean(MameService.class);
  }

  private SystemService getSystemService() {
    return applicationContext.getBean(SystemService.class);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
