package de.mephisto.vpin.server.games;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BackglassNamingHelperTest {

  @TempDir
  Path tempDir;

  @Test
  void backglassNamedAfterTableFileIsPreferred() throws Exception {
    File folder = tempDir.resolve("Twister (1996)").toFile();
    Game game = tableInFolder(folder, "Twister v1.1.vpx");
    create(folder, "Twister v1.1.directb2s");
    create(folder, "Twister (1996).directb2s");

    assertThat(BackglassNamingHelper.getBackglassFileName(game)).isEqualTo("Twister v1.1.directb2s");
    assertThat(BackglassNamingHelper.getBackglassFile(game)).isEqualTo(new File(folder, "Twister v1.1.directb2s"));
  }

  @Test
  void backglassNamedAfterFolderIsFoundAsFallback() throws Exception {
    File folder = tempDir.resolve("Twister (1996)").toFile();
    Game game = tableInFolder(folder, "Twister v1.1.vpx");
    create(folder, "Twister (1996).directb2s");

    assertThat(BackglassNamingHelper.getBackglassFileName(game)).isEqualTo("Twister (1996).directb2s");
    assertThat(BackglassNamingHelper.getBackglassFile(game)).isEqualTo(new File(folder, "Twister (1996).directb2s"));
  }

  @Test
  void missingBackglassKeepsTheNameOfTheTableFile() throws Exception {
    File folder = tempDir.resolve("Twister (1996)").toFile();
    Game game = tableInFolder(folder, "Twister v1.1.vpx");

    assertThat(BackglassNamingHelper.getBackglassFileName(game)).isEqualTo("Twister v1.1.directb2s");
    assertThat(BackglassNamingHelper.getBackglassFile(game)).isEqualTo(new File(folder, "Twister v1.1.directb2s"));
  }

  @Test
  void b2sProBackglassIsFound() throws Exception {
    File folder = tempDir.resolve("Twister (1996)").toFile();
    Game game = tableInFolder(folder, "Twister v1.1.vpx");
    create(folder, "Twister v1.1.B2SPro");

    assertThat(BackglassNamingHelper.getBackglassFileName(game)).isEqualTo("Twister v1.1.B2SPro");
    assertThat(BackglassNamingHelper.getBackglassFile(game)).isEqualTo(new File(folder, "Twister v1.1.B2SPro"));
  }

  @Test
  void b2sProBackglassIsPreferredOverDirectB2S() throws Exception {
    File folder = tempDir.resolve("Twister (1996)").toFile();
    Game game = tableInFolder(folder, "Twister v1.1.vpx");
    create(folder, "Twister v1.1.directb2s");
    create(folder, "Twister v1.1.B2SPro");

    assertThat(BackglassNamingHelper.getBackglassFileName(game)).isEqualTo("Twister v1.1.B2SPro");
    assertThat(BackglassNamingHelper.getSiblingBackglassFiles(new File(folder, "Twister v1.1.B2SPro")))
        .containsExactly(new File(folder, "Twister v1.1.directb2s"));
  }

  @Test
  void b2sProBackglassNamedAfterFolderIsFoundAsFallback() throws Exception {
    File folder = tempDir.resolve("Twister (1996)").toFile();
    Game game = tableInFolder(folder, "Twister v1.1.vpx");
    create(folder, "Twister (1996).B2SPro");

    assertThat(BackglassNamingHelper.getBackglassFileName(game)).isEqualTo("Twister (1996).B2SPro");
  }

  @Test
  void uniqueBackglassFileChecksAllTypes() throws Exception {
    File folder = tempDir.resolve("Twister (1996)").toFile();
    folder.mkdirs();
    create(folder, "Twister.directb2s");
    create(folder, "Twister (1).B2SPro");

    assertThat(BackglassNamingHelper.uniqueBackglassFile(new File(folder, "Twister.B2SPro")))
        .isEqualTo(new File(folder, "Twister (2).B2SPro"));
    assertThat(BackglassNamingHelper.uniqueBackglassFile(new File(folder, "Other.B2SPro")))
        .isEqualTo(new File(folder, "Other.B2SPro"));
  }

  private Game tableInFolder(File folder, String tableFileName) {
    folder.mkdirs();
    Game game = mock(Game.class);
    when(game.getGameFileName()).thenReturn(tableFileName);
    when(game.getGameFile()).thenReturn(new File(folder, tableFileName));
    return game;
  }

  private void create(File folder, String name) throws Exception {
    new File(folder, name).createNewFile();
  }
}
