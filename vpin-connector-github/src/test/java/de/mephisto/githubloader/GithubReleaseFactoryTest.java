package de.mephisto.githubloader;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import de.mephisto.vpin.connectors.github.ReleaseArtifact;
import de.mephisto.vpin.connectors.github.ReleaseArtifactActionLog;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GithubReleaseFactoryTest {

  @BeforeAll
  public static void before() {
    new File("./test/").mkdirs();
  }

  @Test
  public void testMameDownload() throws Exception {
    GithubRelease githubRelease = GithubReleaseFactory.loadRelease("https://github.com/vpinball/pinmame/releases", Arrays.asList("win-", "VPinMAME"), Arrays.asList("linux", "sc-", "osx"));
    assertNotNull(githubRelease);

    ReleaseArtifact artifact = githubRelease.getArtifacts().get(0);
    ReleaseArtifactActionLog install = artifact.install(new File("./test/"), Arrays.asList("VPMAlias.txt"), Collections.emptyList());
    assertNotNull(install);
    assertFalse(install.getLogs().isEmpty());
    assertNull(install.getStatus());
  }

  @Test
  public void testMameDownloadSimulated() throws Exception {
    GithubRelease githubRelease = GithubReleaseFactory.loadRelease("https://github.com/vpinball/pinmame/releases", Arrays.asList("win-", "VPinMAME"), Arrays.asList("linux", "sc-", "osx"));
    assertNotNull(githubRelease);

    ReleaseArtifact artifact = githubRelease.getArtifacts().get(0);
    ReleaseArtifactActionLog install = artifact.simulateInstall(new File("./test/"), Arrays.asList("VPMAlias.txt"), Collections.emptyList());
    assertNotNull(install);
    assertFalse(install.getLogs().isEmpty());
    assertNull(install.getStatus());
  }

//  @Test
  public void testMameDiff() throws Exception {
    GithubRelease githubRelease = GithubReleaseFactory.loadRelease("https://github.com/vpinball/pinmame/releases", Arrays.asList("win-", "VPinMAME"), Arrays.asList("linux", "sc-", "osx"));
    assertNotNull(githubRelease);

    ReleaseArtifact artifact = githubRelease.getArtifacts().get(0);
    ReleaseArtifactActionLog install = artifact.diff(new File("./test/", artifact.getName()), new File("./test/"), Arrays.asList("VPMAlias.txt"), Collections.emptyList(), "*.dll");
    assertNotNull(install);
    assertFalse(install.getLogs().isEmpty());
    assertFalse(install.getDiffEntries().isEmpty());
    assertNull(install.getStatus());
  }


  @Test
  public void testVpx() throws Exception {
    GithubRelease githubRelease = GithubReleaseFactory.loadRelease("https://github.com/vpinball/vpinball/releases", Collections.emptyList(), Arrays.asList("Debug"));
    assertNotNull(githubRelease);

    List<ReleaseArtifact> artifacts = githubRelease.getArtifacts();
    for (ReleaseArtifact artifact : artifacts) {
      System.out.println(artifact.getName());
    }


    ReleaseArtifact artifact = githubRelease.getArtifacts().get(0);
    ReleaseArtifactActionLog install = artifact.install(new File("./test/"), Arrays.asList("VPinballX64.exe", "VPinballX.exe", "VPinballX_GL.exe"), Collections.emptyList());
    assertNotNull(install);
    assertNull(install.getStatus());
    assertFalse(install.getLogs().isEmpty());
  }


  @Test
  public void testVpx2() throws Exception {
    List<GithubRelease> githubReleases = GithubReleaseFactory.loadReleases("https://github.com/vpinball/vpinball/releases", Collections.emptyList(), Arrays.asList("Debug"));
    for (GithubRelease githubRelease : githubReleases) {
      List<ReleaseArtifact> artifacts = githubRelease.getArtifacts();
      for (ReleaseArtifact artifact : artifacts) {
        System.out.println(artifact);
      }

    }

    assertNotEquals(githubReleases, 1);

    ReleaseArtifact artifact = githubReleases.get(0).getArtifacts().get(0);
    ReleaseArtifactActionLog install = artifact.install(new File("./test/"), Arrays.asList("VPinballX64.exe", "VPinballX.exe", "VPinballX_GL.exe"), Collections.emptyList());
    assertNotNull(install);
    assertNull(install.getStatus());
    assertFalse(install.getLogs().isEmpty());
  }


  @Test
  public void testVpxDiff() throws Exception {
    GithubRelease githubRelease = GithubReleaseFactory.loadRelease("https://github.com/vpinball/vpinball/releases", Collections.emptyList(), Arrays.asList("Debug"));
    assertNotNull(githubRelease);

    ReleaseArtifact artifact = githubRelease.getArtifacts().get(0);
    ReleaseArtifactActionLog install = artifact.diff(new File("./test/", artifact.getName()), new File("./test/"), Arrays.asList("VPinballX64.exe", "VPinballX.exe", "VPinballX_GL.exe"), Collections.emptyList());
    assertNotNull(install);
    assertNull(install.getStatus());
    assertFalse(install.getDiffEntries().isEmpty());

  }

  @Test
  public void testBackglass() throws Exception {
    GithubRelease githubRelease = GithubReleaseFactory.loadRelease("https://github.com/vpinball/b2s-backglass/releases", Collections.emptyList(), Arrays.asList("Source"));
    assertNotNull(githubRelease);

    ReleaseArtifact artifact = githubRelease.getArtifacts().get(0);
    ReleaseArtifactActionLog install = artifact.install(new File("./test/"), Arrays.asList("README.txt"), Collections.emptyList());
    assertNotNull(install);
    assertNull(install.getStatus());
    assertFalse(install.getLogs().isEmpty());
  }

  @Test
  public void testSerum() throws Exception {
    GithubRelease githubRelease = GithubReleaseFactory.loadRelease("https://github.com/zesinger/libserum/releases", Collections.emptyList(), Arrays.asList("Source", "tvos", "macOS", "macos", "linux", "arm", "android"));
    assertNotNull(githubRelease);

    ReleaseArtifact artifact = githubRelease.getArtifacts().get(0);
    ReleaseArtifactActionLog install = artifact.install(new File("./test/"), Arrays.asList("serum64.dll", "serum.dll"), Collections.emptyList());
    assertNotNull(install);
    assertNull(install.getStatus());
    assertFalse(install.getLogs().isEmpty());
  }

  @Test
  public void testFlexDMD() throws Exception {
    GithubRelease githubRelease = GithubReleaseFactory.loadRelease("https://github.com/vbousquet/flexdmd/releases", Collections.emptyList(), Arrays.asList("Source"));
    assertNotNull(githubRelease);

    ReleaseArtifact artifact = githubRelease.getArtifacts().get(0);
    ReleaseArtifactActionLog install = artifact.install(new File("./test/"), Arrays.asList("FlexDMDUI.exe"), Collections.emptyList());
    assertNotNull(install);
    assertNull(install.getStatus());
    assertFalse(install.getLogs().isEmpty());
  }

  @Test
  public void testFreezy() throws Exception {
    GithubRelease githubRelease = GithubReleaseFactory.loadRelease("https://github.com/freezy/dmd-extensions/releases", Collections.emptyList(), Arrays.asList("Source", ".msi"));
    assertNotNull(githubRelease);

    ReleaseArtifact artifact = githubRelease.getArtifacts().get(0);
    ReleaseArtifactActionLog install = artifact.install(new File("./test/"), Arrays.asList("DmdDevice.ini"), Arrays.asList("DmdDevice.log.config", "DmdDevice.ini", "dmdext.log.config"));
    assertNotNull(install);
    assertNull(install.getStatus());
    assertFalse(install.getLogs().isEmpty());
  }
}
