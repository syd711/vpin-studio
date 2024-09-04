package de.mephisto.vpin.restclient.alx;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;

/*********************************************************************************************************************
 * Alx
 ********************************************************************************************************************/
public class AlxServiceClient extends VPinStudioClientService {
  public AlxServiceClient(VPinStudioClient client) {
    super(client);
  }

  public AlxSummary getAlxSummary() {
    return getRestClient().get(API + "alx", AlxSummary.class);
  }

  public AlxSummary getAlxSummary(int gameId) {
    return getRestClient().get(API + "alx/" + gameId, AlxSummary.class);
  }

  public boolean deleteNumberPlaysForGame(@PathVariable("gameId") int gameId) {
    return getRestClient().delete(API + "alx/game/numberplays/" + gameId, new HashMap<>());
  }

  public boolean deleteNumberOfPlaysForEmulator(@PathVariable("emulatorId") int emulatorId) {
    return getRestClient().delete(API + "alx/emulator/numberplays/" + emulatorId, new HashMap<>());
  }

  public boolean deleteTimePlayedForGame(@PathVariable("gameId") int gameId) {
    return getRestClient().delete(API + "alx/game/timeplayed/" + gameId, new HashMap<>());
  }

  public boolean deleteTimePlayedForEmulator(@PathVariable("emulatorId") int emulatorId) {
    return getRestClient().delete(API + "alx/emulator/timeplayed/" + emulatorId, new HashMap<>());
  }

  public boolean updateTimePlayedForGame(@PathVariable("gameId") int gameId, long value) throws Exception {
    Map<String, Object> data = new HashMap<>();
    data.put("value", value);
    data.put("dataField", "timePlayed");
    return getRestClient().put(API + "alx/" + gameId, data);
  }

  public boolean updateNumberOfPlaysForGame(@PathVariable("gameId") int gameId, long value) throws Exception {
    Map<String, Object> data = new HashMap<>();
    data.put("value", value);
    data.put("dataField", "numberOfPlays");
    return getRestClient().put(API + "alx/" + gameId, data);
  }
}
