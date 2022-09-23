package eu.coatrack.admin.service;

import be.ceau.chart.color.Color;
import eu.coatrack.admin.model.vo.StatisticsPerHttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.*;

import static be.ceau.chart.color.Color.*;
import static be.ceau.chart.color.Color.DARK_RED;

@Service
public class ColorService {
    private static final Map<Integer, Color> chartColorsPerHttpResponseCode;

    static {
        Map<Integer, Color> colorMap = new HashMap<>();
        colorMap.put(400, ORANGE);
        colorMap.put(401, SALMON);
        colorMap.put(403, LIGHT_YELLOW);
        colorMap.put(404, new Color(255, 255, 102)); // yellow
        colorMap.put(500, RED);
        colorMap.put(503, ORANGE_RED);
        colorMap.put(504, DARK_RED);
        chartColorsPerHttpResponseCode = Collections.unmodifiableMap(colorMap);
    }

    public List<Color> getChartColorsForStatisticsPerHttpStatusCodeList(List<StatisticsPerHttpStatusCode> statisticsPerHttpStatusCode) {
        List<Color> chartColors = new ArrayList<>();

        statisticsPerHttpStatusCode.forEach(statistic -> {
            Color color = getColorForStatusCode(statistic.getStatusCode());
            chartColors.add(color);
        });

        return chartColors;
    }

    public Color getColorForStatusCode(int statusCode) {
        Color colorForStatusCode = chartColorsPerHttpResponseCode.get(statusCode);
        if (colorForStatusCode != null)
            if (statusCode >= 200 && statusCode < 300) {
                colorForStatusCode = new Color(0, 204, 0);  // lighter green
            } else if (statusCode >= 300 && statusCode < 400) {
                colorForStatusCode = LIGHT_BLUE;
            } else if (statusCode >= 404 && statusCode < 500) {
                colorForStatusCode = DARK_ORANGE;
            } else if (statusCode >= 500 && statusCode < 600) {
                colorForStatusCode = new Color(255, 51, 51);    // red
            } else {
                colorForStatusCode = LIGHT_GRAY;
            }
        return colorForStatusCode;
    }
}
