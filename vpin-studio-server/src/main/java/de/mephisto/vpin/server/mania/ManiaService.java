package de.mephisto.vpin.server.mania;

import com.google.common.annotations.VisibleForTesting;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.ManiaAccountRepresentation;
import de.mephisto.vpin.server.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ManiaService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaService.class);

  private VPinManiaClient maniaClient;

  @Value("${vpinmania.server.host}")
  private String maniaHost;

  @Value("${vpinmania.server.context}")
  private String maniaContext;


  @Nullable
  public ManiaAccountRepresentation getAccount() {
    return maniaClient.getAccountClient().getAccount();
  }

  public ManiaAccountRepresentation save(ManiaAccountRepresentation account) throws Exception {
    ManiaAccountRepresentation existingAccount = getAccount();

    if (existingAccount != null) {
      maniaClient.getAccountClient().update(account);
    }
    else {
      account.setUuid(UUID.randomUUID().toString());
      maniaClient.getAccountClient().register(account);
    }

    return getAccount();
  }

  public boolean deleteAccount() {
    return maniaClient.getAccountClient().deleteAccount();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    String cabinetId = SystemUtil.getBoardSerialNumber();
    maniaClient = new VPinManiaClient(maniaHost, maniaContext, cabinetId);
    LOG.info("VPin Mania client created for host " + maniaHost);
  }

  @VisibleForTesting
  public void setCabinetId(String id) {
    maniaClient.setCabinetId(id);
  }
}
