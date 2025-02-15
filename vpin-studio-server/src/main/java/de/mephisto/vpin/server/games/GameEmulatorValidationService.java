package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.ValidationStateFactory;
import de.mephisto.vpin.restclient.validation.GameEmulatorValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class GameEmulatorValidationService {

  public List<ValidationState> validate(@NonNull GameEmulator emulator, boolean findFirst) {
    List<ValidationState> result = new ArrayList<>();

    if (emulator.isFpEmulator() || emulator.isVpxEmulator() || emulator.isFxEmulator()) {
      if (StringUtils.isEmpty(emulator.getInstallationDirectory()) || !emulator.getInstallationFolder().exists()) {
        result.add(ValidationStateFactory.create(GameEmulatorValidationCode.CODE_NO_INSTALLATION_DIRECTORY));
        if (findFirst) {
          return result;
        }
      }
    }

    if (StringUtils.isEmpty(emulator.getGameExt())) {
      result.add(ValidationStateFactory.create(GameEmulatorValidationCode.CODE_NO_INSTALLATION_DIRECTORY));
      if (findFirst) {
        return result;
      }
    }

    if (StringUtils.isEmpty(emulator.getGamesDirectory()) || !new File(emulator.getGamesDirectory()).exists()) {
      result.add(ValidationStateFactory.create(GameEmulatorValidationCode.CODE_NO_GAMES_FOLDER));
      if (findFirst) {
        return result;
      }
    }

    if (!StringUtils.isEmpty(emulator.getRomDirectory()) && !new File(emulator.getRomDirectory()).exists()) {
      result.add(ValidationStateFactory.create(GameEmulatorValidationCode.CODE_INVALID_ROMS_FOLDER));
      if (findFirst) {
        return result;
      }
    }


    if (!StringUtils.isEmpty(emulator.getMediaDirectory()) && !new File(emulator.getMediaDirectory()).exists()) {
      result.add(ValidationStateFactory.create(GameEmulatorValidationCode.CODE_INVALID_MEDIA_FOLDER));
      if (findFirst) {
        return result;
      }
    }

    return result;
  }
}
