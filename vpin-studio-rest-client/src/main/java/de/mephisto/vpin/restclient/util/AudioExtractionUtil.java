package de.mephisto.vpin.restclient.util;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

import java.util.Arrays;
import java.util.List;

/**
 * Shared rules for dropping videos on audio screens: the audio track is extracted as mp3 on the server.
 */
public class AudioExtractionUtil {
  public static final List<String> VIDEO_SUFFIXES = List.of("mp4", "mkv", "avi", "mov", "webm", "m4v", "f4v");

  public static boolean isAudioScreen(VPinScreen screen) {
    return VPinScreen.Audio.equals(screen) || VPinScreen.AudioLaunch.equals(screen);
  }

  public static boolean isVideoSuffix(String suffix) {
    return suffix != null && VIDEO_SUFFIXES.contains(suffix.toLowerCase());
  }

  /**
   * Returns the suffixes accepted by the given screen, including videos for the audio screens.
   */
  public static String[] getAcceptedSuffixes(VPinScreen screen, String... suffixes) {
    if (!isAudioScreen(screen)) {
      return suffixes;
    }
    return java.util.stream.Stream.concat(Arrays.stream(suffixes), VIDEO_SUFFIXES.stream()).distinct().toArray(String[]::new);
  }
}
