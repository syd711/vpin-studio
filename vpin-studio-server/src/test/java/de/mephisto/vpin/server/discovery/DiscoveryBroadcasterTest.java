package de.mephisto.vpin.server.discovery;

import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class DiscoveryBroadcasterTest {

  @Mock
  private PreferencesService preferencesService;

  @InjectMocks
  private DiscoveryBroadcaster discoveryBroadcaster;

  // ---- stop ----

  @Test
  void stop_doesNotThrow_whenNeverStarted() {
    // thread field is null on a fresh instance
    assertDoesNotThrow(() -> discoveryBroadcaster.stop());
  }

  // ---- start ----

  @Test
  void start_doesNotThrow_andCanBeStoppedCleanly() {
    // The thread calls preferencesService.getPreferenceValue asynchronously.
    // stop() sets shouldRun=false; the thread ends after at most one wait cycle.
    assertDoesNotThrow(() -> discoveryBroadcaster.start());
    assertDoesNotThrow(() -> discoveryBroadcaster.stop()); // cleanly joins the thread
  }

  @Test
  void start_isIdempotent_whenCalledTwice() {
    assertDoesNotThrow(() -> {
      discoveryBroadcaster.start();
      discoveryBroadcaster.start(); // second call is no-op (thread != null check)
      discoveryBroadcaster.stop();
    });
  }

  @Test
  void stop_afterStop_doesNotThrow() {
    // stop on an already-stopped broadcaster is safe
    discoveryBroadcaster.stop();
    discoveryBroadcaster.stop();
  }
}
