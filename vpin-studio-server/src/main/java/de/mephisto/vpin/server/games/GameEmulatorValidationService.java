package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.ValidationStateFactory;
import de.mephisto.vpin.restclient.validation.GameEmulatorValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameEmulatorValidationService {

  public List<ValidationState> validate(@NonNull GameEmulator emulator, boolean findFirst) {
    List<ValidationState> result = new ArrayList<>();

    if (StringUtils.isEmpty(emulator.getInstallationDirectory()) || !emulator.getInstallationFolder().exists()) {
      result.add(ValidationStateFactory.create(GameEmulatorValidationCode.CODE_NO_INSTALLATION_DIRECTORY));
      if (findFirst) {
        return result;
      }
    }

    return result;
  }
}
