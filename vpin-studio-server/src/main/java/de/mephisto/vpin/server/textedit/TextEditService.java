package de.mephisto.vpin.server.textedit;

import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.popper.PinUPConnector;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class TextEditService {
  private final static Logger LOG = LoggerFactory.getLogger(TextEditService.class);

  @Autowired
  private PinUPConnector pinUPConnector;

  public TextFile getText(VPinFile file) {
    TextFile textFile = new TextFile();
    try {
      switch (file) {
        case DmdDeviceIni: {
          GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
          File mameFolder = defaultGameEmulator.getMameFolder();
          File init = new File(mameFolder, "DmdDevice.ini");
          Path filePath = init.toPath();
          String iniText =  Files.readString(filePath);
          //Remove BOM
          iniText = iniText.replace("\uFEFF", "");
          textFile.setContent(iniText);
          textFile.setPath(init.getAbsolutePath());
          textFile.setSize(init.length());
          textFile.setLastModified(new Date(init.lastModified()));
          break;
        }
        default: {
          throw new UnsupportedOperationException("Unknown VPin file: " + file);
        }
      }

    } catch (IOException e) {
      LOG.error("Error reading text file: " + e.getMessage(), e);
    }
    return textFile;
  }

  public TextFile save(VPinFile file, String text) {
    try {
      switch (file) {
        case DmdDeviceIni: {
          GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
          File mameFolder = defaultGameEmulator.getMameFolder();
          File iniFile = new File(mameFolder, "DmdDevice.ini");
          File backup = new File(mameFolder, "DmdDevice.ini.bak");
          if (!backup.exists()) {
            FileUtils.copyFile(iniFile, backup);
          }

          if (iniFile.delete()) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(iniFile), StandardCharsets.UTF_8));
            out.write('\ufeff');
            out.write(text);
            out.close();
            LOG.info("Written " + iniFile.getAbsolutePath());
          }
          else {
            throw new IOException("Failed to delete target file.");
          }
          return getText(file);
        }
        default: {
          throw new UnsupportedOperationException("Unknown VPin file: " + file);
        }
      }
    } catch (IOException e) {
      LOG.error("Error reading text file: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
