package de.word_light.document_builder.config;

import java.util.HashMap;
import java.util.Map;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;


/**
 * Class used to set some configurations on api start.<p>
 * 
 * Don't inject any beans in here, does not work for some reason. <p>
 * 
 * @since 0.0.6
 */
@Log4j2
public class ApplicationInitializer {

    private static final String SSL_PASSWORD_ARG_NAME = "--sslPassword"; 

    private String[] args;

    private Map<String, String> argKeyValues;



    /**
     * @param args from main method
     */
    public ApplicationInitializer(String ...args) {

        this.args = args;
        this.argKeyValues = new HashMap<>();
        setupArgKeyValues();
    }


    /**
     * Doing some initializing after successful api start.
     */
    public void init() {
        
        log.info("Initializing API...");

        initSSL();
    }


    /**
    * Set ssl password if not blank.
    * 
    * @param sslPassword password for https certificate to work
    */
    private void initSSL() {

        String sslPassword = retrieveArgItem(SSL_PASSWORD_ARG_NAME);
        String sslPasswordPropName = "server.ssl.key-store-password";

        if (!StringUtils.isBlank(sslPassword)) {
            log.info("Setting ssl password...");
            System.setProperty(sslPasswordPropName, sslPassword);
        }
    }


    private void setupArgKeyValues() {

        for (String arg : this.args) {
            if (!arg.startsWith("--"))
                continue;
            
            int argValueStartIndex = arg.indexOf("=");

            this.argKeyValues.put(arg.substring(0, argValueStartIndex), arg.substring(argValueStartIndex + 1));
        }
    }


    /**
     * @param argsIndex index of element in args array passed from command line
     * @return the arguemnt passed from command line at given index of {@code ""} if index out of bounds
     */
    private String retrieveArgItem(int argsIndex) {

        try {
            return this.args[argsIndex];

        } catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }


    /**
     * @param argKey of element in args array passed from command line, i.e. {@code --someKey}
     * @return the arguemnt value matching given argKey, i.e. if arsg are {@code --args=--someKey=someValue} then 
     *         {@code argKey} is expected to equal {@code "--someKey"} and the return value would be {@code "someValue"}.
     *          Return {@code null} if {@code argKey} not present
     */
    private String retrieveArgItem(String argKey) {

        return this.argKeyValues.get(argKey);
    }
}