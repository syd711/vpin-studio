package de.mephisto.vpin.server.vpx;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class VPXToolCommandTest {

  private final File resources = new File("resources");
  private final File table = new File("tables/Twister (1996)/Twister (1996).vpx");

  @Test
  void windows_runsThroughCmdWithQuotedPath() {
    assertThat(VPXUtil.vpxToolCommand("extractvbs", table, resources, true))
        .containsExactly("vpxtool.exe", "extractvbs", "\"" + table.getAbsolutePath() + "\"");
  }

  @Test
  void standalone_runsAbsoluteBinaryWithPlainPath() {
    assertThat(VPXUtil.vpxToolCommand("importvbs", table, resources, false))
        .containsExactly(new File(resources, "vpxtool").getAbsolutePath(), "importvbs", table.getAbsolutePath());
  }

  @Test
  void windows_multipleArguments_quotesOnlyFiles() {
    File out = new File("temp/patched.vpx");
    assertThat(VPXUtil.vpxToolCommand(resources, true, "assemble", "-f", table, out))
        .containsExactly("vpxtool.exe", "assemble", "-f", "\"" + table.getAbsolutePath() + "\"", "\"" + out.getAbsolutePath() + "\"");
  }

  @Test
  void standalone_multipleArguments_plainPaths() {
    assertThat(VPXUtil.vpxToolCommand(resources, false, "extract", "-f", table))
        .containsExactly(new File(resources, "vpxtool").getAbsolutePath(), "extract", "-f", table.getAbsolutePath());
  }
}
