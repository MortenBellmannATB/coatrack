package eu.coatrack.admin.e2e.api.serviceProvider.serviceOfferingsSetup;

import eu.coatrack.admin.e2e.api.tools.TableType;
import eu.coatrack.admin.e2e.api.tools.TableUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import java.util.Random;

import static eu.coatrack.admin.e2e.api.tools.WaiterUtils.sleepMillis;
import static eu.coatrack.admin.e2e.configuration.TestConfiguration.getAdminServiceListUrl;


public class AdminServiceOfferings {

    private final WebDriver driver;
    private final TableUtils serviceTableUtils;

    public AdminServiceOfferings(WebDriver driver) {
        this.driver = driver;
        serviceTableUtils = new TableUtils(driver, TableType.SERVICE_TABLE);
    }

    public String createPublicService() {
        driver.get(getAdminServiceListUrl());
        driver.findElement(By.linkText("Create Service Offering")).click();

        String serviceName = "my-service-" + (new Random().nextInt());
        String serviceId = serviceName + "-id";
        driver.findElement(By.id("name")).click();
        driver.findElement(By.id("name")).sendKeys(serviceName);

        driver.findElement(By.id("localUrl")).click();
        driver.findElement(By.id("localUrl")).sendKeys("https://www.bing.com");
        driver.findElement(By.id("uriIdentifier")).click();
        driver.findElement(By.id("uriIdentifier")).sendKeys(serviceId);
        driver.findElement(By.id("description")).click();
        driver.findElement(By.id("description")).sendKeys("Some Description");

        driver.findElement(By.linkText("Next")).sendKeys(Keys.RETURN);
        driver.findElement(By.linkText("Next")).sendKeys(Keys.RETURN);
        driver.findElement(By.id("freeButton")).click();
        driver.findElement(By.linkText("Next")).sendKeys(Keys.RETURN);
        sleepMillis(1000);
        driver.findElement(By.linkText("Finish")).sendKeys(Keys.RETURN);
        sleepMillis(1000);

        return serviceName;
    }

    public boolean isServiceWithinList(String serviceName) {
        return serviceTableUtils.isItemWithinList(serviceName);
    }

    public void deleteService(String serviceName){
        serviceTableUtils.deleteItem(serviceName);
    }

    public void deleteAllServices() {
        serviceTableUtils.deleteAllItem();
    }

    public void clickOnFirstEditButtonOfService(String serviceName) {
        serviceTableUtils.clickOnButton(serviceName, 6, "fa-image");
    }

    public void clickDetailsButtonOfService(String serviceName) {
        serviceTableUtils.clickOnButton(serviceName, 6, "fa-search-plus");
    }

    //TODO column indices and the buttons class are to be abstracted and store in central configuration.
    public void clickOnSecondEditButtonOfService(String serviceName) {
        serviceTableUtils.clickOnButton(serviceName, 6, "fa-pencil-square-o");
    }
}