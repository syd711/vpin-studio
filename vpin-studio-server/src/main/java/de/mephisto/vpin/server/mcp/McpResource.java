package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mcp.McpSettings;
import de.mephisto.vpin.restclient.mcp.McpToolGroup;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.*;

/**
 * MCP (Model Context Protocol) endpoint, "Streamable HTTP" transport in its stateless form: every JSON-RPC request is
 * answered with a plain JSON response, the server never opens an SSE stream.
 * <p>
 * Claude Code: {@code claude mcp add --transport http vpin-studio http://<cabinet>:8089/mcp}
 * <p>
 * The endpoint is disabled by default, see {@link McpSettings}. Like the REST API it has no authentication.
 */
@RestController
@RequestMapping("mcp")
public class McpResource {
  private final static Logger LOG = LoggerFactory.getLogger(McpResource.class);

  static final String LATEST_PROTOCOL_VERSION = "2025-06-18";
  private static final Set<String> SUPPORTED_PROTOCOL_VERSIONS = Set.of("2024-11-05", "2025-03-26", "2025-06-18", "2025-11-25");

  private static final int PARSE_ERROR = -32700;
  private static final int INVALID_REQUEST = -32600;
  private static final int METHOD_NOT_FOUND = -32601;
  private static final int INVALID_PARAMS = -32602;
  private static final int INTERNAL_ERROR = -32603;

  private static final String INSTRUCTIONS = "This server manages the virtual pinball cabinet of the user, running VPin Studio. "
      + "Tables are identified by numeric ids: look them up with search_tables before calling the other tools. "
      + "Validation issues are reported by name, e.g. NO_ROM or NO_DIRECTB2S_OR_PUPPACK. "
      + "For questions on how to use or set up VPin Studio, use search_help: it searches the user documentation at "
      + WikiHelpIndex.WIKI_URL + ". Answer from the documentation and include the links instead of guessing.";
  private static final String INSTRUCTIONS_READ_ONLY = " The server is in read-only mode: the tools can not change anything on the cabinet. "
      + "The user can allow changes in the VPin Studio preferences (AI Assistants).";
  private static final String INSTRUCTIONS_WRITE = " Changes are allowed for these groups: %s. Tools marked as destructive "
      + "(deleting tables, backglasses, media, playlists or competitions, resetting highscores, restoring backups, restarting "
      + "the server, shutting down the cabinet) cannot be undone: always name what is affected and ask the user for "
      + "confirmation before calling them. Before changing several tables at once or changing settings, call the tool "
      + "with dryRun=true first where it is offered and show the user the preview. Secret settings (passwords, API keys, "
      + "tokens, webhook URLs) are masked and can only be changed in VPin Studio.";
  private static final String INSTRUCTIONS_NO_GROUPS = " The read-only mode is off, but no group of changes is allowed in "
      + "the VPin Studio preferences (AI Assistants), so the tools can not change anything on the cabinet.";

  private final ObjectMapper mapper = JsonMapper.builder().build();

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private List<McpToolProvider> toolProviders;

  @Autowired
  private McpAuditLog auditLog;

  @PostMapping
  public ResponseEntity<String> post(@RequestHeader HttpHeaders headers, @RequestBody(required = false) byte[] body) {
    McpSettings settings = preferencesService.getJsonPreference(PreferenceNames.MCP_SETTINGS, McpSettings.class);
    ResponseEntity<String> denied = checkAccess(settings, headers);
    if (denied != null) {
      return denied;
    }

    Object message;
    try {
      message = mapper.readValue(body != null ? body : new byte[0], Object.class);
    }
    catch (Exception e) {
      return json(HttpStatus.BAD_REQUEST, error(null, PARSE_ERROR, "Parse error: " + e.getMessage()));
    }

    if (message instanceof List<?> batch) {
      List<Object> responses = new ArrayList<>();
      for (Object entry : batch) {
        Object response = handle(entry, settings);
        if (response != null) {
          responses.add(response);
        }
      }
      return responses.isEmpty() ? accepted() : json(HttpStatus.OK, responses);
    }

    Object response = handle(message, settings);
    return response != null ? json(HttpStatus.OK, response) : accepted();
  }

