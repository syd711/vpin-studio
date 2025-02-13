package de.mephisto.vpin.restclient.util;

import de.mephisto.vpin.restclient.RestClient;
import javafx.stage.Screen;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SystemUtil.class);

  private final static List<String> INVALID_NAMES = Arrays.asList("Default", "filled by", "Serial");

  public static int getPort() {
    try {
      String port = System.getProperty("studio.server.port");
      if (StringUtils.isEmpty(port)) {
        port = System.getenv("studio.server.port");
      }

      if (StringUtils.isEmpty(port)) {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArguments = runtimeMxBean.getInputArguments();
        for (String jvmArgument : jvmArguments) {
          if(jvmArgument.startsWith("-Dstudio.server.port")) {
            port = jvmArgument.split("=")[1];
          }
        }
      }

      if (!StringUtils.isEmpty(port)) {
        return Integer.parseInt(port);
      }
    }
    catch (NumberFormatException e) {
      //ignore
    }
    return RestClient.PORT;
  }

  public static String getUniqueSystemId() {
    String id = getBoardSerialNumber();
    //TODO this is actual a bug and should not be used,
    //Not yet a problem, but the existing system ids must be migrated
    if (StringUtils.isEmpty(id)) {
      id = getProcessorId();
    }

    if (StringUtils.isEmpty(id)) {
      return NetworkUtil.getMacAddress();
    }
    return id;
  }

  private static String getBoardSerialNumber() {
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("wmic", "baseboard", "get", "serialnumber"), false);
      executor.setIgnoreError(true);
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      if (standardOutputFromCommand != null) {
        String[] split = standardOutputFromCommand.toString().trim().split("\n");
        String serial = split[split.length - 1];
        if (!isNotValid(serial)) {
          return null;
        }

        return serial;
      }
    }
    catch (Exception e) {
      LOG.warn("Failed to resolve cabinet id: " + e.getMessage());
    }
    return null;
  }

  /**
   * This one is NOT unique!
   * @return
   */
  private static String getProcessorId() {
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("wmic", "cpu", "get", "ProcessorId"), false);
      executor.setIgnoreError(true);
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      if (standardOutputFromCommand != null) {
        String[] split = standardOutputFromCommand.toString().trim().split("\n");
        String serial = split[split.length - 1];
        if (!isNotValid(serial)) {
          return null;
        }
        return serial;
      }
    }
    catch (Exception e) {
      LOG.warn("Failed to resolve cpu id: " + e.getMessage());
    }
    return null;
  }

  public static Screen getScreenById(int id) {
    if (id == -1) {
      return Screen.getPrimary();
    }

    List<Screen> screens = Screen.getScreens();
    return screens.get(id - 1);
  }

  private static boolean isNotValid(String serial) {
    if (StringUtils.isEmpty(serial)) {
      return false;
    }
    for (String invalidName : INVALID_NAMES) {
      if (serial.contains(invalidName)) {
        return false;
      }
    }
    return true;
  }

  public static List<String> getAudioDevices() {
    List<String> names = new ArrayList<>();
    Mixer.Info[] infos = AudioSystem.getMixerInfo();
    for (int i = 0; i < infos.length; i++) {
      Mixer.Info info = infos[i];

      System.out.println(String.format("Name [%s], description - [%s]\n", info.getName(), info.getDescription()));
      System.out.println(info.getDescription());
      names.add(info.getName());
    }
    return names;
  }

  public static void main(String[] args) {
    System.out.println(getAudioDevices());
  }
}
