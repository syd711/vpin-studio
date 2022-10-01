package de.mephisto.vpin.server.dof;

import com.google.common.annotations.VisibleForTesting;
import de.mephisto.vpin.server.util.SystemCommandExecutor;
import de.mephisto.vpin.server.util.SystemInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DOFCommandExecutor {
  private final static Logger LOG = LoggerFactory.getLogger(DOFCommandExecutor.class);

  public DOFCommandExecutor() {
  }

  private static File getTesterExe() {
    return new File(SystemInfo.RESOURCES + "DOFTest/", "DirectOutputTest.exe");
  }

  public static void execute(DOFCommand command) {
    new Thread(() -> {
      try {
        Thread.currentThread().setName("DOF Command Thread " + command.getId());

        switch (command.getTrigger()) {
          case TableStart:
          case TableExit: {
            executeCmd(command, false);
            if (command.getDurationMs() > 0) {
              Thread.sleep(command.getDurationMs());
              executeCmd(command, true);
            }
            break;
          }
          case SystemStart: {
            //already executed
            break;
          }
          case KeyEvent: {
            executeCmd(command, command.isToggled());
            if (command.isToggle()) {
              command.setToggled(!command.isToggled());
            }
            else {
              executeCmd(command, true);
            }
            break;
          }
        }
      } catch (InterruptedException e) {
        LOG.error("Failed to execute DOF command thread: " + e.getMessage(), e);
      }
    }).start();
  }

  private static DOFCommandResult executeCmd(DOFCommand command, boolean invertValue) {
    int value = command.getValue();
    if (invertValue && value == 0) {
      value = 255;
    }
    else if (invertValue && value == 255) {
      value = 0;
    }

    List<String> commands = Arrays.asList(String.valueOf(command.getUnit()), String.valueOf(command.getPortNumber()), String.valueOf(value));
    return executeDOFTester(commands);
  }

  public static DOFCommandResult executeDOFTester(List<String> commands) {
    List<String> params = new ArrayList<>();
    params.addAll(commands);
    params.add(0, getTesterExe().getAbsolutePath());
    LOG.error("Executing DOF command '" + String.join(" ", params) + "'.");
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(params, false);
      executor.setDir(getTesterExe().getParentFile());
      executor.executeCommand();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("DOF command '" + String.join(" ", params) + "' failed: {}", standardErrorFromCommand);
      }
      return new DOFCommandResult(standardOutputFromCommand.toString(), standardOutputFromCommand.toString());
    } catch (Exception e) {
      LOG.info("Failed execute DOF command: " + e.getMessage(), e);
    }
    return null;
  }

  public static List<Unit> scanUnits() {
    List<String> commands = Arrays.asList(getTesterExe().getAbsolutePath());
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
      executor.setDir(getTesterExe().getParentFile());
      executor.executeCommand();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("DOF command '" + String.join(" ", commands) + "' failed: {}", standardErrorFromCommand);
      }
      String output = standardOutputFromCommand.toString();
      return parseUnitsFromOutput(output.split("\\n"));
    } catch (Exception e) {
      String message = "Failed execute DOF command: " + e.getMessage();
      LOG.info(message, e);
      throw new RuntimeException(message);
    }
  }

  private static List<Unit> parseUnitsFromOutput(String[] output) {
    List<Unit> units = new ArrayList<>();
    for (String line : output) {
      if (line.contains("LedWiz unit ") || line.contains("Pinscape unit ")) {
        Unit unit = parseUnit(line);
        LOG.info("Resolved board " + unit);
        units.add(unit);
      }
    }
//    units.add(new Unit(1, UnitType.Pinscape, "Pinscape"));
    return units;
  }

  @VisibleForTesting
  static Unit parseUnit(String line) {
    try {
      String[] words = line.trim().replaceAll(":", "").split(" ");
      int deviceNumber = Integer.parseInt(words[2]);
      String deviceName = words[3];
      UnitType unitType = UnitType.valueOf(words[0]);
      return new Unit(deviceNumber, unitType, deviceName);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to parse device from output line '" + line + "': " + e.getMessage(), e);
    }
  }
}
