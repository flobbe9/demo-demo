package com.example.vorspiel.documentBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.example.vorspiel.documentParts.TableConfig;
import com.example.vorspiel.documentParts.style.Style;
import com.example.vorspiel.documentParts.style.Color;


/**
 * Test class for {@link TableUtils}.
 * 
 * @since 0.0.1
 */
@TestInstance(Lifecycle.PER_CLASS)
public class TableUtilsTest {

    private XWPFDocument document;

    private List<String> tableContent;

    private Style style;

    private TableConfig tableConfig;
    private int numColumns;
    private int startIndex;
    private int endIndex;
    private int currentContentIndex;

    private TableUtils tableUtils;


    @BeforeEach
    void init() {

        // initialize fields
        this.document = new XWPFDocument();
        this.tableContent = Arrays.asList("cell1", "cell2", "cell3", "cell4", "cell5", "cell6", "cell7", "cell8", "cell9"); /** Don't change size!! */
        this.style = new Style(14, 
                                    "sans serif", 
                                    Color.BLUE, 
                                    false, 
                                    true,
                                    false, 
                                    false, 
                                    false,
                                    ParagraphAlignment.CENTER, 
                                    null);
        this.numColumns = 3;
        this.currentContentIndex = 1; /** Add table after header. */
        this.startIndex = this.currentContentIndex;
        this.endIndex = 9;
        this.tableConfig = new TableConfig(this.numColumns, this.numColumns, this.startIndex, this.endIndex);
        this.tableUtils = new TableUtils(document, tableConfig);
    }
    

    @Test
    void addTableCell_textNull_shouldNotThrow() {

        assertDoesNotThrow(() -> this.tableUtils.addTableCell(this.currentContentIndex, null, style));
    }


    @Test
    void addTableCell_styleNull_shouldNotThrow() {

        assertDoesNotThrow(() -> this.tableUtils.addTableCell(this.currentContentIndex, "some irrelevant text", null));
    }


    @Test
    void addTableCell_shouldAddText() {

        String text = "some irrelevant text";

        // add cell content
        XWPFParagraph paragraph = this.tableUtils.addTableCell(this.currentContentIndex, text, style);

        // should have been added
        int lastRunIndex = paragraph.getRuns().size() - 1;
        String actualText = paragraph.getRuns().get(lastRunIndex).text();
        assertEquals(text, actualText);
    }


    @Test
    void addTableCell_addMoreContentThanCells_shouldThrow() {

        // more content than there actually is
        int contentSize = this.tableContent.size() + 1;

        // iterate longer than there are cells
        assertThrows(NullPointerException.class, () -> {
            for (int i = 0; i < contentSize; i++)
                this.tableUtils.addTableCell(i + 1, "some irrelevant text", style);  
        }); 
    }


    @Test
    void addTableCell_makeTableTooSmall_shouldThrow() {

        // make table too small
        this.tableConfig.setNumColumns(this.numColumns - 1);

        // iterate longer than there are cells
        assertThrows(NullPointerException.class, () -> {
            for (int i = 0; i < this.tableContent.size(); i++)
                this.tableUtils.addTableCell(i + 1, "some irrelevant text", style);  
        });
    }


    @Test
    void addTableCell_shouldNotThrow() {

        // add as much content as there are cells
        assertDoesNotThrow(() -> {
            for (int i = 0; i < this.tableContent.size(); i++) 
                this.tableUtils.addTableCell(i + 1, this.tableContent.get(i), style);
        });
    }


    @Test
    void isTableIndex_tableConfigNull_shouldBeFalse() {

        // set tableConfig null
        this.tableUtils.setTableConfig(null);

        assertFalse(this.tableUtils.isTableIndex(this.currentContentIndex));
    }


    @Test
    void isTableIndex_tableNotStarted_shouldBeFalse() {

        // set not started yet
        this.tableConfig.setStartIndex(this.currentContentIndex + 1);

        assertFalse(this.tableUtils.isTableIndex(this.currentContentIndex));
    }


    @Test
    void isTableIndex_tableEnded_shouldBeFalse() {

        // set table finished
        this.currentContentIndex = this.endIndex + 1;

        assertFalse(this.tableUtils.isTableIndex(this.currentContentIndex));
    }


    @Test
    void isTableIndex_shouldBeTrue() {

        // given values should work
        assertTrue(this.tableUtils.isTableIndex(this.currentContentIndex));

        // set table starts here
        this.currentContentIndex = this.startIndex;
        assertTrue(this.tableUtils.isTableIndex(this.currentContentIndex));

        // set table ends here
        this.currentContentIndex = this.endIndex;
        assertTrue(this.tableUtils.isTableIndex(this.currentContentIndex));    
    }


    @AfterAll
    void cleanUp() throws IOException {

        this.document.close();
    }
}