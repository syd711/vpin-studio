package de.mephisto.vpin.restclient.vpx;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single tweakable option parsed from a VPX table script.
 * <p>
 * Scripts declare options using the pattern:
 * Table1.Option("Name", minValue, maxValue, step, defaultValue, unit [, Array("opt1", "opt2")])
 * <p>
 * unit: 0 = None, 1 = Percent
 * <p>
 * Values are persisted in a per-table INI file located alongside the .vpx file,
 * under the [TableOption] section.
 */
public class TableScriptOption {

  /**
   * Display name of the option (used as the INI key).
   */
  private String name;

  /**
   * Minimum allowed value.
   */
  private double minValue;

  /**
   * Maximum allowed value.
   */
  private double maxValue;

  /**
   * Increment step between valid values.
   */
  private double step;

  /**
   * Default value if no override is present in the INI.
   */
  private double defaultValue;

  /**
   * Unit type:
   * 0 = None (raw number)
   * 1 = Percent (display as %, multiply by 100 for display)
   */
  private int unit;

  /**
   * Optional list of literal string labels for each step value.
   * When present the option should be rendered as a combo/choice control.
   * Index 0 corresponds to minValue, index 1 to minValue+step, etc.
   */
  private List<String> literalOptions = new ArrayList<>();

  /**
   * Current value read from the table INI file, or defaultValue if no
   * override has been saved yet.
   */
  private double currentValue;

  // ── Constructors ─────────────────────────────────────────────────────────

  public TableScriptOption() {
  }

  public TableScriptOption(String name, double minValue, double maxValue,
                           double step, double defaultValue, int unit) {
    this.name = name;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.step = step;
    this.defaultValue = defaultValue;
    this.unit = unit;
    this.currentValue = defaultValue;
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  /**
   * Returns true when literal string labels are available for this option.
   */
  public boolean hasLiteralOptions() {
    return literalOptions != null && !literalOptions.isEmpty();
  }

  /**
   * Returns the label for the given raw value index, or null if no labels
   * have been defined.
   */
  public String getLabelForValue(double value) {
    if (!hasLiteralOptions()) return null;
    int index = (int) Math.round((value - minValue) / step);
    if (index >= 0 && index < literalOptions.size()) {
      return literalOptions.get(index);
    }
    return null;
  }

  /**
   * Calculates how many discrete steps exist between min and max.
   * Useful for configuring a Slider's tick count.
   */
  public int getStepCount() {
    if (step == 0) return 1;
    return (int) Math.round((maxValue - minValue) / step);
  }

  /**
   * Returns the display suffix string based on the unit field.
   */
  public String getUnitSuffix() {
    return unit == 1 ? "%" : "";
  }

  // ── Getters / Setters ────────────────────────────────────────────────────

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getMinValue() {
    return minValue;
  }

  public void setMinValue(double minValue) {
    this.minValue = minValue;
  }

  public double getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(double maxValue) {
    this.maxValue = maxValue;
  }

  public double getStep() {
    return step;
  }

  public void setStep(double step) {
    this.step = step;
  }

  public double getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(double defaultValue) {
    this.defaultValue = defaultValue;
  }

  public int getUnit() {
    return unit;
  }

  public void setUnit(int unit) {
    this.unit = unit;
  }

  public List<String> getLiteralOptions() {
    return literalOptions;
  }

  public void setLiteralOptions(List<String> literalOptions) {
    this.literalOptions = literalOptions != null ? literalOptions : new ArrayList<>();
  }

  public double getCurrentValue() {
    return currentValue;
  }

  public void setCurrentValue(double currentValue) {
    this.currentValue = currentValue;
  }

  @Override
  public String toString() {
    return "TableScriptOption{name='" + name + "', min=" + minValue +
        ", max=" + maxValue + ", step=" + step +
        ", default=" + defaultValue + ", unit=" + unit +
        ", current=" + currentValue +
        ", literals=" + literalOptions + "}";
  }
}
