package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.AltColorArchiveAnalyzer;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.altcolor.AltColor;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.games.GameValidationStateFactory;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.restclient.validation.*;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.mame.MameRomAliasService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.Preferences;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.MimeTypeUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
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

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private SystemService systemService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private MameRomAliasService mameRomAliasService;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  private Preferences preferences;
  private ServerSettings serverSettings;
  private ValidationSettings validationSettings;

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

    if (isVPX && isValidationEnabled(game, GameValidationCode.CODE_ROM_NOT_EXISTS)) {
      if (!game.isRomExists() && game.isRomRequired()) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_ROM_NOT_EXISTS));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isVPX && isValidationEnabled(game, CODE_NOT_ALL_WITH_NVOFFSET)) {
      if (game.getNvOffset() > 0 && !StringUtils.isEmpty(game.getRom())) {
        List<Game> otherGamesWithSameRom = pinUPConnector.getGames().stream().filter(g -> g.getRom() != null && g.getId() != game.getId() && g.getRom().equalsIgnoreCase(game.getRom())).collect(Collectors.toList());
        for (Game rawGame : otherGamesWithSameRom) {
          GameDetails byPupId = gameDetailsRepository.findByPupId(rawGame.getId());
          if (byPupId.getNvOffset() == 0) {
            result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NOT_ALL_WITH_NVOFFSET));
            if (findFirst) {
              return result;
            }
          }
        }
      }
    }

    if (isVPX && isValidationEnabled(game, GameValidationCode.CODE_NO_DIRECTB2S_OR_PUPPACK)) {
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
      if (!validScreenAssets(game, PopperScreen.Audio)) {
        result.add(GameValidationStateFactory.create(CODE_NO_AUDIO));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, CODE_NO_AUDIO_LAUNCH)) {
      if (!validScreenAssets(game, PopperScreen.AudioLaunch)) {
        result.add(GameValidationStateFactory.create(CODE_NO_AUDIO_LAUNCH));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, CODE_NO_APRON)) {
      if (!validScreenAssets(game, PopperScreen.Menu)) {
        result.add(GameValidationStateFactory.create(CODE_NO_APRON));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_INFO)) {
      if (!validScreenAssets(game, PopperScreen.GameInfo)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_INFO));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_HELP)) {
      if (!validScreenAssets(game, PopperScreen.GameHelp)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_HELP));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_TOPPER)) {
      if (!validScreenAssets(game, PopperScreen.Topper)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_TOPPER));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_BACKGLASS)) {
      if (!validScreenAssets(game, PopperScreen.BackGlass)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_BACKGLASS));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_DMD)) {
      if (!validScreenAssets(game, PopperScreen.DMD)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_DMD));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_PLAYFIELD)) {
      if (!validScreenAssets(game, PopperScreen.PlayField)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_PLAYFIELD));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_LOADING)) {
      if (!validScreenAssets(game, PopperScreen.PlayField)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_LOADING));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_OTHER2)) {
      if (!validScreenAssets(game, PopperScreen.Other2)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_OTHER2));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(game, GameValidationCode.CODE_NO_WHEEL_IMAGE)) {
      if (!validScreenAssets(game, PopperScreen.Wheel)) {
        result.add(GameValidationStateFactory.create(GameValidationCode.CODE_NO_WHEEL_IMAGE));
        if (findFirst) {
          return result;
        }
      }
    }


    if (isVPX && isValidationEnabled(game, CODE_PUP_PACK_FILE_MISSING)) {
      if (game.isPupPackAvailable() && !game.getPupPack().getMissingResources().isEmpty()) {
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

    return result;
  }

  private boolean validScreenAssets(Game game, PopperScreen popperScreen) {
    List<File> screenAssets = game.getPinUPMedia(popperScreen);
    ValidationProfile defaultProfile = validationSettings.getDefaultProfile();
    ValidationConfig config = defaultProfile.getOrCreateConfig(popperScreen.getValidationCode());
    if (!screenAssets.isEmpty()) {
      if (config.getOption().equals(ValidatorOption.empty)) {
        return false;
      }

      for (File file : screenAssets) {
        String mimeType = MimeTypeUtil.determineMimeType(file);
        if(mimeType != null) {
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

  public boolean hasNoVpsTableMapping(List<ValidationState> states) {
    List<Integer> codes = states.stream().map(s -> s.getCode()).collect(Collectors.toList());
    if (codes.contains(CODE_VPS_MAPPING_MISSING)) {
      return true;
    }
    return false;
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

    boolean played = tableDetails.getNumberPlays() != null && tableDetails.getNumberPlays() > 0;
    ScoringDB scoringDB = systemService.getScoringDatabase();
    List<String> vpRegEntries = highscoreService.getVPRegEntries();
    List<String> highscoreFiles = highscoreService.getHighscoreFiles();

    String rom = TableDataUtil.getEffectiveRom(tableDetails, gameDetails);
    String originalRom = mameRomAliasService.getRomForAlias(game.getEmulator(), rom);
    boolean aliasedRom = false;
    if (!StringUtils.isEmpty(originalRom)) {
      aliasedRom = true;
      rom = originalRom;
    }

    String tableName = TableDataUtil.getEffectiveTableName(tableDetails, gameDetails);
    String hsName = TableDataUtil.getEffectiveHighscoreFilename(tableDetails, gameDetails, serverSettings);

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
    preferences = preferencesService.getPreferences();
    preferencesService.addChangeListener(this);
    this.preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);
    this.preferenceChanged(PreferenceNames.VALIDATION_SETTINGS, null, null);
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (propertyName.equals(PreferenceNames.SERVER_SETTINGS)) {
      serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    }
    if (propertyName.equals(PreferenceNames.VALIDATION_SETTINGS)) {
      validationSettings = preferencesService.getJsonPreference(PreferenceNames.VALIDATION_SETTINGS, ValidationSettings.class);
    }
    preferences = preferencesService.getPreferences();
  }
}
