package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.BotCommand;
import de.mephisto.vpin.connectors.discord.BotCommandResponse;
import de.mephisto.vpin.connectors.discord.DiscordCommandResolver;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.parsing.HighscoreParsingService;
import de.mephisto.vpin.server.players.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiscordBotResponseServiceTest {

  @Mock
  private CompetitionService competitionService;
  @Mock
  private HighscoreService highscoreService;
  @Mock
  private GameService gameService;
  @Mock
  private PlayerService playerService;
  @Mock
  private DiscordService discordService;
  @Mock
  private HighscoreParsingService highscoreParser;

  @InjectMocks
  private DiscordBotResponseService service;

  private BotCommand commandFor(String raw) {
    DiscordCommandResolver resolver = mock(DiscordCommandResolver.class);
    return new BotCommand(1L, "/" + raw, resolver);
  }

  // ---- onBotCommand — CMD_COMMANDS ----

  @Test
  void onBotCommand_returnsCommandSummary_forCommandsCmd() {
    BotCommand cmd = commandFor(BotCommand.CMD_COMMANDS);

    BotCommandResponse response = service.onBotCommand(cmd);

    assertNotNull(response);
    assertNotNull(response.toDiscordMarkup());
    assertTrue(response.toDiscordMarkup().contains("commands"));
  }

  // ---- onBotCommand — CMD_HELP ----

  @Test
  void onBotCommand_returnsCommandSummary_forHelpCmd() {
    BotCommand cmd = commandFor(BotCommand.CMD_HELP);

    BotCommandResponse response = service.onBotCommand(cmd);

    assertNotNull(response);
    assertNotNull(response.toDiscordMarkup());
  }

  // ---- onBotCommand — CMD_COMPETITIONS (empty) ----

  @Test
  void onBotCommand_returnsNoCompetitions_whenNoneActive() {
    when(competitionService.getActiveCompetitions()).thenReturn(Collections.emptyList());
    BotCommand cmd = commandFor(BotCommand.CMD_COMPETITIONS);

    BotCommandResponse response = service.onBotCommand(cmd);

    assertNotNull(response);
    assertTrue(response.toDiscordMarkup().contains("No active competitions"));
  }

  // ---- onBotCommand — CMD_HS without parameter ----

  @Test
  void onBotCommand_returnsMissingParam_forHsCmdWithNoParameter() {
    BotCommand cmd = commandFor(BotCommand.CMD_HS);

    BotCommandResponse response = service.onBotCommand(cmd);

    assertNotNull(response);
    assertTrue(response.toDiscordMarkup().contains("Missing search parameter"));
  }

  // ---- onBotCommand — CMD_FIND without parameter ----

  @Test
  void onBotCommand_returnsMissingParam_forFindCmdWithNoParameter() {
    BotCommand cmd = commandFor(BotCommand.CMD_FIND);

    BotCommandResponse response = service.onBotCommand(cmd);

    assertNotNull(response);
    assertTrue(response.toDiscordMarkup().contains("Missing search parameter"));
  }

  // ---- onBotCommand — unknown command ----

  @Test
  void onBotCommand_returnsUnknownCommand_forUnrecognisedCmd() {
    BotCommand cmd = commandFor("unknowncmd");

    BotCommandResponse response = service.onBotCommand(cmd);

    assertNotNull(response);
    assertTrue(response.toDiscordMarkup().contains("Unknown bot command"));
  }
}
