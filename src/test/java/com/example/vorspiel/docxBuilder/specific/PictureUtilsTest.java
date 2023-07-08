package com.example.vorspiel.docxBuilder.specific;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.example.vorspiel.docxBuilder.basic.BasicDocumentBuilderTest;


/**
 * Test class for {@link PictureUtils}.
 * 
 * @since 0.0.1
 */
@TestInstance(Lifecycle.PER_CLASS)
public class PictureUtilsTest {

    private XWPFDocument document;

    private XWPFRun run;

    private XWPFParagraph paragraph;

    private PictureUtils pictureUtils;

    private String testPictureName;

    private PictureType testPictureType;


    @BeforeEach
    void init() {

        // initialize fields
        this.document = new XWPFDocument();
        this.paragraph = document.createParagraph();
        this.run = paragraph.createRun();
        this.pictureUtils = new PictureUtils(Arrays.asList(new File(BasicDocumentBuilderTest.TEST_RESOURCE_FOLDER + "/" + testPictureName)));
        this.testPictureName = "test.png";
        this.testPictureType = PictureType.PNG;
    }


    @Test
    void addPicture_picturesNull_shouldNotThrow() {

        // set pictures null
        this.pictureUtils.setPictures(null);

        // should not throw
        assertDoesNotThrow(() -> this.pictureUtils.addPicture(run, testPictureName, testPictureType));
    }


    @Test
    void addPicture_picturesEmpty_shouldNotThrow() {

        // clear pictures
        this.pictureUtils.setPictures(new ArrayList<>());

        // should not throw
        assertDoesNotThrow(() -> this.pictureUtils.addPicture(run, testPictureName, testPictureType));
    }


    @Test
    void addPicture_fielNameNotInList_shouldNotThrow() {

        // set mock fileName
        this.testPictureName = "mockName";
        
        // should not throw
        assertDoesNotThrow(() -> this.pictureUtils.addPicture(run, testPictureName, testPictureType));
    }


    @Test
    void addPicture_runNull_shouldNotThrow() {

        // set run null
        this.run = null;
        
        // should not throw
        assertDoesNotThrow(() -> this.pictureUtils.addPicture(run, testPictureName, testPictureType));
    }


    @Test
    void addPicture_fileNameNull_shouldNotThrow() {

        // set fileName null
        this.testPictureName = null;
        
        // should not throw
        assertDoesNotThrow(() -> this.pictureUtils.addPicture(run, testPictureName, testPictureType));
    }


    @Test
    void testGetPictureType() {

    }


    @AfterAll
    void cleanUp() throws IOException {

        this.document.close();
    }
}