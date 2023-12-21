package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import de.mephisto.vpin.server.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ManiaService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaService.class);

  private String cabinetId;

  private VPinManiaClient maniaClient;

  @Value("${vpinmania.server.host}")
  private String maniaHost;

  @Value("${vpinmania.server.context}")
  private String maniaContext;


  @Nullable
  public ManiaAccountRepresentation getAccount() {
    return maniaClient.getAccountClient().getAccount(this.cabinetId);
  }

  public ManiaAccountRepresentation save(ManiaAccountRepresentation account) throws Exception {
    ManiaAccountRepresentation existingAccount = getAccount();

    if(existingAccount != null) {
      maniaClient.getAccountClient().update(account);
    }
    else {
      account.setCabinetId(this.cabinetId);
      maniaClient.getAccountClient().register(account);
    }

    return getAccount();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.cabinetId = SystemUtil.getBoardSerialNumber();
    maniaClient = new VPinManiaClient(maniaHost, maniaContext, cabinetId);
    LOG.info("VPin Mania client created for host " + maniaHost);
  }

  public boolean deleteAccount() {
    return false;
  }
}
