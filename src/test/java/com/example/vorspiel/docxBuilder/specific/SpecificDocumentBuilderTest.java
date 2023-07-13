package com.example.vorspiel.docxBuilder.specific;

import static com.example.vorspiel.docxBuilder.basic.BasicDocumentBuilder.RESOURCE_FOLDER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.example.vorspiel.docxContent.basic.BasicParagraph;
import com.example.vorspiel.docxContent.basic.style.Color;
import com.example.vorspiel.docxContent.specific.TableConfig;
import com.example.vorspiel.docxContent.basic.style.BasicStyle;


/**
 * Unit tests for {@link SpecificDocumentBuilder}.
 * 
 * @since 0.0.1
 */
@TestInstance(Lifecycle.PER_CLASS)
public class SpecificDocumentBuilderTest {

    private String docxFileName = "specificTest.docx";

    private BasicStyle style = new BasicStyle(20, 
                                            "times new roman", 
                                            Color.BLACK, 
                                            true, 
                                            true, 
                                            false,
                                            false,
                                            false, 
                                            ParagraphAlignment.CENTER, 
                                            null);

    private BasicParagraph header = new BasicParagraph("This is the header", style);
    private BasicParagraph picture = new BasicParagraph("logo.png", style);
    private BasicParagraph title = new BasicParagraph("This is the title", style);
    private BasicParagraph cell0 = new BasicParagraph("Cell 0", style);
    private BasicParagraph cell1 = new BasicParagraph("Cell 1", style);
    private BasicParagraph cell2 = new BasicParagraph("Cel 2", style);
    private BasicParagraph footer = new BasicParagraph("This is the footer", style);

    private TableConfig tableConfig = new TableConfig(3, 1, 2, 4);

    private SpecificDocumentBuilder specificDocumentBuilder = new SpecificDocumentBuilder(Arrays.asList(header, title, title, picture, cell0, cell1, cell2, footer), 
                                                                                          this.docxFileName, 
                                                                                          tableConfig, 
                                                                                          new File(RESOURCE_FOLDER + "/logo.png"));


    @Test
    void build_shouldWork() {

        specificDocumentBuilder.build();

        assertTrue(new File(RESOURCE_FOLDER + "/" + this.docxFileName).exists());
    }


    @AfterAll
    void cleanUp() {

        // delete .docx file
        new File(RESOURCE_FOLDER + "/" + this.docxFileName).delete();
    }
}