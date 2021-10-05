package eu.coatrack.admin.e2e.api.pages.serviceProvider.serviceOfferingsSetup;

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
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import java.util.Random;

import static eu.coatrack.admin.e2e.api.tools.WaiterUtils.sleepMillis;
import static eu.coatrack.admin.e2e.configuration.PageConfiguration.adminServiceListUrl;
import static eu.coatrack.admin.e2e.configuration.PageConfiguration.providerServiceUrl;
import static eu.coatrack.admin.e2e.configuration.TableConfiguration.*;


public class AdminServiceOfferings {

    private final WebDriver driver;
    private final TableUtils serviceTableUtils;

    public AdminServiceOfferings(WebDriver driver) {
        this.driver = driver;
        serviceTableUtils = new TableUtils(driver, TableType.SERVICE_TABLE);
    }

    public String createPublicService() {
        driver.get(adminServiceListUrl);
        String serviceName = "my-service-" + (new Random().nextInt());
        String serviceId = serviceName + "-id";
        workThroughServiceCreationMenu(serviceName, serviceId);
        return serviceName;
    }

    private void workThroughServiceCreationMenu(String serviceName, String serviceId) {
        driver.findElement(By.linkText("Create Service Offering")).click();
        driver.findElement(By.id("name")).click();
        driver.findElement(By.id("name")).sendKeys(serviceName);

        driver.findElement(By.id("localUrl")).click();
        driver.findElement(By.id("localUrl")).sendKeys(providerServiceUrl);
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
        serviceTableUtils.clickOnButton(serviceName, adminServicesFirstEditButtonColumn, adminServicesFirstEditButtonClassName);
    }

    public void clickDetailsButtonOfService(String serviceName) {
        serviceTableUtils.clickOnButton(serviceName, adminServicesDetailsButtonColumn, adminServicesDetailsButtonClassName);
    }

    public void clickOnSecondEditButtonOfService(String serviceName) {
        serviceTableUtils.clickOnButton(serviceName, adminServicesSecondEditButtonColumn, adminServicesSecondEditButtonClassName);
    }
}