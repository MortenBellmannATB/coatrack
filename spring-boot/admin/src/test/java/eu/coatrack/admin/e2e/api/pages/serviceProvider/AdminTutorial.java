package eu.coatrack.admin.e2e.api.pages.serviceProvider;

/*-
 * #%L
 * coatrack-admin
 * %%
 * Copyright (C) 2013 - 2021 Corizon | Institut für angewandte Systemtechnik Bremen GmbH (ATB)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import eu.coatrack.admin.e2e.api.tools.table.TableType;
import eu.coatrack.admin.e2e.api.tools.table.TableUtils;
import eu.coatrack.admin.e2e.api.tools.WaiterUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Random;

import static eu.coatrack.admin.e2e.api.tools.WaiterUtils.sleepMillis;
import static eu.coatrack.admin.e2e.configuration.PageConfiguration.adminTutorialUrl;
import static eu.coatrack.admin.e2e.configuration.PageConfiguration.providerServiceUrl;
import static eu.coatrack.admin.e2e.configuration.TableConfiguration.*;

public class AdminTutorial {

    private final WebDriver driver;
    private final WaiterUtils waiterUtils;
    private final TableUtils adminServiceTableUtils;
    private final TableUtils adminGatewayTableUtils;

    public AdminTutorial(WebDriver driver) {
        this.driver = driver;
        waiterUtils = new WaiterUtils(driver);
        adminServiceTableUtils = new TableUtils(driver, TableType.SERVICE_TABLE);
        adminGatewayTableUtils = new TableUtils(driver, TableType.GATEWAY_TABLE);
    }

    public ItemDetails createItemsViaTutorial(){
        String serviceName = "my-service" + new Random().nextInt();
        workThroughServiceCreationMenu(serviceName);

        WebElement gatewayDownloadLinkElement = new WebDriverWait(driver, 60)
                .until(ExpectedConditions.presenceOfElementLocated(By.linkText("Click here to download your CoatRack Gateway")));

        String gatewayDownloadLink = gatewayDownloadLinkElement.getAttribute("href");
        String apiKeyValue = driver.findElement(By.cssSelector(".row:nth-child(3) p:nth-child(2)")).getText();
        String[] gatewayDownloadLinkParts = gatewayDownloadLink.split("/");
        String gatewayIdentifier = gatewayDownloadLinkParts[gatewayDownloadLinkParts.length-2];
        String serviceId = adminServiceTableUtils.getColumnTextFromItemRow(serviceName, adminServicesNameColumn, adminServicesIdColumn);
        String gatewayName = adminGatewayTableUtils.getColumnTextFromItemRow(gatewayIdentifier, adminGatewaysIdColumn, adminGatewaysNameColumn);

        return new ItemDetails(serviceName, serviceId, gatewayDownloadLink, gatewayName, gatewayIdentifier, apiKeyValue);
    }

    private void workThroughServiceCreationMenu(String serviceName) {
        driver.get(adminTutorialUrl);
        driver.findElement(By.linkText("Next")).sendKeys(Keys.RETURN);

        waiterUtils.waitForElementWithId("serviceName");
        driver.findElement(By.id("serviceName")).click();

        driver.findElement(By.id("serviceName")).sendKeys(serviceName);
        driver.findElement(By.linkText("Next")).sendKeys(Keys.RETURN);

        waiterUtils.waitForElementWithId("serviceUrl");
        driver.findElement(By.id("serviceUrl")).click();
        driver.findElement(By.id("serviceUrl")).sendKeys(providerServiceUrl);
        driver.findElement(By.linkText("Next")).sendKeys(Keys.RETURN);

        waiterUtils.waitForElementWithId("serviceForFreeYes");
        driver.findElement(By.id("serviceForFreeYes")).click();
        driver.findElement(By.linkText("Next")).sendKeys(Keys.RETURN);

        sleepMillis(1000);
        driver.findElement(By.linkText("Finish")).sendKeys(Keys.RETURN);
    }
}