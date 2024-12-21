package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.AccountType;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.Studio;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaPermissions {

  private static Account adminAccount;

  public static void invalidate() {
    adminAccount = null;
  }

  public static boolean isEditor() {
    if (adminAccount == null) {
      loadAccount();
    }
    return adminAccount != null && AccountType.editor.equals(adminAccount.getAccountType());
  }

  public static boolean isAdmin() {
    if (adminAccount == null) {
      loadAccount();
    }
    return adminAccount != null && AccountType.administrator.equals(adminAccount.getAccountType());
  }

  private static void loadAccount() {
    List<PlayerRepresentation> players = Studio.client.getPlayerService().getPlayers();
    List<PlayerRepresentation> collect = players.stream().filter(p -> !StringUtils.isEmpty(p.getTournamentUserUuid()) && p.isAdministrative()).collect(Collectors.toList());
    if (!collect.isEmpty()) {
      PlayerRepresentation playerRepresentation = collect.get(0);
      adminAccount = maniaClient.getAccountClient().getAccountByUuid(playerRepresentation.getTournamentUserUuid());
    }
  }

  public static Account getAccount() {
    if (adminAccount == null) {
      loadAccount();
    }
    return adminAccount;
  }
}
