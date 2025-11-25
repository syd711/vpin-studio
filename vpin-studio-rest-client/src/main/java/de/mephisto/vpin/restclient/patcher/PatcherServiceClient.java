package de.mephisto.vpin.restclient.patcher;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;


/*********************************************************************************************************************
 * Patcher
 ********************************************************************************************************************/
public class PatcherServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public PatcherServiceClient(VPinStudioClient client) {
    super(client);
  }


  public UploadDescriptor proccessTablePatch(UploadDescriptor uploadDescriptor) throws Exception {
    try {
      return getRestClient().post(API + "patcher/process", uploadDescriptor, UploadDescriptor.class);
    }
    catch (Exception e) {
      LOG.error("Failed to patch table: " + e.getMessage(), e);
      throw e;
    }
  }
}
