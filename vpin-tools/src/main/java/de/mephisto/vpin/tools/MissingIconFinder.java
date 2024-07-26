package de.mephisto.vpin.tools;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class MissingIconFinder {

  public static void main(String[] args) {
    VPS vps = new VPS();
    vps.reload();

    File folder = new File("C:\\workspace\\tarcisio-wheel-icons");
    List<VpsTable> tables = vps.getTables();
    for (VpsTable table : tables) {
      if(table.getTableFiles() == null) {
        continue;
      }
      List<VpsTableVersion> vpx = table.getTableFiles().stream().filter(t -> table.getTableVersionById("VPX") == null).collect(Collectors.toList());
      if(vpx.isEmpty()) {
        continue;
      }

      File f = new File(folder, table.getId() + ".png");
      if(f.exists()) {
        continue;
      }
      System.out.println(table.getName() + "|" + table.getId());
    }

  }
}
