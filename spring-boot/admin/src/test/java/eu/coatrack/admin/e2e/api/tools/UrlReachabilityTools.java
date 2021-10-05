package eu.coatrack.admin.e2e.api.tools;

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

import eu.coatrack.admin.e2e.exceptions.UnexpectedErrorPageReceivedException;
import eu.coatrack.admin.e2e.exceptions.UrlCouldNotBeReachedException;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class UrlReachabilityTools {

    private final WebDriver driver;

    public UrlReachabilityTools(WebDriver driver) {
        this.driver = driver;
    }

    public void fastVisit(String urlToBeVisited){
        if(!driver.getCurrentUrl().equals(urlToBeVisited))
            driver.get(urlToBeVisited);
    }

    public void assertThatUrlIsReachable(String url){
        driver.get(url);
        throwExceptionIfErrorPageWasReceived();
        if (!url.equals(driver.getCurrentUrl()))
            throw new UrlCouldNotBeReachedException("The URL " + url + " could not be reached.");
    }

    public void throwExceptionIfErrorPageWasReceived(){
        if (driver.getPageSource().contains("Sorry, an error occurred."))
            throw new UnexpectedErrorPageReceivedException("A non-error page was expected to appear.");
    }
}
