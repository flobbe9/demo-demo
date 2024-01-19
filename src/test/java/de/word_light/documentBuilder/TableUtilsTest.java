package de.word_light.documentBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import de.word_light.documentParts.TableConfig;
import de.word_light.documentParts.style.Style;


/**
 * Test class for {@link TableUtils}.
 * 
 * @since 0.0.1
 */
@TestInstance(Lifecycle.PER_CLASS)
public class TableUtilsTest {

    private XWPFDocument document;

    private Style style;

    private List<TableConfig> tableConfigs;
    private int numColumns;
    private int startIndex;
    private int currentContentIndex;

    private String cellText;

    private TableUtils tableUtils;


    @BeforeEach
    void init() {

        // initialize fields
        this.document = new XWPFDocument();
        this.style = new Style(14, 
                                    "sans serif", 
                                    "2B01FF", // blue
                                    false, 
                                    true,
                                    false, 
                                    ParagraphAlignment.CENTER, 
                                    null);
        this.numColumns = 3;
        this.currentContentIndex = 1; /** Add table after header. */
        this.startIndex = this.currentContentIndex;
        this.tableConfigs = List.of(new TableConfig(this.numColumns, this.numColumns, this.startIndex));

        this.cellText = "cellText";

        this.tableUtils = new TableUtils(document, this.tableConfigs);
    }


//---------- createTableParagraph()
// not a table index, return null
// 
    @Test
    void createTableParagraph_notATableIndex_returnNull() {

        assertNull(this.tableUtils.createTableParagraph(this.startIndex - 1, this.style));
    }


    @Test
    void createTableParagraph_shouldCreateNewTable() {

        assertTrue(this.document.getTables().isEmpty());

        assertNotNull(this.tableUtils.createTableParagraph(this.currentContentIndex, this.style));

        assertFalse(this.document.getTables().isEmpty());
    }
    
    @Test
    void createTableParagraph_shouldUseExistingTable() {

        assertTrue(this.document.getTables().isEmpty());

        assertNotNull(this.tableUtils.createTableParagraph(this.currentContentIndex, this.style));
        assertNotNull(this.tableUtils.createTableParagraph(this.currentContentIndex, this.style));

        assertEquals(1, this.document.getTables().size());
    }

//---------- fillTableCell()
    @Test
    void addTableCell_shouldAddText() {

        XWPFParagraph tableParagraph = this.document.createTable().createRow().createCell().addParagraph();

        // should be blank text
        assertTrue(tableParagraph.getText().isBlank());

        this.tableUtils.fillTableCell(tableParagraph, this.currentContentIndex, this.cellText, this.style);

        // should be cell text
        assertEquals(this.cellText, tableParagraph.getText());
    }


    @Test
    void addTableCell_shouldAddStyle() {

        XWPFParagraph tableParagraph = this.document.createTable().createRow().createCell().addParagraph();

        // should have no paragraphs yet
        assertTrue(tableParagraph.getRuns().isEmpty());

        this.tableUtils.fillTableCell(tableParagraph, this.currentContentIndex, this.cellText, this.style);

        // should be correct style
        assertEquals(this.style.getFontSize(), tableParagraph.getRuns().get(0).getFontSizeAsDouble().intValue());
        assertEquals(this.style.getFontFamily(), tableParagraph.getRuns().get(0).getFontFamily());
    }


//---------- isTableIndex()
    @Test
    void isTableIndex_tableNotStarted_shouldBeFalse() {

        assertTrue(this.tableUtils.isTableIndex(this.startIndex));
        assertFalse(this.tableUtils.isTableIndex(this.startIndex - 1));
    }


    @Test
    void isTableIndex_tableEnded_shouldBeFalse() {

        assertTrue(this.tableUtils.isTableIndex(this.startIndex + 8));
        assertFalse(this.tableUtils.isTableIndex(this.startIndex + 9));
    }


    @AfterAll
    void cleanUp() throws IOException {

        this.document.close();
    }
}