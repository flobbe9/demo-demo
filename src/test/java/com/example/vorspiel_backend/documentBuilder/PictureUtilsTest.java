package com.example.vorspiel_backend.documentBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.example.vorspiel_backend.exception.ApiException;


/**
 * Test class for {@link PictureUtils}.
 * 
 * @since 0.0.1
 */
@TestInstance(Lifecycle.PER_CLASS)
public class PictureUtilsTest {

    private XWPFDocument document;

    private XWPFRun run;
    
    private String testPictureName;
    
    private PictureUtils pictureUtils;


    @BeforeEach
    void init() {

        // initialize fields
        this.document = new XWPFDocument();
        this.run = document.createParagraph().createRun();
        this.testPictureName = "test.png";
        this.pictureUtils = new PictureUtils(Arrays.asList(new File(DocumentBuilderTest.TEST_RESOURCE_FOLDER + "/" + testPictureName)));
    }


//------------ addPicture()
    @Test
    void addPicture_picturesNull_shouldThrow() {

        // set pictures null
        this.pictureUtils.setPictures(null);

        // should not throw
        assertThrows(ApiException.class, () -> this.pictureUtils.addPicture(run, testPictureName));
    }


    @Test
    void addPicture_picturesEmpty_shouldThrow() {

        // clear pictures
        this.pictureUtils.setPictures(new ArrayList<>());

        // should not throw
        assertThrows(ApiException.class, () -> this.pictureUtils.addPicture(run, testPictureName));
    }


    @Test
    void addPicture_fielNameNotInList_shouldThrow() {

        // set mock fileName
        this.testPictureName = "mockName";
        
        // should not throw
        assertThrows(ApiException.class, () -> this.pictureUtils.addPicture(run, testPictureName));
    }


    @Test
    void addPicture_runNull_shouldThrow() {

        // set run null
        this.run = null;
        
        // should not throw
        assertThrows(ApiException.class, () -> this.pictureUtils.addPicture(run, testPictureName));
    }


    @Test
    void addPicture_fileNameNull_shouldThrow() {

        // set fileName null
        this.testPictureName = null;
        
        // should not throw
        assertThrows(ApiException.class, () -> this.pictureUtils.addPicture(run, testPictureName));
    }


    @Test 
    void addPicture_shouldAddPicture() {

        // should start without pictures
        assertTrue(this.run.getEmbeddedPictures().isEmpty());

        // should not throw
        assertDoesNotThrow(() -> this.pictureUtils.addPicture(run, testPictureName));

        // should have picture
        assertFalse(this.run.getEmbeddedPictures().isEmpty());
    }


//------------ getPictureType()
    @Test
    void getPictureType_fileNameNull_shouldReturnNull() {

        assertEquals(null, this.pictureUtils.getPictureType(null));
    }


    @Test
    void getPictureType_invalidFileName_shouldReturnNull() {

        assertEquals(null, this.pictureUtils.getPictureType(""));

        assertEquals(null, this.pictureUtils.getPictureType("mockName"));

        assertEquals(null, this.pictureUtils.getPictureType("mockName.xyz"));

        assertEquals(null, this.pictureUtils.getPictureType("mockName.pdf"));

        // .jpeg not supported, only .jpg
        assertEquals(null, this.pictureUtils.getPictureType("mockName.jpeg"));
    }


    @Test
    void getPictureType_shouldReturnCorrectType() {

        assertEquals(PictureType.PNG, this.pictureUtils.getPictureType(testPictureName));

        assertEquals(PictureType.JPEG, this.pictureUtils.getPictureType("test.jpg"));
    }


    @AfterAll
    void cleanUp() throws IOException {

        this.document.close();
    }
}