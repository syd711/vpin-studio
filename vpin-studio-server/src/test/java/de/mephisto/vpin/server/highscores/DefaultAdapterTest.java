package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.highscores.parsing.listadapters.DefaultAdapter;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultAdapterTest{

  @Test
  public void testParsing() {
    DefaultAdapter adapter = new DefaultAdapter();
    doTest(adapter, "#1  JOE            250.000.000", 1, "JOE", 250000000);
    doTest(adapter, "#3 IND        30.000.000", 3, "IND", 30000000);
    doTest(adapter, "#3 I N        30.000.000", 3, "I N", 30000000);
    doTest(adapter, "#10 MT             110.000.000", 10, "MT ", 110000000);

    doTest(adapter, "1) RA    6.500.000", 1, "RA ", 6500000);
    doTest(adapter, "2) P G    3.500.000", 2, "P G", 3500000);
    doTest(adapter, "3) XAQ    161.000.000", 3, "XAQ", 161000000);
    doTest(adapter, "4) TEX    16.000.000", 4, "TEX", 16000000);

    doTest(adapter, "1) TEX 16", 1, "TEX", 16);
    doTest(adapter, "#1 DAD   267", 1, "DAD", 267);

    doTest(adapter, "1)       4?000?000", 1, "???", 4000000);

    doTest(adapter, "#1 ???   1.000.000", 1, "???", 1000000);
  }

  private void doTest(DefaultAdapter adapter, String input, int index, String initials, long score) {
    Date d = new Date();

    String[] seps = { ".", ",", "?", " ", ""};
    for (String sep : seps) {
      String line = input.replace(".", sep);

      assertTrue(adapter.isScoreLine(line, index));
      Score s = adapter.createScore(d, line, "Test '" + sep + "'", -1);
      assertEquals(initials, s.getPlayerInitials());
      assertEquals(score, s.getScore());
    }
  }
}
