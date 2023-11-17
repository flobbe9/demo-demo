package com.example.vorspiel_backend.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

import org.springframework.lang.Nullable;

import com.example.vorspiel_backend.abstracts.FileDeletionCondition;
import com.example.vorspiel_backend.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.extern.log4j.Log4j2;


/**
 * Util class holding static helper methods and global variables.
 * 
 * @since 0.0.5
 */
@Log4j2
public class Utils {

    public static final String RESOURCE_FOLDER = "./src/main/resources";
    public static final String STATIC_FOLDER = RESOURCE_FOLDER + "/static";
    public static final String USER_GENERATED = RESOURCE_FOLDER + "/static/userGenerated";

    public static final String DOCX_FOLDER = USER_GENERATED + "/docx";
    public static final String PDF_FOLDER = USER_GENERATED + "/pdf";
    public static final String PICTURES_FOLDER = USER_GENERATED + "/pictures";

    /** list of file names that should never be deleted during clean up processes */
    public static final Set<String> KEEP_FILES = Set.of(".gitkeep");


    /**
     * Convert file into String using {@link BufferedReader}.
     * 
     * @param file to convert
     * @return converted string or null, if file is null
     * @throws ApiException
     */
    public static String fileToString(File file) {
        
        // read to string
        try (Reader fis = new FileReader(file);
             BufferedReader br = new BufferedReader(fis)) {
            StringBuilder stringBuilder = new StringBuilder();

            String line = null;
            while ((line = br.readLine()) != null)
                stringBuilder.append(line);

            String str = stringBuilder.toString();
            return replaceOddChars(str);
            
        } catch (Exception e) {
            throw new ApiException("Failed to read file to String.", e);
        }
    }


    /**
     * Write given string to given file.
     * 
     * @param str to write to file
     * @param file to write the string to
     * @return the file
     * @throws ApiException
     */
    public static File stringToFile(String str, File file) {

        try (BufferedWriter br = new BufferedWriter(new FileWriter(file))) {
            br.write(str);

            return file;

        } catch (Exception e) {
            throw new ApiException("Failed to write String to File.", e);
        }
    }


    /**
     * Replace odd characters that java uses for special chars like 'ä, ö, ü, ß' etc. with original chars. <p>
     * 
     * Does not alter given String.
     * 
     * @param str to fix
     * @return fixed string
     */
    public static String replaceOddChars(String str) {

        // alphabetic
        str = str.replace("Ã?", "Ä");
        str = str.replace("Ã¤", "ä");
        str = str.replace("Ã¶", "ö");
        str = str.replace("Ã¼", "ü");
        str = str.replace("ÃŸ", "ß");

        // special chars
        str = str.replace("â?¬", "€");

        return str;
    }
    

    /**
     * Prepends a '/' to given String if there isn't already one.
     * 
     * @param str String to prepend the slash to
     * @return the altered (or not altered) string or "/" if given str is null
     */
    public static String prependSlash(String str) {

        if (str == null || str.equals(""))
            return "/";

        return str.charAt(0) == '/' ? str : "/" + str;
    }


    /** 
     * At least <p>
     * - eight characters, <p>
     * - one uppercase letter, <p>
     * - one lowercase letter,  <p>
     * - one number and <p>
     * - one of given special characters. <p>
     * - maximum 30 characters, 
     */
    public static boolean isPasswordValid(String password) {

        if (password == null)
            throw new ApiException("Failed to validate password. 'password' cannot be null");
        
        String regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[,.;_!#$%&’*+/=?`{|}~^-]).{8,30}$";

        return password.matches(regex);
    }


    /**
     * Iterate given folder and delete files/folders inside if gien lambda returns true. <p>
     * 
     * If lambda is {@code null} all files and folders in given folder will be deleted. <p>
     * 
     * Files from {@link #KEEP_FILES} will not be deleted.
     * 
     * @param folderPath path of the folder to iterate content of
     * @param lambda boolean function taking a {@code File} as param to determine if given file should be deleted or not
     * @return true if all deletions were successfull
     * @see FileDeletionCondition for lambda definition
     */
    public static boolean clearFolder(String folderPath, @Nullable FileDeletionCondition lambda) {

        if (folderPath == null) {
            log.warn("Failed to clear resourceFolder. 'folderPath' cannot be null.");
            return false;
        }

        // case: not a directory
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            log.warn("Failed to clear resourceFolder. 'folderPath' " + folderPath + " does not reference a directory.");
            return false;
        }

        File[] files = folder.listFiles();
        boolean deletionSuccessfull = true;

        // iterate and delete
        for (File file : files)  {
            boolean deletionCondition = lambda != null ? lambda.shouldFileBeDeleted(file) : true;

            if (deletionCondition && !isKeepFile(file))
                if (!file.delete()) {
                    log.warn("Failed to clear resourceFolder. Could not delete file: " + file.getName());
                    deletionSuccessfull = false;
                }
        }
            
        return deletionSuccessfull;
    }


    /**
     * Helper that calls {@link #clearFolder(String, FileDeletionCondition)} and deletes all files with given file names.
     * 
     * @param folder directory to search the file in
     * @param fileNames names of files to delete
     * @return true if deletion was successfull
     */
    public static boolean clearFolderByFileName(String folder, String... fileNames) {

        if (fileNames == null || fileNames.length == 0) 
            return Utils.clearFolder(folder, null);
        
        return Utils.clearFolder(folder, new FileDeletionCondition() {

            @Override
            public boolean shouldFileBeDeleted(File file) {

                return Arrays.asList(fileNames).contains(file.getName());
            }
        });   
    }


    /**
     * Prepends current date and time to given string. Replace ':' with '-' due to .docx naming conditions.
     * 
     * @param str String to format
     * @return current date and time plus str
     */
    public static String prependDateTime(String str) {

        return LocalDateTime.now().toString().replace(":", "-") + "_" + str;
    }


    /**
     * Writes given byte array to file into {@link #STATIC_FOLDER}.
     * 
     * @param bytes content of file
     * @param fileName name of the file
     * @return file
     */
    public static File byteArrayToFile(byte[] bytes, String fileName) {

        String completeFileName = STATIC_FOLDER + prependSlash(fileName);

        if (bytes == null) 
            throw new ApiException("Failed to write byte array to file. 'bytes' is null");
        
        try (OutputStream fos = new FileOutputStream(completeFileName)) {
            fos.write(bytes);

            return new File(completeFileName);

        } catch (Exception e) {
            throw new ApiException("Failed to write byte array to file.", e);
        }
    }


    /**
     * Read given file to byte array.
     * 
     * @param file to read
     * @return byte array
     */
    public static byte[] fileToByteArray(File file) {

        try {
            return Files.readAllBytes(file.toPath());

        } catch (Exception e) {
            throw new ApiException("Failed to read file to byte array.", e);
        }
    }


    public static boolean isKeepFile(File file) {

        return KEEP_FILES.contains(file.getName());
    }
    

    public static boolean isInteger(String str) {

        try {
            Integer.parseInt(str);

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }


    /**
     * @param object to convert to json string
     * @return given object as json string
     */
    public static String objectToJson(Object object) {

        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

        try {
            return objectWriter.writeValueAsString(object);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ApiException("Failed to convert object to json String.", e);
        }
    }
}