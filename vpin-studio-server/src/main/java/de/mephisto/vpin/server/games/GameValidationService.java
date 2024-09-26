package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.altcolor.AltColor;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.games.GameValidationStateFactory;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.restclient.validation.*;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.mame.MameRomAliasService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.restclient.validation.GameValidationCode.*;

/**
 * See ValidationTexts
 */
@Service
public class GameValidationService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(GameValidationService.class);

  private static Map<Integer, VPinScreen> mediaCodeToScreen = new HashMap<>();

  private Frontend frontend;

  static {
    mediaCodeToScreen.put(CODE_NO_AUDIO, VPinScreen.Audio);
    mediaCodeToScreen.put(CODE_NO_AUDIO_LAUNCH, VPinScreen.AudioLaunch);
    mediaCodeToScreen.put(CODE_NO_APRON, VPinScreen.Menu);
    mediaCodeToScreen.put(CODE_NO_INFO, VPinScreen.GameInfo);
    mediaCodeToScreen.put(CODE_NO_HELP, VPinScreen.GameHelp);
    mediaCodeToScreen.put(CODE_NO_TOPPER, VPinScreen.Topper);
    mediaCodeToScreen.put(CODE_NO_BACKGLASS, VPinScreen.BackGlass);
    mediaCodeToScreen.put(CODE_NO_DMD, VPinScreen.DMD);
    mediaCodeToScreen.put(CODE_NO_PLAYFIELD, VPinScreen.PlayField);
    mediaCodeToScreen.put(CODE_NO_LOADING, VPinScreen.Loading);
    mediaCodeToScreen.put(CODE_NO_OTHER2, VPinScreen.Other2);
    mediaCodeToScreen.put(CODE_NO_WHEEL_IMAGE, VPinScreen.Wheel);
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

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private MameRomAliasService mameRomAliasService;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  private ValidationSettings validationSettings;
  private IgnoredValidationSettings ignoredValidationSettings;

  public List<ValidationState> validate(@NonNull Game game, boolean findFirst) {
    List<ValidationState> result = new ArrayList<>();
    boolean isVPX = game.isVpxGame();

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

    if (isVPX && isValidationEnabled(game, CODE_ROM_INVALID)) {
      if (!StringUtils.isEmpty(game.getRom()) && !mameService.isValidRom(game.getRom())) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_ROM_INVALID));
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

    if (isVPX && isValidationEnabled(game, GameValidationCode.CODE_NO_DIRECTB2S_OR_PUPPACK)) {
      if (game.getDirectB2SPath() == null && game.getPupPackPath() == null) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_DIRECTB2S_OR_PUPPACK));
        if (findFirst) {
          return result;
        }
      }

      if (game.getDirectB2SPath() == null && game.getPupPack() != null && pupPacksService.isPupPackDisabled(game)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_DIRECTB2S_AND_PUPPACK_DISABLED));
        if (findFirst) {
          return result;
        }
      }
    }

    //screen assets are validated for all emulators
    List<ValidationState> screenValidationResult = validateScreenAssets(game, findFirst, result);
    if (screenValidationResult != null) {
      return screenValidationResult;
    }

    if (isVPX && isValidationEnabled(game, CODE_PUP_PACK_FILE_MISSING)) {
      if (game.getPupPackPath() != null && !game.getPupPack().getMissingResources().isEmpty()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_PUP_PACK_FILE_MISSING, game.getPupPack().getMissingResources()));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isVPX && isValidationEnabled(game, CODE_VPS_MAPPING_MISSING)) {
      if (StringUtils.isEmpty(game.getExtTableId()) || StringUtils.isEmpty(game.getExtTableVersionId())) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_VPS_MAPPING_MISSING));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isVPX) {
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

      validationStates = validateForceStereo(game);
      if (!validationStates.isEmpty()) {
        result.add(validationStates.get(0));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isVPX && isValidationEnabled(game, CODE_SCRIPT_CONTROLLER_STOP_MISSING)) {
      HighscoreType highscoreType = game.getHighscoreType();
      if (highscoreType == null || highscoreType.equals(HighscoreType.NVRam)) {
        File romFile = game.getRomFile();
        if (romFile != null && romFile.exists()) {
          if (game.isFoundTableExit() && !game.isFoundControllerStop()) {
            result.add(GameValidationStateFactory.create(GameValidationCode.CODE_SCRIPT_CONTROLLER_STOP_MISSING));
            if (findFirst) {
              return result;
            }
          }
        }
      }
    }

    if (isVPX && isValidationEnabled(game, CODE_NVOFFSET_MISMATCH)) {
      if (game.getNvOffset() > 0 && !StringUtils.isEmpty(game.getRom())) {
        List<GameDetails> otherGameDetailsWithSameRom = new ArrayList<>(gameDetailsRepository.findByRomName(game.getRom())).stream().filter(g -> g.getRomName() != null && g.getPupId() != game.getId() && g.getRomName().equalsIgnoreCase(game.getRom())).collect(Collectors.toList());
        for (GameDetails otherGameDetails : otherGameDetailsWithSameRom) {
          if (otherGameDetails.getNvOffset() == 0 || otherGameDetails.getNvOffset() == game.getNvOffset()) {
            Game otherGame = frontendService.getGame(otherGameDetails.getPupId());
            if (otherGame != null) {
              result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NVOFFSET_MISMATCH, otherGame.getGameDisplayName(), String.valueOf(game.getNvOffset()), String.valueOf(otherGameDetails.getNvOffset())));
              if (findFirst) {
                return result;
              }
            }
          }
        }
      }
    }
    return result;
  }

  private @Nullable List<ValidationState> validateScreenAssets(@NotNull Game game, boolean findFirst, List<ValidationState> result) {
    if (isValidationEnabled(game, CODE_NO_AUDIO)) {
      if (!validScreenAssets(game, VPinScreen.Audio)) {
        result.add(GameValidationStateFactory.create(CODE_NO_AUDIO));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, CODE_NO_AUDIO_LAUNCH)) {
      if (!validScreenAssets(game, VPinScreen.AudioLaunch)) {
        result.add(GameValidationStateFactory.create(CODE_NO_AUDIO_LAUNCH));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, CODE_NO_APRON)) {
      if (!validScreenAssets(game, VPinScreen.Menu)) {
        result.add(GameValidationStateFactory.create(CODE_NO_APRON));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_INFO)) {
      if (!validScreenAssets(game, VPinScreen.GameInfo)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_INFO));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_HELP)) {
      if (!validScreenAssets(game, VPinScreen.GameHelp)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_HELP));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_TOPPER)) {
      if (!validScreenAssets(game, VPinScreen.Topper)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_TOPPER));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_BACKGLASS)) {
      if (!validScreenAssets(game, VPinScreen.BackGlass)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_BACKGLASS));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_DMD)) {
      if (!validScreenAssets(game, VPinScreen.DMD)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_DMD));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_PLAYFIELD)) {
      if (!validScreenAssets(game, VPinScreen.PlayField)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_PLAYFIELD));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_LOADING)) {
      if (!validScreenAssets(game, VPinScreen.PlayField)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_LOADING));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_OTHER2)) {
      if (!validScreenAssets(game, VPinScreen.Other2)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_OTHER2));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_WHEEL_IMAGE)) {
      if (!validScreenAssets(game, VPinScreen.Wheel)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_WHEEL_IMAGE));
        if (findFirst) {
          return result;
        }
      }
    }
    return null;
  }

  private boolean validScreenAssets(Game game, VPinScreen screen) {
    List<File> screenAssets = game.getMediaFiles(screen);
    ValidationProfile defaultProfile = validationSettings.getDefaultProfile();
    ValidationConfig config = defaultProfile.getOrCreateConfig(screen.getValidationCode());
    if (!screenAssets.isEmpty()) {
      if (config.getOption().equals(ValidatorOption.empty)) {
        return false;
      }

      for (File file : screenAssets) {
        String mimeType = MimeTypeUtil.determineMimeType(file);
        if (mimeType != null) {
          if (mimeType.contains("audio") && !config.getMedia().equals(ValidatorMedia.audio)) {
            return false;
          }
          if (mimeType.contains("video") && (!config.getMedia().equals(ValidatorMedia.video) && !config.getMedia().equals(ValidatorMedia.imageOrVideo))) {
            return false;
          }
          if (mimeType.contains("image") && (!config.getMedia().equals(ValidatorMedia.image) && !config.getMedia().equals(ValidatorMedia.imageOrVideo))) {
            return false;
          }
        }
      }
    }
    else {
      if (config.getOption().equals(ValidatorOption.mandatory)) {
        return false;
      }
    }
    return true;
  }

  private List<ValidationState> validateForceStereo(Game game) {
    List<ValidationState> result = new ArrayList<>();

    if (isValidationEnabled(game, CODE_FORCE_STEREO) && !StringUtils.isEmpty(game.getRom())) {
      MameOptions gameOptions = mameService.getOptions(game.getRom());
      MameOptions options = mameService.getOptions(MameOptions.DEFAULT_KEY);

      if (gameOptions.isExistInRegistry()) {
        //no in registry, so check against defaults
        if (!gameOptions.isForceStereo()) {
          result.add(GameValidationStateFactory.create(CODE_FORCE_STEREO));
        }
      }
      else {
        //no in registry, so check against defaults
        if (!options.isForceStereo()) {
          result.add(GameValidationStateFactory.create(CODE_FORCE_STEREO));
        }
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
//          if (altColor.contains("pin2dmd.pal") && !altColor.contains("pin2dmd.vni")) {
//            result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_FILES_MISSING, "pin2dmd.vni"));
//          }
          if (!altColor.contains("pin2dmd.pal") && altColor.contains("pin2dmd.vni")) {
            result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_FILES_MISSING, "pin2dmd.pal"));
          }
        }
        break;
      }
      case serum: {
        String name = game.getRom() + "." + UploaderAnalysis.SERUM_SUFFIX;
        if (isValidationEnabled(game, CODE_ALT_COLOR_FILES_MISSING) && !altColor.contains(name)) {
          result.add(GameValidationStateFactory.create(CODE_ALT_COLOR_FILES_MISSING, name));
        }
        break;
      }
      default: {
        //ignore
      }
    }

    if (!StringUtils.isEmpty(game.getRom())) {
      MameOptions gameOptions = mameService.getOptions(game.getRom());
      if (gameOptions.isExistInRegistry()) {
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
    }
    return result;
  }

  public List<ValidationState> validateAltSound(Game game) {
    List<ValidationState> result = new ArrayList<>();
    if (isValidationEnabled(game, CODE_ALT_SOUND_NOT_ENABLED)) {
      if (game.isAltSoundAvailable() && altSoundService.getAltSoundMode(game) <= 0) {
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
      if (game.getPupPack() != null && !game.getPupPack().getMissingResources().isEmpty()) {
        ValidationState validationState = GameValidationStateFactory.create(CODE_PUP_PACK_FILE_MISSING, game.getPupPack().getMissingResources());
        result.add(validationState);
      }
    }

    if (game.getDirectB2SPath() == null && game.getPupPack() != null && pupPacksService.isPupPackDisabled(game)) {
      ValidationState validationState = GameValidationStateFactory.create(CODE_NO_DIRECTB2S_AND_PUPPACK_DISABLED);
      result.add(validationState);
    }
    return result;
  }

  private boolean isValidationEnabled(@NonNull Game game, int code) {
    if (frontend.getIgnoredValidations().contains(code)) {
      return false;
    }
    if (ignoredValidationSettings.isIgnored(String.valueOf(code))) {
      return false;
    }

    List<Integer> ignoredValidations = game.getIgnoredValidations();
    if (ignoredValidations == null) {
      return false;
    }

    if (ignoredValidations.contains(code)) {
      return false;
    }

    return true;
  }

  public boolean hasMissingAssets(List<ValidationState> states) {
    List<Integer> codes = states.stream().map(s -> s.getCode()).collect(Collectors.toList());
    if (codes.isEmpty()) {
      return false;
    }

    if (codes.contains(CODE_NO_AUDIO)
        || codes.contains(CODE_NO_AUDIO_LAUNCH)
        || codes.contains(CODE_NO_APRON)
        || codes.contains(CODE_NO_INFO)
        || codes.contains(CODE_NO_HELP)
        || codes.contains(CODE_NO_TOPPER)
        || codes.contains(CODE_NO_BACKGLASS)
        || codes.contains(CODE_NO_DMD)
        || codes.contains(CODE_NO_PLAYFIELD)
        || codes.contains(CODE_NO_LOADING)
        || codes.contains(CODE_NO_OTHER2)
        || codes.contains(CODE_NO_WHEEL_IMAGE)) {
      return true;
    }
    return false;
  }

  public boolean hasOtherIssues(List<ValidationState> states) {
    List<Integer> codes = states.stream().map(s -> s.getCode()).collect(Collectors.toList());
    if (codes.isEmpty()) {
      return false;
    }

    if (codes.contains(CODE_NO_DIRECTB2S_OR_PUPPACK)
        || codes.contains(CODE_NO_DIRECTB2S_AND_PUPPACK_DISABLED)
        || codes.contains(CODE_NO_ROM)
        || codes.contains(CODE_ROM_NOT_EXISTS)
        || codes.contains(CODE_VPX_NOT_EXISTS)
        || codes.contains(CODE_ALT_SOUND_NOT_ENABLED)
        || codes.contains(CODE_ALT_SOUND_FILE_MISSING)
        || codes.contains(CODE_FORCE_STEREO)
        || codes.contains(CODE_PUP_PACK_FILE_MISSING)
        || codes.contains(CODE_ALT_COLOR_COLORIZE_DMD_ENABLED)
        || codes.contains(CODE_ALT_COLOR_EXTERNAL_DMD_NOT_ENABLED)
        || codes.contains(CODE_ALT_COLOR_FILES_MISSING)
        || codes.contains(CODE_ALT_COLOR_DMDDEVICE_FILES_MISSING)
    ) {
      return true;
    }
    return false;
  }

  public GameScoreValidation validateHighscoreStatus(Game game, GameDetails gameDetails, TableDetails tableDetails) {
    GameScoreValidation validation = new GameScoreValidation();
    validation.setValidScoreConfiguration(true);

    boolean played = tableDetails == null || tableDetails.getNumberPlays() != null && tableDetails.getNumberPlays() > 0;
    ScoringDB scoringDB = systemService.getScoringDatabase();
    List<String> vpRegEntries = highscoreService.getVPRegEntries();
    List<String> highscoreFiles = highscoreService.getHighscoreFiles();

    String rom = StringUtils.defaultIfEmpty(tableDetails != null ? tableDetails.getRomName() : null, gameDetails.getRomName());

    String originalRom = mameRomAliasService.getRomForAlias(game.getEmulator(), rom);
    boolean aliasedRom = false;
    if (!StringUtils.isEmpty(originalRom)) {
      aliasedRom = true;
      rom = originalRom;
    }

    String tableName = StringUtils.defaultIfEmpty(tableDetails != null ? tableDetails.getRomAlt() : null, gameDetails.getTableName());
    String hsName = StringUtils.defaultIfEmpty(tableDetails != null ? tableDetails.getHsFilename() : null, gameDetails.getHsFileName());

    //the highscore file was found
    if (!StringUtils.isEmpty(hsName) && highscoreFiles.contains(hsName)) {
      validation.setHighscoreFilenameIcon(GameScoreValidation.OK_ICON);
      validation.setHighscoreFilenameIconColor(GameScoreValidation.OK_COLOR);
      validation.setHighscoreFilenameStatus(GameScoreValidation.STATUS_HSFILE_MATCH_FOUND);
      return validation;
    }

    //aliased ROM was found as nvram file
    if (aliasedRom && (scoringDB.getSupportedNvRams().contains(rom) || scoringDB.getSupportedNvRams().contains(rom.toLowerCase()) || scoringDB.getSupportedNvRams().contains(tableName))) {
      validation.setRomIcon(GameScoreValidation.OK_ICON);
      validation.setRomIconColor(GameScoreValidation.OK_COLOR);
      validation.setRomStatus(GameScoreValidation.STATUS_ROM_ALIASED_MATCH_FOUND);
      return validation;
    }

    //the ROM was found as nvram file
    if (scoringDB.getSupportedNvRams().contains(String.valueOf(rom)) || scoringDB.getSupportedNvRams().contains(String.valueOf(rom).toLowerCase()) || scoringDB.getSupportedNvRams().contains(tableName)) {
      validation.setRomIcon(GameScoreValidation.OK_ICON);
      validation.setRomIconColor(GameScoreValidation.OK_COLOR);
      validation.setRomStatus(GameScoreValidation.STATUS_ROM_MATCH_FOUND);
      return validation;
    }

    //the ROM was found as VPReg.stg entry
    if (vpRegEntries.contains(String.valueOf(rom)) || vpRegEntries.contains(tableName)) {
      validation.setRomIcon(GameScoreValidation.OK_ICON);
      validation.setRomIconColor(GameScoreValidation.OK_COLOR);
      validation.setRomStatus(GameScoreValidation.STATUS_VPREG_STG_MATCH_FOUND);
      return validation;
    }

    //not played and no highscore file found
    if (!played && !StringUtils.isEmpty(hsName) && !highscoreFiles.contains(hsName)) {
      validation.setHighscoreFilenameIcon(GameScoreValidation.UNPLAYED_ICON);
      validation.setHighscoreFilenameIconColor(GameScoreValidation.OK_COLOR);
      validation.setHighscoreFilenameStatus(GameScoreValidation.STATUS_NOT_PLAYED_HSFILE_NOT_FOUND);
      return validation;
    }

    //not played and the ROM VPReg.stg entry not found
    if (!played && !vpRegEntries.contains(String.valueOf(rom)) && !vpRegEntries.contains(rom) && !game.getNvRamFile().exists()) {
      validation.setRomIcon(GameScoreValidation.UNPLAYED_ICON);
      validation.setRomIconColor(GameScoreValidation.OK_COLOR);
      validation.setRomStatus(GameScoreValidation.STATUS_NOT_PLAYED_NO_MATCH_FOUND);
      return validation;
    }

    //no fields are set
    if (StringUtils.isEmpty(rom) && StringUtils.isEmpty(tableName) && StringUtils.isEmpty(hsName)) {
      validation.setValidScoreConfiguration(false);
      validation.setRomIcon(GameScoreValidation.ERROR_ICON);
      validation.setRomIconColor(GameScoreValidation.ERROR_COLOR);
      validation.setRomStatus(GameScoreValidation.STATUS_FIELDS_NOT_SET);
      return validation;
    }

    //ROM is not supported
    if (!StringUtils.isEmpty(rom) && (scoringDB.getNotSupported().contains(rom) || (!scoringDB.getSupportedNvRams().contains(rom)) && !scoringDB.getSupportedNvRams().contains(rom.toLowerCase()))) {
      validation.setValidScoreConfiguration(false);
      validation.setRomIcon(GameScoreValidation.ERROR_ICON);
      validation.setRomIconColor(GameScoreValidation.ERROR_COLOR);
      validation.setRomStatus(GameScoreValidation.STATUS_ROM_NOT_SUPPORTED);
      return validation;
    }

    //Highscore file is not supported
    if (!StringUtils.isEmpty(hsName) && scoringDB.getNotSupported().contains(hsName)) {
      validation.setValidScoreConfiguration(false);
      validation.setRomIcon(GameScoreValidation.ERROR_ICON);
      validation.setRomIconColor(GameScoreValidation.ERROR_COLOR);
      validation.setRomStatus(GameScoreValidation.STATUS_HSFILE_NOT_SUPPORTED);
      return validation;
    }

    //game has been played, but the text file has not been generated
    if (played && !StringUtils.isEmpty(hsName) && !highscoreFiles.contains(hsName)) {
      validation.setValidScoreConfiguration(false);
      validation.setRomIcon(GameScoreValidation.ERROR_ICON);
      validation.setRomIconColor(GameScoreValidation.ERROR_COLOR);
      validation.setRomStatus(GameScoreValidation.STATUS_PLAYED_HSFILE_NOT_FOUND);
      return validation;
    }

    //game has been played, but the .nvram or VPReg has not been found
    if (played && !StringUtils.isEmpty(rom) && !vpRegEntries.contains(rom) && !vpRegEntries.contains(tableName) && !game.getNvRamFile().exists()) {
      validation.setValidScoreConfiguration(false);
      validation.setRomIcon(GameScoreValidation.ERROR_ICON);
      validation.setRomIconColor(GameScoreValidation.ERROR_COLOR);
      validation.setRomStatus(GameScoreValidation.STATUS_PLAYED_NO_MATCH_FOUND);
      return validation;
    }

    return validation;
  }

  @Override
  public void afterPropertiesSet() {
    preferencesService.addChangeListener(this);
    frontend = frontendService.getFrontend();
    this.preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);
    this.preferenceChanged(PreferenceNames.VALIDATION_SETTINGS, null, null);
    this.preferenceChanged(PreferenceNames.IGNORED_VALIDATIONS, null, null);
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (propertyName.equals(PreferenceNames.IGNORED_VALIDATIONS)) {
      ignoredValidationSettings = preferencesService.getJsonPreference(PreferenceNames.IGNORED_VALIDATIONS, IgnoredValidationSettings.class);
    }
    if (propertyName.equals(PreferenceNames.VALIDATION_SETTINGS)) {
      validationSettings = preferencesService.getJsonPreference(PreferenceNames.VALIDATION_SETTINGS, ValidationSettings.class);
    }
  }
}
