package de.mephisto.vpin.server.system;

import de.mephisto.vpin.server.util.SystemUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SystemUtilTest {

  @Test
  public void testSerialNumber() {
    String boardSerialNumber = SystemUtil.getBoardSerialNumber();
    assertFalse(StringUtils.isEmpty(boardSerialNumber));
  }
}
