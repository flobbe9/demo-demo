package com.example.vorspiel.docxBuilder.basic;

import java.util.Arrays;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.junit.jupiter.api.Test;

import com.example.vorspiel.docxContent.basic.BasicParagraph;
import com.example.vorspiel.docxContent.basic.style.Color;
import com.example.vorspiel.docxContent.basic.style.BasicStyle;


/**
 * Unit tests for {@link BasicDocumentBuilder}.
 * 
 * @since 0.0.1
 */
public class BasicDocumentBuilderTest {

    private BasicStyle style = new BasicStyle(50, 
                                    "times new roman", 
                                    Color.BLUE, 
                                    true, 
                                    true, 
                                    true,
                                    false, 
                                    false, 
                                    ParagraphAlignment.LEFT, 
                                    null);

    private BasicParagraph header = new BasicParagraph("This is the header", style);
    private BasicParagraph title = new BasicParagraph("This is the title", style);
    private BasicParagraph footer = new BasicParagraph("This is the footer", style);

    private BasicDocumentBuilder basicParagraphBuilder = new BasicDocumentBuilder(Arrays.asList(header, title, footer), "basicTest.docx");
    // private BasicParagraphBuilder basicParagraphBuilder = new BasicParagraphBuilder(Arrays.asList(), "basicTest.docx");


    @Test
    void build_shouldWork() {

        // basicParagraphBuilder.build();
    }
}