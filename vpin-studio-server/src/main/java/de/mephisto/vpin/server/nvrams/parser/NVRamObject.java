package de.mephisto.vpin.server.nvrams.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSetter;

public class NVRamObject {

  private List<String> notes = new ArrayList<>();

  public List<String> getNotes() {
    return notes;
  }

  @JsonSetter("_notes")
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void addNotes(Object notes) {
    if (notes instanceof Collection) {
      this.notes.addAll((Collection) notes);
    }
    else if (notes != null) {
      this.notes.add(notes.toString());
    }
  }
}
