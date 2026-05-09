package de.mephisto.vpin.commons.utils;

import org.jspecify.annotations.Nullable;

import java.io.File;

@FunctionalInterface
public interface FileChangeListener {

  void notifyFileChange(@Nullable File file);
}
