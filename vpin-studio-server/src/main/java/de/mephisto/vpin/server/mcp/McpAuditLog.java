package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.mcp.McpAuditEntry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Keeps the latest calls of MCP tools that change the cabinet, so the user can see in the AI Assistants preferences
 * what an assistant did. The log is kept in memory only; every call is also written to the server log.
 */
@Service
public class McpAuditLog {
  static final int MAX_ENTRIES = 200;
  private static final int MAX_TEXT = 2000;

  private final Deque<McpAuditEntry> entries = new ArrayDeque<>();

  public synchronized void add(McpTool tool, String arguments, String result, boolean error, boolean dryRun) {
    McpAuditEntry entry = new McpAuditEntry();
    entry.setTimestamp(Instant.now());
    entry.setTool(tool.name());
    entry.setGroup(tool.group() != null ? tool.group().name() : null);
    entry.setArguments(StringUtils.abbreviate(arguments, MAX_TEXT));
    entry.setResult(StringUtils.abbreviate(result, MAX_TEXT));
    entry.setError(error);
    entry.setDryRun(dryRun);
    entries.addFirst(entry);
    while (entries.size() > MAX_ENTRIES) {
      entries.removeLast();
    }
  }

  /**
   * @return the entries, newest first
   */
  public synchronized List<McpAuditEntry> getEntries() {
    return new ArrayList<>(entries);
  }

  public synchronized void clear() {
    entries.clear();
  }
}
