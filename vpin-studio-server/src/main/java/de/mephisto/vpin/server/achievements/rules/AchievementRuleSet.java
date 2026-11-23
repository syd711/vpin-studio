package de.mephisto.vpin.server.achievements.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A rule file: {"rules": [...]}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AchievementRuleSet {
  public static final ObjectMapper MAPPER = JsonMapper.builder()
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .enable(SerializationFeature.INDENT_OUTPUT)
      .build();

  private List<AchievementRule> rules = new ArrayList<>();

  public AchievementRuleSet() {
  }

  public AchievementRuleSet(List<AchievementRule> rules) {
    this.rules = rules;
  }

  public List<AchievementRule> getRules() {
    return rules;
  }

  public void setRules(List<AchievementRule> rules) {
    this.rules = rules != null ? rules : new ArrayList<>();
  }

  @NonNull
  public static AchievementRuleSet load(@NonNull File file) throws IOException {
    return MAPPER.readValue(file, AchievementRuleSet.class);
  }

  @NonNull
  public static AchievementRuleSet load(@NonNull InputStream in) throws IOException {
    return MAPPER.readValue(in, AchievementRuleSet.class);
  }

  public void save(@NonNull File file) throws IOException {
    File parent = file.getParentFile();
    if (parent != null && !parent.exists()) {
      parent.mkdirs();
    }
    File tmp = new File(file.getAbsolutePath() + ".tmp");
    MAPPER.writeValue(tmp, this);
    if (file.exists()) {
      file.delete();
    }
    if (!tmp.renameTo(file)) {
      throw new IOException("Failed to rename " + tmp + " to " + file);
    }
  }
}
