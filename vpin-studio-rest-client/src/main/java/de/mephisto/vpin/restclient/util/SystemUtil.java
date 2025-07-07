package de.mephisto.vpin.restclient.util;

import de.mephisto.vpin.restclient.RestClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SystemUtil.class);

  private final static List<String> INVALID_NAMES = Arrays.asList("Default", "filled by", "Serial", "Not applicable");

  private static String systemId = null;

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
          if (jvmArgument.startsWith("-Dstudio.server.port")) {
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

  private static String getWindowsSystemId() {
    try {
      Process process = Runtime.getRuntime().exec("wmic csproduct get UUID");
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.isEmpty() && !line.contains("UUID")) {
          reader.close();
          return line.trim();
        }
      }
      reader.close();
    }
    catch (Exception e) {
      //ignore
    }
    return null;
  }

  private static String getSystemId() {
    if (systemId == null) {
      String firstSegment = NetworkUtil.getMacAddress() != null ? NetworkUtil.getMacAddress().trim() : "#";
      if (StringUtils.isEmpty(firstSegment)) {
        firstSegment = getWindowsSystemId();
      }
      String driveId = getDriveId() != null ? getDriveId().trim() : "#";
      String boardId = getBoardSerialNumber() != null ? getBoardSerialNumber().trim() : "#";
      String id = firstSegment + "~" + driveId + "~" + boardId;
      if (id.length() > 100) {
        id = id.substring(0, 99);
      }

      systemId = id;
    }

    return systemId;
  }

  public static String getUniqueSystemId() {
    return getSystemId();
  }

  public static String getDriveId() {
    try {
      Process process = Runtime.getRuntime().exec("wmic diskdrive get serialnumber");
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      List<String> lines = new ArrayList<>();
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (StringUtils.isEmpty(line) || line.trim().equals("SerialNumber")) {
          continue;
        }
        lines.add(line.trim());
      }

      if (!lines.isEmpty()) {
        return lines.get(lines.size() - 1);
      }
    }
    catch (IOException e) {
      //ignore
    }
    return null;
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
      //ignore
    }
    return null;
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
    System.out.println(getSystemId());
  }
}
