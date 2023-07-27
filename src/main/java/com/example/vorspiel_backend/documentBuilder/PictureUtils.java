package com.example.vorspiel_backend.documentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.example.vorspiel_backend.exception.ApiException;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * Util class for adding pictures to an {@link XWPFDocument}.
 * 
 * @since 0.0.1
 */
@Getter
@Setter
@AllArgsConstructor
public class PictureUtils {

    /** 
     * Used for calculating picture dimensions to centimeters.
     * @see org.apache.poi.util.Units
     */
    public static final Integer EMU_PER_CENTIMETER = 360000;   
     
    private List<File> pictures;
    
    
    /**
     * Adds any picture to given {@link XWPFRun} if fileName param is found in {@link #pictures} list. <p>
     * Dimensions are hard coded.
     * 
     * @param run to add the picture to
     * @param fileName of the picture. Has to match at least one file name from {@link #pictures}.
     * In case of duplicates the first match will be used
     * @param pictureType format of the picture
     */
    void addPicture(XWPFRun run, String fileName) {

        PictureType pictureType = getPictureType(fileName);
        
        if (this.pictures == null)
            throw new ApiException("Failed to add picture. Picture list cannot be null.");

        // find picture in list
        Optional<File> optionalPicture = this.pictures.stream()
                                                      .filter(picture -> picture.getName().equals(fileName))
                                                      .findFirst();

        // add picture
        try (InputStream fis = new FileInputStream(optionalPicture.get())) {

            run.addPicture(fis, 
                           pictureType.ordinal(),
                           fileName, 
                           cmToEMUs(DocumentBuilder.PICTURE_WIDTH_LANDSCAPE_HALF),
                           cmToEMUs(DocumentBuilder.PICTURE_HEIGHT_LANDSCAPE_HALF));

        } catch (Exception e) {
            throw new ApiException("Failed to add picture.", e);
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
    PictureType getPictureType(String fileName) {

        if (fileName == null)
            return null;

        // check file extension for matching picture extension
        for (PictureType pictureType : PictureType.values()) {

            if (fileName.endsWith(pictureType.getExtension()))
                return pictureType;
        };

        return null;
    }


    boolean isPicture(String text) {

        return getPictureType(text) != null;
    }


    /**
     * Converts centimeters to EMUs. Rounds up from .5 on (e.g. 0.5 = 1 but 0.4 = 0).
     * 
     * @see org.apache.poi.util.Units
     * @param centimeters to convert
     * @return EMUs as int
     */
    private int cmToEMUs(@DecimalMin("0.0") double centimeters) {

        return (int) Math.round(EMU_PER_CENTIMETER * centimeters);
    }

}