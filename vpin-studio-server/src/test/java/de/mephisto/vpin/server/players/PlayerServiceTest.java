package de.mephisto.vpin.server.players;

import de.mephisto.vpin.restclient.players.PlayerDomain;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetRepository;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.highscores.Score;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

  @Mock
  private PlayerRepository playerRepository;

  @Mock
  private DiscordService discordService;

  @Mock
  private AssetRepository assetRepository;

  @InjectMocks
  private PlayerService playerService;

  // ---- getPlayerForInitials ----

  @Test
  void getPlayerForInitials_returnsNull_whenInitialsIsNull() {
    assertNull(playerService.getPlayerForInitials(-1, null));
  }

  @Test
  void getPlayerForInitials_returnsNull_whenInitialsIsBlank() {
    assertNull(playerService.getPlayerForInitials(-1, ""));
  }

  @Test
  void getPlayerForInitials_returnsPlayerFromRepo_whenFoundByInitials() {
    Player player = new Player();
    player.setInitials("MTF");
    player.setName("Matthias");
    when(playerRepository.findByInitials("MTF")).thenReturn(new ArrayList<>(List.of(player)));

    Player result = playerService.getPlayerForInitials(-1, "mtf"); // lowercase input uppercased internally

    assertNotNull(result);
    assertEquals("MTF", result.getInitials());
  }

  @Test
  void getPlayerForInitials_returnsPlayerFromDiscord_whenNotInRepo() {
    when(playerRepository.findByInitials("DCO")).thenReturn(Collections.emptyList());
    Player discordPlayer = new Player();
    discordPlayer.setInitials("DCO");
    when(discordService.getPlayerByInitials(-1, "DCO")).thenReturn(discordPlayer);

    Player result = playerService.getPlayerForInitials(-1, "DCO");

    assertNotNull(result);
    assertEquals("DCO", result.getInitials());
  }

  @Test
  void getPlayerForInitials_returnsNull_whenNotInRepoOrDiscord() {
    when(playerRepository.findByInitials("XYZ")).thenReturn(Collections.emptyList());
    when(discordService.getPlayerByInitials(-1, "XYZ")).thenReturn(null);

    assertNull(playerService.getPlayerForInitials(-1, "XYZ"));
  }

  // ---- getAdminPlayer ----

  @Test
  void getAdminPlayer_returnsAdminPlayer_whenOneExists() {
    Player admin = new Player();
    admin.setInitials("ADM");
    admin.setAdministrative(true);

    Player regular = new Player();
    regular.setInitials("REG");
    regular.setAdministrative(false);

    when(playerRepository.findAll()).thenReturn(new ArrayList<>(List.of(regular, admin)));
    when(discordService.isEnabled()).thenReturn(false);

    Player result = playerService.getAdminPlayer();

    assertNotNull(result);
    assertTrue(result.isAdministrative());
  }

  @Test
  void getAdminPlayer_returnsNull_whenNoAdminExists() {
    Player regular = new Player();
    regular.setInitials("REG");
    regular.setAdministrative(false);

    when(playerRepository.findAll()).thenReturn(new ArrayList<>(List.of(regular)));
    when(discordService.isEnabled()).thenReturn(false);

    assertNull(playerService.getAdminPlayer());
  }

  // ---- validateInitials ----

  @Test
  void validateInitials_substitutesAdminInitials_whenScoreHasQuestionMarks() {
    Player admin = new Player();
    admin.setInitials("ADM");
    admin.setAdministrative(true);
    when(playerRepository.findAll()).thenReturn(new ArrayList<>(List.of(admin)));
    when(discordService.isEnabled()).thenReturn(false);

    Score score = mock(Score.class);
    when(score.getPlayerInitials()).thenReturn("???");
    when(score.getScore()).thenReturn(1000L);

    playerService.validateInitials(score);

    verify(score).setPlayerInitials("ADM");
  }

  @Test
  void validateInitials_doesNotSubstitute_whenScoreIsZero() {
    Player admin = new Player();
    admin.setInitials("ADM");
    admin.setAdministrative(true);
    when(playerRepository.findAll()).thenReturn(new ArrayList<>(List.of(admin)));
    when(discordService.isEnabled()).thenReturn(false);

    Score score = mock(Score.class);
    when(score.getPlayerInitials()).thenReturn("???");
    // default return for long is 0 — no stub needed

    playerService.validateInitials(score);

    verify(score, never()).setPlayerInitials(any());
  }

  @Test
  void validateInitials_doesNotSubstitute_whenInitialsAreAlreadySet() {
    when(playerRepository.findAll()).thenReturn(new ArrayList<>());
    when(discordService.isEnabled()).thenReturn(false);

    Score score = mock(Score.class);
    when(score.getPlayerInitials()).thenReturn("ABC");

    playerService.validateInitials(score);

    verify(score, never()).setPlayerInitials(any());
  }

  // ---- getPlayersForDomain ----

  @Test
  void getPlayersForDomain_returnsEmptyList_whenDiscordDisabled() {
    when(discordService.isEnabled()).thenReturn(false);

    List<Player> result = playerService.getPlayersForDomain(PlayerDomain.DISCORD);

    assertTrue(result.isEmpty());
  }

  @Test
  void getPlayersForDomain_returnsPlayers_whenDiscordEnabled() {
    Player discordPlayer = new Player();
    discordPlayer.setName("DiscordUser");
    when(discordService.isEnabled()).thenReturn(true);
    when(discordService.getPlayers()).thenReturn(new ArrayList<>(List.of(discordPlayer)));

    List<Player> result = playerService.getPlayersForDomain(PlayerDomain.DISCORD);

    assertEquals(1, result.size());
    assertEquals("DiscordUser", result.get(0).getName());
  }

  // ---- delete ----

  @Test
  void delete_removesPlayerAndAvatar_whenPlayerExists() {
    Player player = new Player();
    player.setInitials("DEL");

    Asset avatar = mock(Asset.class);
    player.setAvatar(avatar);

    when(playerRepository.findById(42L)).thenReturn(Optional.of(player));

    playerService.delete(42L);

    verify(assetRepository).delete(avatar);
    verify(playerRepository).deleteById(42L);
  }

  @Test
  void delete_doesNothing_whenPlayerNotFound() {
    when(playerRepository.findById(99L)).thenReturn(Optional.empty());

    playerService.delete(99L);

    verify(playerRepository, never()).deleteById(anyLong());
  }
}
