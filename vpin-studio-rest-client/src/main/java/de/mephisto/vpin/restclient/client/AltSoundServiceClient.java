package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.AltSound;

/*********************************************************************************************************************
 * Alt Sound
 ********************************************************************************************************************/
public class AltSoundServiceClient extends VPinStudioClientService {

  AltSoundServiceClient(VPinStudioClient client) {
    super(client);
  }

  public AltSound saveAltSound(int gameId, AltSound altSound) throws Exception {
    return getRestClient().post(API + "altsound/save/" + gameId, altSound, AltSound.class);
  }

  public AltSound getAltSound(int gameId) {
    return getRestClient().get(API + "altsound/" + gameId, AltSound.class);
  }

  public AltSound restoreAltSound(int gameId) {
    return getRestClient().get(API + "altsound/restore/" + gameId, AltSound.class);
  }

  public boolean isAltSoundEnabled(int gameId) {
    return getRestClient().get(API + "altsound/enabled/" + gameId, Boolean.class);
  }

  public boolean setAltSoundEnabled(int gameId, boolean b) {
    return getRestClient().get(API + "altsound/set/" + gameId + "/" + b, Boolean.class);
  }
}
