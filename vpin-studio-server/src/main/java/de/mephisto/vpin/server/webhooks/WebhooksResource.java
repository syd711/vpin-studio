package de.mephisto.vpin.server.webhooks;

import de.mephisto.vpin.restclient.webhooks.WebhookSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  @PostMapping("/save")
  public WebhookSet save(@RequestBody WebhookSet webhookSet) throws Exception {
    return webhooksService.save(webhookSet);
  }
}
