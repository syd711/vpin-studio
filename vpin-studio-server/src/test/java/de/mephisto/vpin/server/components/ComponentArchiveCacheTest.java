package de.mephisto.vpin.server.components;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.ReleaseArtifact;
import de.mephisto.vpin.connectors.github.ReleaseArtifactActionLog;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.system.SystemService;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComponentArchiveCacheTest {

  @TempDir
  Path tempDir;

  private ComponentArchiveCache cache;
  private File componentFolder;

  /**
   * Artifact that "downloads" by writing a small file and counts the downloads.
   */
  static class CountingArtifact extends ReleaseArtifact {
    final AtomicInteger downloads = new AtomicInteger();

    CountingArtifact(GithubRelease release, String name) {
      super(release);
      setName(name);
      setUrl("https://example.invalid/" + name);
    }

    @Override
    public @NonNull File download(@NonNull File targetFile, @NonNull ReleaseArtifactActionLog log) throws Exception {
      downloads.incrementAndGet();
      targetFile.getParentFile().mkdirs();
      Files.writeString(targetFile.toPath(), "archive of " + getName());
      return targetFile;
    }
  }

  @BeforeEach
  void setUp() {
    componentFolder = tempDir.resolve("vpinmame").toFile();
    componentFolder.mkdirs();
    SystemService systemService = mock(SystemService.class);
    when(systemService.getComponentArchiveFolder(any())).thenReturn(componentFolder);
    cache = new ComponentArchiveCache();
    ReflectionTestUtils.setField(cache, "systemService", systemService);
  }

  private static GithubRelease release(String tag) {
    GithubRelease release = new GithubRelease();
    release.setTag(tag);
    return release;
  }

  @Test
  void downloadsAnArchiveOnlyOnce() throws Exception {
    GithubRelease release = release("v3.6.0");
    CountingArtifact artifact = new CountingArtifact(release, "VPinMAME-x64.zip");

    File first = cache.get(ComponentType.vpinmame, release, artifact, new ReleaseArtifactActionLog(false, true));
    ReleaseArtifactActionLog secondLog = new ReleaseArtifactActionLog(true, false);
    File second = cache.supplier(ComponentType.vpinmame, release, artifact).get(secondLog);

    assertThat(artifact.downloads).hasValue(1);
    assertThat(second).isEqualTo(first).isFile();
    assertThat(first.getParentFile().getName()).isEqualTo("v3.6.0");
    assertThat(secondLog.getLogs()).anyMatch(l -> l.contains("Using cached archive"));
  }

  @Test
  void sameArtifactNameOfAnotherReleaseIsDownloadedAgain() throws Exception {
    CountingArtifact old = new CountingArtifact(release("v3.5.0"), "VPinMAME.zip");
    CountingArtifact current = new CountingArtifact(release("v3.6.0"), "VPinMAME.zip");

    File a = cache.get(ComponentType.vpinmame, release("v3.5.0"), old, new ReleaseArtifactActionLog(false, true));
    File b = cache.get(ComponentType.vpinmame, release("v3.6.0"), current, new ReleaseArtifactActionLog(false, true));

    assertThat(a).isNotEqualTo(b);
    assertThat(old.downloads).hasValue(1);
    assertThat(current.downloads).hasValue(1);
  }

  @Test
  void invalidateForcesANewDownload() throws Exception {
    GithubRelease release = release("v3.6.0");
    CountingArtifact artifact = new CountingArtifact(release, "VPinMAME.zip");

    cache.get(ComponentType.vpinmame, release, artifact, new ReleaseArtifactActionLog(false, true));
    cache.invalidate(ComponentType.vpinmame, release, artifact);
    cache.get(ComponentType.vpinmame, release, artifact, new ReleaseArtifactActionLog(false, true));

    assertThat(artifact.downloads).hasValue(2);
  }

  @Test
  void keepsOnlyTheMostRecentReleasesAndRemovesLegacyArchives() throws Exception {
    // archive of the previous flat layout
    File legacy = new File(componentFolder, "VPinMAME.zip");
    Files.writeString(legacy.toPath(), "legacy");

    long now = System.currentTimeMillis();
    String[] tags = {"v1", "v2", "v3"};
    for (int i = 0; i < tags.length; i++) {
      GithubRelease release = release(tags[i]);
      File archive = cache.get(ComponentType.vpinmame, release, new CountingArtifact(release, "a.zip"), new ReleaseArtifactActionLog(false, true));
      // make the usage order explicit, the file system time resolution may be coarse
      archive.getParentFile().setLastModified(now - (tags.length - i) * 60_000L);
    }

    assertThat(legacy).doesNotExist();
    assertThat(new File(componentFolder, "v1")).doesNotExist();
    assertThat(new File(componentFolder, "v2")).isDirectory();
    assertThat(new File(componentFolder, "v3")).isDirectory();
    assertThat(componentFolder.listFiles(File::isDirectory)).hasSize(ComponentArchiveCache.KEEP_RELEASES);
  }

  @Test
  void safeNames() {
    assertThat(ComponentArchiveCache.toSafeName("v3.6.0-beta+1")).isEqualTo("v3.6.0-beta_1");
    assertThat(ComponentArchiveCache.toSafeName("../evil")).isEqualTo("__evil");
    assertThat(ComponentArchiveCache.toSafeName("rel/10.8")).isEqualTo("rel_10.8");
  }
}
