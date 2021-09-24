package eu.coatrack.admin.e2e.tests;

import org.junit.jupiter.api.Test;

import static eu.coatrack.admin.e2e.configuration.PageConfiguration.*;

public class PageReachabilityTests extends AbstractTestSetup{

    @Test
    public void testAdminDashboardReachability(){
        pageFactory.getPageChecker().assertThatUrlIsReachable(adminDashboardUrl);
    }

    @Test
    public void testAdminTutorialReachability(){
        pageFactory.getPageChecker().assertThatUrlIsReachable(adminTutorialUrl);
    }

    @Test
    public void testAdminServiceListReachability(){
        pageFactory.getPageChecker().assertThatUrlIsReachable(adminServiceListUrl);
    }

    @Test
    public void testAdminGatewayListReachability(){
        pageFactory.getPageChecker().assertThatUrlIsReachable(adminGatewayListUrl);
    }

    @Test
    public void testAdminApiKeyListReachability(){
        pageFactory.getPageChecker().assertThatUrlIsReachable(adminApiKeyListUrl);
    }

    @Test
    public void testAdminReportsReachability(){
        pageFactory.getPageChecker().assertThatUrlIsReachable(adminReportsUrl);
    }

    @Test
    public void testConsumerDashboardReachability(){
        pageFactory.getPageChecker().assertThatUrlIsReachable(consumerDashboardUrl);
    }

    @Test
    public void testConsumerTutorialReachability(){
        pageFactory.getPageChecker().assertThatUrlIsReachable(consumerTutorialUrl);
    }

    @Test
    public void testConsumerApiKeyListReachability(){
        pageFactory.getPageChecker().assertThatUrlIsReachable(consumerApiKeyListUrl);
    }

    @Test
    public void testConsumerServiceListReachability(){
        pageFactory.getPageChecker().assertThatUrlIsReachable(consumerServiceListUrl);
    }

    @Test
    public void testConsumerReportsReachability(){
        pageFactory.getPageChecker().assertThatUrlIsReachable(consumerReportsUrl);
    }

}
