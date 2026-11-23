package de.mephisto.vpin.server.achievements.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Condition of an achievement rule, JSON compatible with the Achievement Watcher's rule files.
 * Types: nvram_delta, session_time, nvram_tally, nvram_overall, rom_count, rom_complete_set, rom_multi_brand.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "field", "min", "min_seconds", "manufacturer", "manufacturers", "min_brands"})
public class RuleCondition {
  public static final String NVRAM_DELTA = "nvram_delta";
  public static final String SESSION_TIME = "session_time";
  public static final String NVRAM_TALLY = "nvram_tally";
  public static final String NVRAM_OVERALL = "nvram_overall";
  public static final String ROM_COUNT = "rom_count";
  public static final String ROM_COMPLETE_SET = "rom_complete_set";
  public static final String ROM_MULTI_BRAND = "rom_multi_brand";
  public static final String ANY_MANUFACTURER = "__any__";

  @JsonProperty("type")
  private String type;
  @JsonProperty("field")
  private String field;
  @JsonProperty("min")
  private Integer min;
  @JsonProperty("min_seconds")
  private Integer minSeconds;
  @JsonProperty("manufacturer")
  private String manufacturer;
  @JsonProperty("manufacturers")
  private List<String> manufacturers;
  @JsonProperty("min_brands")
  private Integer minBrands;

  public RuleCondition() {
  }

  public static RuleCondition nvramDelta(String field, int min) {
    RuleCondition c = new RuleCondition();
    c.type = NVRAM_DELTA;
    c.field = field;
    c.min = min;
    return c;
  }

  public static RuleCondition sessionTime(int seconds) {
    RuleCondition c = new RuleCondition();
    c.type = SESSION_TIME;
    c.minSeconds = seconds;
    return c;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public Integer getMin() {
    return min;
  }

  public void setMin(Integer min) {
    this.min = min;
  }

  public Integer getMinSeconds() {
    return minSeconds;
  }

  public void setMinSeconds(Integer minSeconds) {
    this.minSeconds = minSeconds;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public List<String> getManufacturers() {
    return manufacturers;
  }

  public void setManufacturers(List<String> manufacturers) {
    this.manufacturers = manufacturers;
  }

  public Integer getMinBrands() {
    return minBrands;
  }

  public void setMinBrands(Integer minBrands) {
    this.minBrands = minBrands;
  }

  /**
   * The Watcher's {@code int(cond.get("min", 1))}.
   */
  public int minOrDefault(int def) {
    return min != null ? min : def;
  }

  /**
   * The Watcher's {@code cond.get("min_seconds", cond.get("min", 0))}.
   */
  public int sessionSeconds() {
    if (minSeconds != null) {
      return minSeconds;
    }
    return min != null ? min : 0;
  }

  /**
   * A short human readable description of the condition, shown as tooltip.
   */
  public String describe() {
    if (type == null) {
      return "";
    }
    return switch (type) {
      case NVRAM_DELTA -> field + " ≥ " + min + " in one game";
      case SESSION_TIME -> "Play " + (sessionSeconds() / 60) + " minutes in one game";
      case NVRAM_TALLY -> field + " ≥ " + min + " in total over all tables";
      case NVRAM_OVERALL -> field + " reaches " + min;
      case ROM_COUNT -> {
        if (ANY_MANUFACTURER.equals(manufacturer)) {
          yield minBrands != null ? "Play tables of " + minBrands + " different manufacturers" : "Play " + minOrDefault(1) + " different tables";
        }
        yield "Play " + minOrDefault(1) + " different " + manufacturer + " tables";
      }
      case ROM_COMPLETE_SET -> ANY_MANUFACTURER.equals(manufacturer) ? "Play all installed tables" : "Play all installed " + manufacturer + " tables";
      case ROM_MULTI_BRAND -> "Play tables of " + String.join(", ", manufacturers != null ? manufacturers : List.of());
      default -> type;
    };
  }
}
