package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.highscores.parsing.listadapters.DefaultAdapter;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultAdapterTest{

  @Test
  public void testParsing() {
    DefaultAdapter adapter = new DefaultAdapter(new ScoringDB());
    doTest(adapter, "#1  JOE            250.000.000", 1, "JOE", 250000000);
    doTest(adapter, "#3 IND        30.000.000", 3, "IND", 30000000);
    doTest(adapter, "#3 I N        30.000.000", 3, "I N", 30000000);
    doTest(adapter, "#10 MT             110.000.000", 10, "MT ", 110000000);

    doTest(adapter, "1) RA        161.000", 1, "RA ", 161000);
    doTest(adapter, "2) P G     1.610.000", 2, "P G", 1610000);
    doTest(adapter, "3) X      16.100.000", 3, "X  ", 16100000);
    doTest(adapter, "4) TEX   161.000.000", 4, "TEX", 161000000);

    doTest(adapter, "1) TEX 16", 1, "TEX", 16);
    doTest(adapter, "1# DAD   267", 1, "DAD", 267);

    doTest(adapter, "1)       4.000.000", 1, "???", 4000000);

    doTest(adapter, "#1 ???   1.000.000", 1, "???", 1000000);
  }

  private void doTest(DefaultAdapter adapter, String input, int index, String initials, long score) {
    Instant d = Instant.now();

    String[] seps = { ".", ",", "?", " ", ""};
    for (String sep : seps) {
      String line = input.replace(".", sep);

      assertTrue(DefaultAdapter.isScoreLine(line));
      Score s = adapter.createScore(d, "TEST", line, "Test '" + sep + "'", -1);
      assertEquals(initials, s.getPlayerInitials());
      assertEquals(score, s.getScore());
      assertEquals(index, s.getPosition());
    }
  }
}
