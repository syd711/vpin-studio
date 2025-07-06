package de.mephisto.vpin.restclient.recorder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class RecordingDataSummary {
  private List<RecordingData> recordingData = new ArrayList<>();

  public List<RecordingData> getRecordingData() {
    return recordingData;
  }

  public void setRecordingData(List<RecordingData> recordingData) {
    this.recordingData = recordingData;
  }

  public void add(RecordingData recordingData) {
    if (!this.recordingData.contains(recordingData)) {
      this.recordingData.add(recordingData);
    }
  }

  public void remove(int id) {
    Iterator<RecordingData> iterator = recordingData.iterator();
    while (iterator.hasNext()) {
      if (iterator.next().getGameId() == id) {
        iterator.remove();
      }
    }
  }

  public boolean contains(int gameId) {
    for (RecordingData data : recordingData) {
      if (data.getGameId() == gameId) {
        return true;
      }
    }
    return false;
  }

  public void clear() {
    this.recordingData.clear();
  }

  public void addAll(List<RecordingData> collect) {
    this.recordingData.addAll(collect);
  }

  public boolean isEmpty() {
    return this.recordingData.isEmpty();
  }

  public int size() {
    return this.recordingData.size();
  }

  public RecordingData get(int gameId) {
    return recordingData.stream().filter(d -> d.getGameId() == gameId).findFirst().orElse(null);
  }

  public List<Integer> getGameIds() {
    return this.recordingData.stream().map(d -> d.getGameId()).collect(Collectors.toList());
  }
}
