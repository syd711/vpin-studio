package de.mephisto.vpin.tools;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VPSAnalyzer {

  public static void main(String[] args) {
    VPS vps = new VPS();
    vps.reload();

    List<String> letters = new ArrayList<>();
    List<VpsTable> tables = vps.getTables();
    Collections.sort(tables, new Comparator<VpsTable>() {
      @Override
      public int compare(VpsTable o1, VpsTable o2) {
        if (o1.getName().isEmpty() || o2.getName().isEmpty()) {
          return 0;
        }
        return o1.getName().compareTo(o2.getName());
      }
    });

    for (VpsTable table : tables) {
      if (table.getName().isEmpty()) {
        continue;
      }

      String letter = table.getName().substring(0, 1);
      if (!letters.contains(letter)) {
        letters.add(letter);
      }
    }

    letters.stream().forEach(l -> System.out.print("\"" + l + "\","));

  }
}