  /**
   * No server initiated messages: the SSE stream is not offered.
   */
  @GetMapping
  public ResponseEntity<String> get() {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).header(HttpHeaders.ALLOW, "POST").build();
  }

  /**
   * The server is stateless, so there is no session to terminate.
   */
  @DeleteMapping
  public ResponseEntity<String> delete() {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).header(HttpHeaders.ALLOW, "POST").build();
  }

  //-------------------------------- access ---------------------------------------------------------

  private ResponseEntity<String> checkAccess(McpSettings settings, HttpHeaders headers) {
    if (settings == null || !settings.isEnabled()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    //browsers always send an Origin, MCP clients don't: blocks web pages (DNS rebinding) from talking to the cabinet
    if (headers.getOrigin() != null) {
      LOG.warn("Rejected MCP request from browser origin {}", headers.getOrigin());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return null;
  }

  //-------------------------------- JSON-RPC -------------------------------------------------------

  /**
   * @return the response for a request, null for notifications and responses sent by the client
   */
  @SuppressWarnings("unchecked")
  Object handle(Object message, McpSettings settings) {
    if (!(message instanceof Map)) {
      return error(null, INVALID_REQUEST, "Invalid request: a JSON-RPC object was expected.");
    }
    Map<String, Object> request = (Map<String, Object>) message;
    Object id = request.get("id");
    Object method = request.get("method");
    if (!(method instanceof String methodName)) {
      //a response from the client (we never send requests), nothing to answer
      return request.containsKey("result") || request.containsKey("error")
          ? null : error(id, INVALID_REQUEST, "Invalid request: \"method\" is missing.");
    }
    if (id == null) {
      //notification, e.g. notifications/initialized
      return null;
    }

    Map<String, Object> params = request.get("params") instanceof Map ? (Map<String, Object>) request.get("params") : Map.of();
    try {
      return switch (methodName) {
        case "initialize" -> result(id, initialize(params, settings));
        case "ping" -> result(id, Map.of());
        case "tools/list" -> result(id, Map.of("tools", listTools(settings)));
        case "tools/call" -> callTool(id, params, settings);
        default -> error(id, METHOD_NOT_FOUND, "Method not found: " + methodName);
      };
    }
    catch (Exception e) {
      LOG.error("MCP request {} failed: {}", methodName, e.getMessage(), e);
      return error(id, INTERNAL_ERROR, "Internal error: " + e.getMessage());
    }
  }

  private Map<String, Object> initialize(Map<String, Object> params, McpSettings settings) {
    Object requested = params.get("protocolVersion");
    String version = requested instanceof String v && SUPPORTED_PROTOCOL_VERSIONS.contains(v) ? v : LATEST_PROTOCOL_VERSION;

    Map<String, Object> clientInfo = params.get("clientInfo") instanceof Map<?, ?> m ? castMap(m) : Map.of();
    LOG.info("MCP client connected: {} {}", clientInfo.get("name"), clientInfo.get("version"));

    Map<String, Object> serverInfo = new LinkedHashMap<>();
    serverInfo.put("name", "vpin-studio");
    serverInfo.put("title", "VPin Studio");
    serverInfo.put("version", systemService.getVersion());

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("protocolVersion", version);
    result.put("capabilities", Map.of("tools", Map.of("listChanged", false)));
    result.put("serverInfo", serverInfo);
    result.put("instructions", INSTRUCTIONS + getModeInstructions(settings));
    return result;
  }

  static String getModeInstructions(McpSettings settings) {
    if (settings.isReadOnly()) {
      return INSTRUCTIONS_READ_ONLY;
    }
    List<String> groups = Arrays.stream(McpToolGroup.values()).filter(settings::isAllowed).map(g -> g.name().toLowerCase(Locale.ROOT)).toList();
    return groups.isEmpty() ? INSTRUCTIONS_NO_GROUPS : String.format(INSTRUCTIONS_WRITE, String.join(", ", groups));
  }

  /**
   * @return true if the tool may be called with the given settings
   */
  static boolean isOffered(McpTool tool, McpSettings settings) {
    return tool.readOnly() || (!settings.isReadOnly() && tool.group() != null && settings.isAllowed(tool.group()));
  }

  /**
   * All tools of the server, the write tools only when the read-only mode is off and their group is allowed.
   */
  private List<McpTool> getTools(McpSettings settings) {
    List<McpTool> tools = getAllTools();
    tools.removeIf(t -> !isOffered(t, settings));
    return tools;
  }

  private List<McpTool> getAllTools() {
    List<McpTool> tools = new ArrayList<>();
    for (McpToolProvider provider : toolProviders) {
      tools.addAll(provider.getTools());
    }
    return tools;
  }

  private List<Map<String, Object>> listTools(McpSettings settings) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (McpTool tool : getTools(settings)) {
      Map<String, Object> t = new LinkedHashMap<>();
      t.put("name", tool.name());
      t.put("title", tool.title());
      t.put("description", tool.description());
      t.put("inputSchema", tool.inputSchema());
      Map<String, Object> annotations = new LinkedHashMap<>();
      annotations.put("title", tool.title());
      annotations.put("readOnlyHint", tool.readOnly());
      if (!tool.readOnly()) {
        annotations.put("destructiveHint", tool.destructive());
      }
      annotations.put("openWorldHint", false);
      t.put("annotations", annotations);
      result.add(t);
    }
    return result;
  }

  private Map<String, Object> callTool(Object id, Map<String, Object> params, McpSettings settings) {
    Object name = params.get("name");
    Optional<McpTool> known = getAllTools().stream().filter(t -> t.name().equals(name)).findFirst();
    if (known.isPresent() && !isOffered(known.get(), settings)) {
      //the client still knows the tool from before the settings were changed
      String reason = settings.isReadOnly() ? "The VPin Studio MCP server is in read-only mode."
          : "Changes of the group \"" + known.get().group().name().toLowerCase(Locale.ROOT) + "\" are not allowed.";
      return result(id, toolResult(reason + " The user can allow changes in the VPin Studio preferences (AI Assistants).", true));
    }
    if (known.isEmpty()) {
      return error(id, INVALID_PARAMS, "Unknown tool: " + name);
    }
    McpTool tool = known.get();

    Map<String, Object> arguments = params.get("arguments") instanceof Map<?, ?> m ? castMap(m) : Map.of();
    McpArguments args = new McpArguments(arguments);
    long start = System.currentTimeMillis();
    String text;
    boolean isError;
    try {
      Object value = tool.handler().apply(args);
      LOG.info("MCP tool {} executed in {}ms", name, System.currentTimeMillis() - start);
      text = mapper.writeValueAsString(value);
      isError = false;
    }
    catch (McpToolException e) {
      text = e.getMessage();
      isError = true;
    }
    catch (Exception e) {
      LOG.error("MCP tool {} failed: {}", name, e.getMessage(), e);
      text = "The tool failed: " + e.getMessage();
      isError = true;
    }
    if (!tool.readOnly()) {
      auditLog.add(tool, mapper.writeValueAsString(arguments), text, isError, args.getBoolean("dryRun", false));
    }
    return result(id, toolResult(text, isError));
  }

  private static Map<String, Object> toolResult(String text, boolean isError) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("content", List.of(Map.of("type", "text", "text", text)));
    result.put("isError", isError);
    return result;
  }

  private static Map<String, Object> result(Object id, Object result) {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("jsonrpc", "2.0");
    response.put("id", id);
    response.put("result", result);
    return response;
  }

  private static Map<String, Object> error(Object id, int code, String message) {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("jsonrpc", "2.0");
    response.put("id", id);
    response.put("error", Map.of("code", code, "message", message));
    return response;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> castMap(Map<?, ?> map) {
    return (Map<String, Object>) map;
  }

  private ResponseEntity<String> json(HttpStatus status, Object value) {
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(value));
  }

  private static ResponseEntity<String> accepted() {
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }
}
