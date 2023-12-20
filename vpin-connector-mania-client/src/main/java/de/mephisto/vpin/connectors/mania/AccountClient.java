package de.mephisto.vpin.connectors.mania;

import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountClient extends VPinManiaClientService{
  private final static Logger LOG = LoggerFactory.getLogger(AccountClient.class);

  public AccountClient(VPinManiaClient client) {
    super(client);
  }

  public ManiaAccountRepresentation save(ManiaAccountRepresentation account) throws Exception {
    try {
      return getRestClient().post(API + "account/save", account, ManiaAccountRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save account: " + e.getMessage(), e);
      throw e;
    }
  }

  public ManiaAccountRepresentation getAccount(String cabinetId) {
    return getRestClient().get(API + "account/get/" + cabinetId, ManiaAccountRepresentation.class);
  }
}
