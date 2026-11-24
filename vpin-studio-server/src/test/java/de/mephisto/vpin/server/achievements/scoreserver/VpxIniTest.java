package de.mephisto.vpin.server.achievements.scoreserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class VpxIniTest {

  @Test
  public void addsMissingSection() {
    String ini = "[Player]\r\nBallSize = 50\r\n";
    String result = VpxIni.setPluginEnabled(ini, "ScoreServer", true);
    assertThat(result).isEqualTo("[Player]\r\nBallSize = 50\r\n\r\n[Plugin.ScoreServer]\r\nEnable = 1\r\n");
    assertThat(VpxIni.isPluginEnabled(result, "ScoreServer")).isTrue();
    assertThat(VpxIni.isPluginEnabled(result, "PinMAME")).isFalse();
  }

  @Test
  public void changesExistingValueOnly() {
    String ini = "; comment\n[Plugin.PinMAME]\nEnable = 0\nPath = x\n[Player]\nEnable = 0\n";
    String result = VpxIni.setPluginEnabled(ini, "PinMAME", true);
    assertThat(result).isEqualTo("; comment\n[Plugin.PinMAME]\nEnable = 1\nPath = x\n[Player]\nEnable = 0\n");
    assertThat(VpxIni.setPluginEnabled(result, "PinMAME", true)).isSameAs(result);
    assertThat(VpxIni.setPluginEnabled(result, "pinmame", false)).isEqualTo(ini);
  }

  @Test
  public void addsKeyToSectionWithoutEnable() {
    String ini = "[plugin.scoreserver]\nMachineId = cab\n";
    assertThat(VpxIni.setPluginEnabled(ini, "ScoreServer", true)).isEqualTo("[plugin.scoreserver]\nEnable = 1\nMachineId = cab\n");
    // disabling something that is not there changes nothing
    assertThat(VpxIni.setPluginEnabled(ini, "ScoreServer", false)).isSameAs(ini);
    assertThat(VpxIni.setPluginEnabled("[Player]\n", "ScoreServer", false)).isEqualTo("[Player]\n");
  }

  @Test
  public void writesFileWithBackupAndKeepsBom(@TempDir Path tmp) throws Exception {
    File ini = tmp.resolve("VPinballX.ini").toFile();
    byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    byte[] body = "[Player]\r\nName = Jürgen\r\n".getBytes(StandardCharsets.UTF_8);
    byte[] content = new byte[bom.length + body.length];
    System.arraycopy(bom, 0, content, 0, 3);
    System.arraycopy(body, 0, content, 3, body.length);
    Files.write(ini.toPath(), content);

    assertThat(VpxIni.setPluginEnabled(ini, "ScoreServer", true)).isTrue();
    assertThat(VpxIni.setPluginEnabled(ini, "ScoreServer", true)).isFalse();
    assertThat(VpxIni.isPluginEnabled(ini, "ScoreServer")).isTrue();

    byte[] written = Files.readAllBytes(ini.toPath());
    assertThat(written[0]).isEqualTo(bom[0]);
    assertThat(new String(written, 3, written.length - 3, StandardCharsets.UTF_8))
        .isEqualTo("[Player]\r\nName = Jürgen\r\n\r\n[Plugin.ScoreServer]\r\nEnable = 1\r\n");
    assertThat(Files.readAllBytes(tmp.resolve("VPinballX.ini.studio.bak"))).isEqualTo(content);
  }

  @Test
  public void findsVersionFolderFirst(@TempDir Path tmp) throws Exception {
    File appData = tmp.resolve("appdata").toFile();
    File vpx = tmp.resolve("vpx").toFile();
    File base = new File(appData, "VPinballX");
    File v108 = new File(base, "10.8/VPinballX.ini");
    File legacy = new File(base, "VPinballX.ini");
    File alongExe = new File(vpx, "VPinballX.ini");
    for (File f : new File[]{v108, legacy, alongExe}) {
      f.getParentFile().mkdirs();
      Files.writeString(f.toPath(), "");
    }
    assertThat(VpxIni.find(appData, vpx, "10.8.1.5436")).isEqualTo(v108);
    assertThat(VpxIni.find(appData, vpx, null)).isEqualTo(v108);
    assertThat(VpxIni.find(appData, vpx, "10.9.0")).isEqualTo(alongExe);
    assertThat(VpxIni.find(appData, null, "10.9.0")).isEqualTo(legacy);
  }

  @Test
  public void comparesVersions() {
    assertThat(VpxIni.compareVersion("10.8.1.3788", "10.8.1")).isPositive();
    assertThat(VpxIni.compareVersion("10.8.0.2000", "10.8.1")).isNegative();
    assertThat(VpxIni.compareVersion("10.10.0", "10.8.1")).isPositive();
    assertThat(VpxIni.sameRevision("10.8.1.5436", "10.8.1")).isTrue();
    assertThat(VpxIni.sameRevision("10.8.2.1", "10.8.1")).isFalse();
    assertThat(VpxIni.sameRevision(null, "10.8.1")).isFalse();
  }

  @Test
  public void checksPluginBuildCompatibility() {
    // 10.8.1 beta build 3788 crashes with plugins built against build 5436
    assertThat(VpxIni.isPluginCompatible("10.8.1.3788", "10.8.1.5436")).isFalse();
    assertThat(VpxIni.isPluginCompatible("10.8.1.5436", "10.8.1.5436")).isTrue();
    assertThat(VpxIni.isPluginCompatible("10.8.1.5500", "10.8.1.5436")).isTrue();
    assertThat(VpxIni.isPluginCompatible("10.8.2.5500", "10.8.1.5436")).isFalse();
    // unknown plugin build: only an installation without build number is accepted
    assertThat(VpxIni.isPluginCompatible("10.8.1.3788", "10.8.1")).isFalse();
    assertThat(VpxIni.isPluginCompatible("10.8.1", "10.8.1")).isTrue();
    assertThat(VpxIni.isPluginCompatible("10.8.1", "10.8.1.5436")).isFalse();
    assertThat(VpxIni.isPluginCompatible(null, "10.8.1.5436")).isFalse();
  }
}
