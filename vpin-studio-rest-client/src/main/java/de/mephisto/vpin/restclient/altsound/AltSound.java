package de.mephisto.vpin.restclient.altsound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.restclient.representations.ValidationState;
import org.springframework.lang.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AltSound {
  private String format;
  private List<AltSoundEntry> entries = new ArrayList<>();
  private List<String> headers = new ArrayList<>();
  private int files;
  private long filesize;
  private Date modificationDate;
  private boolean missingAudioFiles;
  private List<ValidationState> validationStates;

  //altsound2
  private boolean recordSoundCmds;
  private boolean romVolumeControl;

  private File csvFile;

  private AltSound2Group music;
  private List<AltSound2DuckingProfile> musicDuckingProfiles;

  private AltSound2Group callout;
  private List<AltSound2DuckingProfile> calloutDuckingProfiles;

  private AltSound2Group sfx;
  private List<AltSound2DuckingProfile> sfxDuckingProfiles;

  private AltSound2Group solo;
  private List<AltSound2DuckingProfile> soloDuckingProfiles;

  private AltSound2Group overlay;
  private List<AltSound2DuckingProfile> overlayDuckingProfiles;

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
}
