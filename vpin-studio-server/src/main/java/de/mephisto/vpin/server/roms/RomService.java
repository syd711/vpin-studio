package de.mephisto.vpin.server.roms;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.restclient.system.ScoringDBMapping;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.VPXFileScanner;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
public class RomService {
  private final static Logger LOG = LoggerFactory.getLogger(RomService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private FolderLookupService folderLookupService;

  @NonNull
  public ScanResult scanGameFile(@NonNull Game game) {
    if (game.isVpxGame()) {
      if (game.getGameFile().exists()) {
        File gameScriptFolder  = folderLookupService.getScriptsFolder(game);
        File scripts = gameScriptFolder != null ? gameScriptFolder : game.getGameFile().getParentFile();
        ScanResult scan = VPXFileScanner.scan(game.getGameFile(), scripts);
        if (!StringUtils.isEmpty(scan.getRom())) {
          ScoringDB scoringDatabase = systemService.getScoringDatabase();
          Optional<ScoringDBMapping> first = scoringDatabase.getHighscoreMappings().stream().filter(mapping -> mapping.getScannedRom() != null && mapping.getScannedRom().equals(scan.getRom())).findFirst();
          if (first.isPresent()) {
            ScoringDBMapping scoringDBMapping = first.get();
            if (!StringUtils.isEmpty(scoringDBMapping.getScannedRom())) {
              updateScanResult(scoringDBMapping, scan);
              LOG.info("Applied scoring DB post processing for scan of \"{}\"", game.getGameDisplayName());
            }
          }
          else {
            first = scoringDatabase.getHighscoreMappings().stream().filter(mapping -> mapping.getRom().equals(scan.getRom())).findFirst();
            if (first.isPresent()) {
              //enrich the scan result with data from the scoringdb.json
              ScoringDBMapping scoringDBMapping = first.get();
              updateScanResult(scoringDBMapping, scan);
              LOG.info("Applied scoring DB post processing for scan of \"{}\"", game.getGameDisplayName());
            }
          }
        }

        return scan;
      }

      LOG.info("Skipped reading of \"{}\", VPX file '{}' does not exist.", game.getGameDisplayName(), game.getGameFile().getAbsolutePath());
      return new ScanResult();
    }
    LOG.info("Skipped reading of \"{}\" (emulator '{}'), only VPX tables can be scanned.", game.getGameDisplayName(), game.getEmulator());
    return new ScanResult();
  }

  private static void updateScanResult(ScoringDBMapping scoringDBMapping, ScanResult scan) {
    if (!StringUtils.isEmpty(scoringDBMapping.getScannedRom())) {
      scan.setRom(scoringDBMapping.getRom());
      scan.setTableName(scoringDBMapping.getTableName());
      scan.setHsFileName(scoringDBMapping.getTextFile());
      return;
    }

    if (scoringDBMapping.getTextFile() != null) {
      scan.setHsFileName(scoringDBMapping.getTextFile());
    }
    if (scoringDBMapping.getTableName() != null) {
      scan.setTableName(scoringDBMapping.getTableName());
    }
  }
}
