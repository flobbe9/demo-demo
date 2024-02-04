package de.word_light.document_builder.documentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import de.word_light.document_builder.exception.ApiException;
import de.word_light.document_builder.utils.Utils;
import io.micrometer.common.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;


/**
 * Util class for adding pictures to an {@link XWPFDocument}.
 * 
 * @since 0.0.1
 */
@Getter
@Setter
@Log4j2
public class PictureUtils {

    /** 
     * Used for calculating picture dimensions to centimeters.
     * @see org.apache.poi.util.Units
     */
    public static final Integer EMU_PER_CENTIMETER = 360000;   
     
    private Map<String, byte[]> pictures;


    public PictureUtils(Map<String, byte[]> pictures) {

        this.pictures = pictures;
    }


    /**
     * Adds picture to given {@link XWPFRun} if {@code fileName} is found in {@link #pictures} list. 
     * If {@code this.pictures} is empty do nothing. <p>
     * 
     * Dimensions should fit the original.
     * 
     * @param run to add the picture to
     * @param fileName of the picture. Has to match at least one file name from {@link #pictures}.
     *                 In case of duplicates the first match will be used.
     *                 Assuming format like "${someFileName.png}"
     * @param pictureType format of the picture
     */
    void addPicture(XWPFRun run, String fileName) {

        // remove ${} braces
        fileName = getRawPictureName(fileName);

        PictureType pictureType = getPictureType(fileName);

        // case: invalid picture format
        if (pictureType == null)
            throw new ApiException("Did not add pictures. " + fileName + " is not of a valid picture type.");
        
        // case: no pictures uploaded
        if (this.pictures == null || this.pictures.isEmpty()) {
            log.warn("Did not add pictures. 'pictures' list is either null or empty.");
            return;
        }
        
        File picture = Utils.byteArrayToFile(this.pictures.get(fileName), fileName);
        
        // add picture
        try (InputStream fis = new FileInputStream(picture)) {

            // for dimensions
            BufferedImage bimg = ImageIO.read(picture);

            run.addPicture(fis, 
                           pictureType.ordinal(),
                           fileName, 
                           dxaToEMUs(bimg.getWidth()),
                           dxaToEMUs(bimg.getHeight()));

        } catch (Exception e) {
            throw new ApiException("Failed to add picture.", e);
        
        } finally {
            if (picture != null)
                picture.delete();
        }
    }


    /**
     * Checks if given string ends on a picture extension like ".jpg" or ".png" and returns the {@link PictureType}.<p>
     * 
     * ".jpeg" is not supported.
     * 
     * @param fileName to find the pictureType of
     * @return the pictureType if fileName ends on an extension from {@link PictureType} or null
     */
    public static PictureType getPictureType(String fileName) {

        if (fileName == null)
            return null;

        // check file extension for matching picture extension
        for (PictureType pictureType : PictureType.values()) {

            if (fileName.toLowerCase().endsWith(pictureType.getExtension()))
                return pictureType;
        };

        return null;
    }


    /**
     * Determine if given text should be treated as file name for a picture in a document.
     * 
     * @param text of basic paragraph to check
     * @return true if text is formatted like: "${someFileName.someValidPictureSuffix}", i.e. "${beautifulView.png}".
     */
    public static boolean isPicture(String text) {

        // case: null or blank
        if (StringUtils.isBlank(text))
            return false;

        boolean hasBraces = text.startsWith("${") && text.endsWith("}");

        return hasBraces && getPictureType(getRawPictureName(text)) != null;
    }


    /**
     * Converts centimeters to EMUs. Rounds up from .5 on (e.g. 0.5 = 1 but 0.4 = 0).
     * 
     * @see org.apache.poi.util.Units
     * @param centimeters to convert
     * @return EMUs as int
     */
    private int cmToEMUs(double centimeters) {

        return (int) Math.round(EMU_PER_CENTIMETER * centimeters);
    }

    /**
     * I have no idea what unit this is, but it works, when multiplying by 2.
     * 
     * @see org.apache.poi.util.Units
     * @param dxa to convert
     * @return EMUs as int
     */
    private int dxaToEMUs(double dxa) {

        return (int) Math.round(Units.EMU_PER_DXA * dxa) * 2;
    }

    
    /**
     * @param pictureName unaltered text from basicParagraph that is expected to be formatted like {@code "${somePictureName.png}"}
     * @return given picture name without curly braces, i.e. {@code getRawPictureName("${somePictureName.png}")} would return
     *         {@code "somePictureName.png"}. Does not alter {@code pictureName}.
     *         If picture is not formatted as expected, return {@code pictureName}.
     *         Return {@code null} if {@code pictureName} is {@code null} or too short
     */
    private static String getRawPictureName(String pictureName) {

        try {
            // case: not formatted correctly, assuming is raw already
            if (!pictureName.startsWith("${") || !pictureName.endsWith("}"))
                return pictureName;

            return pictureName.substring(2, pictureName.length() - 1);

        } catch (IndexOutOfBoundsException | NullPointerException e) {
            return null;
        }
    }
}