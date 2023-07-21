package de.mephisto.vpin.server.volume;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VolumeUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VolumeUtil.class);
  private final static String VPX_NAME = "VPinballX.exe";


  public static float getVPXVolume() {
    String out = executeSvclCommand("/GetPercent", null);
    if (!StringUtils.isEmpty(out)) {
      return Float.parseFloat(out);
    }
    return -1;
  }

  public static void setVPXVolume(float value) {
    executeSvclCommand("/SetVolume", null);
  }

  public static void increaseVolume() {
    float vpxVolume = getVPXVolume();
    vpxVolume += 2;
    executeSvclCommand("/SetVolume", String.valueOf(vpxVolume));
  }

  public static void decreaseVolume() {
    float vpxVolume = getVPXVolume();
    vpxVolume -= 2;
    executeSvclCommand("/SetVolume", String.valueOf(vpxVolume));
  }

  public static void awaitVPX() {
    List<ProcessHandle> pinUpProcesses = new ArrayList<>();
    while(pinUpProcesses.isEmpty()) {
      List<ProcessHandle> collect = ProcessHandle.allProcesses().collect(Collectors.toList());
      pinUpProcesses = collect.stream()
          .filter(p -> p.info().command().isPresent() && (p.info().command().get().contains("Visual")))
          .collect(Collectors.toList());
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        //ignore11
      }
      System.out.println(pinUpProcesses.size());
      pinUpProcesses.clear();
    }

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      //ignore11
    }
  }

  private static String executeSvclCommand(@NonNull String command, @Nullable String parameter) {
    List<ProcessHandle> collect = ProcessHandle.allProcesses().collect(Collectors.toList());
    List<ProcessHandle> pinUpProcesses = collect.stream()
        .filter(p -> p.info().command().isPresent() && (p.info().command().get().contains("Visual")))
        .collect(Collectors.toList());
    if (!pinUpProcesses.isEmpty()) {
      try {
        File dir = new File("./resources");
        List<String> commands = new ArrayList<>(Arrays.asList("svcl.exe", "/Stdout", command, "\"VisualPinball\""));
        if(parameter != null) {
          commands.add(parameter);
        }
        LOG.info("Executing command (" + dir.getAbsolutePath() + "): " + String.join(" ", commands));
        SystemCommandExecutor executor = new SystemCommandExecutor(commands);
        executor.setDir(dir);
        StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
        return standardOutputFromCommand.toString();
      } catch (Exception e) {
        LOG.error("Failed to read VPX volume: " + e.getMessage());
      }
    }
    else {
      LOG.warn("No VPX process found to adjust volume.");
    }

    return null;
  }
}
