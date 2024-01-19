package de.word_light.documentParts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import de.word_light.documentParts.style.Style;


@TestInstance(Lifecycle.PER_CLASS)
public class DocumentWrapperTest {

    private Style style;

    private List<BasicParagraph> content;

    private List<TableConfig> tableConfigs;

    private int numSingleColumnLines;

    private DocumentWrapper documentWrapper;


    @BeforeEach
    void setup() {
        
        this.style = new Style(8, "Calibri", "000000", true, true, true, ParagraphAlignment.LEFT, null);
        this.content = List.of(new BasicParagraph("header", this.style), 
                               new BasicParagraph("par1", this.style), 
                               new BasicParagraph("par2", this.style), 
                               new BasicParagraph("par3", this.style), 
                               new BasicParagraph("par4", this.style), 
                               new BasicParagraph("par5", this.style), 
                               new BasicParagraph("footer", this.style));
        this.tableConfigs = new ArrayList<>(List.of(new TableConfig(2, 1, 1), new TableConfig(2, 1, 3), new TableConfig(2, 1, 5)));
        this.numSingleColumnLines = 1;
        this.documentWrapper = new DocumentWrapper(this.content, tableConfigs, false, "Document_1.docx", 1, this.numSingleColumnLines);
    }


    @Test
    void testIsTableConfigsValid_shouldBeValid() {

        assertTrue(this.documentWrapper.isTableConfigsValid());
    }


    @Test
    void testIsNumSingleColumnLinesValid_tooMany() {

        assertTrue(this.documentWrapper.isNumSingleColumnLinesValid());

        this.documentWrapper.setNumSingleColumnLines(this.content.size() - 1);

        assertFalse(this.documentWrapper.isNumSingleColumnLinesValid());
    }


    @Test
    void testIsNumSingleColumnLinesValid_shouldBeValid() {

        this.documentWrapper.setNumSingleColumnLines(this.content.size() - 2);
        assertTrue(this.documentWrapper.isNumSingleColumnLinesValid());

        this.documentWrapper.setNumSingleColumnLines(0);
        assertTrue(this.documentWrapper.isNumSingleColumnLinesValid());
    }

}