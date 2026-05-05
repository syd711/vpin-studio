package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.vpx.TableScriptOption;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VpxScriptOptionsServiceTest {

  @Mock
  private GameService gameService;
  @Mock
  private VPXService vpxService;

  @InjectMocks
  private VpxScriptOptionsService service;

  @TempDir
  Path tempDir;

  private static final String SCRIPT_TWO_OPTIONS =
      "Dim Ball\n" +
      "Ball.Option(\"PhysicsMultiplier\", 0.0, 3.0, 0.5, 1.0, 0, Array(\"Low\", \"Normal\", \"High\"))\n" +
      "Ball.Option(\"TableSounds\", 0.0, 1.0, 1.0, 1.0, 1)\n";

  // ---- parseOptions ----

  @Test
  void parseOptions_parsesCorrectCount() {
    List<TableScriptOption> opts = service.parseOptions(SCRIPT_TWO_OPTIONS);
    assertThat(opts).hasSize(2);
  }

  @Test
  void parseOptions_setsCorrectNumericValues() {
    List<TableScriptOption> opts = service.parseOptions(SCRIPT_TWO_OPTIONS);
    TableScriptOption opt = opts.stream()
        .filter(o -> o.getName().equals("PhysicsMultiplier"))
        .findFirst().orElseThrow();

    assertThat(opt.getMinValue()).isEqualTo(0.0);
    assertThat(opt.getMaxValue()).isEqualTo(3.0);
    assertThat(opt.getStep()).isEqualTo(0.5);
    assertThat(opt.getDefaultValue()).isEqualTo(1.0);
    assertThat(opt.getUnit()).isEqualTo(0);
  }

  @Test
  void parseOptions_populatesLiteralOptionsArray() {
    List<TableScriptOption> opts = service.parseOptions(SCRIPT_TWO_OPTIONS);
    TableScriptOption opt = opts.stream()
        .filter(o -> o.getName().equals("PhysicsMultiplier"))
        .findFirst().orElseThrow();

    assertThat(opt.getLiteralOptions()).containsExactly("Low", "Normal", "High");
  }

  @Test
  void parseOptions_optionWithNoArrayHasEmptyLiterals() {
    List<TableScriptOption> opts = service.parseOptions(SCRIPT_TWO_OPTIONS);
    TableScriptOption opt = opts.stream()
        .filter(o -> o.getName().equals("TableSounds"))
        .findFirst().orElseThrow();

    assertThat(opt.getLiteralOptions()).isEmpty();
    assertThat(opt.getUnit()).isEqualTo(1);
  }

  @Test
  void parseOptions_deduplicatesCaseInsensitive() {
    String script = "Ball.Option(\"MyOpt\", 0, 1, 1, 0, 0)\n" +
        "Ball.Option(\"myopt\", 0, 2, 1, 0, 0)\n";

    List<TableScriptOption> opts = service.parseOptions(script);

    assertThat(opts).hasSize(1);
  }

  @Test
  void parseOptions_returnsSortedByName() {
    String script =
        "Ball.Option(\"Zebra\", 0, 1, 1, 0, 0)\n" +
        "Ball.Option(\"Alpha\", 0, 1, 1, 0, 0)\n" +
        "Ball.Option(\"Middle\", 0, 1, 1, 0, 0)\n";

    List<TableScriptOption> opts = service.parseOptions(script);

    assertThat(opts).extracting(TableScriptOption::getName)
        .containsExactly("Alpha", "Middle", "Zebra");
  }

  @Test
  void parseOptions_returnsEmptyListForBlankScript() {
    assertThat(service.parseOptions("")).isEmpty();
    assertThat(service.parseOptions("   \n  ")).isEmpty();
  }

  @Test
  void parseOptions_defaultValuePopulatedAsCurrentValue() {
    String script = "Ball.Option(\"Volume\", 0.0, 1.0, 0.1, 0.7, 0)\n";

    List<TableScriptOption> opts = service.parseOptions(script);

    assertThat(opts).hasSize(1);
    assertThat(opts.get(0).getCurrentValue()).isEqualTo(0.7);
  }

  // ---- getOptions ----

  @Test
  void getOptions_returnsEmptyListForUnknownGame() {
    when(gameService.getGame(99)).thenReturn(null);

    assertThat(service.getOptions(99)).isEmpty();
  }

  @Test
  void getOptions_returnsEmptyListWhenGameFileDoesNotExist() {
    Game game = mock(Game.class);
    File nonExistent = new File(tempDir.toFile(), "missing.vpx");
    when(gameService.getGame(1)).thenReturn(game);
    when(game.getGameFile()).thenReturn(nonExistent);

    assertThat(service.getOptions(1)).isEmpty();
  }

  // ---- saveOptions ----

  @Test
  void saveOptions_returnsFalseForUnknownGame() {
    when(gameService.getGame(99)).thenReturn(null);

    assertThat(service.saveOptions(99, List.of())).isFalse();
  }
}
