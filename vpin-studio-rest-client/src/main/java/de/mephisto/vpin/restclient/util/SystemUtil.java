package de.mephisto.vpin.restclient.util;

import de.mephisto.vpin.restclient.RestClient;
import javafx.stage.Screen;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
}
