package eu.coatrack.admin.service.admin;

import be.ceau.chart.DoughnutChart;
import be.ceau.chart.LineChart;
import be.ceau.chart.color.Color;
import be.ceau.chart.data.DoughnutData;
import be.ceau.chart.data.LineData;
import be.ceau.chart.dataset.DoughnutDataset;
import be.ceau.chart.dataset.LineDataset;
import be.ceau.chart.enums.PointStyle;
import be.ceau.chart.options.LineOptions;
import be.ceau.chart.options.scales.LinearScale;
import be.ceau.chart.options.scales.LinearScales;
import be.ceau.chart.options.ticks.LinearTicks;
import eu.coatrack.admin.model.vo.StatisticsPerApiUser;
import eu.coatrack.admin.model.vo.StatisticsPerDay;
import eu.coatrack.admin.model.vo.StatisticsPerHttpStatusCode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ChartService {
    private static final Map<Integer, Color> chartColorsPerHttpResponseCode;

    static {
        Map<Integer, Color> colorMap = new HashMap<>();
        colorMap.put(400, Color.ORANGE);
        colorMap.put(401, Color.SALMON);
        colorMap.put(403, Color.LIGHT_YELLOW);
        colorMap.put(404, new Color(255, 255, 102)); // yellow
        colorMap.put(500, Color.RED);
        colorMap.put(503, Color.ORANGE_RED);
        colorMap.put(504, Color.DARK_RED);
        chartColorsPerHttpResponseCode = Collections.unmodifiableMap(colorMap);
    }

    public DoughnutChart createDoughnutChartFromUserStatistics(List<StatisticsPerApiUser> userStatistics) {
        DoughnutDataset dataset = new DoughnutDataset()
                .setLabel("API calls")
                .setBorderWidth(2)
                .addBackgroundColors(
                        Color.AQUA_MARINE,
                        Color.LIGHT_BLUE,
                        Color.LIGHT_SALMON,
                        Color.LIGHT_BLUE,
                        Color.GRAY
                );

        userStatistics.forEach(stats -> dataset.addData(stats.getNoOfCalls()));

        DoughnutData data = new DoughnutData();
        if (userStatistics.size() > 0) {
            data.addDataset(dataset);
            userStatistics.forEach(stats -> data.addLabel(stats.getUserName()));
        }
        return new DoughnutChart(data);
    }

    public DoughnutChart createDoughnutChartFromHttpStatistics(List<StatisticsPerHttpStatusCode> statisticsPerHttpStatusCode) {
        DoughnutData data = new DoughnutData();
        if (statisticsPerHttpStatusCode.size() > 0) {

            List<Color> chartColors = new ArrayList<>();

            for(StatisticsPerHttpStatusCode stats: statisticsPerHttpStatusCode)
                    chartColors.add(getColorByStatusCode(stats.getStatusCode()));

            DoughnutDataset dataset = new DoughnutDataset()
                    .setLabel("HTTP response codes")
                    .addBackgroundColors(chartColors.toArray(new Color[0]))
                    .setBorderWidth(2);

            statisticsPerHttpStatusCode.forEach(stats -> dataset.addData(stats.getNoOfCalls()));
            data.addDataset(dataset);
            statisticsPerHttpStatusCode.forEach(stats -> data.addLabel(stats.getStatusCode().toString()));
        }
        return new DoughnutChart(data);
    }

    public LineChart createLineChartFromStatisticsPerDays(
            List<StatisticsPerDay> statisticsPerDays, LocalDate from, LocalDate until
    ) {
        // create a map with entries for all days in the given date range
        Map<LocalDate, Long> callsPerDay = new TreeMap<>();

        long timePeriodDurationInDays = ChronoUnit.DAYS.between(from, until);
        // put "0" as default, in case no calls are registered in database
        for (int i = 0; i <= timePeriodDurationInDays; i++) {
            callsPerDay.put(from.plusDays(i), 0L);
        }

        // add numbers from database, if any
        statisticsPerDays.forEach(statisticsPerDay -> callsPerDay.put(statisticsPerDay.getLocalDate(), statisticsPerDay.getNoOfCalls()));

        // create actual chart
        LineDataset dataset = new LineDataset()
                .setLabel("Total number of API calls per day")
                .setBackgroundColor(Color.LIGHT_YELLOW)
                .setBorderWidth(3);

        LineData data = new LineData()
                .addDataset(dataset);

        callsPerDay.forEach((date, noOfCalls) -> {
            data.addLabel(DateTimeFormatter.ISO_LOCAL_DATE.format(date));
            dataset.addData(noOfCalls)
                    .addPointStyle(PointStyle.CIRCLE)
                    .addPointBorderWidth(2)
                    .setLineTension(0f)
                    .setSteppedLine(false)
                    .addPointBackgroundColor(Color.LIGHT_YELLOW)
                    .addPointBorderColor(Color.LIGHT_GRAY);
        });

        return new LineChart(data, getDefaultLineOptions());
    }

    private static LineOptions getDefaultLineOptions() {
        LinearTicks ticks = new LinearTicks().setBeginAtZero(true);
        LinearScale scale = new LinearScale().setTicks(ticks);
        LinearScales scales = new LinearScales().addyAxis(scale);
        LineOptions lineOptions = new LineOptions().setScales(scales);
        return lineOptions;
    }

    private static Color getColorByStatusCode(int statusCode) {
        Color color = chartColorsPerHttpResponseCode.get(statusCode);

        if (color == null) {
            // there is no fixed color defined for this status code, set it based on the
            // range
            if (statusCode >= 200 && statusCode < 300) {
                // lighter green
                color = new Color(0, 204, 0);
            } else if (statusCode >= 300 && statusCode < 400) {
                color = Color.LIGHT_BLUE;
            } else if (statusCode >= 404 && statusCode < 500) {
                color = Color.DARK_ORANGE;
            } else if (statusCode >= 500 && statusCode < 600) {
                // red
                color = new Color(255, 51, 51);
            } else {
                color = Color.LIGHT_GRAY;
            }
        }
        return color;
    }
}
