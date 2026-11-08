package de.mephisto.vpin.connectors.wovp.patcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class WovpTablePatcherTest {

  private static final String INDEX = """
      [
        {
          "file_name": "Primitive.Primitive22.json",
          "is_locked": true,
          "editor_layer": 9
        },
        {
          "file_name": "Timer.LampTimer.json",
          "is_locked": false,
          "editor_layer": 3
        }
      ]
      """;

  private static final String LAMP_TIMER = """
      {
        "Timer": {
          "center": {
            "x": -196.25,
            "y": 1328.5
          },
          "is_timer_enabled": false,
          "timer_interval": 100,
          "name": "LampTimer",
          "backglass": false
        }
      }
      """;

  // contains a non-ASCII (Windows-1252) character that must survive the patch untouched
  private static final byte[] SCRIPT = "Option Explicit\r\n' Café\r\nSub Table1_Init\r\nEnd Sub\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1);

  @TempDir
  Path tempDir;

  private File createTable(boolean withTimer) throws Exception {
    File table = tempDir.resolve("table").toFile();
    File items = new File(table, "gameitems");
    assertTrue(items.mkdirs());
    Files.writeString(new File(items, "Primitive.Primitive22.json").toPath(), "{\"Primitive\":{\"name\":\"Primitive22\"}}");
    if (withTimer) {
      Files.writeString(new File(table, "gameitems.json").toPath(), INDEX);
      Files.writeString(new File(items, "Timer.LampTimer.json").toPath(), LAMP_TIMER);
    }
    else {
      Files.writeString(new File(table, "gameitems.json").toPath(), "[{\"file_name\": \"Primitive.Primitive22.json\", \"is_locked\": true, \"editor_layer\": 9}]");
    }
    Files.write(new File(table, "script.vbs").toPath(), SCRIPT);
    return table;
  }

  @Test
  void patch_addsTimersAndScript() throws Exception {
    File table = createTable(true);

    assertEquals(WovpPatchStatus.PATCHED, WovpTablePatcher.patch(table));

    String index = Files.readString(new File(table, "gameitems.json").toPath());
    assertTrue(index.contains("Timer.WoVP_Timer.json"));
    assertTrue(index.contains("Timer.WoVP_FastTimer.json"));

    String timer = Files.readString(new File(table, "gameitems/Timer.WoVP_Timer.json").toPath());
    assertTrue(timer.contains("\"name\" : \"WoVP_Timer\""), timer);
    assertTrue(timer.contains("\"timer_interval\" : 1000"), timer);
    assertTrue(timer.contains("\"is_timer_enabled\" : true"), timer);
    assertTrue(timer.contains("-156.25"), timer);

    String fastTimer = Files.readString(new File(table, "gameitems/Timer.WoVP_FastTimer.json").toPath());
    assertTrue(fastTimer.contains("\"timer_interval\" : 50"), fastTimer);

    byte[] script = Files.readAllBytes(new File(table, "script.vbs").toPath());
    String text = new String(script, StandardCharsets.ISO_8859_1);
    assertTrue(text.startsWith("Option Explicit\r\n' Café\r\n"));
    assertTrue(WovpTablePatcher.isPatched(text));
    assertTrue(text.contains("End Sub\r\n\r\n' ====="));
  }

  @Test
  void patch_twice_isDetected() throws Exception {
    File table = createTable(true);
    assertEquals(WovpPatchStatus.PATCHED, WovpTablePatcher.patch(table));
    byte[] script = Files.readAllBytes(new File(table, "script.vbs").toPath());

    assertEquals(WovpPatchStatus.ALREADY_PATCHED, WovpTablePatcher.patch(table));
    assertArrayEquals(script, Files.readAllBytes(new File(table, "script.vbs").toPath()));
  }

  @Test
  void patch_withoutTimer_createsTimersFromScratch() throws Exception {
    File table = createTable(false);

    assertEquals(WovpPatchStatus.PATCHED, WovpTablePatcher.patch(table));

    String timer = Files.readString(new File(table, "gameitems/Timer.WoVP_FastTimer.json").toPath());
    assertTrue(timer.contains("\"Timer\""), timer);
    assertTrue(timer.contains("\"name\" : \"WoVP_FastTimer\""), timer);
    assertTrue(timer.contains("\"timer_interval\" : 50"), timer);
  }

  @Test
  void unpatch_restoresTable() throws Exception {
    File table = createTable(true);
    WovpTablePatcher.patch(table);

    assertEquals(WovpPatchStatus.UNPATCHED, WovpTablePatcher.unpatch(table));

    String index = Files.readString(new File(table, "gameitems.json").toPath());
    assertFalse(index.contains("WoVP"));
    assertTrue(index.contains("Timer.LampTimer.json"));
    assertFalse(new File(table, "gameitems/Timer.WoVP_Timer.json").exists());
    assertFalse(new File(table, "gameitems/Timer.WoVP_FastTimer.json").exists());

    String script = new String(Files.readAllBytes(new File(table, "script.vbs").toPath()), StandardCharsets.ISO_8859_1);
    assertEquals("Option Explicit\r\n' Café\r\nSub Table1_Init\r\nEnd Sub\r\n", script);

    assertEquals(WovpPatchStatus.NOT_PATCHED, WovpTablePatcher.unpatch(table));
  }

  @Test
  void unpatch_handlesTablesPatchedByOriginalTool() throws Exception {
    File table = createTable(true);
    WovpTablePatcher.patch(table);
    // the PowerShell patcher writes lower case file names
    File items = new File(table, "gameitems");
    assertTrue(new File(items, "Timer.WoVP_Timer.json").renameTo(new File(items, "Timer.wovp_timer.json")));
    Path index = new File(table, "gameitems.json").toPath();
    Files.writeString(index, Files.readString(index).replace("Timer.WoVP_Timer.json", "Timer.wovp_timer.json"));

    assertEquals(WovpPatchStatus.UNPATCHED, WovpTablePatcher.unpatch(table));
    assertFalse(new File(items, "Timer.wovp_timer.json").exists());
    assertFalse(Files.readString(index).toLowerCase().contains("wovp"));
  }

  @Test
  void loadScriptTemplate() throws Exception {
    String template = new String(WovpTablePatcher.loadScriptTemplate(), StandardCharsets.US_ASCII);
    assertTrue(WovpTablePatcher.isPatched(template));
    assertTrue(template.contains("Sub WoVP_FastTimer_Timer()"));
  }
}
