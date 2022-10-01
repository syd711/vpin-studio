package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.server.util.JSON;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DOFCommandData {

  public static final String DOF_COMMANDS = "dofCommands";
  private final List<DOFCommand> commandList = new ArrayList<>();

  public static DOFCommandData create() {
    DOFCommandData data = JSON.read(DOFCommandData.class, DOFCommandData.DOF_COMMANDS);
    if (data == null) {
      data = new DOFCommandData();
    }
    return data;
  }

  public List<DOFCommand> getCommands() {
    return new ArrayList<>(commandList);
  }

  public List<DOFCommand> getCommandsFor(Trigger trigger) {
    return getCommands().stream().filter(c -> c.getTrigger().equals(trigger)).collect(Collectors.toList());
  }

  public void updateDOFCommand(DOFCommand command) {
    save();
  }

  public void removeDOFCommand(DOFCommand command) {
    this.commandList.remove(command);
    save();
  }

  public void addDOFCommand(DOFCommand command) {
    this.commandList.add(command);
    save();
  }

  private void save() {
    JSON.write(this, DOF_COMMANDS);
  }
}
