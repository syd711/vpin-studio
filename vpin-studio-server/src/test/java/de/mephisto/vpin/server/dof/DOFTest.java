package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.server.util.WindowsShortcut;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class DOFTest {

  @Test
  public void testDof() throws Exception {
    File dofConfigFolder = new File("C:\\vPinball\\visualpinball\\Tables\\Plugins64", "DirectOutputx64.lnk");
    boolean potentialValidLink = WindowsShortcut.isPotentialValidLink(dofConfigFolder);
    System.out.println(potentialValidLink);

    String realFilename = new WindowsShortcut(dofConfigFolder).getRealFilename();
    System.out.println(realFilename);
  }
}
