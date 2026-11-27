package de.mephisto.vpin.server.emulators;

import de.mephisto.vpin.commons.utils.FileVersion;
import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.server.games.GameEmulator;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the version of an emulator from the file version of its executable.
 * For VPX, the version decides about the file layout: VPX 10.8.1 introduced the per table file layout,
 * see VPX-10.8.1-FileLayout.md.
 */
public class EmulatorVersion {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * The executables of a VPX installation, 64 bit first, as the 32 bit builds are rarely used.
   */
  private static final String[] VPX_EXES = {"VPinballX64.exe", "VPinballX_BGFX64.exe", "VPinballX_GL64.exe", "VPinballX.exe", "VPinballX_BGFX.exe", "VPinballX_GL.exe"};

  private static final int[] VPX_PER_TABLE_FILE_LAYOUT_VERSION = {10, 8, 1};
  private static final int[] VPX_109_COMMAND_LINE_VERSION = {10, 9, 0};

  /**
   * @return the file version of the executable of the emulator, e.g. "10.8.1.3788", or null if it can't be
   * determined, which is always the case on other platforms than Windows
   */
  @Nullable
  public static String resolve(@NonNull GameEmulator emulator) {
    if (!OSUtil.isWindows()) {
      return null;
    }

    for (File exe : getCandidates(emulator)) {
      try {
        if (exe.isFile()) {
          String version = FileVersion.fetch(exe);
          if (!StringUtils.isEmpty(version) && !version.startsWith("0.")) {
            return version;
          }
        }
      }
      catch (Throwable e) {
        LOG.warn("Failed to read the version of \"{}\": {}", exe.getAbsolutePath(), e.getMessage());
      }
    }
    return null;
  }

  /**
   * The executable configured for the emulator first, it may be a launcher script though.
   * For VPX, the default executables of the installation folder are checked next.
   */
  private static List<File> getCandidates(@NonNull GameEmulator emulator) {
    List<File> candidates = new ArrayList<>();
    File exe = emulator.getExe();
    if (exe != null && exe.getName().toLowerCase().endsWith(".exe")) {
      candidates.add(exe);
    }
    if (emulator.isVpxEmulator()) {
      for (String name : VPX_EXES) {
        candidates.add(new File(emulator.getInstallationFolder(), name));
      }
    }
    return candidates;
  }

  /**
   * @return true if the given VPX version supports the per table file layout, i.e. it is 10.8.1 or newer
   */
  public static boolean isVpxPerTableFileLayoutSupported(@Nullable String version) {
    return isAtLeast(version, VPX_PER_TABLE_FILE_LAYOUT_VERSION);
  }

  /**
   * VPX 10.9 reworked the command line: -Primary and the true fullscreen options are gone ("Invalid Parameter")
   * and -Minimized/-ExtMinimized are only accepted together with the Win32 editor, not with -Play.
   *
   * @return true if the given VPX version is 10.9 or newer
   */
  public static boolean isVpx109CommandLine(@Nullable String version) {
    return isAtLeast(version, VPX_109_COMMAND_LINE_VERSION);
  }

  private static boolean isAtLeast(@Nullable String version, int[] required) {
    if (StringUtils.isEmpty(version)) {
      return false;
    }

    String[] segments = version.trim().split("\\.");
    for (int i = 0; i < required.length; i++) {
      int value;
      try {
        value = i < segments.length ? Integer.parseInt(segments[i].replaceAll("\\D.*", "")) : 0;
      }
      catch (NumberFormatException e) {
        return false;
      }
      if (value != required[i]) {
        return value > required[i];
      }
    }
    return true;
  }
}
