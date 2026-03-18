package de.mephisto.vpin.server.nvrams.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.nvrams.parser.NVRamToolDecoder.SearchResult;
import edu.umd.cs.findbugs.annotations.Nullable;

public class NVRamToolMapGenerator {

  private static final String[] LABELS = {"First Place", "Second Place", "Third Place", "Fourth Place",
    "Fifth Place", "Sixth Place", "Seventh Place", "Eighth Place", "Ninth Place", "Tenth Place"
  };
  private static final String[] SHORT_LABELS = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th" };

  public static final File DECODED_ROOT = new File("C:/temp/_NVRAMS/decoded");


  
  public void generateHighscores(String rom, VpsTable table, boolean useHexForPosition,
      LinkedHashMap<Score, SearchResult> selectedScores, LinkedHashMap<String, SearchResult> checksums) throws IOException {

    // read or create an existing map
    JsonObject map = readOrCreateMap(rom, table);

    JsonArray highScores = new JsonArray();
    JsonArray modeChampion = new JsonArray();
    
    // calculate how often a score label is used
    // for generating labels like CHAMPION#3
    Map<String, Long> occursMap = selectedScores.keySet().stream()
              .collect(Collectors.groupingBy(sc -> sc.getLabel(), Collectors.counting()));

    int index = 1;
    Map<String, Integer> indexMap = new HashMap<>();

    for (Entry<Score, SearchResult> sr : selectedScores.entrySet()) {
      Score sc = sr.getKey();
      SearchResult result = sr.getValue();

      // generate the name of the score
      String label = sc.getLabel();
      String shortLabel;
      if (label != null && !StringUtils.containsIgnoreCase(label, "Highest Score")) {
        long occurrence = occursMap.get(label);
        label = capitalize(label);
        shortLabel = abbreviate(label);
        if (occurrence > 1) {
          int idx = Objects.requireNonNullElse(indexMap.get(label), 1);
          indexMap.put(label, idx+1);

          label += " #" + idx;
          shortLabel += idx;
        }
      }
      else {
        // index starts with 1 !!
        label = index <= LABELS.length ? LABELS[index-1] : "Position " + index;
        shortLabel = index <= SHORT_LABELS.length ? SHORT_LABELS[index-1] : index + "th";
        index++;
      }

      JsonObject score = new JsonObject();
      score.addProperty("label", label);
      score.addProperty("short_label", shortLabel);
      score.add("initials", createMapping(result.initialPosition, 3, "ch", useHexForPosition));
      JsonObject scoreObject = createMapping(result.scorePosition, result.scoreLength, "bcd", useHexForPosition);
      score.add("score", scoreObject);
      if (StringUtils.isNotEmpty(sc.getSuffix())) {
        scoreObject.addProperty("suffix", capitalize(sc.getSuffix()));
        modeChampion.add(score);
      }
      else {
        highScores.add(score);
      }
    }

    if (highScores.size() > 0) {
      map.add("high_scores", highScores);
    }
    if (modeChampion.size() > 0) {
      map.add("mode_champions", modeChampion);
    }


    //-------
    JsonArray checksums16 = new JsonArray();

    for (Entry<String, SearchResult> sr : checksums.entrySet()) {
      String label = sr.getKey();
      SearchResult result = sr.getValue();
      checksums16.add(createMapping(label, result.scorePosition, result.scorePosition + result.scoreLength, useHexForPosition));
    }

    writeMap(rom, map);
  }

  private JsonObject readOrCreateMap(String rom, @Nullable VpsTable table) throws IOException, FileNotFoundException {
    JsonObject map;

    File mapfile = new File(DECODED_ROOT, rom + ".map.json");
    if (mapfile.exists()) {
      try (FileReader reader = new FileReader(mapfile)) {
        JsonReader jsonreader = new JsonReader(reader);
        jsonreader.setLenient(true);
        map = (JsonObject) JsonParser.parseReader(jsonreader);
      }
    }
    else {
      map = new JsonObject();
    }

    if (!map.has("_notes")) {
      JsonArray notes = new JsonArray();
      notes.add("Compiled by Olivier Leprince");
      if (table != null) {
        notes.add(table.getDisplayName());
        if (table.getIpdbUrl() != null) {
          notes.add(table.getIpdbUrl());
        }
        if (table.getMPU() != null) {
          notes.add(table.getMPU());
        }
      }
      map.add("_notes", notes);
    }

    // override _fileFormat if present
    map.addProperty("_fileformat", 0.8);

    if (!map.has("_metadata")) {
      JsonObject _metadata = new JsonObject();
      _metadata.addProperty("version", 1);
      JsonArray copyright = new JsonArray();
      copyright.add("Copyright (C) 2026 by Olivier Leprince <leprinco@yahoo.com>");
      copyright.add("Copyright (C) 2026 by Tom Collins <tom@scorbit.io>");
      _metadata.add("copyright", copyright);

      _metadata.addProperty("license", "GNU Lesser General Public License v3.0");
      if (table != null && table.getMPU() != null) {
        String platformName = table.getMPU();
        NVRamParser p = new NVRamParser();
        String platform = p.mapPathForPlatform(platformName);
        if (platform != null) {
          _metadata.addProperty("platform", platform);
        }
      }

      JsonArray roms = new JsonArray();
      roms.add(rom);
      _metadata.add("roms", roms);

      map.add("_metadata", _metadata);
    }
    return map;
  }

  private void writeMap(String rom, JsonObject map) throws IOException {
    File mapfile = new File(DECODED_ROOT, rom + ".map.json");
    try (FileWriter writer = new FileWriter(mapfile)) {
      JsonWriter jsonWriter = new JsonWriter(writer);
      jsonWriter.setIndent("  ");
      jsonWriter.setLenient(true);
      Streams.write(map, jsonWriter);
    }
  }

  public void appendText(String rom, String text) throws IOException {
    File mapfile = new File(DECODED_ROOT, rom + ".map.json");
    try (FileWriter writer = new FileWriter(mapfile, true)) {
      writer.append(text);
    }
  }

  private JsonObject createMapping(int position, int length, String encoding, boolean useHexForPosition) {
    JsonObject mapping = new JsonObject();
    if (useHexForPosition) {
      mapping.addProperty("start", "0x" + Integer.toHexString(position).toUpperCase());
    } else {
      mapping.addProperty("start", position);
    }
    mapping.addProperty("encoding", encoding);
    mapping.addProperty("length", length);
    return mapping;
  }

  private JsonObject createMapping(String label, int start, int end, boolean useHexForPosition) {
    JsonObject mapping = new JsonObject();
    if (useHexForPosition) {
      mapping.addProperty("start", "0x" + Integer.toHexString(start).toUpperCase());
    } else {
      mapping.addProperty("start", start);
    }
    if (useHexForPosition) {
      mapping.addProperty("end", "0x" + Integer.toHexString(end).toUpperCase());
    } else {
      mapping.addProperty("end", end);
    }
    mapping.addProperty("label", label);
    return mapping;
  }


    public static String capitalize(String input) {
    return Arrays.stream(input.split("\\s+"))
                 .map(word -> StringUtils.capitalize(word.toLowerCase()))
                 .collect(Collectors.joining(" "));
  }

  public static String abbreviate(String input) {
    return Arrays.stream(input.trim().split("\\s+"))
                 .map(word -> String.valueOf(word.charAt(0)))
                 .collect(Collectors.joining())
                 .toUpperCase();
  }
}