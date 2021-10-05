package eu.coatrack.admin.selenium.tests.itemListsButtonsTests;

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

import eu.coatrack.admin.selenium.tests.AbstractTestSetup;
import org.junit.jupiter.api.*;

import static eu.coatrack.admin.selenium.api.PageFactory.*;

public class ApiKeyListButtonsTests extends AbstractTestSetup {

    private String apiKeyValue;
    private String serviceName;

    @BeforeAll
    public void setupApiKey() {
        serviceName = serviceProviderServices.createPublicService();
        apiKeyValue = serviceProviderApiKeys.createApiKey(serviceName);
    }

    @Test
    public void clickingOnCalendarButtonShouldNotCauseErrorPage(){
        serviceProviderApiKeys.clickOnCalenderButtonOfApiKey(apiKeyValue);
        urlReachabilityTools.throwExceptionIfErrorPageWasReceived();
    }

    @Test
    public void clickingDetailsButtonShouldNotCauseErrorPage(){
        serviceProviderApiKeys.clickOnDetailsButtonOfApiKey(apiKeyValue);
        urlReachabilityTools.throwExceptionIfErrorPageWasReceived();
    }

    @Test
    public void clickingEditButtonShouldNotCauseErrorPage(){
        serviceProviderApiKeys.clickOnEditButtonOfApiKey(apiKeyValue);
        urlReachabilityTools.throwExceptionIfErrorPageWasReceived();
    }

    @AfterAll
    public void assertHavingNoErrorAndCleanup(){
        serviceProviderApiKeys.deleteApiKey(apiKeyValue);
        serviceProviderServices.deleteService(serviceName);
    }

}
