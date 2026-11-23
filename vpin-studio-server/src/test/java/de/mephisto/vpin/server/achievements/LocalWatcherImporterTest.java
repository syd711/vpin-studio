package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.server.achievements.migration.LocalWatcherImporter;
import de.mephisto.vpin.server.achievements.migration.WatcherData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The fixture was written by the Python Achievement Watcher's save_json (signed).
 */
public class LocalWatcherImporterTest {

  static final File INSTALL = new File("src/test/resources/achievements/watcher-install");

  @Test
  void readsSignedWatcherInstallation() {
    WatcherData data = new LocalWatcherImporter().read(INSTALL);
    assertThat(data).isNotNull();
    assertThat(data.signatureValid).isTrue();
    assertThat(data.pid).isEqualTo("ab12");
    assertThat(data.recoveryKey).hasSize(32);
    assertThat(data.entries).extracting(WatcherData.Entry::title).containsExactlyInAnyOrder(
        "Global – Games Started: 50 Total", "Bally Rookie",
        "afm_113b – Ramps Made: 5", "afm_113b – Play 5 Minutes", "tz_94h – Play 10 Minutes");
    assertThat(data.entries.stream().filter(e -> e.title().contains("Ramps")).findFirst().get().vpsId()).isEqualTo("abc123XYZ");
    assertThat(data.entries.stream().filter(e -> e.title().startsWith("Global")).findFirst().get().rom()).isNull();
    assertThat(data.romsPlayed).containsExactly("afm_113b", "tz_94h", "mm_109c");
    assertThat(data.badges).containsExactly("first_steps", "night_owl");
    assertThat(data.selectedBadge).isEqualTo("night_owl");
    assertThat(data.tally).containsEntry("Global – Games Started: 100 Total", 61L);
    assertThat(data.cloudUploadDone).isTrue();
    assertThat(data.ruleFiles).hasSize(1);
    assertThat(data.uniqueCount()).isEqualTo(5);
  }

  @Test
  void detectsModifiedFile(@TempDir Path tmp) throws Exception {
    Path achievements = Files.createDirectories(tmp.resolve("Achievements"));
    String json = Files.readString(new File(INSTALL, "Achievements/achievements_state.json").toPath(), StandardCharsets.UTF_8);
    Files.writeString(achievements.resolve("achievements_state.json"), json.replace("\"cloud_upload_done\": true", "\"cloud_upload_done\": false"), StandardCharsets.UTF_8);
    WatcherData data = new LocalWatcherImporter().read(tmp.toFile());
    assertThat(data).isNotNull();
    assertThat(data.cloudUploadDone).isFalse();
    assertThat(data.signatureValid).isFalse();
  }
}
