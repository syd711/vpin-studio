package de.mephisto.vpin.ui.tables.dialogs.bulk;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BulkImportMatcherTest {

  private static GameRepresentation game(int id, String name, String file) {
    GameRepresentation g = new GameRepresentation();
    g.setId(id);
    g.setGameDisplayName(name);
    g.setGameFileName(file);
    return g;
  }

  private final GameRepresentation addams = game(1, "The Addams Family (Bally 1992)", "Addams Family.vpx");
  private final GameRepresentation tz = game(2, "Twilight Zone (Bally 1993)", "Twilight Zone.vpx");

  @Test
  public void matchesByFilename() {
    BulkImportMatcher matcher = new BulkImportMatcher(Arrays.asList(addams, tz));
    List<BulkImportMatcher.Match> result = matcher.suggest(new File("C:/in/Addams_Family_Instructions_v2.png"), new File("C:/in"), BulkImportMatcher.MatchMode.FILENAME, 3);
    assertFalse(result.isEmpty());
    assertEquals(1, result.get(0).game.getId());
    assertTrue(result.get(0).score > 0.6);
  }

  @Test
  public void matchesByFolder() {
    BulkImportMatcher matcher = new BulkImportMatcher(Arrays.asList(addams, tz));
    List<BulkImportMatcher.Match> result = matcher.suggest(new File("C:/in/Twilight Zone/help.png"), new File("C:/in"), BulkImportMatcher.MatchMode.FOLDER, 3);
    assertEquals(2, result.get(0).game.getId());
    assertEquals("folder", result.get(0).source);
  }

  @Test
  public void filenameModeIgnoresFolder() {
    BulkImportMatcher matcher = new BulkImportMatcher(Arrays.asList(addams, tz));
    assertTrue(matcher.suggest(new File("C:/in/Twilight Zone/help.png"), new File("C:/in"), BulkImportMatcher.MatchMode.FILENAME, 3).isEmpty());
  }

  @Test
  public void matchesByVpsTableId() {
    tz.setExtTableId("fVjpuc-H");
    BulkImportMatcher matcher = new BulkImportMatcher(Arrays.asList(addams, tz));
    List<BulkImportMatcher.Match> byFile = matcher.suggest(new File("C:/in/fVjpuc-H.png"), new File("C:/in"), BulkImportMatcher.MatchMode.FILENAME, 3);
    assertEquals(2, byFile.get(0).game.getId());
    assertEquals("vps", byFile.get(0).source);
    assertEquals(1.0, byFile.get(0).score);

    List<BulkImportMatcher.Match> byFolder = matcher.suggest(new File("C:/in/fVjpuc-H/wheel.png"), new File("C:/in"), BulkImportMatcher.MatchMode.FOLDER, 3);
    assertEquals(2, byFolder.get(0).game.getId());
    assertEquals("vps", byFolder.get(0).source);
  }

  /**
   * Help card names from issue #1117, written together without separators.
   */
  @Test
  public void matchesNamesWrittenTogether() {
    List<GameRepresentation> games = Arrays.asList(
        game(10, "Dark (1986)", "Dark (1986).vpx"),
        game(11, "Batman The Dark Knight (Stern 2008)", "Batman The Dark Knight.vpx"),
        game(12, "Attack and Revenge from Mars", "Attack and Revenge from Mars.vpx"),
        game(13, "Attack from Mars (Bally 1995)", "Attack from Mars.vpx"),
        game(14, "Ace of Speed (Original 2019)", "Ace of Speed.vpx"),
        game(15, "Dr. Dude (Bally 1990)", "Dr Dude.vpx"),
        game(16, "F-14 Tomcat (Williams 1987)", "F14 Tomcat.vpx"),
        game(17, "Fish Tales (Williams 1992)", "Fish Tales.vpx"),
        game(18, "Funhouse (Williams 1990)", "Funhouse.vpx"),
        game(19, "Red & Ted's Road Show (Williams 1994)", "Road Show.vpx"),
        game(20, "Whirlwind (Williams 1990)", "Whirlwind (Williams 1990) VPW 1.1.vpx"),
        game(21, "Batman (Data East 1991)", "Batman.vpx"),
        game(22, "Batman 66 (Stern 2016)", "Batman 66.vpx"),
        game(23, "Flintstones (Williams 1994), The", "Flintstones.vpx"),
        game(24, "The A-Team", "A-Team.vpx"));
    BulkImportMatcher matcher = new BulkImportMatcher(games);

    assertBestMatch(matcher, "darkknight_help.png", 11);
    assertBestMatch(matcher, "attackfrommars_help.png", 13);
    assertBestMatch(matcher, "aceofspeed_help.png", 14);
    assertBestMatch(matcher, "drdude_help.png", 15);
    // "tomcat" is missing, so this is the best suggestion but below the default threshold
    assertEquals(16, matcher.suggest(new File("C:/in/f14_help.png"), new File("C:/in"), BulkImportMatcher.MatchMode.FILENAME, 3).get(0).game.getId());
    assertBestMatch(matcher, "fishtales_help.png", 17);
    assertBestMatch(matcher, "funhouse_help.png", 18);
    assertBestMatch(matcher, "fun_house_help.png", 18);
    assertBestMatch(matcher, "roadshow_help.png", 19);
    assertBestMatch(matcher, "whirlwind_help.png", 20);
    assertBestMatch(matcher, "batman66_help.png", 22);
    assertBestMatch(matcher, "flintstones_help.png", 23);
    assertBestMatch(matcher, "ateam_help.png", 24);
  }

  @Test
  public void partialNameIsNoCertainMatch() {
    BulkImportMatcher matcher = new BulkImportMatcher(List.of(game(10, "Dark (1986)", "Dark (1986).vpx")));
    List<BulkImportMatcher.Match> result = matcher.suggest(new File("C:/in/darkknight_help.png"), new File("C:/in"), BulkImportMatcher.MatchMode.FILENAME, 3);
    assertTrue(result.get(0).score < 0.8, "score " + result.get(0).score);
  }

  private static void assertBestMatch(BulkImportMatcher matcher, String fileName, int expectedId) {
    List<BulkImportMatcher.Match> result = matcher.suggest(new File("C:/in/" + fileName), new File("C:/in"), BulkImportMatcher.MatchMode.FILENAME, 3);
    assertFalse(result.isEmpty(), fileName + " has no match");
    assertEquals(expectedId, result.get(0).game.getId(), fileName + " matched " + result.get(0).game.getGameDisplayName());
    assertTrue(result.get(0).score >= 0.6, fileName + " score " + result.get(0).score);
  }

  @Test
  public void detectsScreen() {
    assertEquals(VPinScreen.BackGlass, BulkImportMatcher.detectScreen("Addams_Backglass"));
    assertEquals(VPinScreen.GameHelp, BulkImportMatcher.detectScreen("AddamsFamilyInstructions"));
    assertNull(BulkImportMatcher.detectScreen("Addams Family"));
    assertEquals(VPinScreen.DMD, BulkImportMatcher.detectScreen(new File("C:/in/DMD/addams.png"), new File("C:/in")));
    assertEquals(VPinScreen.Audio, BulkImportMatcher.detectScreen(new File("C:/in/addams.mp3"), new File("C:/in")));
  }
}
