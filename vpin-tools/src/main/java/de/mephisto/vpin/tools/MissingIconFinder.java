package de.mephisto.vpin.tools;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MissingIconFinder {

  public static void main(String[] args) throws IOException {
    VPS vps = new VPS();
    vps.reload();

    File folder = new File("C:\\workspace\\tarcisio-wheel-icons");
    List<VpsTable> tables = vps.getTables();
    tables.sort(new Comparator<VpsTable>() {
      @Override
      public int compare(VpsTable o1, VpsTable o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    File origFolder = new File("C:\\workspace\\tarcisio-wheel-icons\\original");
    File[] files = origFolder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".png");
      }
    });

    for (VpsTable table : tables) {
      if (table.getTableFiles() == null || table.getTableFiles().isEmpty()) {
        continue;
      }
      List<VpsTableVersion> vpx = table.getTableFiles().stream().filter(t -> table.getTableVersionById("VPX") == null).collect(Collectors.toList());
      if (vpx.isEmpty()) {
        continue;
      }

      File f = new File(folder, table.getId() + ".png");
      if (f.exists()) {
        continue;
      }

      for (File file : files) {
        if(file.getName().startsWith(table.getName().trim() + " (")) {
          File target = new File("C:\\workspace\\tarcisio-wheel-icons\\transfer", table.getId() + ".png");
          if (target.exists()) {
            continue;
          }
          FileUtils.copyFile(file, target);
        }
      }

    }

  }
}
