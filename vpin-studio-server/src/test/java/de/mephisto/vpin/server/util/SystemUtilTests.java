package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.vpx.VPXUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotEquals;


public class SystemUtilTests {

  @Test
  public void testChecksum() {
    File table = new File("../testsystem/vPinball/VisualPinball/Tables/Baseball (1970).vpx");
    String sum1 = VPXUtil.getChecksum(table);

    table = new File("../testsystem/vPinball/VisualPinball/Tables/Baseball2 (1970).vpx");
    String sum2 = VPXUtil.getChecksum(table);

    assertNotEquals(sum1, sum2);
  }

  @Test
  public void testProcesses() {
    List<ProcessHandle> allProcesses = ProcessHandle.allProcesses().collect(Collectors.toList());
    for (ProcessHandle p : allProcesses) {
      if (p.info().command().isPresent()) {
        String cmdName = p.info().command().get();
        System.out.println(cmdName);
      }
    }

  }
}


