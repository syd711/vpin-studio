package de.mephisto.vpin.server.games;

public enum TableStatusChangedOrigin {
  ORIGIN_POPPER, ORIGIN_VPS,
  /**
   * The table has been launched from the VPin Studio, the exit is detected by monitoring the emulator processes.
   */
  ORIGIN_STUDIO
}
