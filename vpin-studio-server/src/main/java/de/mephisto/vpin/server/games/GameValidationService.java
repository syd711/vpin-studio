package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.AltColorArchiveAnalyzer;
import de.mephisto.vpin.restclient.altcolor.AltColor;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.preferences.Preferences;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

import static de.mephisto.vpin.restclient.validation.GameValidationCode.*;

/**
 * See ValidationTexts
 */
@Service
public class GameValidationService implements InitializingBean {

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

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private AltColorService altColorService;

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private MameService mameService;

  private Preferences preferences;

  public List<ValidationState> validate(@NonNull Game game, boolean findFirst) {
    List<ValidationState> result = new ArrayList<>();
    boolean isVPX = game.getEmulator().isVpx();

    if (isVPX && isValidationEnabled(game, CODE_VPX_NOT_EXISTS)) {
      if (!game.getGameFile().exists()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_VPX_NOT_EXISTS));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isVPX && isValidationEnabled(game, GameValidationCode.CODE_NO_ROM)) {
      if (StringUtils.isEmpty(game.getRom())) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_ROM));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isVPX && isValidationEnabled(game, GameValidationCode.CODE_ROM_NOT_EXISTS)) {
      if (!game.isRomExists() && game.isRomRequired()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_ROM_NOT_EXISTS));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_DIRECTB2S_OR_PUPPACK)) {
      if (!game.isDirectB2SAvailable() && !game.isPupPackAvailable()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_DIRECTB2S_OR_PUPPACK));
        if (findFirst) {
          return result;
        }
      }

      if (!game.isDirectB2SAvailable() && game.getPupPack() != null && pupPacksService.isPupPackDisabled(game)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_DIRECTB2S_AND_PUPPACK_DISABLED));
        if (findFirst) {
          return result;
        }
      }
    }


    if (isValidationEnabled(game, CODE_NO_AUDIO)) {
      List<File> audio = game.getPinUPMedia(PopperScreen.Audio);
      if (audio.isEmpty()) {
        result.add(GameValidationStateFactory.create(CODE_NO_AUDIO));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, CODE_NO_AUDIO_LAUNCH)) {
      List<File> audioLaunch = game.getPinUPMedia(PopperScreen.AudioLaunch);
      if (audioLaunch.isEmpty()) {
        result.add(GameValidationStateFactory.create(CODE_NO_AUDIO_LAUNCH));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, CODE_NO_APRON)) {
      List<File> apron = game.getPinUPMedia(PopperScreen.Menu);
      if (apron.isEmpty()) {
        result.add(GameValidationStateFactory.create(CODE_NO_APRON));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_INFO)) {
      List<File> info = game.getPinUPMedia(PopperScreen.GameInfo);
      if (info.isEmpty()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_INFO));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_HELP)) {
      List<File> help = game.getPinUPMedia(PopperScreen.GameHelp);
      if (help.isEmpty()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_HELP));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_TOPPER)) {
      List<File> topper = game.getPinUPMedia(PopperScreen.Topper);
      if (topper.isEmpty()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_TOPPER));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_BACKGLASS)) {
      List<File> backglass = game.getPinUPMedia(PopperScreen.BackGlass);
      if (backglass.isEmpty()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_BACKGLASS));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_DMD)) {
      List<File> dmd = game.getPinUPMedia(PopperScreen.DMD);
      if (dmd.isEmpty()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_DMD));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_PLAYFIELD)) {
      List<File> playfield = game.getPinUPMedia(PopperScreen.PlayField);
      if (playfield.isEmpty()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_PLAYFIELD));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_LOADING)) {
      List<File> loading = game.getPinUPMedia(PopperScreen.Loading);
      if (loading.isEmpty()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_LOADING));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_OTHER2)) {
      List<File> other2 = game.getPinUPMedia(PopperScreen.Other2);
      if (other2.isEmpty()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_OTHER2));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_WHEEL_IMAGE)) {
      List<File> wheels = game.getPinUPMedia(PopperScreen.Wheel);
      if (wheels.isEmpty()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_WHEEL_IMAGE));
        if (findFirst) {
          return result;
        }
      }
    }


    if (isValidationEnabled(game, CODE_PUP_PACK_FILE_MISSING)) {
      if (game.isPupPackAvailable() && !game.getPupPack().getMissingResources().isEmpty()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_PUP_PACK_FILE_MISSING, game.getPupPack().getMissingResources()));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, CODE_VPS_MAPPING_MISSING)) {
      if (StringUtils.isEmpty(game.getExtTableId()) || StringUtils.isEmpty(game.getExtTableVersionId())) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_VPS_MAPPING_MISSING));
        if (findFirst) {
          return result;
        }
      }
    }

    List<ValidationState> validationStates = validateAltSound(game);
    if (!validationStates.isEmpty()) {
      result.add(validationStates.get(0));
      if (findFirst) {
        return result;
      }
    }

    validationStates = validateAltColor(game);
    if (!validationStates.isEmpty()) {
      result.add(validationStates.get(0));
      if (findFirst) {
        return result;
      }
    }

    return result;
  }

  public List<ValidationState> validateAltColor(Game game) {
    if (game.getAltColorType() == null || game.getAltColorType().equals(AltColorTypes.mame)) {
      return Collections.emptyList();
    }
    List<ValidationState> result = new ArrayList<>();

    MameOptions options = mameService.getOptions(MameOptions.DEFAULT_KEY);
    MameOptions gameOptions = mameService.getOptions(game.getRom());

    AltColor altColor = altColorService.getAltColor(game);
    AltColorTypes altColorType = altColor.getAltColorType();
    if (altColorType == null) {
      return Collections.emptyList();
    }

    File dmdDevicedll = new File(game.getEmulator().getMameFolder(), "DmdDevice.dll");
    File dmdDevice64dll = new File(game.getEmulator().getMameFolder(), "DmdDevice64.dll");
    File dmdextexe = new File(game.getEmulator().getMameFolder(), "dmdext.exe");
    File dmdDeviceIni = new File(game.getEmulator().getMameFolder(), "DmdDevice.ini");

    if (isValidationEnabled(game, CODE_ALT_COLOR_DMDDEVICE_FILES_MISSING)) {
      if (!dmdDevicedll.exists() && !dmdDevice64dll.exists()) {
        result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_DMDDEVICE_FILES_MISSING, dmdDevicedll.getName()));
      }

      if (!dmdextexe.exists()) {
        result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_DMDDEVICE_FILES_MISSING, dmdextexe.getName()));
      }

      if (!dmdDeviceIni.exists()) {
        result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_DMDDEVICE_FILES_MISSING, dmdDeviceIni.getName()));
      }
    }

    switch (altColorType) {
      case pal: {
        if (isValidationEnabled(game, CODE_ALT_COLOR_FILES_MISSING)) {
          if (altColor.contains("pin2dmd.pal") && !altColor.contains("pin2dmd.vni")) {
            result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_FILES_MISSING, "pin2dmd.vni"));
          }
          else if (!altColor.contains("pin2dmd.pal") && altColor.contains("pin2dmd.vni")) {
            result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_FILES_MISSING, "pin2dmd.pal"));
          }
        }
        break;
      }
      case serum: {
        String name = game.getRom() + AltColorArchiveAnalyzer.SERUM_SUFFIX;
        if (isValidationEnabled(game, CODE_ALT_COLOR_FILES_MISSING) && !altColor.contains(name)) {
          result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_FILES_MISSING, name));
        }
        break;
      }
      default: {
        //ignore
      }
    }

    if (gameOptions.isExistInRegistry()) {
      //no in registry, so check against defaults
      if (isValidationEnabled(game, CODE_ALT_COLOR_COLORIZE_DMD_ENABLED) && !gameOptions.isColorizeDmd()) {
        result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_COLORIZE_DMD_ENABLED));
      }
      if (isValidationEnabled(game, CODE_ALT_COLOR_EXTERNAL_DMD_NOT_ENABLED) && !gameOptions.isUseExternalDmd()) {
        result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_EXTERNAL_DMD_NOT_ENABLED));
      }
    }
    else {
      //no in registry, so check against defaults
      if (isValidationEnabled(game, CODE_ALT_COLOR_COLORIZE_DMD_ENABLED) && !options.isColorizeDmd()) {
        result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_COLORIZE_DMD_ENABLED));
      }
      if (isValidationEnabled(game, CODE_ALT_COLOR_EXTERNAL_DMD_NOT_ENABLED) && !options.isUseExternalDmd()) {
        result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_EXTERNAL_DMD_NOT_ENABLED));
      }
    }
    return result;
  }

  public List<ValidationState> validateAltSound(Game game) {
    List<ValidationState> result = new ArrayList<>();
    if (isValidationEnabled(game, CODE_ALT_SOUND_NOT_ENABLED)) {
      if (game.isAltSoundAvailable() && !altSoundService.isAltSoundEnabled(game)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_ALT_SOUND_NOT_ENABLED));
      }
    }

    if (isValidationEnabled(game, CODE_ALT_SOUND_FILE_MISSING)) {
      if (game.isAltSoundAvailable() && altSoundService.getAltSound(game).isMissingAudioFiles()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_ALT_SOUND_FILE_MISSING));
      }
    }
    return result;
  }

  public List<ValidationState> validatePupPack(Game game) {
    List<ValidationState> result = new ArrayList<>();
    if (isValidationEnabled(game, CODE_PUP_PACK_FILE_MISSING)) {
      if (game.isPupPackAvailable() && !game.getPupPack().getMissingResources().isEmpty()) {
        ValidationState validationState = GameValidationStateFactory.create(CODE_PUP_PACK_FILE_MISSING, game.getPupPack().getMissingResources());
        result.add(validationState);
      }
    }

    if (!game.isDirectB2SAvailable() && game.getPupPack() != null && pupPacksService.isPupPackDisabled(game)) {
      ValidationState validationState = GameValidationStateFactory.create(CODE_NO_DIRECTB2S_AND_PUPPACK_DISABLED);
      result.add(validationState);
    }
    return result;
  }

  private boolean isValidationEnabled(@NonNull Game game, int code) {
    if (mediaCodeToScreen.containsKey(code)) {
      PopperScreen popperScreen = mediaCodeToScreen.get(code);
      if (preferences.getIgnoredMedia() != null && Arrays.asList(preferences.getIgnoredMedia().split(",")).contains(popperScreen.name())) {
        return false;
      }
    }

    List<Integer> ignoredValidations = game.getIgnoredValidations();
    if (ignoredValidations.contains(code)) {
      return false;
    }

    String ignoredPrefValidations = preferences.getIgnoredValidations();
    List<Integer> ignoredIds = ValidationState.toIds(ignoredPrefValidations);

    if (ignoredIds.contains(code)) {
      return false;
    }

    return true;
  }

  @Override
  public void afterPropertiesSet() {
    preferences = preferencesService.getPreferences();
  }
}
