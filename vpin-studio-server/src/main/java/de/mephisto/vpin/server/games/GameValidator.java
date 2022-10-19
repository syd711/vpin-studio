package de.mephisto.vpin.server.games;

import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.popper.PopperScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class GameValidator {
  public static int CODE_NO_ROM = 1;
  public static int CODE_NO_DIRECTB2S_OR_PUPPACK = 2;
  public static int CODE_NO_WHEEL_ICON = 3;

  @Autowired
  private PinUPConnector pinUPConnector;

  public int validate(@NonNull Game game) {
    if (StringUtils.isEmpty(game.getRom())) {
      return CODE_NO_ROM;
    }

    if (!game.isDirectB2SAvailable() && !game.isPupPackAvailable()) {
      return CODE_NO_DIRECTB2S_OR_PUPPACK;
    }

    File media = game.getEmulator().getPinUPMedia(PopperScreen.Wheel);
    if (media == null || !media.exists()) {
      return CODE_NO_WHEEL_ICON;
    }

    return -1;
  }
}
