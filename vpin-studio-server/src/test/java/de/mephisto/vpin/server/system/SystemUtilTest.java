package de.mephisto.vpin.server.system;

import de.mephisto.vpin.server.util.SystemUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SystemUtilTest {

  @Test
  public void testWindowNames() {
    List<String> allWindowNames = SystemUtil.getAllWindowNames();
    for (String allWindowName : allWindowNames) {
      System.out.println(allWindowName);
    }
    assertFalse(allWindowNames.isEmpty());
  }
}
