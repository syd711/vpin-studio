package de.mephisto.vpin.ui.util;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the central color palette: every color of the UI lives in stylesheet-colors.css.
 */
class ThemeColorsTest {
  private static final Path RESOURCES = Path.of("src/main/resources");
  private static final Path JAVA = Path.of("src/main/java");
  private static final Path COLORS_CSS = RESOURCES.resolve("de/mephisto/vpin/ui/stylesheet-colors.css");

  private static final Pattern HEX = Pattern.compile("#[0-9a-fA-F]{3,8}\\b");
  private static final Pattern TOKEN_USE = Pattern.compile("(?<![\\w-])-c-[\\w-]+");
  private static final Pattern COLOR_CLASS_USE = Pattern.compile("(?<![\\w-])c-(?:text|icon|fill)-[\\w-]+");

  /** web-style.css is HTML CSS for a WebView, the demos are developer main classes */
  private static final Set<String> IGNORED = Set.of("stylesheet-colors.css", "web-style.css", "ThemeColors.java",
      "PositionResizerDemo.java", "PositionResizerDemo2.java", "PositionResizerDemo3.java");

  @Test
  void constantsResolveToColors() throws IllegalAccessException {
    for (Field field : ThemeColors.class.getFields()) {
      if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class && !field.getName().equals("RESOURCE")) {
        String value = (String) field.get(null);
        assertDoesNotThrow(() -> Color.web(value), field.getName());
      }
    }
    assertEquals("#FFFFFF", ThemeColors.WHITE);
    assertEquals(ThemeColors.INFO_BORDER, ThemeColors.ROW_SELECTED);
  }

  @Test
  void referencedTokensAndClassesAreDefined() throws IOException {
    String colors = Files.readString(COLORS_CSS, StandardCharsets.UTF_8);
    Set<String> missing = new TreeSet<>();
    for (Path file : files(RESOURCES, ".css", ".fxml")) {
      String content = Files.readString(file, StandardCharsets.UTF_8);
      Matcher tokens = TOKEN_USE.matcher(content);
      while (tokens.find()) {
        if (!ThemeColors.getTokens().containsKey(tokens.group())) {
          missing.add(file.getFileName() + ": " + tokens.group());
        }
      }
      Matcher classes = COLOR_CLASS_USE.matcher(content);
      while (classes.find()) {
        if (!colors.contains("." + classes.group() + " {")) {
          missing.add(file.getFileName() + ": ." + classes.group());
        }
      }
    }
    assertTrue(missing.isEmpty(), "Undefined color tokens / classes: " + missing);
  }

  @Test
  void noHardCodedColors() throws IOException {
    List<String> found = new ArrayList<>();
    for (Path file : files(RESOURCES, ".css", ".fxml")) {
      String content = Files.readString(file, StandardCharsets.UTF_8);
      if (file.toString().endsWith(".fxml")) {
        Matcher attr = Pattern.compile("\\s(textFill|iconColor|fill|stroke)=\"[^\"]*\"").matcher(content);
        while (attr.find()) {
          found.add(file.getFileName() + ": " + attr.group().trim());
        }
      }
      content = content.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?s)<!--.*?-->", "");
      Matcher hex = HEX.matcher(content);
      while (hex.find()) {
        found.add(file.getFileName() + ": " + hex.group());
      }
    }
    Pattern javaHex = Pattern.compile("\"[^\"\\n]*#[0-9a-fA-F]{6}");
    for (Path file : files(JAVA, ".java")) {
      for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
        if (!line.trim().startsWith("//") && javaHex.matcher(line).find()) {
          found.add(file.getFileName() + ": " + line.trim());
        }
      }
    }
    assertTrue(found.isEmpty(), "Hard coded colors, use stylesheet-colors.css / ThemeColors instead:\n" + String.join("\n", found));
  }

  private static List<Path> files(Path root, String... extensions) throws IOException {
    try (Stream<Path> walk = Files.walk(root)) {
      return walk.filter(Files::isRegularFile)
          .filter(p -> !IGNORED.contains(p.getFileName().toString()))
          .filter(p -> Stream.of(extensions).anyMatch(e -> p.toString().endsWith(e)))
          .toList();
    }
  }
}
