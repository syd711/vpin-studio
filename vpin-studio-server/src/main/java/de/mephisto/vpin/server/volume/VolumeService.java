package de.mephisto.vpin.server.volume;

import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.PopperStatusChangeListener;
import de.mephisto.vpin.server.popper.TableStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VolumeService implements InitializingBean, PopperStatusChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(VolumeService.class);

  @Autowired
  private PopperService popperService;

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    new Thread(() -> {
      VolumeUtil.awaitVPX();
      float vpxVolume = VolumeUtil.getVPXVolume();
      LOG.info("Initial table volume for " + event.getGame().getGameDisplayName() + ": " + vpxVolume);
    }).start();

  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {

  }

  public float getCurrentVolume() {
    return VolumeUtil.getVPXVolume();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    popperService.addPopperStatusChangeListener(this);
  }
}
