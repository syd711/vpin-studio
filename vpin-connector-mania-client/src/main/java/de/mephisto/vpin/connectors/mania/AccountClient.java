package de.mephisto.vpin.connectors.mania;

import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

public class AccountClient extends VPinManiaClientService {
  private final static Logger LOG = LoggerFactory.getLogger(AccountClient.class);

  public AccountClient(VPinManiaClient client) {
    super(client);
  }

  public ManiaAccountRepresentation update(ManiaAccountRepresentation account) throws Exception {
    try {
      return getRestClient().post(API + "account/update", account, ManiaAccountRepresentation.class);
    } catch (HttpClientErrorException e) {
      LOG.error("Failed to upate account: " + e.getMessage(), e);
      throw new ResponseStatusException(e.getStatusCode(), "Account update failed: " + e.getMessage());
    }
  }

  public ManiaAccountRepresentation register(ManiaAccountRepresentation account) throws Exception {
    try {
      return getRestClient().post(API + "account/register", account, ManiaAccountRepresentation.class);
    } catch (HttpClientErrorException e) {
      throw new ResponseStatusException(e.getStatusCode(), "Account registration failed: " + e.getMessage());
    }
  }

  public ManiaAccountRepresentation getAccount() {
    try {
      return getRestClient().get(API + "account", ManiaAccountRepresentation.class);
    } catch (HttpClientErrorException e) {
      LOG.error("Account request failed: " + e.getMessage());
      throw new ResponseStatusException(e.getStatusCode(), "Account request failed: " + e.getMessage());
    }
  }

  public boolean deleteAccount() {
    try {
      return getRestClient().delete(API + "account/delete");
    } catch (HttpClientErrorException e) {
      LOG.error("Account deletion failed: " + e.getMessage());
      throw new ResponseStatusException(e.getStatusCode(), "Account deletion failed: " + e.getMessage());
    }
  }
}
