package de.mephisto.vpin.server.tournaments;

import com.google.common.annotations.VisibleForTesting;
import de.mephisto.vpin.connectors.mania.ManiaServiceConfig;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class TournamentsService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsService.class);

  private VPinManiaClient maniaClient;

  @Value("${vpinmania.server.host}")
  private String maniaHost;

  @Autowired
  private PreferencesService preferencesService;

  public ManiaServiceConfig getConfig() {
    ManiaServiceConfig config = new ManiaServiceConfig();
    config.setCabinetId(SystemUtil.getBoardSerialNumber());
    config.setUrl(maniaHost);
    return config;
  }

  public TournamentSettings saveSettings(TournamentSettings settings) {
    try {
      preferencesService.savePreference(PreferenceNames.TOURNAMENTS_SETTINGS, settings);
      return getSettings();
    } catch (Exception e) {
      LOG.error("Saving tournament settings failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Saving tournament settings failed: " + e.getMessage());
    }
  }

  public TournamentSettings getSettings() {
    try {
      return preferencesService.getJsonPreference(PreferenceNames.TOURNAMENTS_SETTINGS, TournamentSettings.class);
    } catch (Exception e) {
      LOG.error("Getting tournament settings failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Get of settings failed: " + e.getMessage());
    }
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
