package de.mephisto.vpin.restclient.util;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
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
    SimpleDateFormat df = new SimpleDateFormat("HH-mm");
    return df.format(date);
  }

  public static String formatDateTimeFileString(Date date) {
    SimpleDateFormat df = new SimpleDateFormat("HH:mm");
    return df.format(date);
  }

  public static String formatDateTime(Date date) {
    return DateFormat.getDateTimeInstance().format(date);
  }

  public static Date formatDate(LocalDate value, String time) {
    try {
      if (value == null) {
        return null;
      }

      if (time.contains("--")) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
        return df.parse(time);
      }

      String[] split = time.replaceAll("-", ":").split(":");
      int hours = Integer.parseInt(split[0]);
      int minutes = Integer.parseInt(split[1]);
      Date date = Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant());
      return DateUtil.toDate(date, hours, minutes);
    }
    catch (ParseException e) {
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

  public static Date today() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    return calendar.getTime();
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
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, hours);
    calendar.set(Calendar.MINUTE, minutes);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    return calendar.getTime();
  }
}
