package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.highscores.HighscoreBackup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HigscoreBackupServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(HigscoreBackupServiceClient.class);

  HigscoreBackupServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<HighscoreBackup> get(String rom) {
    return Arrays.asList(getRestClient().get(API + "highscorebackups/" + rom, HighscoreBackup[].class));
  }

  public boolean delete(String rom, String filename) {
    return getRestClient().delete(API + "highscorebackups/" + rom + "/" + filename);
  }

  public boolean backup(int gameId) throws Exception {
    return getRestClient().put(API + "highscorebackups/backup/" + gameId, new HashMap<>(), Boolean.class);
  }

  public boolean restore(int gameId, String filename) throws Exception {
    return getRestClient().put(API + "highscorebackups/restore/" + gameId + "/" + filename, new HashMap<>(), Boolean.class);
  }
}
