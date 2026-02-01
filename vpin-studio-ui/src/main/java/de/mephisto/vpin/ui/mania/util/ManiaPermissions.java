package de.mephisto.vpin.ui.mania.util;

import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.AccountType;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.Studio;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaPermissions {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaPermissions.class);

  private static Account adminAccount;
  private final static List<String> ADMIN_IDS = Arrays.asList("7b15900b9488e1e0517833f97ac2b3c1");

  public static void invalidate() {
    adminAccount = null;
  }

  public static boolean isEditor() {
    if (adminAccount == null) {
      loadAccount();
    }
    if (isAdmin()) {
      return true;
    }

    return adminAccount != null && AccountType.editor.equals(adminAccount.getAccountType());
  }

  public static boolean isAdmin() {
    try {
      if (adminAccount == null) {
        loadAccount();
      }

      Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
      if (cabinet != null && ADMIN_IDS.contains(cabinet.getUuid())) {
        return true;
      }

      return adminAccount != null && AccountType.administrator.equals(adminAccount.getAccountType());
    }
    catch (Exception e) {
      LOG.error("Mania permissions check failed: {}", e.getMessage());
      return false;
    }
  }

  private static void loadAccount() {
    List<PlayerRepresentation> players = Studio.client.getPlayerService().getPlayers();
    List<PlayerRepresentation> collect = players.stream().filter(p -> !StringUtils.isEmpty(p.getManiaAccountUuid()) && p.isAdministrative()).collect(Collectors.toList());
    if (!collect.isEmpty()) {
      PlayerRepresentation playerRepresentation = collect.get(0);
      adminAccount = maniaClient.getAccountClient().getAccountByUuid(playerRepresentation.getManiaAccountUuid());
    }
  }

  public static Account getAccount() {
    if (adminAccount == null) {
      loadAccount();
    }
    return adminAccount;
  }
}
