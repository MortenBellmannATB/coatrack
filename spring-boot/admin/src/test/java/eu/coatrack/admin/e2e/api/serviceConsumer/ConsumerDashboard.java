package eu.coatrack.admin.e2e.api.serviceConsumer;

import eu.coatrack.admin.e2e.api.AbstractDashboardTemplate;
import org.openqa.selenium.WebDriver;

import static eu.coatrack.admin.e2e.configuration.PageConfiguration.consumerDashboardUrl;

public class ConsumerDashboard extends AbstractDashboardTemplate {

    public ConsumerDashboard(WebDriver driver) {
        super(driver, consumerDashboardUrl);
    }

    public int getNumberOfServicesCalled(){
        return getIntegerValueOfElementWithId("servicesCalled");
    }

}
