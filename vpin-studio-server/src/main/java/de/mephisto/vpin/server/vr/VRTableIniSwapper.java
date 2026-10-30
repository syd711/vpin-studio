package de.mephisto.vpin.server.vr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Swaps the table specific .ini file of a VPX table with its VR variant while the table is played in VR mode.
 * <p>
 * At rest:       &lt;table&gt;.ini = desktop ini, &lt;table&gt;.vr.ini = VR ini
 * While swapped: &lt;table&gt;.ini = VR ini,      &lt;table&gt;.ini.desktop = parked desktop ini (only if one existed)
 * <p>
 * Every active swap is recorded in a journal file (one absolute .ini path per line), so swaps that were never
 * reverted by a game exit event (crash, server restart, launch from Studio) can be restored later.
 */
public class VRTableIniSwapper {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final String VR_INI_SUFFIX = ".vr.ini";
  public static final String DESKTOP_INI_SUFFIX = ".desktop";

  private final File journalFile;

  public VRTableIniSwapper(@NonNull File journalFile) {
    this.journalFile = journalFile;
  }

  @NonNull
  public static File getVRIniFile(@NonNull File iniFile) {
    return new File(iniFile.getParentFile(), FilenameUtils.getBaseName(iniFile.getName()) + VR_INI_SUFFIX);
  }

  @NonNull
  public static File getParkedDesktopIniFile(@NonNull File iniFile) {
    return new File(iniFile.getParentFile(), iniFile.getName() + DESKTOP_INI_SUFFIX);
  }

  /**
   * Puts the VR ini of the table in place of the table ini.
   *
   * @return true if the VR ini is active afterwards
   */
  public synchronized boolean swapIn(@NonNull File iniFile) {
    File vrIniFile = getVRIniFile(iniFile);
    File parkedFile = getParkedDesktopIniFile(iniFile);

    if (isSwapped(iniFile)) {
      LOG.info("[VR ini] VR ini is already active for {}, skipping swap.", iniFile.getAbsolutePath());
      return true;
    }
    if (!vrIniFile.exists()) {
      LOG.info("[VR ini] No VR ini {} found, keeping {}", vrIniFile.getAbsolutePath(), iniFile.getAbsolutePath());
      return false;
    }
    if (parkedFile.exists()) {
      LOG.error("[VR ini] Cannot swap in VR ini for {}, the parked desktop ini {} already exists.", iniFile.getAbsolutePath(), parkedFile.getAbsolutePath());
      return false;
    }

    LOG.info("[VR ini] Swapping in VR ini for {}", iniFile.getAbsolutePath());
    addToJournal(iniFile);

    if (iniFile.exists()) {
      if (!rename(iniFile, parkedFile)) {
        removeFromJournal(iniFile);
        return false;
      }
    }
    else {
      LOG.info("[VR ini] No desktop ini {} exists, nothing to park.", iniFile.getAbsolutePath());
    }

    if (!rename(vrIniFile, iniFile)) {
      //roll back the parked desktop ini
      if (parkedFile.exists()) {
        rename(parkedFile, iniFile);
      }
      removeFromJournal(iniFile);
      return false;
    }

    LOG.info("[VR ini] VR ini is active for {}", iniFile.getAbsolutePath());
    return true;
  }

  /**
   * Reverts a swap done by {@link #swapIn(File)}: the active ini goes back to the VR ini
   * (keeping all changes made during the VR session) and the desktop ini is restored.
   *
   * @return true if a swap was reverted
   */
  public synchronized boolean swapOut(@NonNull File iniFile) {
    if (!isSwapped(iniFile)) {
      LOG.info("[VR ini] No active VR ini swap for {}, nothing to restore.", iniFile.getAbsolutePath());
      return false;
    }
    restore(iniFile);
    return true;
  }

