package de.mephisto.vpin.server.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An uitility used to update lines in a file while preserving its structure and its comments
 */
public class FileUpdateWriter {

  /**
   * lines to update
   */
  private List<String> lines = new ArrayList<>();

  public void read(Path filePath) throws IOException {
    this.lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
  }

  public boolean updateLine(String property, String newvalue, String section) {
    Pattern pattern = Pattern.compile("^" + property + "\\s*=.*");

    boolean inSection = section == null;
    int insertAfter = -1;

    for (int t = 0; t < lines.size(); t++) {
      String line = lines.get(t).trim();

      // ignore comments
      if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) {
      }
      // start of a section
      else if (section != null && line.startsWith("[")) {
        if (StringUtils.equals(line, "[" + section + "]")) {
          inSection = true;
          insertAfter = t;
        }
        else {
          boolean wasInSection = inSection;
          inSection = false;
          // was in section, but get out => stop iteration
          if (wasInSection) {
            break;
          }
        }
      }
      // else normal line, ignore it if we are not in the section
      else if (inSection) {
        Matcher match = pattern.matcher(line);
        if (match.find()) {
          String oldvalue = StringUtils.substringAfter(lines.get(t), "=").trim();
          if (newvalue == null) {
            // remove the line
            lines.remove(t);
            return true;
          }
          else if (!newvalue.equals(oldvalue)) {
            // update existing line
            lines.set(t, property + " = " + newvalue);
            return true;
          }
          else {
            // property found but no update needed
            return false;
          }
        }
        // compare key to find insertion point
        if (property.compareTo(line) > 0) {
          insertAfter = t;
        }
      }
    }
    // value to remove not found => no update
    if (newvalue == null) {
      return false;
    }

    // create a new line if there was no existing line
    if (insertAfter != -1) {
      lines.add(insertAfter + 1, property + " = " + newvalue);
    }
    else {
      // add at the end, add a section if needed
      if (section != null) {
        if (lines.size() > 0) {
          lines.add("");
        }
        lines.add("[" + section + "]");
      }
      lines.add(property + " = " + newvalue);
    }
    return true;
  }

  public void removeSection(@NonNull String section) {
    String sectionEntry = "[" + section + "]";
    int sectionIndex = this.lines.indexOf(sectionEntry);

    if (sectionIndex != -1) {
      int nextSectionIndex = sectionIndex + 1;
      while (nextSectionIndex < this.lines.size() && !this.lines.get(nextSectionIndex).startsWith("[")) {
        nextSectionIndex++;
      }

      this.lines.subList(sectionIndex, nextSectionIndex).clear();
    }
  }

  public boolean removeLine(String property, String section) {
    return updateLine(property, null, section);
  }

  public boolean updateLines(Map<String, String> updates, String section) {
    boolean hasUpdate = false;
    for (Map.Entry<String, String> update : updates.entrySet()) {
      hasUpdate |= updateLine(update.getKey(), update.getValue(), section);
    }
    return hasUpdate;
  }

  public void write(Path filePath) throws IOException {
    // Write back to file
    Files.write(filePath, lines, StandardCharsets.UTF_8);
  }

}
