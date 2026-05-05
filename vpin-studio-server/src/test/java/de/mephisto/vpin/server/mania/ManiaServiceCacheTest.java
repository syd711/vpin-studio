package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ManiaServiceCacheTest {

  @Mock
  private GameService gameService;

  @Mock
  private PlayerService playerService;

  @InjectMocks
  private ManiaServiceCache cache;

  // ---- clear ----

  @Test
  void clear_returnsTrue() {
    assertTrue(cache.clear());
  }

  // ---- getGame ----

  @Test
  void getGame_returnsNull_whenCacheIsEmpty() {
    assertNull(cache.getGame("vps-table-1"));
  }

  // ---- containsAccountForInitials ----

  @Test
  void containsAccountForInitials_returnsFalse_whenCacheIsEmpty() {
    assertFalse(cache.containsAccountForInitials("AAA"));
  }

  // ---- getCachedPlayerAccounts ----

  @Test
  void getCachedPlayerAccounts_returnsEmptyList_whenCacheIsEmpty() {
    List<Account> accounts = cache.getCachedPlayerAccounts();

    assertNotNull(accounts);
    assertTrue(accounts.isEmpty());
  }

  // ---- setManiaService ----

  @Test
  void setManiaService_doesNotThrow() {
    assertDoesNotThrow(() -> cache.setManiaService(null));
  }

  // ---- clear resets state ----

  @Test
  void clear_returnsTrueRepeatedly() {
    assertTrue(cache.clear());
    assertTrue(cache.clear());
  }
}
