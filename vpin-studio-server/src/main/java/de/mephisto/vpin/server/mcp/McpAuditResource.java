package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.mcp.McpAuditEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

/**
 * REST access to the {@link McpAuditLog} for the AI Assistants preferences of the Studio.
 */
@RestController
@RequestMapping(API_SEGMENT + "mcp")
public class McpAuditResource {

  @Autowired
  private McpAuditLog auditLog;

  @GetMapping("/audit")
  public List<McpAuditEntry> getAuditLog() {
    return auditLog.getEntries();
  }

  @DeleteMapping("/audit")
  public boolean clearAuditLog() {
    auditLog.clear();
    return true;
  }
}
