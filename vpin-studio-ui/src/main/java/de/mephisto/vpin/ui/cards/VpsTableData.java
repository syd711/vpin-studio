package de.mephisto.vpin.ui.cards;

import java.util.ArrayList;
import java.util.List;

public class VpsTableData {
  private String id;
  private String displayName;

  private List<VpsTableInstructions> instructionSets = new ArrayList<>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public List<VpsTableInstructions> getInstructionSets() {
    return instructionSets;
  }

  public void setInstructionSets(List<VpsTableInstructions> instructionSets) {
    this.instructionSets = instructionSets;
  }

  public VpsTableInstructions getInstructionSetFor(String imageBase64) {
    for (VpsTableInstructions instructionSet : instructionSets) {
      if (imageBase64.equalsIgnoreCase(instructionSet.getImageBase64())) {
        return instructionSet;
      }
    }
    VpsTableInstructions instructionSet = new VpsTableInstructions();
    instructionSet.setId(id);
    instructionSet.setImageBase64(imageBase64);
    instructionSets.add(instructionSet);
    return instructionSet;
  }
}
