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
}
