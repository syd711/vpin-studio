package de.mephisto.vpin.server.games;

import de.mephisto.vpin.server.preferences.Preferences;
import de.mephisto.vpin.server.popper.PopperScreen;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * See ValidationTexts
 */
@Service
public class GameValidator implements InitializingBean {
  public static int CODE_NO_ROM = 1;
  public static int CODE_DUPLICATE_ROM = 2;
  public static int CODE_ROM_NOT_EXISTS = 3;

  public static int CODE_NO_DIRECTB2S_OR_PUPPACK = 20;

  public static int CODE_NO_HIGHSCORE_FILES = 60;

  public static int CODE_NO_AUDIO = 30;
  public static int CODE_NO_AUDIO_LAUNCH = 31;
  public static int CODE_NO_APRON = 32;
  public static int CODE_NO_INFO = 33;
  public static int CODE_NO_HELP = 34;
  public static int CODE_NO_TOPPER = 35;
  public static int CODE_NO_BACKGLASS = 36;
  public static int CODE_NO_DMD = 37;
  public static int CODE_NO_PLAYFIELD = 38;
  public static int CODE_NO_LOADING = 39;
  public static int CODE_NO_OTHER2 = 40;
  public static int CODE_NO_WHEEL_IMAGE = 41;

  @Autowired
  private PreferencesService preferencesService;

  private Preferences preferences;

  public int validate(@NonNull Game game, @NonNull List<Game> games) {
    if(isValidationEnabled(game, CODE_NO_ROM)) {
      if (StringUtils.isEmpty(game.getRom())) {
        return CODE_NO_ROM;
      }
    }

    if(isValidationEnabled(game, CODE_ROM_NOT_EXISTS)) {
      if (!game.isRomExists()) {
        return CODE_ROM_NOT_EXISTS;
      }
    }


    if(isValidationEnabled(game, CODE_DUPLICATE_ROM)) {
      for (Game g : games) {
        if (g.getId() != game.getId() && !StringUtils.isEmpty(g.getRom()) && g.getRom().equals(game.getRom())) {
          return CODE_DUPLICATE_ROM;
        }
      }
    }

    if(isValidationEnabled(game, CODE_NO_DIRECTB2S_OR_PUPPACK)) {
      if (!game.isDirectB2SAvailable() && !game.isPupPackAvailable()) {
        return CODE_NO_DIRECTB2S_OR_PUPPACK;
      }
    }

    if(isValidationEnabled(game, CODE_NO_HIGHSCORE_FILES)) {
      if (!game.hasHighscore()) {
        return CODE_NO_HIGHSCORE_FILES;
      }
    }

    File audio = game.getEmulator().getPinUPMedia(PopperScreen.Audio);
    File audioLaunch = game.getEmulator().getPinUPMedia(PopperScreen.AudioLaunch);
    File apron = game.getEmulator().getPinUPMedia(PopperScreen.Menu);
    File info = game.getEmulator().getPinUPMedia(PopperScreen.GameInfo);
    File help = game.getEmulator().getPinUPMedia(PopperScreen.GameHelp);
    File topper = game.getEmulator().getPinUPMedia(PopperScreen.Topper);
    File backglass = game.getEmulator().getPinUPMedia(PopperScreen.BackGlass);
    File dmd = game.getEmulator().getPinUPMedia(PopperScreen.DMD);
    File playfield = game.getEmulator().getPinUPMedia(PopperScreen.PlayField);
    File loading = game.getEmulator().getPinUPMedia(PopperScreen.Loading);
    File other2 = game.getEmulator().getPinUPMedia(PopperScreen.Other2);
    File wheel = game.getEmulator().getPinUPMedia(PopperScreen.Wheel);

    if(isValidationEnabled(game, CODE_NO_AUDIO)) {
      if(audio == null || !audio.exists()) {
        return CODE_NO_AUDIO;
      }
    }

    if(isValidationEnabled(game, CODE_NO_AUDIO_LAUNCH)) {
      if(audioLaunch == null || !audioLaunch.exists()) {
        return CODE_NO_AUDIO_LAUNCH;
      }
    }

    if(isValidationEnabled(game, CODE_NO_APRON)) {
      if(apron == null || !apron.exists()) {
        return CODE_NO_APRON;
      }
    }

    if(isValidationEnabled(game, CODE_NO_INFO)) {
      if(info == null || !info.exists()) {
        return CODE_NO_INFO;
      }
    }

    if(isValidationEnabled(game, CODE_NO_HELP)) {
      if(help == null || !help.exists()) {
        return CODE_NO_HELP;
      }
    }

    if(isValidationEnabled(game, CODE_NO_TOPPER)) {
      if(topper == null || !topper.exists()) {
        return CODE_NO_TOPPER;
      }
    }

    if(isValidationEnabled(game, CODE_NO_BACKGLASS)) {
      if(backglass == null || !backglass.exists()) {
        return CODE_NO_BACKGLASS;
      }
    }

    if(isValidationEnabled(game, CODE_NO_DMD)) {
      if(dmd == null || !dmd.exists()) {
        return CODE_NO_DMD;
      }
    }

    if(isValidationEnabled(game, CODE_NO_PLAYFIELD)) {
      if(playfield == null || !playfield.exists()) {
        return CODE_NO_PLAYFIELD;
      }
    }

    if(isValidationEnabled(game, CODE_NO_LOADING)) {
      if(loading == null || !loading.exists()) {
        return CODE_NO_LOADING;
      }
    }

    if(isValidationEnabled(game, CODE_NO_OTHER2)) {
      if(other2 == null || !other2.exists()) {
        return CODE_NO_OTHER2;
      }
    }

    if(isValidationEnabled(game, CODE_NO_WHEEL_IMAGE)) {
      if(wheel == null || !wheel.exists()) {
        return CODE_NO_WHEEL_IMAGE;
      }
    }

    return -1;
  }

  private boolean isValidationEnabled(@NonNull Game game, int code) {
    String ignoredValidations = game.getIgnoredValidations();
    if (containsIgnoreCode(code, ignoredValidations)){
      return false;
    }

    String ignoredPrefValidations = preferences.getIgnoredValidations();
    if (containsIgnoreCode(code, ignoredPrefValidations)){
      return false;
    }

    return true;
  }

  private boolean containsIgnoreCode(int code, String ignoredValidations) {
    if(!StringUtils.isEmpty(ignoredValidations)) {
      String[] split = ignoredValidations.split(",");
      List<String> ignoreList = Arrays.asList(split);
      if(ignoreList.contains(String.valueOf(code))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void afterPropertiesSet() {
     preferences = preferencesService.getPreferences();
  }
}
