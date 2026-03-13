package de.mephisto.vpin.restclient.highscores;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HigscoreBackupServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public HigscoreBackupServiceClient(VPinStudioClient client) {
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
