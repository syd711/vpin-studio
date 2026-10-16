package de.mephisto.vpin.restclient.altsound;

import java.util.ArrayList;
import java.util.List;

/**
 * The sound names of an ALT sound pack that are shared with the rom2altsound community naming effort.
 * Names are attached to the sound ROM (sound_rom_id), not to a game revision.
 */
public class Rom2AltSoundNames {
  /**
   * The names are shared as CC0, the user has to opt in before they are sent.
   */
  public static final String LICENSE = "CC0";

  private String rom;
  private String soundRomId;
  private String version;
  private List<Entry> entries = new ArrayList<>();
  /**
   * The name of the uploaded file, set after the upload
   */
  private String uploadedFile;
  private String error;

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public String getSoundRomId() {
    return soundRomId;
  }

  public void setSoundRomId(String soundRomId) {
    this.soundRomId = soundRomId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public List<Entry> getEntries() {
    return entries;
  }

  public void setEntries(List<Entry> entries) {
    this.entries = entries;
  }

  public String getUploadedFile() {
    return uploadedFile;
  }

  public void setUploadedFile(String uploadedFile) {
    this.uploadedFile = uploadedFile;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public boolean isExportable() {
    return error == null && soundRomId != null && !entries.isEmpty();
  }

  public static class Entry {
    private String id;
    private String name;

    public Entry() {
    }

    public Entry(String id, String name) {
      this.id = id;
      this.name = name;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
