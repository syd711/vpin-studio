package de.mephisto.vpin.server.alx;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "alx")
public class AlxResource {
  private final static Logger LOG = LoggerFactory.getLogger(AlxResource.class);

  @Autowired
  private AlxService analyticsService;

  @GetMapping
  public List<TableAlxEntry> getAlxEntries() {
    return analyticsService.getAlxEntries();
  }
}
