package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.server.games.Game;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.File;

public class RecordingResult {
  private long duration = 0;
  private String fileName;
  private String command;
  private String infoLog;
  private String errorLog;

  private Game game;
  private File recordingTempFile;
  private VPinScreen screen;
  private RecordingScreenOptions recordingScreenOptions;

  public Game getGame() {
    return game;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public File getRecordingTempFile() {
    return recordingTempFile;
  }

  public void setRecordingTempFile(File recordingTempFile) {
    this.recordingTempFile = recordingTempFile;
  }

  public VPinScreen getScreen() {
    return screen;
  }

  public void setScreen(VPinScreen screen) {
    this.screen = screen;
  }

  public RecordingScreenOptions getRecordingScreenOptions() {
    return recordingScreenOptions;
  }

  public void setRecordingScreenOptions(RecordingScreenOptions recordingScreenOptions) {
    this.recordingScreenOptions = recordingScreenOptions;
  }

  public String getInfoLog() {
    return infoLog;
  }

  public void setInfoLog(String infoLog) {
    this.infoLog = infoLog;
  }

  public String getErrorLog() {
    return errorLog;
  }

  public void setErrorLog(String errorLog) {
    this.errorLog = errorLog;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public boolean hasRecorded() {
    return this.duration > 0;
  }

  @Override
  public String toString() {
    return "Recording of \"" + fileName + "\", duration: " + DurationFormatUtils.formatDuration(duration, "HH 'hours', mm 'minutes', ss 'seconds'");
  }
}
