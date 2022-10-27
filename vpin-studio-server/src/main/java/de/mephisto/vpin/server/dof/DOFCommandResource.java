package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.server.VPinStudioServer;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(VPinStudioServer.API_SEGMENT + "dof")
public class DOFCommandResource {

  @Autowired
  private DOFCommandService dofCommandService;

  @SuppressWarnings("unused")
  @NonNull
  public List<DOFCommand> getDOFCommands() {
//    return dofCommandData.getCommands();
    return Collections.emptyList();
  }

  @SuppressWarnings("unused")
  public void updateDOFCommand(@NonNull DOFCommand command) {
//    this.dofCommandData.updateDOFCommand(command);
  }

  @SuppressWarnings("unused")
  public void addDOFCommand(@NonNull DOFCommand command) {
//    this.dofCommandData.addDOFCommand(command);
  }

  @SuppressWarnings("unused")
  public void removeDOFCommand(@NonNull DOFCommand command) {
//    this.dofCommandData.removeDOFCommand(command);
  }

  @SuppressWarnings("unused")
  @NonNull
  public List<Unit> getUnits() {
    return dofCommandService.getUnits();
  }

  @SuppressWarnings("unused")
  public Unit getUnit(int id) {
    return dofCommandService.getUnit(id);
  }
}
