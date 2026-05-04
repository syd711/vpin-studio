package de.mephisto.vpin.restclient.iscored;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.hooks.HookList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/*********************************************************************************************************************
 * iScored
 ********************************************************************************************************************/
public class IScoredServiceClient extends VPinStudioClientService {
    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public IScoredServiceClient(VPinStudioClient client) {
        super(client);
    }

    public void deleteGameRoom(String uuid) {
        getRestClient().delete(API + "iscored/" + uuid);
    }
}
