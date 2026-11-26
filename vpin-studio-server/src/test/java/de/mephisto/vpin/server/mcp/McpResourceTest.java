package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mcp.McpSettings;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class McpResourceTest {

  @Mock
  private PreferencesService preferencesService;

  @Mock
  private SystemService systemService;

  @Mock
  private McpTableTools tableTools;

  @Mock
  private McpManagementTools managementTools;

  private final List<String> deleted = new ArrayList<>();

  @InjectMocks
  private McpResource resource;

  private final McpSettings settings = new McpSettings();

  @BeforeEach
  void setUp() {
    settings.setEnabled(true);
    when(preferencesService.getJsonPreference(PreferenceNames.MCP_SETTINGS, McpSettings.class)).thenReturn(settings);
    when(systemService.getVersion()).thenReturn("1.0.0");
    Map<String, Object> idSchema = McpTableTools.schema(Map.of("id", McpTableTools.prop("integer", "id")), List.of("id"));
    when(tableTools.getTools()).thenReturn(List.of(
        McpTool.read("echo", "Echo", "Echoes the id", idSchema, args -> Map.of("id", args.requireInt("id")))));
    when(managementTools.getTools()).thenReturn(List.of(
        McpTool.delete("remove", "Remove", "Removes the id", idSchema, args -> {
          deleted.add(String.valueOf(args.requireInt("id")));
          return Map.of("deleted", true);
        })));
  }

  @Test
  void enabledEndpointNeedsNoAuthentication() {
    assertThat(post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}").getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void disabledEndpointIsHidden() {
    settings.setEnabled(false);
    assertThat(post(headers(), "{}").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void rejectsBrowserOrigins() {
    HttpHeaders headers = headers();
    headers.setOrigin("http://evil.example");
    assertThat(post(headers, "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}").getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void initializeNegotiatesVersion() {
    ResponseEntity<String> response = post(headers(),
        "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2025-03-26\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test\",\"version\":\"1\"}}}");
    assertThat(response.getBody()).contains("\"protocolVersion\":\"2025-03-26\"").contains("\"tools\"").contains("\"name\":\"vpin-studio\"");

    response = post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"1999-01-01\"}}");
    assertThat(response.getBody()).contains("\"protocolVersion\":\"" + McpResource.LATEST_PROTOCOL_VERSION + "\"");
  }

  @Test
  void notificationsAreAccepted() {
    ResponseEntity<String> response = post(headers(), "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(response.getBody()).isNull();
  }

  @Test
  void listsTools() {
    String body = post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/list\"}").getBody();
    assertThat(body).contains("\"name\":\"echo\"").contains("\"readOnlyHint\":true").contains("\"required\":[\"id\"]");
  }

  @Test
  void callsTool() {
    String body = post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":4,\"method\":\"tools/call\",\"params\":{\"name\":\"echo\",\"arguments\":{\"id\":42}}}").getBody();
    assertThat(body).contains("\"id\":4").contains("{\\\"id\\\":42}").contains("\"isError\":false");
  }

  @Test
  void toolErrorsAreReportedAsToolResults() {
    String body = post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":5,\"method\":\"tools/call\",\"params\":{\"name\":\"echo\",\"arguments\":{}}}").getBody();
    assertThat(body).contains("Missing argument").contains("\"isError\":true");
  }

  @Test
  void unknownToolAndMethodAreProtocolErrors() {
    assertThat(post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":6,\"method\":\"tools/call\",\"params\":{\"name\":\"nope\"}}").getBody()).contains("-32602");
    assertThat(post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":7,\"method\":\"resources/list\"}").getBody()).contains("-32601");
  }

  @Test
  void invalidJsonIsParseError() {
    ResponseEntity<String> response = post(headers(), "{not json");
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).contains("-32700");
  }

  @Test
  void batchesAreAnswered() {
    String body = post(headers(), "[{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"},{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}]").getBody();
    assertThat(body).startsWith("[").contains("\"id\":1");
  }

  @Test
  void readOnlyModeHidesAndBlocksWriteTools() {
    String list = post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}").getBody();
    assertThat(list).contains("\"echo\"").doesNotContain("\"remove\"");

    String call = post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/call\",\"params\":{\"name\":\"remove\",\"arguments\":{\"id\":1}}}").getBody();
    assertThat(call).contains("read-only mode").contains("\"isError\":true");
    assertThat(deleted).isEmpty();

    String init = post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"initialize\",\"params\":{}}").getBody();
    assertThat(init).contains("read-only mode");
  }

  @Test
  void writeModeOffersWriteTools() {
    settings.setReadOnly(false);
    String list = post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}").getBody();
    assertThat(list).contains("\"echo\"").contains("\"remove\"").contains("\"destructiveHint\":true");

    String call = post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/call\",\"params\":{\"name\":\"remove\",\"arguments\":{\"id\":7}}}").getBody();
    assertThat(call).contains("\"isError\":false");
    assertThat(deleted).containsExactly("7");

    String init = post(headers(), "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"initialize\",\"params\":{}}").getBody();
    assertThat(init).contains("ask the user for confirmation");
  }

  @Test
  void readOnlyIsTheDefault() {
    assertThat(new McpSettings().isReadOnly()).isTrue();
  }

  private ResponseEntity<String> post(HttpHeaders headers, String body) {
    return resource.post(headers, body.getBytes(StandardCharsets.UTF_8));
  }

  private static HttpHeaders headers() {
    return new HttpHeaders();
  }
}
