package de.mephisto.vpin.server.util;

import de.mephisto.vpin.restclient.util.DateUtil;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateUtilTest {

  @Test
  public void testToday() {
    Date today = DateUtil.today();
    System.out.println(today.getTime());
    assertTrue(today.getMinutes() == 0);
  }
}
