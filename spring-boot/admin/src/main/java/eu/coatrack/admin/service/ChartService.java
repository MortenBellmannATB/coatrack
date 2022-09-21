package eu.coatrack.admin.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
public class ChartService {
    private static final Map<Integer, Color> chartColorsPerHttpResponseCode;

    @Autowired
    private MetricService metricService;

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

    public DoughnutChart generateHttpResponseStatisticsDoughnutChart(LocalDate from, LocalDate until, String apiProviderUsername) {
        DoughnutChart chart = new DoughnutChart();
        List<StatisticsPerHttpStatusCode> statisticsPerHttpStatusCodeList = metricService
                .getNoOfCallsPerHttpResponseCode(from, until, apiProviderUsername);
        DoughnutDataset dataset = new DoughnutDataset();
        DoughnutData data = chart.getData().addDataset(dataset);

        if (statisticsPerHttpStatusCodeList.size() > 0) {
            List<Color> chartColors = new ArrayList<>();

            statisticsPerHttpStatusCodeList.forEach(statistic ->
                    chartColors.add(getColorForStatusCode(statistic.getStatusCode())));

            dataset.setLabel("HTTP response codes")
                    .addBackgroundColors(chartColors.toArray(new Color[0]))
                    .setBorderWidth(2);

            statisticsPerHttpStatusCodeList.forEach(stats -> dataset.addData(stats.getNoOfCalls()));
            statisticsPerHttpStatusCodeList.forEach(stats -> data.addLabel(stats.getStatusCode().toString()));
        }
        return chart;
    }

    private Color getColorForStatusCode(int statusCode) {
        Color colorForStatusCode = chartColorsPerHttpResponseCode.get(statusCode);
        if (colorForStatusCode != null)
            if (statusCode >= 200 && statusCode < 300) {
                colorForStatusCode = new Color(0, 204, 0);  // lighter green
            } else if (statusCode >= 300 && statusCode < 400) {
                colorForStatusCode = Color.LIGHT_BLUE;
            } else if (statusCode >= 404 && statusCode < 500) {
                colorForStatusCode = Color.DARK_ORANGE;
            } else if (statusCode >= 500 && statusCode < 600) {
                colorForStatusCode = new Color(255, 51, 51);    // red
            } else {
                colorForStatusCode = Color.LIGHT_GRAY;
            }
        return colorForStatusCode;
    }

    public DoughnutChart generateUserStatisticsDoughnutChart(LocalDate from, LocalDate until, String apiProviderUsername) {
        List<StatisticsPerApiUser> userStatsList = metricService.getStatisticsPerApiConsumer(from, until, apiProviderUsername);
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

        DoughnutData data = new DoughnutData().addDataset(dataset);

        userStatsList.forEach(stats -> dataset.addData(stats.getNoOfCalls()));
        userStatsList.forEach(stats -> data.addLabel(stats.getUserName()));
        return new DoughnutChart(data);
    }


    public LineChart generateStatsPerDayLineChart(LocalDate from, LocalDate until, String apiProviderUsername) {
        List<StatisticsPerDay> statsList = metricService.getNoOfCallsPerDayForDateRange(from, until, apiProviderUsername);
        Map<LocalDate, Long> callsPerDay = getCallsPerDay(from, until);
        LineData data = getLineDataForCallsPerDay(statsList, callsPerDay);
        return new LineChart(data, getDefaultLineOptions());
    }

    private LineData getLineDataForCallsPerDay(List<StatisticsPerDay> statsList, Map<LocalDate, Long> callsPerDay) {
        LineDataset dataset = getDefaultLineDataSet("Total number of API calls per day");
        LineData data = new LineData().addDataset(dataset);

        statsList.forEach(statisticsPerDay ->
                callsPerDay.put(statisticsPerDay.getLocalDate(), statisticsPerDay.getNoOfCalls()));

        callsPerDay.forEach((date, noOfCalls) -> {
            data.addLabel(DateTimeFormatter.ISO_LOCAL_DATE.format(date));
            dataset.addData(noOfCalls);
        });
        return data;
    }

    private static LineDataset getDefaultLineDataSet(String label) {
        return new LineDataset()
                .setLabel(label)
                .setBackgroundColor(Color.LIGHT_YELLOW)
                .setBorderWidth(3)
                .addPointStyle(PointStyle.CIRCLE)
                .addPointBorderWidth(2)
                .setLineTension(0f)
                .setSteppedLine(false)
                .addPointBackgroundColor(Color.LIGHT_YELLOW)
                .addPointBorderColor(Color.LIGHT_GRAY);
    }

    private Map<LocalDate, Long> getCallsPerDay(LocalDate from, LocalDate until) {
        Map<LocalDate, Long> callsPerDay = new TreeMap<>();
        long timePeriodDurationInDays = ChronoUnit.DAYS.between(from, until);

        for (int i = 0; i <= timePeriodDurationInDays; i++) {
            callsPerDay.put(from.plusDays(i), 0L);
        }
        return callsPerDay;
    }

    private static LineOptions getDefaultLineOptions() {
        LinearTicks ticks = new LinearTicks().setBeginAtZero(true);
        LinearScale scale = new LinearScale().setTicks(ticks);
        LinearScales scales = new LinearScales().addyAxis(scale);
        return new LineOptions().setScales(scales);
    }
}
