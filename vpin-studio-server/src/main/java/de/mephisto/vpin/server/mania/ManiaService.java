package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import de.mephisto.vpin.server.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ManiaService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaService.class);

  private String cabinetId;

  private VPinManiaClient maniaClient;

  @Nullable
  public ManiaAccountRepresentation getAccount() {
    return maniaClient.getAccountClient().getAccount(this.cabinetId);
  }

  public ManiaAccountRepresentation save(ManiaAccountRepresentation update) {
    update.setCabinetId(this.cabinetId);
    maniaClient.getAccountClient().save(update);
    return update;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.cabinetId = SystemUtil.getBoardSerialNumber();
    maniaClient = new VPinManiaClient();
  }

  public boolean deleteAccount() {
    return false;
  }
}
