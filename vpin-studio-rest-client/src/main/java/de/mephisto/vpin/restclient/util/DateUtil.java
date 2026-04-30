package de.mephisto.vpin.restclient.util;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DateUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final static List<String> MINUTE_INTERVALS = Arrays.asList("00", "15", "30", "45");

  public static List<String> TIMES = new ArrayList<>();

  static {
    for (int i = 0; i < 24; i++) {
      String hours = i < 10 ? ("0" + i) : String.valueOf(i);
      for (String minuteInterval : MINUTE_INTERVALS) {
        TIMES.add(hours + ":" + minuteInterval);
      }
    }
  }

  public static String formatTimeString(Date date) {
    DateTimeFormatter df = DateTimeFormatter.ofPattern("HH-mm");
    return df.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
  }

  public static String formatDateTimeFileString(Date date) {
    DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
    return df.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
  }

  public static String formatDateTime(Date date) {
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
  }

  public static String formatDateTime(OffsetDateTime date) {
    if (date == null) {
      return "-";
    }
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(date);
  }

  public static String formatDateTime(LocalDateTime date) {
    if (date == null) {
      return "-";
    }
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(date);
  }

  public static Date formatDate(LocalDate value, String time) {
    try {
      if (value == null) {
        return null;
      }

      if (time.contains("--")) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss");
        LocalDateTime localDateTime = LocalDateTime.parse(time, df);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
      }

      String[] split = time.replaceAll("-", ":").split(":");
      int hours = Integer.parseInt(split[0]);
      int minutes = Integer.parseInt(split[1]);
      Date date = Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant());
      return DateUtil.toDate(date, hours, minutes);
    }
    catch (Exception e) {
      LOG.error("Date format failed: {}", e.getMessage(), e);
    }
    return new Date();
  }

  public static String formatDuration(Date start, Date end) {
    if (start != null && end != null) {
      long ms = end.getTime() - start.getTime();
      if (ms < 0) {
        return "-";
      }

      LocalDate s = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate e = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

      long diff = ChronoUnit.DAYS.between(s, e);
      if (diff == 1) {
        return diff + " day";
      }

      if (diff > 0) {
        return diff + " days";
      }
      return DurationFormatUtils.formatDuration(ms, "HH 'hours', mm 'minutes'", false);
    }
    return "-";
  }

  public static String formatDuration(OffsetDateTime start, OffsetDateTime end) {
    if (start != null && end != null) {
      long ms = ChronoUnit.MILLIS.between(start, end);
      if (ms < 0) {
        return "-";
      }

      long diff = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
      if (diff == 1) {
        return diff + " day";
      }

      if (diff > 0) {
        return diff + " days";
      }
      return DurationFormatUtils.formatDuration(ms, "HH 'hours', mm 'minutes'", false);
    }
    return "-";
  }

  public static String formatDuration(LocalDateTime start, LocalDateTime end) {
    if (start != null && end != null) {
      long ms = ChronoUnit.MILLIS.between(start, end);
      if (ms < 0) {
        return "-";
      }

      long diff = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
      if (diff == 1) {
        return diff + " day";
      }

      if (diff > 0) {
        return diff + " days";
      }
      return DurationFormatUtils.formatDuration(ms, "HH 'hours', mm 'minutes'", false);
    }
    return "-";
  }

  public static String formatDuration(OffsetDateTime start, LocalDateTime end) {
    if (start != null && end != null) {
      return formatDuration(start.toLocalDateTime(), end);
    }
    return "-";
  }

  public static Date today() {
    return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
  }
//
//  public static Date endOfToday() {
//    Calendar calendar = Calendar.getInstance();
//    calendar.set(Calendar.HOUR_OF_DAY, 23);
//    calendar.set(Calendar.MINUTE, 59);
//    calendar.set(Calendar.SECOND, 59);
//    calendar.set(Calendar.MILLISECOND, 0);
//
//    return calendar.getTime();
//  }
//
//  public static Date endOfDay(Date date) {
//    Calendar calendar = Calendar.getInstance();
//    calendar.setTime(date);
//    calendar.set(Calendar.HOUR_OF_DAY, 23);
//    calendar.set(Calendar.MINUTE, 59);
//    calendar.set(Calendar.SECOND, 59);
//    calendar.set(Calendar.MILLISECOND, 0);
//
//    return calendar.getTime();
//  }

  public static Date toDate(Date date, int hours, int minutes) {
    LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    localDateTime = localDateTime.withHour(hours).withMinute(minutes).withSecond(0).withNano(0);
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }
}
