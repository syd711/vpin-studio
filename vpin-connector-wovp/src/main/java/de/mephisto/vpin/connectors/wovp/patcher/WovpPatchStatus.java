package de.mephisto.vpin.connectors.wovp.patcher;

public enum WovpPatchStatus {
  /** The WoVP timers and the telemetry script have been added. */
  PATCHED,
  /** The table already contains the WoVP timers, nothing has been changed. */
  ALREADY_PATCHED,
  /** The WoVP timers and the telemetry script have been removed. */
  UNPATCHED,
  /** The table does not contain any WoVP telemetry, nothing has been changed. */
  NOT_PATCHED,
  /** The patch could not be applied, see the message of the result. */
  FAILED
}
