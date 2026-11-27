package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.validation.GameValidationCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class McpToolsHelpersTest {

  private static final String SIDEBAR = """
      ## [Welcome](https://github.com/syd711/vpin-studio/wiki)
      * [Getting Started](https://github.com/syd711/vpin-studio/wiki#getting-started)
      ## [iScored Integration](https://github.com/syd711/vpin-studio/wiki/iScored)
      * [Adding Alias Mappings](https://github.com/syd711/vpin-studio/wiki/Guides-%E2%80%90-Adding-Alias-Mappings)
      ## [Competitions](https://github.com/syd711/vpin-studio/wiki/Competitions)
      * [Offline Competitions](https://github.com/syd711/vpin-studio/wiki/Competitions#offline-competitions)
      """;

  private static final String ISCORED = """
      The iScored integration allows you to submit highscores to a game room.

      ## iScored Table Lineup

      The administrator has to set up the line up of tables.
      <img src="https://github.com/syd711/vpin-studio/blob/main/documentation/iscored/lineup.png?raw=true" width="800" />
      Copy the VPS link into the **tags** field.

      ## iScored Options

      Make sure that the game room is public and public score entries are allowed.
      """;

  private static final String COMPETITIONS = """
      # Competitions

      ## Offline Competitions

      Offline competitions run on your cabinet only. Pick a table, a start and an end date.
      """;

  @Test
  void parsesPageNamesFromSidebar() {
    assertThat(WikiHelpIndex.parsePageNames(SIDEBAR))
        .containsExactly("Home", "iScored", "Guides-‐-Adding-Alias-Mappings", "Competitions");
  }

  @Test
  void splitsPagesIntoCleanSections() {
    List<WikiHelpIndex.Section> sections = WikiHelpIndex.sections("iScored", ISCORED);
    assertThat(sections).extracting(WikiHelpIndex.Section::title).containsExactly("iScored", "iScored Table Lineup", "iScored Options");
    assertThat(sections.get(1).anchor()).isEqualTo("iscored-table-lineup");
    assertThat(sections.get(1).url()).isEqualTo("https://github.com/syd711/vpin-studio/wiki/iScored#iscored-table-lineup");
    assertThat(sections.get(1).text()).contains("[image]").doesNotContain("<img");
  }

  @Test
  void searchRanksMatchingSectionsFirst() {
    WikiHelpIndex index = new WikiHelpIndex() {
      @Override
      String fetch(String url) {
        if (url.endsWith("_Sidebar.md")) {
          return SIDEBAR;
        }
        if (url.endsWith("/iScored.md")) {
          return ISCORED;
        }
        if (url.endsWith("/Competitions.md")) {
          return COMPETITIONS;
        }
        return null;
      }
    };

    List<WikiHelpIndex.Section> hits = index.search("How do I set up an iScored game room?", 3);
    assertThat(hits).isNotEmpty();
    assertThat(hits.getFirst().page()).isEqualTo("iScored");
    assertThat(hits.subList(0, 2)).extracting(WikiHelpIndex.Section::title).contains("iScored Options");
    assertThat(hits).extracting(WikiHelpIndex.Section::page).doesNotContain("Competitions");

    assertThat(index.search("offline competition", 1).getFirst().anchor()).isEqualTo("offline-competitions");
    assertThat(index.search("the and how", 5)).isEmpty();

    assertThat(index.findPage("iscored")).isEqualTo("iScored");
    assertThat(index.findPage("https://github.com/syd711/vpin-studio/wiki/Competitions#offline-competitions")).isEqualTo("Competitions");
    assertThat(index.findPage("nope")).isNull();
  }

  @Test
  void unreachableWikiIsAToolError() {
    WikiHelpIndex index = new WikiHelpIndex() {
      @Override
      String fetch(String url) {
        return null;
      }
    };
    assertThatThrownBy(() -> index.search("iscored", 3)).isInstanceOf(McpToolException.class).hasMessageContaining(WikiHelpIndex.WIKI_URL);
  }

  @Test
  void parsesCompetitionDates() {
    assertThat(McpCompetitionTools.parseDate("2026-12-24", "endDate"))
        .isEqualTo(LocalDate.of(2026, 12, 24).atStartOfDay(ZoneId.systemDefault()).toInstant());
    assertThat(McpCompetitionTools.parseDate("2026-12-24T18:30", "endDate"))
        .isEqualTo(LocalDateTime.of(2026, 12, 24, 18, 30).atZone(ZoneId.systemDefault()).toInstant());
    assertThatThrownBy(() -> McpCompetitionTools.parseDate("24.12.2026", "endDate")).isInstanceOf(McpToolException.class);
    assertThatThrownBy(() -> McpCompetitionTools.parseDate(" ", "endDate")).hasMessageContaining("Missing");
  }

  @Test
  void parsesValidationIssueNames() {
    assertThat(McpTableTools.parseIssue("NO_ROM")).isEqualTo(GameValidationCode.CODE_NO_ROM);
    assertThat(McpTableTools.parseIssue("code_no_rom")).isEqualTo(GameValidationCode.CODE_NO_ROM);
    assertThat(McpTableTools.parseIssue(null)).isNull();
    assertThatThrownBy(() -> McpTableTools.parseIssue("NO_SUCH_THING")).hasMessageContaining("Known issues");
  }

  @Test
  void normalizesTags() {
    assertThat(McpManagementTools.normalizeTags(" Favorites, EM ,,Favorites ")).isEqualTo("Favorites,EM");
    assertThat(McpManagementTools.normalizeTags(" , ")).isNull();
  }

  @Test
  void optionalBooleans() {
    Map<String, Object> values = new HashMap<>();
    values.put("a", true);
    values.put("b", "false");
    McpArguments args = new McpArguments(values);
    assertThat(args.getBooleanOrNull("a")).isTrue();
    assertThat(args.getBooleanOrNull("b")).isFalse();
    assertThat(args.getBooleanOrNull("c")).isNull();
    assertThatThrownBy(() -> args.requireBoolean("c")).isInstanceOf(McpToolException.class);
  }
}
