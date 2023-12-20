package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ManiaService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaService.class);

  @Autowired
  private ManiaAccountRepository maniaAccountRepository;

  private String cabinetId;

  @Nullable
  public ManiaAccount getAccount() {
    if (!StringUtils.isEmpty(cabinetId)) {
      return maniaAccountRepository.findByCabinetId(cabinetId);
    }
    return null;
  }

  public String getCabinetId() {
    return cabinetId;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.cabinetId = SystemUtil.getBoardSerialNumber();
  }

  public ManiaAccount save(ManiaAccount update) {
    ManiaAccount accountRecord = maniaAccountRepository.findByCabinetId(cabinetId);
    if(accountRecord == null) {
      accountRecord = new ManiaAccount();
      accountRecord.setUuid(UUID.randomUUID().toString());
      accountRecord.setCabinetId(SystemUtil.getBoardSerialNumber());
    }
    accountRecord.setInitials(update.getInitials());
    accountRecord.setDisplayName(update.getDisplayName());
    maniaAccountRepository.saveAndFlush(accountRecord);
    LOG.info("Saved " + accountRecord);
    return accountRecord;
  }
}
