package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.ValidationCode;
import de.mephisto.vpin.server.altsound.AltSoundService;
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
    mediaCodeToScreen.put(CODE_NO_APRON, PopperScreen.FullDMD);
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

  @Autowired
  private AltSoundService altSoundService;

  private Preferences preferences;

  public int validate(@NonNull Game game) {
    boolean isVPX = game.getEmulator().getName().equals(Emulator.VISUAL_PINBALL_X) || game.getEmulator().getName().equals(Emulator.VISUAL_PINBALL);
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
      if (!game.isRomExists() && game.isRomRequired()) {
        return ValidationCode.CODE_ROM_NOT_EXISTS;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_DIRECTB2S_OR_PUPPACK)) {
      if (!game.isDirectB2SAvailable() && !game.isPupPackAvailable()) {
        return ValidationCode.CODE_NO_DIRECTB2S_OR_PUPPACK;
      }
    }


    if (isValidationEnabled(game, CODE_NO_AUDIO)) {
      File audio = game.getPinUPMedia(PopperScreen.Audio);
      if (audio == null || !audio.exists()) {
        return CODE_NO_AUDIO;
      }
    }

    if (isValidationEnabled(game, CODE_NO_AUDIO_LAUNCH)) {
      File audioLaunch = game.getPinUPMedia(PopperScreen.AudioLaunch);
      if (audioLaunch == null || !audioLaunch.exists()) {
        return CODE_NO_AUDIO_LAUNCH;
      }
    }

    if (isValidationEnabled(game, CODE_NO_APRON)) {
      File apron = game.getPinUPMedia(PopperScreen.FullDMD);
      if (apron == null || !apron.exists()) {
        return CODE_NO_APRON;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_INFO)) {
      File info = game.getPinUPMedia(PopperScreen.GameInfo);
      if (info == null || !info.exists()) {
        return ValidationCode.CODE_NO_INFO;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_HELP)) {
      File help = game.getPinUPMedia(PopperScreen.GameHelp);
      if (help == null || !help.exists()) {
        return ValidationCode.CODE_NO_HELP;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_TOPPER)) {
      File topper = game.getPinUPMedia(PopperScreen.Topper);
      if (topper == null || !topper.exists()) {
        return ValidationCode.CODE_NO_TOPPER;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_BACKGLASS)) {
      File backglass = game.getPinUPMedia(PopperScreen.BackGlass);
      if (backglass == null || !backglass.exists()) {
        return ValidationCode.CODE_NO_BACKGLASS;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_DMD)) {
      File dmd = game.getPinUPMedia(PopperScreen.DMD);
      if (dmd == null || !dmd.exists()) {
        return ValidationCode.CODE_NO_DMD;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_PLAYFIELD)) {
      File playfield = game.getPinUPMedia(PopperScreen.PlayField);
      if (playfield == null || !playfield.exists()) {
        return ValidationCode.CODE_NO_PLAYFIELD;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_LOADING)) {
      File loading = game.getPinUPMedia(PopperScreen.Loading);
      if (loading == null || !loading.exists()) {
        return ValidationCode.CODE_NO_LOADING;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_OTHER2)) {
      File other2 = game.getPinUPMedia(PopperScreen.Other2);
      if (other2 == null || !other2.exists()) {
        return ValidationCode.CODE_NO_OTHER2;
      }
    }

    if (isValidationEnabled(game, ValidationCode.CODE_NO_WHEEL_IMAGE)) {
      File wheel = game.getPinUPMedia(PopperScreen.Wheel);
      if (wheel == null || !wheel.exists()) {
        return ValidationCode.CODE_NO_WHEEL_IMAGE;
      }
    }

    if (isValidationEnabled(game, CODE_ALT_SOUND_NOT_ENABLED)) {
      if (game.isAltSoundAvailable() && !altSoundService.isAltSoundEnabled(game)) {
        return ValidationCode.CODE_ALT_SOUND_NOT_ENABLED;
      }
    }

    if (isValidationEnabled(game, CODE_ALT_SOUND_FILE_MISSING)) {
      if (game.isAltSoundAvailable() && altSoundService.getAltSound(game).isMissingAudioFiles()) {
        return ValidationCode.CODE_ALT_SOUND_FILE_MISSING;
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
