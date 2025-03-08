package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.server.util.WindowsShortcut;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

public class DOFTest {

  @Test
  public void testDof() throws Exception {
    File dofConfigFolder = new File("../testsystem/vPinball/visualpinball/Tables/Plugins64", "DirectOutputx64.lnk");
    boolean potentialValidLink = WindowsShortcut.isPotentialValidLink(dofConfigFolder);
    System.out.println(potentialValidLink);

    String realFilename = new WindowsShortcut(dofConfigFolder).getRealFilename();
    System.out.println(realFilename);
  }

  @Test
  public void testSync() {
    DOFSettings settings = new DOFSettings();
    // toaster is Doffy Duck / D0ffyDuck
    settings.setApiKey("KTmvUlykEgDqW6VgXxfpwTLdTu6ieiB7");
    settings.setInstallationPath("../testsystem/vPinball/dof64");
    DOFSynchronizationJob sync = new DOFSynchronizationJob(settings, "../resources/");

    // run sync job
    JobDescriptor job = new JobDescriptor();
    sync.execute(job);

    // check job completion
    assertEquals(1.0, job.getProgress());
    assertFalse(job.isErrorneous());
  }
}
