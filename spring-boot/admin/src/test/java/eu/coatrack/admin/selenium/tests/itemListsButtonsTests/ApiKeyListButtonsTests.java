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

import eu.coatrack.admin.selenium.api.pages.serviceProvider.serviceOfferingsSetup.ServiceProviderApiKeys;
import eu.coatrack.admin.selenium.api.pages.serviceProvider.serviceOfferingsSetup.ServiceProviderServices;
import eu.coatrack.admin.selenium.tests.AbstractTestSetup;
import org.junit.jupiter.api.*;

public class ApiKeyListButtonsTests extends AbstractTestSetup {

    private ServiceProviderApiKeys serviceProviderApiKeys;
    private String apiKeyValue;
    private ServiceProviderServices serviceProviderServices;
    private String serviceName;

    @BeforeAll
    public void setupApiKey() {
        serviceProviderApiKeys = pageFactory.getServiceProviderApiKeys();
        serviceProviderServices = pageFactory.getServiceProviderServices();
        serviceName = serviceProviderServices.createPublicService();
        apiKeyValue = serviceProviderApiKeys.createApiKey(serviceName);
    }

    @Test
    public void clickingOnCalendarButtonShouldNotCauseErrorPage(){
        serviceProviderApiKeys.clickOnCalenderButtonOfApiKey(apiKeyValue);
    }

    @Test
    public void clickingDetailsButtonShouldNotCauseErrorPage(){
        serviceProviderApiKeys.clickOnDetailsButtonOfApiKey(apiKeyValue);
    }

    @Test
    public void clickingEditButtonShouldNotCauseErrorPage(){
        serviceProviderApiKeys.clickOnEditButtonOfApiKey(apiKeyValue);
    }

    @AfterAll
    public void assertHavingNoErrorAndCleanup(){
        pageFactory.getPageChecker().throwExceptionIfErrorPageWasReceived();
        serviceProviderApiKeys.deleteApiKey(apiKeyValue);
        serviceProviderServices.deleteService(serviceName);
    }

}
