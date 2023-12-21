package de.mephisto.vpin.connectors.mania;

import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountClient extends VPinManiaClientService{
  private final static Logger LOG = LoggerFactory.getLogger(AccountClient.class);

  public AccountClient(VPinManiaClient client) {
    super(client);
  }

  public ManiaAccountRepresentation update(ManiaAccountRepresentation account) throws Exception {
    try {
      return getRestClient().post(API + "account/update", account, ManiaAccountRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to upate account: " + e.getMessage(), e);
      throw e;
    }
  }
  public ManiaAccountRepresentation register(ManiaAccountRepresentation account) throws Exception {
    try {
      return getRestClient().post(API + "account/register", account, ManiaAccountRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to register account: " + e.getMessage(), e);
      throw e;
    }
  }

  public ManiaAccountRepresentation getAccount(String uuid) {
    return getRestClient().get(API + "account/get/" + uuid, ManiaAccountRepresentation.class);
  }

  public boolean deleteAccount(String uuid) {
    return getRestClient().delete(API + "account/delete" + uuid);
  }
}