  /**
   * Reverts all swaps found in the journal.
   */
  public synchronized void restoreAll() {
    List<File> activeSwaps = getActiveSwaps();
    if (activeSwaps.isEmpty()) {
      return;
    }
    LOG.info("[VR ini] Restoring {} active VR ini swap(s)", activeSwaps.size());
    for (File iniFile : activeSwaps) {
      restore(iniFile);
    }
  }

  public synchronized boolean isSwapped(@NonNull File iniFile) {
    return readJournal().contains(iniFile.getAbsolutePath());
  }

  @NonNull
  public synchronized List<File> getActiveSwaps() {
    List<File> result = new ArrayList<>();
    for (String path : readJournal()) {
      result.add(new File(path));
    }
    return result;
  }

  /**
   * Restores the at-rest state, regardless of how far the swap-in got.
   */
  private void restore(@NonNull File iniFile) {
    File vrIniFile = getVRIniFile(iniFile);
    File parkedFile = getParkedDesktopIniFile(iniFile);
    LOG.info("[VR ini] Swapping out VR ini for {}", iniFile.getAbsolutePath());

    //the active ini is the VR ini, move it back
    if (!vrIniFile.exists() && iniFile.exists()) {
      if (!rename(iniFile, vrIniFile)) {
        LOG.error("[VR ini] Failed to restore VR ini {}, keeping the swap journal entry.", vrIniFile.getAbsolutePath());
        return;
      }
    }

    //restore the parked desktop ini
    if (parkedFile.exists()) {
      if (iniFile.exists()) {
        LOG.error("[VR ini] Cannot restore parked desktop ini {}, {} still exists, keeping the swap journal entry.", parkedFile.getAbsolutePath(), iniFile.getAbsolutePath());
        return;
      }
      if (!rename(parkedFile, iniFile)) {
        return;
      }
    }
    else {
      LOG.info("[VR ini] No parked desktop ini {} found, nothing to restore.", parkedFile.getAbsolutePath());
    }

    removeFromJournal(iniFile);
    LOG.info("[VR ini] Desktop ini is active for {}", iniFile.getAbsolutePath());
  }

  private static boolean rename(@NonNull File from, @NonNull File to) {
    if (from.renameTo(to)) {
      LOG.info("[VR ini] Renamed {} → {}", from.getAbsolutePath(), to.getAbsolutePath());
      return true;
    }
    LOG.error("[VR ini] Failed to rename {} → {}", from.getAbsolutePath(), to.getAbsolutePath());
    return false;
  }

  //------------------------------------- Journal ---------------------------------------------------------------------

  @NonNull
  private Set<String> readJournal() {
    Set<String> entries = new LinkedHashSet<>();
    if (journalFile.exists()) {
      try {
        for (String line : FileUtils.readLines(journalFile, StandardCharsets.UTF_8)) {
          if (!line.isBlank()) {
            entries.add(line.trim());
          }
        }
      }
      catch (IOException e) {
        LOG.error("[VR ini] Failed to read swap journal {}: {}", journalFile.getAbsolutePath(), e.getMessage(), e);
      }
    }
    return entries;
  }

  private void addToJournal(@NonNull File iniFile) {
    Set<String> entries = readJournal();
    entries.add(iniFile.getAbsolutePath());
    writeJournal(entries);
  }

  private void removeFromJournal(@NonNull File iniFile) {
    Set<String> entries = readJournal();
    if (entries.remove(iniFile.getAbsolutePath())) {
      writeJournal(entries);
    }
  }

  private void writeJournal(@NonNull Set<String> entries) {
    try {
      if (entries.isEmpty()) {
        if (journalFile.exists() && !journalFile.delete()) {
          LOG.error("[VR ini] Failed to delete swap journal {}", journalFile.getAbsolutePath());
        }
        return;
      }
      FileUtils.writeLines(journalFile, StandardCharsets.UTF_8.name(), entries);
    }
    catch (IOException e) {
      LOG.error("[VR ini] Failed to write swap journal {}: {}", journalFile.getAbsolutePath(), e.getMessage(), e);
    }
  }
}
