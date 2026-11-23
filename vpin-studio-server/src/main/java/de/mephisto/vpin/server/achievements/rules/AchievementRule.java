package de.mephisto.vpin.server.achievements.rules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * An achievement rule, JSON compatible with the Achievement Watcher's rule files
 * ({@code global_achievements.json} and {@code <rom>.ach.json}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"title", "condition", "scope"})
public class AchievementRule {
  public static final String SCOPE_GLOBAL = "global";
  public static final String SCOPE_SESSION = "session";

  @JsonProperty("title")
  private String title;
  @JsonProperty("condition")
  private RuleCondition condition;
  @JsonProperty("scope")
  private String scope;

  public AchievementRule() {
  }

  public AchievementRule(String title, RuleCondition condition, String scope) {
    this.title = title;
    this.condition = condition;
    this.scope = scope;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public RuleCondition getCondition() {
    return condition;
  }

  public void setCondition(RuleCondition condition) {
    this.condition = condition;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @JsonIgnore
  public boolean isGlobal() {
    return SCOPE_GLOBAL.equalsIgnoreCase(scope == null ? "" : scope.trim());
  }

  @JsonIgnore
  public String getTitleOrDefault() {
    return title == null || title.isBlank() ? "Achievement" : title.trim();
  }

  @Override
  public String toString() {
    return title;
  }
}
