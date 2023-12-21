package de.mephisto.vpin.connectors.mania;

import de.mephisto.vpin.restclient.mania.ManiaTournamentRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

public class TournamentClient extends VPinManiaClientService {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentClient.class);

  public TournamentClient(VPinManiaClient client) {
    super(client);
  }

  public ManiaTournamentRepresentation update(ManiaTournamentRepresentation Tournament) throws Exception {
    try {
      return getRestClient().post(API + "tournament/update", Tournament, ManiaTournamentRepresentation.class);
    } catch (HttpClientErrorException e) {
      LOG.error("Failed to upate tournament: " + e.getMessage(), e);
      throw new ResponseStatusException(e.getStatusCode(), "Tournament update failed: " + e.getMessage());
    }
  }

  public ManiaTournamentRepresentation create(ManiaTournamentRepresentation Tournament) throws Exception {
    try {
      return getRestClient().post(API + "tournament/create", Tournament, ManiaTournamentRepresentation.class);
    } catch (HttpClientErrorException e) {
      throw new ResponseStatusException(e.getStatusCode(), "Tournament registration failed: " + e.getMessage());
    }
  }

  public ManiaTournamentRepresentation getTournament(String uuid) {
    try {
      return getRestClient().get(API + "tournament/" + uuid, ManiaTournamentRepresentation.class);
    } catch (HttpClientErrorException e) {
      LOG.error("Tournament request failed: " + e.getMessage());
      throw new ResponseStatusException(e.getStatusCode(), "Tournament request failed: " + e.getMessage());
    }
  }

  public boolean deleteTournament(String uuid) {
    try {
      return getRestClient().delete(API + "tournament/delete/" + uuid);
    } catch (HttpClientErrorException e) {
      LOG.error("Tournament deletion failed: " + e.getMessage());
      throw new ResponseStatusException(e.getStatusCode(), "Tournament deletion failed: " + e.getMessage());
    }
  }
}
