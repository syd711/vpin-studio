package de.mephisto.vpin.server.webhooks;

import de.mephisto.vpin.restclient.webhooks.WebhookSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "webhooks")
public class WebhooksResource {
  private final static Logger LOG = LoggerFactory.getLogger(WebhooksResource.class);

  @Autowired
  private WebhooksService webhooksService;

  /**
   * Note that his endpoint is not used within the Studio, but for external integrations.
   */
  @PostMapping
  public WebhookSet save(@RequestBody WebhookSet webhookSet) throws Exception {
    return webhooksService.save(webhookSet);
  }

  /**
   * Delete the given webhook set.
   */
  @DeleteMapping("/{uuid}")
  public boolean delete(@PathVariable("uuid") String uuid) throws Exception {
    return webhooksService.delete(uuid);
  }
}
