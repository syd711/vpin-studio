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
  public void detectsScreen() {
    assertEquals(VPinScreen.BackGlass, BulkImportMatcher.detectScreen("Addams_Backglass"));
    assertEquals(VPinScreen.GameHelp, BulkImportMatcher.detectScreen("AddamsFamilyInstructions"));
    assertNull(BulkImportMatcher.detectScreen("Addams Family"));
    assertEquals(VPinScreen.DMD, BulkImportMatcher.detectScreen(new File("C:/in/DMD/addams.png"), new File("C:/in")));
    assertEquals(VPinScreen.Audio, BulkImportMatcher.detectScreen(new File("C:/in/addams.mp3"), new File("C:/in")));
  }
}
