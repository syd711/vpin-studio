package de.mephisto.vpin.server.vpsdb;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VpsEntryService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VpsEntryService.class);

  @Autowired
  private VpsEntriesRepository vpsEntriesRepository;

  public VpsDbEntry getVpsEntry(@NonNull String vpsTableId) {
    return vpsEntriesRepository.findByVpsTableId(vpsTableId);
  }

  public void save(VpsTable vpsTable) {
    VpsDbEntry vpsEntry = getVpsEntry(vpsTable.getId());
    if (vpsEntry == null) {
      vpsEntry = new VpsDbEntry();
    }
    vpsEntry.setComment(vpsTable.getComment());
    vpsEntry.setVpsTableId(vpsTable.getId());
    vpsEntriesRepository.saveAndFlush(vpsEntry);
  }

  @Override
  public void afterPropertiesSet() throws Exception {

  }
}
