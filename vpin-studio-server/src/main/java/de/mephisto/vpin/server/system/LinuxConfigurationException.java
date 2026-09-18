package de.mephisto.vpin.server.system;

import de.mephisto.vpin.server.VPinStudioException;

/**
 * Thrown when resources/system-linux.properties is missing or contains values that do not
 * resolve to existing files or folders. The server on Linux only supports Standalone mode, so
 * rather than start half-configured it stops with this exception's message.
 */
public class LinuxConfigurationException extends VPinStudioException {
  public LinuxConfigurationException(String msg) {
    super(msg);
  }
}
