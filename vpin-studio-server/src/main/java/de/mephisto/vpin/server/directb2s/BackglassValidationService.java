package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.directb2s.DirectB2SDetail;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.ValidationStateFactory;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.validation.*;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static de.mephisto.vpin.restclient.validation.BackglassValidationCode.*;

/**
 * See ValidationTexts
 */
@Service
public class BackglassValidationService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassValidationService.class);

  private Frontend frontend;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private FrontendService frontendService;

  private IgnoredValidationSettings ignoredValidationSettings;


  public List<ValidationState> validate(@NonNull DirectB2SDetail directb2s, @Nullable Game game, 
      @Nullable DirectB2STableSettings tableSettings, DirectB2ServerSettings serverSettings, 
      boolean findFirst) {
        
    List<ValidationState> result = new ArrayList<>();

    GameEmulator emulator = emulatorService.getGameEmulator(directb2s.getEmulatorId());

    if (isValidationEnabled(directb2s, CODE_NO_GAME)) {
      String mainBaseName = FileUtils.baseUniqueFile(directb2s.getFilename());
      String gameFilename = mainBaseName + "." + emulator.getGameExt();
      boolean gameAvailable = new File(emulator.getGamesDirectory(), gameFilename).exists();
      if (!gameAvailable) {
        result.add(ValidationStateFactory.create(CODE_NO_GAME));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(directb2s, CODE_NOT_RUN_AS_EXE)) {
      boolean serverLaunchAsExe = serverSettings != null && serverSettings.getDefaultStartMode() == DirectB2ServerSettings.EXE_START_MODE;
      // when no tableSettings, use standard
      int tableLaunchAsExe = tableSettings != null ? tableSettings.getStartAsEXE() : 2;
      if (tableLaunchAsExe == 0 || tableLaunchAsExe == 2 && !serverLaunchAsExe) {
        result.add(ValidationStateFactory.create(CODE_NOT_RUN_AS_EXE));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(directb2s, CODE_NO_FULLDMD)) {
      if (!directb2s.isDmdImageAvailable()) {
        result.add(ValidationStateFactory.create(CODE_NO_FULLDMD));
        if (findFirst) {
          return result;
        }
      }
    }

    if (isValidationEnabled(directb2s, CODE_WRONG_FULLDMD_RATIO)) {
      if (directb2s.isDmdImageAvailable() && !directb2s.isFullDmd()) {
        result.add(ValidationStateFactory.create(CODE_WRONG_FULLDMD_RATIO));
        if (findFirst) {
          return result;
        }
      }
    }

    return result;
  }

  //---------------------------------------

  private boolean isValidationEnabled(@NonNull DirectB2SDetail directb2s, int code) {
    if (frontend.getIgnoredValidations().contains(code)) {
      return false;
    }
    if (ignoredValidationSettings.isIgnored(String.valueOf(code))) {
      return false;
    }

    //List<Integer> ignoredValidations = directb2s.getIgnoredValidations();
    //if (ignoredValidations != null && ignoredValidations.contains(code) ) {
    //  return false;
    //}

    return true;
  }


  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (propertyName.equals(PreferenceNames.IGNORED_VALIDATION_SETTINGS)) {
      ignoredValidationSettings = preferencesService.getJsonPreference(PreferenceNames.IGNORED_VALIDATION_SETTINGS, IgnoredValidationSettings.class);
    }
  }

  @Override
  public void afterPropertiesSet() {
    preferencesService.addChangeListener(this);
    frontend = frontendService.getFrontend();
    this.preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);
    this.preferenceChanged(PreferenceNames.IGNORED_VALIDATION_SETTINGS, null, null);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
