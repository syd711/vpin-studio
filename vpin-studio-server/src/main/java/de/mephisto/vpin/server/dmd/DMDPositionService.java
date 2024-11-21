package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class DMDPositionService {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionService.class);

  @Autowired
  private GameService gameService;

  public DMDInfo getDMDInfo(int gameId) {
//    File iniFile = new File(emulator.getMameFolder(), "DmdDevice.ini");
//    try {
//      if (!iniFile.exists()) {
//        //TODO
//      }
//
//      String defaultEncoding = "UTF-8";
//      FileInputStream in = new FileInputStream(iniFile);
//      BOMInputStream bOMInputStream = new BOMInputStream(in);
//      ByteOrderMark bom = bOMInputStream.getBOM();
//      String charsetName = bom == null ? defaultEncoding : bom.getCharsetName();
//      InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bOMInputStream), charsetName);
//
//      INIConfiguration iniConfiguration = new INIConfiguration();
//      iniConfiguration.setCommentLeadingCharsUsedInInput(";");
//      iniConfiguration.setSeparatorUsedInOutput("=");
//      iniConfiguration.setSeparatorUsedInInput("=");
//
//      try {
//        iniConfiguration.read(reader);
//      }
//      catch (Exception e) {
//        LOG.error("Failed to read: " + iniFile.getAbsolutePath() + ": " + e.getMessage(), e);
//        throw e;
//      }
//      finally {
//        in.close();
//        reader.close();
//      }
//      return null;
//    }
    return null;
  }

  public ResponseEntity<byte[]> getFullDMDBackground(int gameId) throws Exception {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      return RequestUtil.serializeImage(new File("./resources/media-raw/193_background.png"));
    }
    //TODO
//    throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + gameId);
    return RequestUtil.serializeImage(new File("./resources/media-raw/193_background.png"));
  }
}
