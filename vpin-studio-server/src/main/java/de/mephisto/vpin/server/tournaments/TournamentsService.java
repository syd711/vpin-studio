package de.mephisto.vpin.server.tournaments;

import com.google.common.annotations.VisibleForTesting;
import de.mephisto.vpin.connectors.mania.ManiaServiceConfig;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.restclient.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TournamentsService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsService.class);

  private VPinManiaClient maniaClient;

  @Value("${vpinmania.server.host}")
  private String maniaHost;

  public ManiaServiceConfig getConfig() {
    ManiaServiceConfig config = new ManiaServiceConfig();
    config.setCabinetId(SystemUtil.getBoardSerialNumber());
    config.setUrl(maniaHost);
    return config;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    String cabinetId = SystemUtil.getBoardSerialNumber();
    maniaClient = new VPinManiaClient(maniaHost, cabinetId);
    LOG.info("VPin Mania client created for host " + maniaHost);
  }

  @VisibleForTesting
  public void setCabinetId(String id) {
    maniaClient.setCabinetId(id);
  }
}
