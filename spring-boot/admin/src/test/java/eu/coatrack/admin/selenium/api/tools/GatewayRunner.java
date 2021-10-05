package eu.coatrack.admin.selenium.api.tools;

import eu.coatrack.admin.selenium.api.pages.serviceProvider.ServiceProviderTutorial;
import eu.coatrack.admin.selenium.api.pages.serviceProvider.ItemDetails;
import eu.coatrack.admin.selenium.api.pages.serviceProvider.serviceOfferingsSetup.ServiceProviderApiKeys;
import eu.coatrack.admin.selenium.api.pages.serviceProvider.serviceOfferingsSetup.ServiceProviderGateways;
import eu.coatrack.admin.selenium.api.pages.serviceProvider.serviceOfferingsSetup.ServiceProviderServices;
import eu.coatrack.admin.selenium.exceptions.FileCouldNotBeDeletedException;
import eu.coatrack.admin.selenium.exceptions.GatewayDownloadFailedException;
import eu.coatrack.admin.selenium.exceptions.GatewayRunnerInitializationException;
import eu.coatrack.admin.selenium.exceptions.ServiceCouldNotBeAccessedUsingApiKeyException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;

import static eu.coatrack.admin.selenium.configuration.CookieInjector.sessionCookie;
import static eu.coatrack.admin.selenium.configuration.PageConfiguration.localGatewayAccessUrl;
import static eu.coatrack.admin.selenium.configuration.PageConfiguration.host;

public class GatewayRunner {

    private static final Logger logger = LoggerFactory.getLogger(GatewayRunner.class);

    private static WebDriver driver;
    private static GatewayRunner gatewayRunner = null;

    private final ServiceProviderTutorial adminTutorial;
    private final ServiceProviderApiKeys adminApiKeys;
    private final ServiceProviderGateways adminServiceGateways;
    private final ServiceProviderServices adminServiceOfferings;

    private Thread jarThread;
    private ItemDetails itemDetails;
    private File file;

    private GatewayRunner() {
        adminTutorial = new ServiceProviderTutorial(driver);
        adminApiKeys = new ServiceProviderApiKeys(driver);
        adminServiceGateways = new ServiceProviderGateways(driver);
        adminServiceOfferings = new ServiceProviderServices(driver);
    }

    //TODO I think this whole singleton logic to prevent two concurrently running gateways is too complex and not necessary at all.
    public static GatewayRunner createAndRunGateway(WebDriver driver) {
        GatewayRunner.driver = driver;
        if (gatewayRunner != null)
            gatewayRunner.stopGatewayAndCleanup();

        gatewayRunner = new GatewayRunner();
        try {
            gatewayRunner.itemDetails = gatewayRunner.adminTutorial.createItemsViaTutorial();
            gatewayRunner.file = downloadGateway(gatewayRunner.itemDetails.gatewayDownloadLink);
            gatewayRunner.jarThread = executeGatewayJar(gatewayRunner.file);
        } catch (Exception e) {
            if (gatewayRunner.jarThread != null)
                gatewayRunner.stopGatewayAndCleanup();
            throw new GatewayRunnerInitializationException("Something went wrong during the initialization process.", e);
        }
        return gatewayRunner;
    }

    //TODO Split the methods to smaller ones for better readability.
    private static File downloadGateway(String gatewayDownloadLink) throws IOException, InterruptedException {
        File file = new File("test.jar");
        if (file.exists())
            file.delete();
        if (file.exists())
            throw new FileCouldNotBeDeletedException("The file " + file.getName() + "could not be deleted.");

        Runtime rt = Runtime.getRuntime();
        //TODO This is very slow when used for coatrack.eu. Maybe 'wsl curl' would be faster.
        String firstPartOfCommand = "cmd /c curl -v ";
        if (host.equals("localhost"))
            firstPartOfCommand += "-k ";
        String command = firstPartOfCommand + "--cookie \"SESSION=" + sessionCookie.getValue() + "\" --output ./test.jar " + gatewayDownloadLink;
        Process pr = rt.exec(command);
        pr.waitFor();

        if (!file.exists() || file.length() < 1000)
            throw new GatewayDownloadFailedException("Trying to download the Gateway '" + gatewayRunner.itemDetails.gatewayName + "' an error occurred.");
        return file;
    }

    private static Thread executeGatewayJar(File file) throws InterruptedException {
        Thread jarExecutionThread = new Thread(() -> {
            String line = "java -jar " + file.getPath();
            CommandLine cmdLine = CommandLine.parse(line);
            DefaultExecutor executor = new DefaultExecutor();
            ExecuteWatchdog watchdog = new ExecuteWatchdog(300000);
            executor.setWatchdog(watchdog);
            try {
                executor.execute(cmdLine);
            } catch (IOException e) {
                logger.info("The execution of the jar file was interrupted.");
            }
        });
        jarExecutionThread.setDaemon(true);
        jarExecutionThread.start();

        //TODO Wait until the gateway is setup and listens on port 8088. Maybe by fetching the logs?
        Thread.sleep(30000);

        return jarExecutionThread;
    }

    public void stopGatewayAndCleanup() {
        jarThread.interrupt();

        String gatewayName = adminServiceGateways.getGatewayNameByIdentifier(itemDetails.gatewayIdentifier);
        adminServiceGateways.deleteGateway(gatewayName);

        adminApiKeys.deleteApiKey(itemDetails.apiKeyValue);
        adminServiceOfferings.deleteService(itemDetails.serviceName);

        file.delete();
        if (file.exists())
            throw new FileCouldNotBeDeletedException("The file " + file.getName() + "could not be deleted.");
        GatewayRunner.gatewayRunner = null;
    }

    public void makeValidServiceCall() {
        if (!isServiceAccessUsingApiKeySuccessful(itemDetails.serviceId, itemDetails.apiKeyValue))
            throw new ServiceCouldNotBeAccessedUsingApiKeyException("Api key " + itemDetails.apiKeyValue +
                    " could not access the service with the ID " + itemDetails.serviceId + ".");
    }

    private boolean isServiceAccessUsingApiKeySuccessful(String serviceId, String apiKeyValue) {
        String servicesAccessUrl = localGatewayAccessUrl + "/" + serviceId + "?api-key=" + apiKeyValue;
        driver.get(servicesAccessUrl);
        return driver.findElement(By.cssSelector("h1")).getText().equals("Example Domain");
    }

    public ItemDetails getItemDetails() {
        return itemDetails;
    }

}
