package de.mephisto.vpin.server.alx;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.server.popper.PinUPConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlxService {

  @Autowired
  private PinUPConnector pinUPConnector;

  public List<TableAlxEntry> getAlxEntries() {
    return pinUPConnector.getAlxData();
  }
}
