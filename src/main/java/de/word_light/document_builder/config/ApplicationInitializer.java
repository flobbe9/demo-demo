package de.word_light.document_builder.config;

import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;


/**
 * Class used to set some configurations on api start.<p>
 * 
 * Don't inject any beans in here, does not work for some reason. <p>
 * 
 * Main method arguments: <p>
 * {@code args[0] == SSL_KEY_STORE_PASSWORD} <p>
 * 
 * @since 0.0.6
 */
@Log4j2
public class ApplicationInitializer {

    private String[] args;

    private String keyStorePassword;


    /**
     * @param args from main method
     */
    public ApplicationInitializer(String ...args) {

        this.args = args;
        this.keyStorePassword = retrieveArgsItem(0);
    }


    /**
     * Doing some initializing after successful api start.
     */
    public void init() {

        // case: no args passed
        if (args == null || args.length == 0)
            return;
        
        log.info("Initializing API...");

        initSSL();
    }


    /**
     * Set ssl password and disabled ssl if no valid password is present.
     * 
     * @param keyStorePassword password for https certificate to work
     */
    private void initSSL() {

        // case: no ssl password
        if (StringUtils.isBlank(this.keyStorePassword)) {
            log.warn("Failed to set ssl password. Disabling ssl...");
            System.setProperty("SSL_ENABLED", "false");
            
        } else {
            log.info("Setting ssl password...");
            System.setProperty("SSL_KEY_STORE_PASSWORD", this.keyStorePassword);
        }
    }


    /**
     * @param argsIndex index of element in args array passed from command line
     * @return the arguemnt passed from command line at given index of {@code ""} if index out of bounds
     */
    private String retrieveArgsItem(int argsIndex) {

        try {
            return this.args[argsIndex];

        } catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }
}