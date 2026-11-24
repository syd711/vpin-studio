package de.mephisto.vpin.server.achievements.scoreserver;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class VpxPluginInstallerReleasesTest {

  /**
   * Serves the GitHub release list from the given pages, records the requested URLs.
   */
  private static class StubInstaller extends VpxPluginInstaller {
    final List<String> requests = new ArrayList<>();
    Function<Integer, String> pages;

    StubInstaller(Function<Integer, String> pages) {
      this.pages = pages;
    }

    @Override
    byte[] get(String url) throws Exception {
      requests.add(url);
      int page = Integer.parseInt(url.replaceAll(".*[?&]page=(\\d+).*", "$1"));
      String json = pages.apply(page);
      if (json == null) {
        throw new IOException("HTTP 503 for " + url);
      }
      return json.getBytes(StandardCharsets.UTF_8);
    }
  }

  private static String release(String tag, String published) {
    return "{\"tag_name\":\"" + tag + "\",\"draft\":false,\"published_at\":\"" + published + "\",\"assets\":[]}";
  }

  /**
   * One page of n releases, the regular ones named "5.x.n", plus the given plugin releases.
   */
  private static String page(int regular, String... pluginReleases) {
    List<String> items = new ArrayList<>(List.of(pluginReleases));
    for (int i = 0; i < regular; i++) {
      items.add(release("5.7." + i, "2026-10-01T00:00:00Z"));
    }
    return "[" + String.join(",", items) + "]";
  }

  private static List<String> tags(List<JsonNode> releases) {
    return releases.stream().map(r -> r.path("tag_name").asString("")).toList();
  }

  @Test
  public void readsAllPagesUntilAShortOne() {
    Map<Integer, String> pages = Map.of(
        1, page(VpxPluginInstaller.RELEASES_PER_PAGE - 1, release("vpx-plugins-10.8.1-aaa-r3", "2026-11-01T00:00:00Z")),
        2, page(VpxPluginInstaller.RELEASES_PER_PAGE - 1, release("vpx-plugins-10.8.1-bbb-r1", "2026-03-01T00:00:00Z")),
        3, page(2, release("vpx-plugins-10.8.0-ccc-r7", "2026-05-01T00:00:00Z")));
    StubInstaller installer = new StubInstaller(pages::get);

    List<JsonNode> releases = installer.releases(true);

    // the build on the third page is found, the list is sorted by publication date
    assertThat(tags(releases)).containsExactly("vpx-plugins-10.8.1-aaa-r3", "vpx-plugins-10.8.0-ccc-r7", "vpx-plugins-10.8.1-bbb-r1");
    assertThat(installer.requests).hasSize(3);
    assertThat(installer.requests.get(0)).contains("per_page=" + VpxPluginInstaller.RELEASES_PER_PAGE).contains("page=1");
  }

  @Test
  public void stopsAtAnEmptyPage() {
    StubInstaller installer = new StubInstaller(p -> p == 1 ? page(VpxPluginInstaller.RELEASES_PER_PAGE) : "[]");
    assertThat(installer.releases(true)).isEmpty();
    assertThat(installer.requests).hasSize(2);
  }

  @Test
  public void cachesTheListUntilARefresh() {
    StubInstaller installer = new StubInstaller(p -> page(1, release("vpx-plugins-10.8.1-aaa-r3", "2026-11-01T00:00:00Z")));

    assertThat(installer.releases(false)).hasSize(1);
    assertThat(installer.releases(false)).hasSize(1);
    assertThat(installer.requests).hasSize(1);

    assertThat(installer.releases(true)).hasSize(1);
    assertThat(installer.requests).hasSize(2);
  }

  @Test
  public void keepsTheLastListWhenGitHubFails() {
    StubInstaller installer = new StubInstaller(p -> page(1, release("vpx-plugins-10.8.1-aaa-r3", "2026-11-01T00:00:00Z")));
    assertThat(installer.releases(true)).hasSize(1);

    installer.pages = p -> null;
    assertThat(tags(installer.releases(true))).containsExactly("vpx-plugins-10.8.1-aaa-r3");
    // the failed request is not repeated on the next (periodic) check
    assertThat(installer.releases(false)).hasSize(1);
    assertThat(installer.requests).hasSize(2);
  }

  @Test
  public void noListWhenGitHubIsNeverReachable() {
    StubInstaller installer = new StubInstaller(p -> null);
    assertThat(installer.releases(false)).isEmpty();
    assertThat(installer.releases(false)).isEmpty();
    assertThat(installer.requests).hasSize(1);
  }
}
