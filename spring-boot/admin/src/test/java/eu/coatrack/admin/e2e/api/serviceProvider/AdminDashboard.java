package eu.coatrack.admin.e2e.api.serviceProvider;

import eu.coatrack.admin.e2e.api.tools.UrlReachabilityTools;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

import static eu.coatrack.admin.e2e.configuration.PageConfiguration.adminDashboardUrl;
import static eu.coatrack.admin.e2e.configuration.PageConfiguration.username;

public class AdminDashboard {

    private final WebDriver driver;
    private final UrlReachabilityTools urlReachabilityTools;

    public AdminDashboard(WebDriver driver) {
        this.driver = driver;
        urlReachabilityTools = new UrlReachabilityTools(driver);
    }

    public int getTotalApiCalls() {
        return getIntegerValueOfElementWithId("callsThisPeriod");
    }

    private int getIntegerValueOfElementWithId(String elementId){
        urlReachabilityTools.fastVisit(adminDashboardUrl);
        WebElement callsThisPeriod = driver.findElement(By.id(elementId));
        return Integer.parseInt(callsThisPeriod.getText());
    }

    public int getErrorCount() {
        return getIntegerValueOfElementWithId("errorsThisPeriod");
    }

    public int getApiUsageTrend() {
        return getIntegerValueOfElementWithId("callsDiff");
    }

    public int getNumberOfUsers() {
        return getIntegerValueOfElementWithId("users");
    }

    public int getCallsOfLoggedInUser() {
        urlReachabilityTools.fastVisit(adminDashboardUrl);
        List<String> list = driver.findElement(By.id("userStatisticsTable")).findElements(By.cssSelector("span"))
                .stream().map(WebElement::getText).collect(Collectors.toList());

        for (int i = 0; i < list.size(); i += 2){
            if (list.get(i).equals(username))
                return Integer.parseInt(list.get(i+1));
        }
        return 0;
    }

}
