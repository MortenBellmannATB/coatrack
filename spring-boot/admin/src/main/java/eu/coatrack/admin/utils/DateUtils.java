package eu.coatrack.admin.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public static LocalDate parseDateStringOrGetTodayIfNull(String dateString) {
        LocalDate date = LocalDate.now();
        if (dateString != null)
            date = LocalDate.parse(dateString);
        return date;
    }


    public static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static String getTodayAsString() {
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH) + 1;
        String zeroPrefix = month < 10 ? "0" : "";
        String monthString = String.format("%s%d", zeroPrefix, month);
        return String.format("%d-%s-%d",
                today.get(Calendar.YEAR), monthString, today.get(Calendar.DAY_OF_MONTH));
    }

    public static String getTodayLastMonthAsString() {
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        String zeroPrefix = month < 10 ? "0" : "";
        String monthString = String.format("%s%d", zeroPrefix, month);
        return String.format("%d-%s-%d",
                today.get(Calendar.YEAR), monthString, today.get(Calendar.DAY_OF_MONTH));
    }

    public static Date getDateFromString(String dateString) {
        Date date;
        try {
            date = df.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return date;
    }


    public static void setFormat(String format) {
        df = new SimpleDateFormat(format);
    }

}
