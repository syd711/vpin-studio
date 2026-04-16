package de.mephisto.vpin.server.highscores.parsing.nvram;

import java.io.File;

public interface NvRamOutputToRaw {

  String convertOutputToRaw(String nvRamFileName, File originalNVRamFile) throws Exception;

}
