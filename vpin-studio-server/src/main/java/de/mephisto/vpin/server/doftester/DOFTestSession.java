package de.mephisto.vpin.server.doftester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A single long-running "dof-test.ps1" PowerShell process wrapping one DirectOutput COM session.
 * The DOF framework's Finish() call turns off every output on the physical controller (LedWiz/PacLed/etc.),
 * not just the one that was tested, so Init()/Finish() must bracket a whole test session instead of a single toy.
 */
class DOFTestSession {
  private static final Logger LOG = LoggerFactory.getLogger(DOFTestSession.class);

  private static final long STARTUP_TIMEOUT_MS = 15_000;
  private static final long COMMAND_TIMEOUT_MS = 10_000;
  private static final long SHUTDOWN_TIMEOUT_MS = 2_000;

  private final Process process;
  private final String romName;
  private final BufferedWriter stdin;
  private final BlockingQueue<String> stdoutLines = new LinkedBlockingQueue<>();

  private DOFTestSession(Process process, String romName, BufferedWriter stdin) {
    this.process = process;
    this.romName = romName;
    this.stdin = stdin;
  }

  static DOFTestSession start(String powershell, File scriptFile, File dllFile, String romName) throws IOException {
    List<String> cmd = Arrays.asList(
        powershell, "-ExecutionPolicy", "Bypass", "-File", scriptFile.getAbsolutePath(),
        "-DllPath", dllFile.getAbsolutePath(),
        "-RomName", romName
    );
    Process process = new ProcessBuilder(cmd).start();
    BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
    DOFTestSession session = new DOFTestSession(process, romName, stdin);
    session.startReaderThread(process.getInputStream(), session.stdoutLines);
    session.startStderrLoggerThread(process.getErrorStream());

    String ready;
    try {
      ready = session.stdoutLines.poll(STARTUP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      session.close();
      throw new IOException("Interrupted while waiting for DOF test session to start", e);
    }
    if (ready == null) {
      session.close();
      throw new IOException("Timed out waiting for DOF test session to start for ROM '" + romName + "'");
    }
    if (!"READY".equals(ready)) {
      session.close();
      throw new IOException("DOF test session failed to start for ROM '" + romName + "': " + ready);
    }
    return session;
  }

  private void startReaderThread(InputStream in, BlockingQueue<String> target) {
    Thread t = new Thread(() -> {
      try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name())) {
        while (scanner.hasNextLine()) {
          target.offer(scanner.nextLine());
        }
      }
    }, "dof-test-session-stdout");
    t.setDaemon(true);
    t.start();
  }

  private void startStderrLoggerThread(InputStream err) {
    Thread t = new Thread(() -> {
      try (Scanner scanner = new Scanner(err, StandardCharsets.UTF_8.name())) {
        while (scanner.hasNextLine()) {
          LOG.warn("DOF test session stderr: {}", scanner.nextLine());
        }
      }
    }, "dof-test-session-stderr");
    t.setDaemon(true);
    t.start();
  }

  boolean isAlive() {
    return process.isAlive();
  }

  boolean matches(String romName) {
    return Objects.equals(this.romName, romName);
  }

  String getRomName() {
    return romName;
  }

  synchronized boolean fire(DOFEventCode code, int durationMs) {
    if (!isAlive()) {
      LOG.warn("DOF test session for ROM '{}' is no longer alive", romName);
      return false;
    }
    try {
      stdin.write(code.getType() + "," + code.getNumber() + "," + durationMs);
      stdin.newLine();
      stdin.flush();
    }
    catch (IOException e) {
      LOG.error("Failed to send DOF test command {} to session for ROM '{}': {}", code, romName, e.getMessage(), e);
      return false;
    }

    String response;
    try {
      response = stdoutLines.poll(COMMAND_TIMEOUT_MS + durationMs, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
    if (response == null) {
      LOG.warn("Timed out waiting for DOF test session response for {} (ROM '{}')", code, romName);
      return false;
    }
    if (!"OK".equals(response)) {
      LOG.warn("DOF test session reported error for {} (ROM '{}'): {}", code, romName, response);
      return false;
    }
    return true;
  }

  void close() {
    try {
      stdin.write("EXIT");
      stdin.newLine();
      stdin.flush();
    }
    catch (IOException e) {
      // process may already be gone, fall through to forced termination below
    }
    try {
      stdin.close();
    }
    catch (IOException e) {
      // ignore
    }
    try {
      if (!process.waitFor(SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
        process.destroyForcibly();
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      process.destroyForcibly();
    }
    LOG.info("Closed DOF test session for ROM '{}'", romName);
  }
}
