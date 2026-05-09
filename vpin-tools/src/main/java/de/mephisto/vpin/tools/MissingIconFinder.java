package de.mephisto.vpin.tools;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MissingIconFinder {

  public static void main(String[] args) throws IOException {
    VPS vps = new VPS();
    vps.reload();

    List<VpsTable> tables = vps.getTables();
    List<String> entries = new ArrayList<>();
    for (VpsTable table : tables) {
      if (table.getTableFiles() == null || table.getTableFiles().isEmpty()) {
        continue;
      }

      List<VpsTableVersion> vpx = table.getTableFiles().stream().filter(t -> table.getTableVersionById("VPX") == null).toList();
      if (vpx.isEmpty()) {
        continue;
      }

      File f = new File(new File("C:\\workspace\\tarcisio-wheel-icons\\icons"), table.getId() + ".png");
      if (f.exists()) {
        continue;
      }

      entries.add(table.getName() + " [" + table.getId() + "]");
    }

    Collections.sort(entries);
    for (String entry : entries) {
      System.out.println(entry);
    }
    System.out.println("Total: " + entries.size());
  }
}
