package de.mephisto.vpin.server.achievements.scoreserver;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Client of the score-server VPX plugin (VPX 10.8.1+), which broadcasts JSON messages on ws://127.0.0.1:3131
 * (port of the Watcher's {@code ScoreServerClient}):
 * <ul>
 *   <li>{@code game_start}: the table (ROM) was loaded</li>
 *   <li>{@code round_start}: a pinball game starts (VPin Studio build of the plugin)</li>
 *   <li>{@code current_scores}: players, current player, ball, scores</li>
 *   <li>{@code nvram_snapshot}: the live NVRAM (VPin Studio build of the plugin)</li>
 *   <li>{@code game_end}: a pinball game ended ({@code reason: game_over}) or the table was closed
 *   ({@code reason: plugin_unload})</li>
 * </ul>
 * The client reconnects every few seconds while it is started, the plugin only runs while VPX plays a table.
 */
public class ScoreServerClient {
  private static final Logger LOG = LoggerFactory.getLogger(ScoreServerClient.class);

  public static final URI DEFAULT_URI = URI.create("ws://127.0.0.1:3131");
  private static final long RECONNECT_MS = 3000;
  /** a snapshot that arrives shortly after game_end still belongs to the game */
  private static final long LATE_SNAPSHOT_MS = 5000;

  public interface Listener {
    void roundStarted(@NonNull ScoreServerGame game);

    void snapshot(@NonNull ScoreServerGame game, ScoreServerGame.@NonNull Snapshot snapshot);

    void roundEnded(@NonNull ScoreServerGame game);
  }

  private final URI uri;
  private final Listener listener;
  private final ObjectMapper mapper = JsonMapper.builder().build();
  private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

  private volatile boolean running;
  private volatile boolean connected;
  private Thread thread;

  private final Object lock = new Object();
  private final List<ScoreServerGame> games = new ArrayList<>();
  private ScoreServerGame current;
  /** the plugin sends round_start (VPin Studio build) */
  private boolean roundProtocol;

  public ScoreServerClient(@NonNull Listener listener) {
    this(DEFAULT_URI, listener);
  }

  public ScoreServerClient(@NonNull URI uri, @NonNull Listener listener) {
    this.uri = uri;
    this.listener = listener;
  }

  public synchronized void start() {
    if (running) {
      return;
    }
    running = true;
    thread = new Thread(this::run, "Score Server Client");
    thread.setDaemon(true);
    thread.start();
  }

  public synchronized void stop() {
    running = false;
    if (thread != null) {
      thread.interrupt();
      thread = null;
    }
  }

  public boolean isRunning() {
    return running;
  }

  public boolean isConnected() {
    return connected;
  }

  public boolean isRoundProtocol() {
    synchronized (lock) {
      return roundProtocol;
    }
  }

  /**
   * @return the games that were running at or started after the given time
   */
  @NonNull
  public List<ScoreServerGame> getGamesSince(@NonNull Instant since) {
    synchronized (lock) {
      List<ScoreServerGame> result = new ArrayList<>();
      for (ScoreServerGame g : games) {
        Instant ended = g.getEnded();
        if (ended == null || !ended.isBefore(since)) {
          result.add(g);
        }
      }
      return result;
    }
  }

  /**
   * Forgets finished games (end of a table session).
   */
  public void clearFinishedGames() {
    synchronized (lock) {
      games.removeIf(g -> !g.isRunning());
    }
  }

  //------------------------------------------- Connection ------------------------------------------------------------

  private void run() {
    while (running) {
      WebSocket ws = null;
      CompletableFuture<Void> closed = new CompletableFuture<>();
      try {
        ws = http.newWebSocketBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .buildAsync(uri, new SocketListener(closed))
            .get(5, TimeUnit.SECONDS);
        connected = true;
        LOG.info("Connected to the score-server plugin");
        while (running && !closed.isDone()) {
          try {
            closed.get(1, TimeUnit.SECONDS);
          }
          catch (java.util.concurrent.TimeoutException e) {
            // keep waiting
          }
        }
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
      catch (Exception e) {
        // plugin not running (no table played), try again later
        LOG.debug("No connection to the score-server plugin: {}", e.toString());
      }
      finally {
        if (connected) {
          LOG.info("Connection to the score-server plugin closed");
        }
        connected = false;
        if (ws != null) {
          ws.abort();
        }
      }
      try {
        Thread.sleep(RECONNECT_MS);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    connected = false;
  }

  private class SocketListener implements WebSocket.Listener {
    private final CompletableFuture<Void> closed;
    private final StringBuilder text = new StringBuilder();

    SocketListener(CompletableFuture<Void> closed) {
      this.closed = closed;
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
      text.append(data);
      if (last) {
        String message = text.toString();
        text.setLength(0);
        try {
          handle(message);
        }
        catch (Exception e) {
          LOG.warn("Failed to handle score-server message: {}", e.getMessage());
        }
      }
      webSocket.request(1);
      return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
      closed.complete(null);
      return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
      closed.complete(null);
    }
  }

  //------------------------------------------- Messages --------------------------------------------------------------

  /**
   * Handles one message of the plugin. Package visible for tests.
   */
  void handle(@NonNull String message) {
    JsonNode msg = mapper.readTree(message);
    if (msg == null || !msg.isObject()) {
      return;
    }
    handle(msg, Instant.now());
  }

  void handle(@NonNull JsonNode msg, @NonNull Instant now) {
    String type = msg.path("type").asString("");
    String rom = msg.path("rom").asString("");
    ScoreServerGame startedGame = null;
    ScoreServerGame endedGame = null;
    ScoreServerGame snapshotGame = null;
    ScoreServerGame.Snapshot snapshot = null;

    synchronized (lock) {
      switch (type) {
        case "game_start" -> {
          // table loaded, a pinball game starts with round_start (or the first live scores of older plugin builds)
          if (current != null && current.isRunning()) {
            current.finish(now, null, null);
          }
          current = null;
        }
        case "round_start" -> {
          roundProtocol = true;
          if (current != null && current.isRunning()) {
            current.finish(now, null, null); // the end of the previous game was missed
          }
          current = new ScoreServerGame(rom, now, true);
          games.add(current);
          startedGame = current;
        }
        case "nvram_snapshot" -> {
          byte[] data = decode(msg.path("data").asString(""));
          ScoreServerGame g = current;
          if (data.length > 0 && g != null) {
            Instant ended = g.getEnded();
            if (ended == null || now.toEpochMilli() - ended.toEpochMilli() < LATE_SNAPSHOT_MS) {
              snapshot = new ScoreServerGame.Snapshot(now, toInt(msg.get("current_player")), toInt(msg.get("current_ball")),
                  toInt(msg.get("players")), data);
              g.addSnapshot(snapshot);
              if (ended == null) {
                snapshotGame = g;
              }
            }
          }
        }
        case "current_scores" -> {
          if (current == null || !current.isRunning()) {
            if (toInt(msg.get("current_ball")) <= 0) {
              return; // attract mode
            }
            if (roundProtocol && current != null) {
              return; // game over: the next game starts with round_start
            }
            // joined during a game, or a plugin build without round_start
            current = new ScoreServerGame(rom, now, false);
            games.add(current);
          }
          int players = toInt(msg.get("players"));
          current.update(players > 0 ? players : 1, toInt(msg.get("current_player")), toInt(msg.get("current_ball")), scores(msg));
        }
        case "game_end" -> {
          if (current != null && current.isRunning()) {
            String reason = msg.path("reason").asString("");
            current.finish(now, scores(msg), reason);
            if ("game_over".equals(reason)) {
              endedGame = current;
            }
          }
        }
        default -> {
          // high_scores, table_loaded, badge: not used
        }
      }
      // keep the list short, only the games of the current table session are needed
      if (games.size() > 50) {
        games.subList(0, games.size() - 50).clear();
      }
    }

    if (startedGame != null) {
      listener.roundStarted(startedGame);
    }
    if (snapshotGame != null) {
      listener.snapshot(snapshotGame, snapshot);
    }
    if (endedGame != null) {
      listener.roundEnded(endedGame);
    }
  }

  @NonNull
  private static Map<Integer, Long> scores(@NonNull JsonNode msg) {
    Map<Integer, Long> result = new HashMap<>();
    JsonNode scores = msg.get("scores");
    if (scores != null && scores.isArray()) {
      int i = 0;
      for (JsonNode e : scores) {
        i++;
        if (e.isObject()) {
          result.put(playerIndex(e, i), toLong(e.get("score")));
        }
      }
    }
    return result;
  }

  /**
   * "Player 2" -> 2
   */
  private static int playerIndex(@NonNull JsonNode entry, int fallback) {
    String digits = entry.path("player").asString("").replaceAll("\\D", "");
    if (!digits.isEmpty()) {
      try {
        return Integer.parseInt(digits);
      }
      catch (NumberFormatException e) {
        // fallback
      }
    }
    return fallback;
  }

  static int toInt(@Nullable JsonNode v) {
    return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, toLong(v)));
  }

  static long toLong(@Nullable JsonNode v) {
    if (v == null || v.isNull()) {
      return 0;
    }
    if (v.isNumber()) {
      return v.asLong();
    }
    String s = v.asString("").trim().replace(",", "").replace(".", "").replace(" ", "");
    try {
      return s.isEmpty() ? 0 : Long.parseLong(s);
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }

  private static byte[] decode(@NonNull String base64) {
    try {
      return Base64.getMimeDecoder().decode(base64);
    }
    catch (IllegalArgumentException e) {
      return new byte[0];
    }
  }
}
