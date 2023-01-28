package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.ValidationCode;
import de.mephisto.vpin.server.popper.Emulator;
import de.mephisto.vpin.server.preferences.Preferences;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.mephisto.vpin.restclient.ValidationCode.*;

/**
 * See ValidationTexts
 */
@Service
public class GameValidator implements InitializingBean {

  private static Map<Integer, PopperScreen> mediaCodeToScreen = new HashMap<>();

  static {
    mediaCodeToScreen.put(CODE_NO_AUDIO, PopperScreen.Audio);
    mediaCodeToScreen.put(CODE_NO_AUDIO_LAUNCH, PopperScreen.AudioLaunch);
    mediaCodeToScreen.put(CODE_NO_APRON, PopperScreen.Menu);
    mediaCodeToScreen.put(CODE_NO_INFO, PopperScreen.GameInfo);
    mediaCodeToScreen.put(CODE_NO_HELP, PopperScreen.GameHelp);
    mediaCodeToScreen.put(CODE_NO_TOPPER, PopperScreen.Topper);
    mediaCodeToScreen.put(CODE_NO_BACKGLASS, PopperScreen.BackGlass);
    mediaCodeToScreen.put(CODE_NO_DMD, PopperScreen.DMD);
    mediaCodeToScreen.put(CODE_NO_PLAYFIELD, PopperScreen.PlayField);
    mediaCodeToScreen.put(CODE_NO_LOADING, PopperScreen.Loading);
    mediaCodeToScreen.put(CODE_NO_OTHER2, PopperScreen.Other2);
    mediaCodeToScreen.put(CODE_NO_WHEEL_IMAGE, PopperScreen.Wheel);
  }

  @Autowired
  private PreferencesService preferencesService;

  private Preferences preferences;

  public int validate(@NonNull Game game) {
    boolean isVPX = game.getEmulator().getName().equals(Emulator.VISUAL_PINBALL_X);
    boolean isFP = game.getEmulator().getName().equals(Emulator.FUTURE_PINBALL);

    if (isVPX && isValidationEnabled(game, CODE_VPX_NOT_EXISTS)) {
      if (!game.getGameFile().exists()) {
        return ValidationCode.CODE_VPX_NOT_EXISTS;
      }
    }

    if (isVPX && isValidationEnabled(game, ValidationCode.CODE_NO_ROM)) {
      if (StringUtils.isEmpty(game.getRom())) {
        return ValidationCode.CODE_NO_ROM;
      }
    }

    if (isVPX && isValidationEnabled(game, ValidationCode.CODE_ROM_NOT_EXISTS)) {
      if (!game.isRomExists()) {
        return ValidationCode.CODE_ROM_NOT_EXISTS;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_DIRECTB2S_OR_PUPPACK)) {
      if (!game.isDirectB2SAvailable() && !game.isPupPackAvailable()) {
        return ValidationCode.CODE_NO_DIRECTB2S_OR_PUPPACK;
      }
    }

    File audio = game.getPinUPMedia(PopperScreen.Audio);
    File audioLaunch = game.getPinUPMedia(PopperScreen.AudioLaunch);
    File apron = game.getPinUPMedia(PopperScreen.Menu);
    File info = game.getPinUPMedia(PopperScreen.GameInfo);
    File help = game.getPinUPMedia(PopperScreen.GameHelp);
    File topper = game.getPinUPMedia(PopperScreen.Topper);
    File backglass = game.getPinUPMedia(PopperScreen.BackGlass);
    File dmd = game.getPinUPMedia(PopperScreen.DMD);
    File playfield = game.getPinUPMedia(PopperScreen.PlayField);
    File loading = game.getPinUPMedia(PopperScreen.Loading);
    File other2 = game.getPinUPMedia(PopperScreen.Other2);
    File wheel = game.getPinUPMedia(PopperScreen.Wheel);

    if (isValidationEnabled(game, CODE_NO_AUDIO)) {
      if (audio == null || !audio.exists()) {
        return CODE_NO_AUDIO;
      }
    }

    if (isValidationEnabled(game, CODE_NO_AUDIO_LAUNCH)) {
      if (audioLaunch == null || !audioLaunch.exists()) {
        return CODE_NO_AUDIO_LAUNCH;
      }
    }

    if (isValidationEnabled(game, CODE_NO_APRON)) {
      if (apron == null || !apron.exists()) {
        return CODE_NO_APRON;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_INFO)) {
      if (info == null || !info.exists()) {
        return ValidationCode.CODE_NO_INFO;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_HELP)) {
      if (help == null || !help.exists()) {
        return ValidationCode.CODE_NO_HELP;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_TOPPER)) {
      if (topper == null || !topper.exists()) {
        return ValidationCode.CODE_NO_TOPPER;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_BACKGLASS)) {
      if (backglass == null || !backglass.exists()) {
        return ValidationCode.CODE_NO_BACKGLASS;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_DMD)) {
      if (dmd == null || !dmd.exists()) {
        return ValidationCode.CODE_NO_DMD;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_PLAYFIELD)) {
      if (playfield == null || !playfield.exists()) {
        return ValidationCode.CODE_NO_PLAYFIELD;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_LOADING)) {
      if (loading == null || !loading.exists()) {
        return ValidationCode.CODE_NO_LOADING;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_OTHER2)) {
      if (other2 == null || !other2.exists()) {
        return ValidationCode.CODE_NO_OTHER2;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_WHEEL_IMAGE)) {
      if (wheel == null || !wheel.exists()) {
        return ValidationCode.CODE_NO_WHEEL_IMAGE;
      }
    }

    return -1;
  }

  private boolean isValidationEnabled(@NonNull Game game, int code) {
    if (mediaCodeToScreen.containsKey(code)) {
      PopperScreen popperScreen = mediaCodeToScreen.get(code);
      if (preferences.getIgnoredMedia() != null && Arrays.asList(preferences.getIgnoredMedia().split(",")).contains(popperScreen.name())) {
        return false;
      }
    }

    String ignoredValidations = game.getIgnoredValidations();
    if (containsIgnoreCode(code, ignoredValidations)) {
      return false;
    }

    String ignoredPrefValidations = preferences.getIgnoredValidations();
    if (containsIgnoreCode(code, ignoredPrefValidations)) {
      return false;
    }

    return true;
  }

  private boolean containsIgnoreCode(int code, String ignoredValidations) {
    if (!StringUtils.isEmpty(ignoredValidations)) {
      String[] split = ignoredValidations.split(",");
      List<String> ignoreList = Arrays.asList(split);
      if (ignoreList.contains(String.valueOf(code))) {
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
