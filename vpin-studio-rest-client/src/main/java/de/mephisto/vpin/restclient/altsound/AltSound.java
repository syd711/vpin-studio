package de.mephisto.vpin.restclient.altsound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.validation.ValidationState;
import org.springframework.lang.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AltSound {
  private String name;
  private String format;
  private List<AltSoundEntry> entries = new ArrayList<>();
  private List<String> headers = new ArrayList<>();
  private int files;
  private long filesize;
  private Date modificationDate;
  private boolean missingAudioFiles;
  private List<ValidationState> validationStates;

  private int gameId;
  private String folder;

  //altsound2
  private int commandSkipCount;
  private boolean recordSoundCmds;
  private boolean romVolumeControl;

  private File csvFile;

  private AltSound2Group music;
  private List<AltSound2DuckingProfile> musicDuckingProfiles = new ArrayList<>();

  private AltSound2Group callout;
  private List<AltSound2DuckingProfile> calloutDuckingProfiles = new ArrayList<>();

  private AltSound2Group sfx;
  private List<AltSound2DuckingProfile> sfxDuckingProfiles = new ArrayList<>();

  private AltSound2Group solo;
  private List<AltSound2DuckingProfile> soloDuckingProfiles = new ArrayList<>();

  private AltSound2Group overlay;
  private List<AltSound2DuckingProfile> overlayDuckingProfiles = new ArrayList<>();

  public String getFolder() {
    return folder;
  }

  public void setFolder(String folder) {
    this.folder = folder;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public int getCommandSkipCount() {
    return commandSkipCount;
  }

  public void setCommandSkipCount(int commandSkipCount) {
    this.commandSkipCount = commandSkipCount;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonIgnore
  @Nullable
  public File getCsvFile() {
    return csvFile;
  }

  public void setCsvFile(File csvFile) {
    this.csvFile = csvFile;
  }

  public boolean isRecordSoundCmds() {
    return recordSoundCmds;
  }

  public void setRecordSoundCmds(boolean recordSoundCmds) {
    this.recordSoundCmds = recordSoundCmds;
  }

  public boolean isRomVolumeControl() {
    return romVolumeControl;
  }

  public void setRomVolumeControl(boolean romVolumeControl) {
    this.romVolumeControl = romVolumeControl;
  }

  public AltSound2Group getMusic() {
    return music;
  }

  public void setMusic(AltSound2Group music) {
    this.music = music;
  }

  public List<AltSound2DuckingProfile> getMusicDuckingProfiles() {
    return musicDuckingProfiles;
  }

  public void setMusicDuckingProfiles(List<AltSound2DuckingProfile> musicDuckingProfiles) {
    this.musicDuckingProfiles = musicDuckingProfiles;
  }

  public AltSound2Group getCallout() {
    return callout;
  }

  public void setCallout(AltSound2Group callout) {
    this.callout = callout;
  }

  public List<AltSound2DuckingProfile> getCalloutDuckingProfiles() {
    return calloutDuckingProfiles;
  }

  public void setCalloutDuckingProfiles(List<AltSound2DuckingProfile> calloutDuckingProfiles) {
    this.calloutDuckingProfiles = calloutDuckingProfiles;
  }

  public AltSound2Group getSfx() {
    return sfx;
  }

  public void setSfx(AltSound2Group sfx) {
    this.sfx = sfx;
  }

  public List<AltSound2DuckingProfile> getSfxDuckingProfiles() {
    return sfxDuckingProfiles;
  }

  public void setSfxDuckingProfiles(List<AltSound2DuckingProfile> sfxDuckingProfiles) {
    this.sfxDuckingProfiles = sfxDuckingProfiles;
  }

  public AltSound2Group getSolo() {
    return solo;
  }

  public void setSolo(AltSound2Group solo) {
    this.solo = solo;
  }

  public List<AltSound2DuckingProfile> getSoloDuckingProfiles() {
    return soloDuckingProfiles;
  }

  public void setSoloDuckingProfiles(List<AltSound2DuckingProfile> soloDuckingProfiles) {
    this.soloDuckingProfiles = soloDuckingProfiles;
  }

  public AltSound2Group getOverlay() {
    return overlay;
  }

  public void setOverlay(AltSound2Group overlay) {
    this.overlay = overlay;
  }

  public List<AltSound2DuckingProfile> getOverlayDuckingProfiles() {
    return overlayDuckingProfiles;
  }

  public void setOverlayDuckingProfiles(List<AltSound2DuckingProfile> overlayDuckingProfiles) {
    this.overlayDuckingProfiles = overlayDuckingProfiles;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public List<ValidationState> getValidationStates() {
    return validationStates;
  }

  public void setValidationStates(List<ValidationState> validationStates) {
    this.validationStates = validationStates;
  }

  public boolean isMissingAudioFiles() {
    return missingAudioFiles;
  }

  public void setMissingAudioFiles(boolean missingAudioFiles) {
    this.missingAudioFiles = missingAudioFiles;
  }

  public List<String> getHeaders() {
    return headers;
  }

  public void setHeaders(List<String> headers) {
    this.headers = headers;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }

  public int getFiles() {
    return files;
  }

  public void setFiles(int files) {
    this.files = files;
  }

  public long getFilesize() {
    return filesize;
  }

  public void setFilesize(long filesize) {
    this.filesize = filesize;
  }

  public List<AltSoundEntry> getEntries() {
    return entries;
  }

  public void setEntries(List<AltSoundEntry> entries) {
    this.entries = entries;
  }

  public String toCSV() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"");
    builder.append(String.join("\",\"", this.headers));
    builder.append("\"");
    builder.append("\n");

    for (AltSoundEntry entry : this.entries) {
      builder.append(entry.toCSV(this));
      builder.append("\n");
    }

    return builder.toString();
  }

  public String toGSoundCSV() {
    StringBuilder builder = new StringBuilder();
    builder.append(String.join(",", this.headers));
    builder.append("\n");

    for (AltSoundEntry entry : this.entries) {
      builder.append(entry.toGSoundCSV(this));
      builder.append("\n");
    }

    return builder.toString();
  }

  public List<AltSound2DuckingProfile> getProfiles(AltSound2SampleType sampleType) {
    switch (sampleType) {
      case sfx: {
        return getSfxDuckingProfiles();
      }
      case music: {
        return getMusicDuckingProfiles();
      }
      case callout: {
        return getCalloutDuckingProfiles();
      }
      case solo: {
        return getSoloDuckingProfiles();
      }
      case overlay: {
        return getOverlayDuckingProfiles();
      }
      default: {
        throw new UnsupportedOperationException("Invalid sample type");
      }
    }
  }

  public AltSound2DuckingProfile getProfile(AltSound2SampleType sampleType, int id) {
    switch (sampleType) {
      case sfx: {
        return getSfxDuckingProfiles().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
      }
      case music: {
        return getMusicDuckingProfiles().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
      }
      case callout: {
        return getCalloutDuckingProfiles().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
      }
      case solo: {
        return getSoloDuckingProfiles().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
      }
      case overlay: {
        return getOverlayDuckingProfiles().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
      }
      default: {
        throw new UnsupportedOperationException("Invalid sample type");
      }
    }
  }

  public void addProfile(AltSound2DuckingProfile profile) {
    switch (profile.getType()) {
      case sfx: {
        getSfxDuckingProfiles().add(profile);
        break;
      }
      case music: {
        getMusicDuckingProfiles().add(profile);
        break;
      }
      case callout: {
        getCalloutDuckingProfiles().add(profile);
        break;
      }
      case solo: {
        getSoloDuckingProfiles().add(profile);
        break;
      }
      case overlay: {
        getOverlayDuckingProfiles().add(profile);
        break;
      }
      default: {
        throw new UnsupportedOperationException("Invalid sample type");
      }
    }
  }

  public AltSound2Group getGroup(AltSound2SampleType sampleType) {
    switch (sampleType) {
      case sfx: {
        return sfx;
      }
      case music: {
        return music;
      }
      case callout: {
        return callout;
      }
      case solo: {
        return solo;
      }
      case overlay: {
        return overlay;
      }
      default: {
        throw new UnsupportedOperationException("Invalid sample type");
      }
    }
  }

  public void removeDuckingProfileValue(AltSound2SampleType profileListType, AltSound2SampleType typeToRemove) {
    switch (profileListType) {
      case sfx: {
        getSfxDuckingProfiles().stream().forEach(p -> p.removeProfileValue(typeToRemove));
        break;
      }
      case callout: {
        getCalloutDuckingProfiles().stream().forEach(p -> p.removeProfileValue(typeToRemove));
        break;
      }
      case overlay: {
        getOverlayDuckingProfiles().stream().forEach(p -> p.removeProfileValue(typeToRemove));
        break;
      }
    }
  }

  public void addDuckingProfileValue(AltSound2SampleType profileListType, AltSound2SampleType typeToAdd) {
    switch (profileListType) {
      case sfx: {
        getSfxDuckingProfiles().stream().forEach(p -> p.addProfileValue(typeToAdd, 60));
        break;
      }
      case callout: {
        getCalloutDuckingProfiles().stream().forEach(p -> p.addProfileValue(typeToAdd, 60));
        break;
      }
      case overlay: {
        getOverlayDuckingProfiles().stream().forEach(p -> p.addProfileValue(typeToAdd, 60));
        break;
      }
    }
  }
}
