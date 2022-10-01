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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DOFManager implements TableStatusChangeListener, NativeKeyListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(DOFManager.class);

  private List<Unit> units;

  @Autowired
  private DOFCommandData dofCommandData;

  public DOFManager(DOFCommandData dofCommandData) {

  }

  private void startRuleEngine() {
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

  @Override
  public void afterPropertiesSet() throws Exception {
    this.units = DOFCommandExecutor.scanUnits();
    this.startRuleEngine();
  }
}
