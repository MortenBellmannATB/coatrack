package eu.coatrack.admin.e2e.api.serviceProvider;

import eu.coatrack.admin.e2e.api.tools.WaiterUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Random;

import static eu.coatrack.admin.e2e.api.tools.WaiterUtils.sleepMillis;
import static eu.coatrack.admin.e2e.configuration.TestConfiguration.getAdminTutorialUrl;

public class AdminTutorial {

    private final WebDriver driver;
    private final WaiterUtils waiterUtils;

    public AdminTutorial(WebDriver driver) {
        this.driver = driver;
        waiterUtils = new WaiterUtils(driver);
    }

    public ItemDto createItemsViaTutorial(){
        driver.get(getAdminTutorialUrl());
        driver.findElement(By.linkText("Next")).sendKeys(Keys.RETURN);

        waiterUtils.waitForElementWithId("serviceName");
        driver.findElement(By.id("serviceName")).click();
        String serviceName = "my-service" + new Random().nextInt();
        driver.findElement(By.id("serviceName")).sendKeys(serviceName);
        driver.findElement(By.linkText("Next")).sendKeys(Keys.RETURN);

        waiterUtils.waitForElementWithId("serviceUrl");
        driver.findElement(By.id("serviceUrl")).click();
        driver.findElement(By.id("serviceUrl")).sendKeys("https://www.bing.com");
        driver.findElement(By.linkText("Next")).sendKeys(Keys.RETURN);

        waiterUtils.waitForElementWithId("serviceForFreeYes");
        driver.findElement(By.id("serviceForFreeYes")).click();
        driver.findElement(By.linkText("Next")).sendKeys(Keys.RETURN);

        sleepMillis(1000);
        driver.findElement(By.linkText("Finish")).sendKeys(Keys.RETURN);
        WebElement gatewayDownloadLinkElement = new WebDriverWait(driver, 60)
                .until(ExpectedConditions.presenceOfElementLocated(By.linkText("Click here to download your CoatRack Gateway")));

        String gatewayDownloadLink = gatewayDownloadLinkElement.getAttribute("href");
        String apiKeyValue = driver.findElement(By.cssSelector(".row:nth-child(3) p:nth-child(2)")).getText();
        return new ItemDto(serviceName, gatewayDownloadLink, apiKeyValue);
    }
}