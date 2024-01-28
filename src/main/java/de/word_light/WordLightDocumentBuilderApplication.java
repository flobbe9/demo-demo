package de.word_light;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;


@SpringBootApplication
@ServletComponentScan
public class WordLightDocumentBuilderApplication {

	public static void main(String[] args) {
		SpringApplication.run(WordLightDocumentBuilderApplication.class, args);
        new Initializer().init();
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