package eu.coatrack.system_integration_testing.api.pages.serviceConsumer;

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

import static eu.coatrack.system_integration_testing.api.UtilFactory.serviceConsumerApiKeyTableUtils;

public class ServiceConsumerApiKeys {

    public boolean isApiKeyWithinList(String apiKeyValue){
        return serviceConsumerApiKeyTableUtils.isItemWithinList(apiKeyValue);
    }

    public void deletePublicApiKey(String apiKeyValue) {
        serviceConsumerApiKeyTableUtils.deleteItem(apiKeyValue);
    }
}