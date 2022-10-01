package de.mephisto.vpin.server.dof;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class DOFCommandDataTest {

  @Test
  public void testDataWrite() {
    DOFCommand cmd = new DOFCommand(1, 1, 1, 1, 1, Trigger.TableStart, "1", false, "");
    DOFCommandData dofCommandData = new DOFCommandData();
    dofCommandData.addDOFCommand(cmd);

    DOFCommandData data = DOFCommandData.create();
    assertFalse(data.getCommands().isEmpty());
  }

}
