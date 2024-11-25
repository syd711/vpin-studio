package de.mephisto.vpin.restclient.util;

import javafx.stage.Screen;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SystemUtil.class);

  private final static List<String> INVALID_NAMES = Arrays.asList("Default", "filled by", "Serial");

  public static String getUniqueSystemId() {
    String id = getBoardSerialNumber();
    if (StringUtils.isEmpty(id)) {
      id = getCpuSerialNumber();
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

  private static String getCpuSerialNumber() {
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
