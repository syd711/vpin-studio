package de.mephisto.vpin.server.achievements.scoreserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Minimal WebSocket server for tests: accepts one client and sends the queued text messages, like the score-server
 * plugin does (unmasked text frames).
 */
public class TestScoreServer implements AutoCloseable {
  private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
  private static final String STOP = "\u0000stop";

  private final ServerSocket server;
  private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
  private final Thread thread;

  public TestScoreServer() throws IOException {
    server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
    thread = new Thread(this::run, "Test Score Server");
    thread.setDaemon(true);
    thread.start();
  }

  public URI getUri() {
    return URI.create("ws://127.0.0.1:" + server.getLocalPort());
  }

  public void send(String json) {
    messages.add(json);
  }

  private void run() {
    try (Socket socket = server.accept()) {
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
      String key = null;
      String line;
      while ((line = in.readLine()) != null && !line.isEmpty()) {
        if (line.toLowerCase().startsWith("sec-websocket-key:")) {
          key = line.substring(line.indexOf(':') + 1).trim();
        }
      }
      String accept = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1")
          .digest((key + GUID).getBytes(StandardCharsets.ISO_8859_1)));
      OutputStream out = socket.getOutputStream();
      out.write(("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n"
          + "Sec-WebSocket-Accept: " + accept + "\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1));
      out.flush();
      while (true) {
        String message = messages.poll(30, TimeUnit.SECONDS);
        if (message == null || STOP.equals(message)) {
          break;
        }
        out.write(frame(message.getBytes(StandardCharsets.UTF_8)));
        out.flush();
      }
    }
    catch (Exception e) {
      // closed
    }
  }

  private static byte[] frame(byte[] payload) {
    int n = payload.length;
    int header = n < 126 ? 2 : n < 65536 ? 4 : 10;
    byte[] frame = new byte[header + n];
    frame[0] = (byte) 0x81;
    if (n < 126) {
      frame[1] = (byte) n;
    }
    else if (n < 65536) {
      frame[1] = 126;
      frame[2] = (byte) (n >> 8);
      frame[3] = (byte) n;
    }
    else {
      frame[1] = 127;
      for (int i = 0; i < 8; i++) {
        frame[2 + i] = (byte) ((long) n >> (56 - 8 * i));
      }
    }
    System.arraycopy(payload, 0, frame, header, n);
    return frame;
  }

  @Override
  public void close() throws IOException {
    messages.add(STOP);
    server.close();
  }
}
