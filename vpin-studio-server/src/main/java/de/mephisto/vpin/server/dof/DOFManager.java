package de.mephisto.vpin.server.dof;

import de.mephisto.vpin.server.popper.TableStatusChangeListener;
import de.mephisto.vpin.server.popper.TableStatusChangedEvent;
import de.mephisto.vpin.server.util.KeyChecker;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DOFManager implements TableStatusChangeListener, NativeKeyListener {
  private final static Logger LOG = LoggerFactory.getLogger(DOFManager.class);

  private final DOFCommandData dofCommandData;

  private List<Unit> units;

  public DOFManager(DOFCommandData dofCommandData) {
    this.dofCommandData = dofCommandData;
    this.units = DOFCommandExecutor.scanUnits();
  }

  public void startRuleEngine() {
    GlobalScreen.addNativeKeyListener(this);
    LOG.info("Starting Rule Engine");
    List<DOFCommand> startupRules = this.dofCommandData.getCommandsFor(Trigger.SystemStart);
    for (DOFCommand startupRule : startupRules) {
      startupRule.execute();
    }
  }

  @NonNull
  public List<Unit> getUnits() {
    return units;
  }

  @Nullable
  public Unit getUnit(int id) {
    List<Unit> units = this.getUnits();
    for (Unit unit : units) {
      if (unit.getId() == id) {
        return unit;
      }
    }

    return null;
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    List<DOFCommand> rules = this.dofCommandData.getCommandsFor(Trigger.TableStart);
    for (DOFCommand rule : rules) {
      rule.execute();
    }
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    List<DOFCommand> rules = this.dofCommandData.getCommandsFor(Trigger.TableStart);
    for (DOFCommand rule : rules) {
      rule.execute();
    }
  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    List<DOFCommand> rules = this.dofCommandData.getCommandsFor(Trigger.KeyEvent);
    for (DOFCommand rule : rules) {
      String keyBinding = rule.getKeyBinding();
      if (!StringUtils.isEmpty(keyBinding)) {
        KeyChecker checker = new KeyChecker(keyBinding);
        if (checker.matches(nativeKeyEvent)) {
          rule.execute();
        }
      }
    }
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

  }
}
