package eu.coatrack.admin.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import static java.lang.Math.toIntExact;
import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.MONTHS;

public class DateUtils {
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public static LocalDate getLocalDateOrTodayIfNull(String dateString) {
        LocalDate date = LocalDate.now();
        if (dateString != null)
            date = LocalDate.parse(dateString);
        return date;
    }

    // TODO Date should be replaced completely with LocalDate
    @Deprecated
    public static Date localDateToDate(LocalDate localDate)  {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // TODO Date should be replaced completely with LocalDate
    @Deprecated
    public static Date getTodayAsDate() {
        return localDateToDate(LocalDate.now());
    }

    public static String getTodayAsString() {
        LocalDate today = LocalDate.now();
        int month = today.get(MONTH_OF_YEAR);
        String zeroPrefix = month < 10 ? "0" : "";
        String monthString = String.format("%s%d", zeroPrefix, month);
        return String.format("%d-%s-%d", today.get(YEAR), monthString, today.get(DAY_OF_MONTH));
    }

    public static String getTodayLastMonthAsString() {
        LocalDate today = LocalDate.now().minusMonths(1);
        int month = today.get(MONTH_OF_YEAR);
        String zeroPrefix = month < 10 ? "0" : "";
        String monthString = String.format("%s%d", zeroPrefix, month);
        return String.format("%d-%s-%d", today.get(YEAR), monthString, today.get(DAY_OF_MONTH));
    }

    public static Date getLocalDateFromString(String dateString) {
        Date date;
        try {
            date = df.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return date;
    }

    public static int getMonthDifference(LocalDate from, LocalDate until) {
        return toIntExact(MONTHS.between(from, until));
    }


    public static void setFormat(String format) {
        df = new SimpleDateFormat(format);
    }

}
