package de.mephisto.vpin.server.frontend;

public interface FrontendStatusChangeListener {
  void frontendLaunched();

  void frontendExited();

  void frontendRestarted();
}
