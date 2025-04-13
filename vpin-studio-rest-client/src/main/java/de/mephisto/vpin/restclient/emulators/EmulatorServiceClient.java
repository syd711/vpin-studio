package de.mephisto.vpin.restclient.emulators;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.preferences.UISettings;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/*********************************************************************************************************************
 * Emulators
 ********************************************************************************************************************/
public class EmulatorServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorServiceClient.class);

  private final static String API_SEGMENT_EMULATORS = "emulators";

  private static final int ALL_VPX_ID = -10;

  public EmulatorServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<String> getAltExeNames(int emulatorId) {
    String[] emus = getRestClient().getCached(API + API_SEGMENT_EMULATORS + "/altExeNames/" + emulatorId, String[].class);
    return emus != null ? Arrays.asList(emus) : Collections.emptyList();
  }

  public List<GameEmulatorRepresentation> getVpxGameEmulators() {
    return getValidatedGameEmulators().stream().filter(e -> e.isVpxEmulator()).collect(Collectors.toList());
  }

  public List<GameEmulatorRepresentation> getFpGameEmulators() {
    return getValidatedGameEmulators().stream().filter(e -> e.isFpEmulator()).collect(Collectors.toList());
  }

  public GameEmulatorRepresentation getGameEmulator(int id) {
    List<GameEmulatorRepresentation> gameEmulators = getValidatedGameEmulators();
    return gameEmulators.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
  }

  public List<GameEmulatorRepresentation> getGameEmulatorsByType(@Nullable EmulatorType emutype) {
    if (emutype != null) {
      if (emutype.equals(EmulatorType.VisualPinball)) {
        return getVpxGameEmulators();
      }
      else if (emutype.equals(EmulatorType.FuturePinball)) {
        return getFpGameEmulators();
      }
    }
    return Collections.emptyList();
  }

  public List<GameEmulatorRepresentation> getValidatedGameEmulators() {
    GameEmulatorRepresentation[] emus = getRestClient().getCached(API + API_SEGMENT_EMULATORS, GameEmulatorRepresentation[].class);
    return emus != null ? Arrays.asList(emus) : Collections.emptyList();
  }


  public List<GameEmulatorRepresentation> getGameEmulatorsUncached() {
    return Arrays.asList(getRestClient().get(API + API_SEGMENT_EMULATORS, GameEmulatorRepresentation[].class));
  }

  public List<GameEmulatorRepresentation> getBackglassGameEmulators() {
    return Arrays.asList(getRestClient().getCached(API + API_SEGMENT_EMULATORS + "/backglassemulators", GameEmulatorRepresentation[].class));
  }

  public List<GameEmulatorRepresentation> getFilteredEmulatorsWithAllVpx(UISettings uiSettings) {
    List<GameEmulatorRepresentation> emulators = getGameEmulatorsUncached();
    List<GameEmulatorRepresentation> filtered = emulators.stream().filter(e -> e.isEnabled()).filter(e -> !uiSettings.getIgnoredEmulatorIds().contains(Integer.valueOf(e.getId()))).collect(Collectors.toList());
    List<GameEmulatorRepresentation> vpxEmulators = filtered.stream().filter(e -> e.isVpxEmulator()).collect(Collectors.toList());

    if (vpxEmulators.size() > 1) {
      filtered.add(0, createAllVpx());
    }
    return filtered;
  }

  public boolean isAllVpx(GameEmulatorRepresentation emu) {
    return emu != null ? emu.getId() == ALL_VPX_ID : true;
  }

  private GameEmulatorRepresentation createAllVpx() {
    GameEmulatorRepresentation allVpx = new GameEmulatorRepresentation();
    allVpx.setId(ALL_VPX_ID);
    allVpx.setName("All VPX Tables");
    allVpx.setType(EmulatorType.VisualPinball);
    return allVpx;
  }

  public List<GameEmulatorRepresentation> getFilteredEmulatorsWithEmptyOption(UISettings uiSettings) {
    List<GameEmulatorRepresentation> emulators = getGameEmulatorsUncached();
    List<GameEmulatorRepresentation> filtered = emulators.stream().filter(e -> !uiSettings.getIgnoredEmulatorIds().contains(Integer.valueOf(e.getId()))).collect(Collectors.toList());
    filtered.add(0, null);
    return filtered;
  }

  public void deleteGameEmulator(int id) {
    getRestClient().delete(API + API_SEGMENT_EMULATORS + "/" + id);
  }

  public GameEmulatorRepresentation saveGameEmulator(GameEmulatorRepresentation emulator) {
    try {
      clearCache();
      return getRestClient().post(API + API_SEGMENT_EMULATORS + "/save", emulator, GameEmulatorRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save emulator: " + e.getMessage(), e);
      throw e;
    }
  }

  public void clearCache() {
    getRestClient().clearCache(API + API_SEGMENT_EMULATORS);
    getRestClient().get(API + API_SEGMENT_EMULATORS + "/clearcache", Boolean.class);
  }
}
