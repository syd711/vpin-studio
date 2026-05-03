package de.mephisto.vpin.server.altsound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class AltSoundBackupServiceTest {

  private final AltSoundBackupService service = new AltSoundBackupService();

  @TempDir
  Path tempDir;

  @Test
  void synchronizeBackup_createsBackupWhenOriginalExists() throws IOException {
    Files.writeString(tempDir.resolve("altsound.csv"), "csv data");

    service.synchronizeBackup(tempDir.toFile());

    assertThat(tempDir.resolve("altsound.csv.bak").toFile()).exists();
  }

  @Test
  void synchronizeBackup_doesNotOverwriteExistingBackup() throws IOException {
    Files.writeString(tempDir.resolve("altsound.csv"), "new content");
    Files.writeString(tempDir.resolve("altsound.csv.bak"), "original backup");

    service.synchronizeBackup(tempDir.toFile());

    assertThat(Files.readString(tempDir.resolve("altsound.csv.bak"))).isEqualTo("original backup");
  }

  @Test
  void synchronizeBackup_skipsNonExistentOriginals() {
    service.synchronizeBackup(tempDir.toFile());

    assertThat(tempDir.resolve("altsound.csv.bak").toFile()).doesNotExist();
    assertThat(tempDir.resolve("g-sound.csv.bak").toFile()).doesNotExist();
    assertThat(tempDir.resolve("altsound.ini.bak").toFile()).doesNotExist();
  }

  @Test
  void synchronizeBackup_backupsAllThreeFiles() throws IOException {
    Files.writeString(tempDir.resolve("altsound.csv"), "csv");
    Files.writeString(tempDir.resolve("g-sound.csv"), "gsound");
    Files.writeString(tempDir.resolve("altsound.ini"), "ini");

    service.synchronizeBackup(tempDir.toFile());

    assertThat(tempDir.resolve("altsound.csv.bak").toFile()).exists();
    assertThat(tempDir.resolve("g-sound.csv.bak").toFile()).exists();
    assertThat(tempDir.resolve("altsound.ini.bak").toFile()).exists();
  }

  @Test
  void restore_copiesBackupOverMissingOriginal() throws IOException {
    Files.writeString(tempDir.resolve("altsound.csv.bak"), "backup content");

    service.restore(tempDir.toFile());

    assertThat(Files.readString(tempDir.resolve("altsound.csv"))).isEqualTo("backup content");
  }

  @Test
  void restore_replacesExistingOriginalWithBackup() throws IOException {
    Files.writeString(tempDir.resolve("altsound.csv"), "current");
    Files.writeString(tempDir.resolve("altsound.csv.bak"), "backup");

    service.restore(tempDir.toFile());

    assertThat(Files.readString(tempDir.resolve("altsound.csv"))).isEqualTo("backup");
  }

  @Test
  void restore_skipsWhenNoBackupExists() throws IOException {
    Files.writeString(tempDir.resolve("altsound.csv"), "current");

    service.restore(tempDir.toFile());

    assertThat(Files.readString(tempDir.resolve("altsound.csv"))).isEqualTo("current");
  }

  @Test
  void restore_restoresAllThreeFiles() throws IOException {
    Files.writeString(tempDir.resolve("altsound.csv.bak"), "csv bak");
    Files.writeString(tempDir.resolve("g-sound.csv.bak"), "gsound bak");
    Files.writeString(tempDir.resolve("altsound.ini.bak"), "ini bak");

    service.restore(tempDir.toFile());

    assertThat(Files.readString(tempDir.resolve("altsound.csv"))).isEqualTo("csv bak");
    assertThat(Files.readString(tempDir.resolve("g-sound.csv"))).isEqualTo("gsound bak");
    assertThat(Files.readString(tempDir.resolve("altsound.ini"))).isEqualTo("ini bak");
  }
}
