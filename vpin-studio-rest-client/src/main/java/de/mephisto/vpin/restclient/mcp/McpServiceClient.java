package de.mephisto.vpin.restclient.mcp;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * MCP (AI assistants)
 ********************************************************************************************************************/
public class McpServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public McpServiceClient(VPinStudioClient client) {
    super(client);
  }

  /**
   * @return the latest changes made through the MCP endpoint, newest first
   */
  public List<McpAuditEntry> getAuditLog() {
    try {
      McpAuditEntry[] entries = getRestClient().get(API + "mcp/audit", McpAuditEntry[].class);
      return entries != null ? new ArrayList<>(Arrays.asList(entries)) : new ArrayList<>();
    }
    catch (Exception e) {
      //servers older than this client don't offer the audit log
      LOG.warn("Failed to read the MCP audit log: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  public boolean clearAuditLog() {
    return getRestClient().delete(API + "mcp/audit");
  }
}
