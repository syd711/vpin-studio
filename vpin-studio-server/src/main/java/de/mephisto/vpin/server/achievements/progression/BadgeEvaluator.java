package de.mephisto.vpin.server.achievements.progression;

import de.mephisto.vpin.restclient.achievements.BadgeInfo;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Port of the Achievement Watcher's {@code BADGE_CHECKS}, extended with progress values for locked badges.
 * Earned badges are sticky: once earned they stay earned.
 */
public final class BadgeEvaluator {

  private BadgeEvaluator() {
  }

  /**
   * @param alreadyEarned badges stored for the player
   */
  @NonNull
  public static List<BadgeInfo> evaluate(@NonNull BadgeStats s, @NonNull Collection<String> alreadyEarned, @Nullable String selectedBadge) {
    List<BadgeInfo> result = new ArrayList<>();
    for (Badge badge : Badge.values()) {
      BadgeInfo info = new BadgeInfo();
      info.id = badge.name();
      info.name = badge.getDisplayName();
      info.description = badge.getDescription();
      info.category = badge.getCategory().getLabel();
      apply(badge, s, info);
      if (alreadyEarned.contains(badge.name())) {
        info.earned = true;
      }
      if (info.earned) {
        info.progressText = null;
        info.current = Math.max(info.current, info.target);
      }
      info.selected = badge.name().equals(selectedBadge);
      result.add(info);
    }
    return result;
  }

  private static void apply(Badge badge, BadgeStats s, BadgeInfo i) {
    switch (badge) {
      case first_steps -> count(i, s.totalAchievements, 1);
      case getting_started -> count(i, s.totalAchievements, 5);
      case deca -> count(i, s.totalAchievements, 10);
      case half_century -> count(i, s.totalAchievements, 50);
      case century -> count(i, s.totalAchievements, 100);
      case hoarder -> count(i, s.totalAchievements, 500);
      case thousandaire -> count(i, s.totalAchievements, 1000);
      case first_star -> {
        count(i, s.totalAchievements, LevelCalculator.PRESTIGE_THRESHOLD);
        i.earned = s.prestige >= 1;
      }
      case two_stars -> prestige(i, s.prestige, 2);
      case three_stars -> prestige(i, s.prestige, 3);
      case four_stars -> prestige(i, s.prestige, 4);
      case five_stars -> {
        prestige(i, s.prestige, 5);
        i.earned = s.fullyMaxed;
      }
      case explorer -> count(i, s.romsPlayed.size(), 10);
      case globetrotter -> count(i, s.manufacturerRoms.size(), 5);
      case bally_fan -> count(i, s.manufacturerCount("Bally"), 5);
      case williams_fan -> count(i, s.manufacturerCount("Williams"), 5);
      case stern_fan -> count(i, s.manufacturerCount("Stern"), 5);
      case gottlieb_fan -> count(i, s.manufacturerCount("Gottlieb"), 5);
      case dedicated -> hours(i, s.totalPlaytimeSec, 10);
      case marathon -> hours(i, s.totalPlaytimeSec, 50);
      case addict -> hours(i, s.totalPlaytimeSec, 100);
      case long_session -> {
        i.current = s.maxSessionSec / 60;
        i.target = 60;
        i.earned = s.maxSessionSec >= 3600;
        i.progressText = "best: " + (s.maxSessionSec / 60) + " min";
        i.progressKind = "best_minutes";
      }
      case hot_streak -> best(i, s.maxSessionUnlocks, 5);
      case night_owl -> flag(i, s.nightOwl);
      case speed_demon -> best(i, s.maxUnlocksIn5Min, 3);
      case rare_finder -> flag(i, s.hasRare || s.hasEpic || s.hasLegendary);
      case epic_hunter -> flag(i, s.hasEpic || s.hasLegendary);
      case legendary_hunter -> flag(i, s.hasLegendary);
      case cloud_pioneer -> flag(i, s.cloudUploadDone);
      case level_5 -> level(i, s.level, 5);
      case level_10 -> level(i, s.level, 10);
    }
  }

  private static void count(BadgeInfo i, long current, long target) {
    i.progressKind = "count";
    i.current = current;
    i.target = target;
    i.earned = current >= target;
    i.progressText = format(current) + " / " + format(target);
  }

  private static void prestige(BadgeInfo i, int prestige, int target) {
    i.progressKind = "prestige";
    i.current = prestige;
    i.target = target;
    i.earned = prestige >= target;
    i.progressText = "Prestige " + prestige + " / " + target;
  }

  private static void hours(BadgeInfo i, long seconds, int targetHours) {
    i.progressKind = "hours";
    i.current = seconds / 3600;
    i.target = targetHours;
    i.earned = seconds >= targetHours * 3600L;
    i.progressText = (seconds / 3600) + " / " + targetHours + " h";
  }

  private static void best(BadgeInfo i, int best, int target) {
    i.progressKind = "best";
    i.current = best;
    i.target = target;
    i.earned = best >= target;
    i.progressText = "best: " + best;
  }

  private static void flag(BadgeInfo i, boolean value) {
    i.progressKind = "flag";
    i.current = value ? 1 : 0;
    i.target = 1;
    i.earned = value;
    i.progressText = "not yet";
  }

  private static void level(BadgeInfo i, int level, int target) {
    i.progressKind = "level";
    i.current = level;
    i.target = target;
    i.earned = level >= target;
    i.progressText = "Level " + level + " / " + target;
  }

  private static String format(long value) {
    return String.format(Locale.ENGLISH, "%,d", value);
  }
}
