package com.example.vorspiel_backend;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class VorspielApplication {

	public static void main(String[] args) {
		SpringApplication.run(VorspielApplication.class, args);
        initProps();
	}


    /**
     * Set some properties in 'application.yml' file
     */
    private static void initProps() {

        Properties props = System.getProperties();
        
        // set ddl-auto (drop or update database)
        String env = System.getenv("ENV");
        props.setProperty("spring.jpa.hibernate.ddl-auto", env.equals("dev") ? "create-drop" : "update");
    }

        
    /**
     * @return the version of this api from {@code build.gradle} or and empty String if 'version' prop is not found
     */
    public static String getApiVersion() {

        String fileName = "build.gradle";
        String propName = "version";

        try (InputStream in = new FileInputStream(fileName)) {
            Properties props = new Properties();
            props.load(in);
            Object versionProp = props.get(propName);

            String version = versionProp != null ? versionProp.toString().replace("'", "") 
                                                 : "Failed to get version. Could not find '" + propName + "' attribute in '" + fileName + "' file.";

            return version;

        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to get version. Could not find file '" + fileName + "'";
        }
    }
}