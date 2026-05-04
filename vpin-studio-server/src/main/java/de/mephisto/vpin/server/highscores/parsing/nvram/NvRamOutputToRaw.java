package de.mephisto.vpin.server.highscores.parsing.nvram;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public interface NvRamOutputToRaw {

    boolean isSupportedRom(String rom);

    List<String> getRaw(String rom, File originalNVRamFile, Locale locale) throws IOException;

}
