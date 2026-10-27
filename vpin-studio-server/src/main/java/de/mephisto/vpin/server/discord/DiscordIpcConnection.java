package de.mephisto.vpin.server.discord;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Minimal implementation of the Discord RPC protocol on top of the local named pipe of the Discord client.
 * Every frame consists of an 8 byte little endian header (opcode + payload length) followed by a JSON payload.
 * See https://github.com/discord/discord-rpc/blob/master/documentation/hard-mode.md
 */
class DiscordIpcConnection implements Closeable {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordIpcConnection.class);
  private final static ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

  private final static String PIPE_PREFIX = "\\\\.\\pipe\\discord-ipc-";
  private final static int MAX_PIPE_INDEX = 10;

  private final static int OP_HANDSHAKE = 0;
  private final static int OP_FRAME = 1;
  private final static int OP_CLOSE = 2;

  private final RandomAccessFile pipe;

  private DiscordIpcConnection(RandomAccessFile pipe) {
    this.pipe = pipe;
  }

  /**
   * Connects to the first available Discord IPC pipe and performs the handshake.
   */
  static DiscordIpcConnection connect(String applicationId) throws IOException {
    for (int i = 0; i < MAX_PIPE_INDEX; i++) {
      String pipeName = PIPE_PREFIX + i;
      RandomAccessFile pipe;
      try {
        pipe = new RandomAccessFile(pipeName, "rw");
      }
      catch (FileNotFoundException e) {
        continue;
      }

      DiscordIpcConnection connection = new DiscordIpcConnection(pipe);
      try {
        Map<String, Object> handshake = new LinkedHashMap<>();
        handshake.put("v", 1);
        handshake.put("client_id", applicationId);
        connection.write(OP_HANDSHAKE, handshake);
        connection.readResponse();
        LOG.info("Connected to Discord IPC pipe \"{}\"", pipeName);
        return connection;
      }
      catch (IOException e) {
        connection.close();
        LOG.warn("Discord IPC handshake on \"{}\" failed: {}", pipeName, e.getMessage());
      }
    }
    throw new IOException("No Discord IPC pipe found, is the Discord client running?");
  }

  /**
   * Sets the rich presence activity, null clears it.
   */
  void setActivity(@Nullable Map<String, Object> activity) throws IOException {
    Map<String, Object> args = new LinkedHashMap<>();
    args.put("pid", ProcessHandle.current().pid());
    args.put("activity", activity);

    Map<String, Object> command = new LinkedHashMap<>();
    command.put("cmd", "SET_ACTIVITY");
    command.put("args", args);
    command.put("nonce", UUID.randomUUID().toString());

    write(OP_FRAME, command);
    readResponse();
  }

  private void write(int opcode, Map<String, Object> payload) throws IOException {
    byte[] json = OBJECT_MAPPER.writeValueAsBytes(payload);
    ByteBuffer buffer = ByteBuffer.allocate(8 + json.length).order(ByteOrder.LITTLE_ENDIAN);
    buffer.putInt(opcode);
    buffer.putInt(json.length);
    buffer.put(json);
    pipe.write(buffer.array());
  }

  private JsonNode readResponse() throws IOException {
    byte[] header = new byte[8];
    pipe.readFully(header);
    ByteBuffer headerBuffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
    int opcode = headerBuffer.getInt();
    int length = headerBuffer.getInt();

    byte[] payload = new byte[length];
    pipe.readFully(payload);
    JsonNode response = OBJECT_MAPPER.readTree(payload);

    if (opcode == OP_CLOSE) {
      throw new IOException("Discord closed the connection: " + response);
    }
    if ("ERROR".equals(response.path("evt").asString(null))) {
      throw new IOException("Discord returned an error: " + response.path("data"));
    }
    return response;
  }

  @Override
  public void close() {
    try {
      pipe.close();
    }
    catch (IOException e) {
      LOG.warn("Failed to close Discord IPC pipe: {}", e.getMessage());
    }
  }
}
