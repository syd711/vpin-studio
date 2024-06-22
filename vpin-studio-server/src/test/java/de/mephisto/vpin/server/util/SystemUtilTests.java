package de.mephisto.vpin.server.util;

import de.mephisto.vpin.server.vpx.VPXUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
  public void testAllWindowNames() {
        List<ProcessHandle> filteredProcesses = ProcessHandle.allProcesses()
            .filter(p -> p.info().command().isPresent() && (p.info().command().get().contains("Pinball")))
            .collect(Collectors.toList());
    for (ProcessHandle process : filteredProcesses) {
      System.out.println(process.info());
      List<ProcessHandle> collect = process.children().collect(Collectors.toList());
      for (ProcessHandle processHandle : collect) {
        System.out.println(processHandle.info());
      }

    }
  }

  @Test
  public void testKill() {
    ProcessHandle.allProcesses().forEach(p -> {
      if (p.info().command().isPresent()) {
        System.out.println(p.info().command().get());
      }

    });


  }
}


