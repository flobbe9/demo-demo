package com.example.vorspiel.docxBuilder.specific;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.junit.jupiter.api.Test;
import org.springframework.validation.annotation.Validated;

import com.example.vorspiel.docxBuilder.basic.BasicDocumentBuilder;
import com.example.vorspiel.docxContent.basic.BasicParagraph;
import com.example.vorspiel.docxContent.basic.style.Color;
import com.example.vorspiel.docxContent.specific.TableData;
import com.example.vorspiel.docxContent.basic.style.BasicStyle;


/**
 * Unit tests for {@link SpecificDocumentBuilder}.
 * 
 * @since 0.0.1
 */
public class SpecificDocumentBuilderTest {

    private BasicStyle style = new BasicStyle(30, 
                                            "times new roman", 
                                            Color.BLUE, 
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

    private TableData tableData = new TableData(3, 1, 2, 4);

    private SpecificDocumentBuilder specificDocumentBuilder = new SpecificDocumentBuilder(Arrays.asList(header, picture, cell0, cell1, cell2, title, footer), "specificTest.docx", tableData, new File(BasicDocumentBuilder.RESOURCE_FOLDER + "/logo.png"));
    

    @Test
    void build_shouldWork() {

        specificDocumentBuilder.build();
    }
}