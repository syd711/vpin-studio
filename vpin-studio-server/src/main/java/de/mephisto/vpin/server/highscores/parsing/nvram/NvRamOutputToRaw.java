package de.mephisto.vpin.server.highscores.parsing.nvram;

import java.io.File;
import java.util.Set;

public interface NvRamOutputToRaw {

  String convertOutputToRaw(String nvRamFileName, File originalNVRamFile) throws Exception;

  Set<String> getSupportedRoms();
}
