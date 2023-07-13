package com.example.vorspiel.docxBuilder.basic;

import static com.example.vorspiel.docxBuilder.basic.BasicDocumentBuilder.INDENT_ONE_THIRD_PORTRAIT;
import static com.example.vorspiel.docxBuilder.basic.BasicDocumentBuilder.RESOURCE_FOLDER;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import com.example.vorspiel.docxContent.basic.BasicParagraph;
import com.example.vorspiel.docxContent.basic.style.Color;
import com.example.vorspiel.docxContent.basic.style.BasicStyle;


/**
 * Unit tests for {@link BasicDocumentBuilder}.
 * 
 * @since 0.0.1
 */
@TestInstance(Lifecycle.PER_CLASS)
public class BasicDocumentBuilderTest {

    public static final String TEST_RESOURCE_FOLDER = "./src/test/java/com/example/vorspiel/testResources";

    private XWPFDocument document;

    private String docxFileName;
    
    private BasicStyle style;
    
    private BasicParagraph header; 
    private BasicParagraph title; 
    private BasicParagraph footer; 
    
    private List<BasicParagraph> content;
    
    private BasicDocumentBuilder basicParagraphBuilder;

    private String pdfFileName;
    

    @BeforeEach
    void setup() {
        
        this.docxFileName = "basicTest.docx";
        this.style = new BasicStyle(11, 
                                    "times new roman", 
                                    Color.BLUE, 
                                    true, 
                                    true, 
                                    true,
                                    false,
                                    false,
                                    ParagraphAlignment.CENTER, 
                                    null);        
        this.header = new BasicParagraph("This is the header", this.style);
        this.title = new BasicParagraph("This is the title", this.style);
        this.footer = new BasicParagraph("This is the footer", this.style);
        this.content = Arrays.asList(this.header, this.title, this.footer);
        this.basicParagraphBuilder = new BasicDocumentBuilder(this.content, this.docxFileName);
        this.document = this.basicParagraphBuilder.getDocument();

        this.pdfFileName = "test.pdf";

        new File(RESOURCE_FOLDER + "/" + this.docxFileName).delete();
    }


    // check paragraph exists
    // check file exists
//----------- build()
    @Test
    void build_paragarphAndFileShouldExist() {

        // should have no paragraphs
        assertTrue(this.document.getParagraphs().size() == 0);

        // file should not exist
        assertFalse(new File(BasicDocumentBuilder.RESOURCE_FOLDER + "/" + this.docxFileName).exists());

        this.basicParagraphBuilder.build();

        // should have number of paragraphs minus header and footer
        assertTrue(this.document.getParagraphs().size() == this.content.size() - 2);        

        // should have written to file
        assertTrue(new File(BasicDocumentBuilder.RESOURCE_FOLDER + "/" + this.docxFileName).exists());
    }


//----------- addContent()
    @Test
    void addContent_shouldHaveParagraph() {

        // should have no paragraphs
        assertTrue(this.document.getParagraphs().size() == 0);

        this.basicParagraphBuilder.addContent();

        // should have number of paragraphs minus header and footer
        assertTrue(this.document.getParagraphs().size() == this.content.size() - 2);
    }


//----------- addParagraph()
    @Test
    void addParagraph_basicParagraphNull_shouldNotThrow_shouldAddParagraph() {

        int currentContentIndex = this.content.indexOf(this.title);

        // should have no paragraphs
        assertTrue(this.document.getParagraphs().size() == 0);

        // set basicParagraph null
        this.content.set(currentContentIndex, null);
        assertDoesNotThrow(() -> this.basicParagraphBuilder.addParagraph(currentContentIndex));

        // should still have created paragraph
        assertTrue(this.document.getParagraphs().size() == 1);
    }


    @Test
    void addParagraph_shouldAddTextAndStyle() {

        // use title
        int currentContentIndex = 1;

        this.basicParagraphBuilder.addParagraph(currentContentIndex);

        BasicParagraph expectedParagraph = this.content.get(currentContentIndex);
        XWPFParagraph actualParagraph = this.document.getLastParagraph();
        
        // should have text
        assertEquals(expectedParagraph.getText(), actualParagraph.getText());

        // should have style, check one attribute
        assertEquals(expectedParagraph.getStyle().getTextAlign(), actualParagraph.getAlignment());
    }


//----------- createParagraphByContentIndex()
    @Test
    void createParagraphByContentIndex_basicParagraphNull_shouldReturnNull() {

        // header
        this.content.set(0, null);
        assertEquals(null, this.basicParagraphBuilder.createParagraphByContentIndex(0));

        // footer
        int endIndex = this.content.size() - 1;
        this.content.set(endIndex, null);
        assertEquals(null, this.basicParagraphBuilder.createParagraphByContentIndex(endIndex));
    }

    
    @Test
    void createParagraphByContentIndex_shouldAddHeaderParagraph() {

        // should not have header yet
        assertThrows(NullPointerException.class, () -> this.basicParagraphBuilder.getDocument().getHeaderFooterPolicy().getDefaultHeader().getParagraphs());

        this.basicParagraphBuilder.createParagraphByContentIndex(0);
        assertTrue(this.basicParagraphBuilder.getDocument().getHeaderFooterPolicy().getDefaultHeader().getParagraphs().size() == 1);
    }


    @Test
    void createParagraphByContentIndex_shouldAddFooterParagraph() {

        // should not have header yet
        assertThrows(NullPointerException.class, () -> this.basicParagraphBuilder.getDocument().getHeaderFooterPolicy().getDefaultFooter().getParagraphs());

        this.basicParagraphBuilder.createParagraphByContentIndex(this.content.size() - 1);
        assertTrue(this.basicParagraphBuilder.getDocument().getHeaderFooterPolicy().getDefaultFooter().getParagraphs().size() == 1);
    }


    @Test
    void createParagraphByContentIndex_shouldReturnNormalParagraph() {

        // should not have paragraphs yet
        assertTrue(this.basicParagraphBuilder.getDocument().getParagraphs().size() == 0);

        this.basicParagraphBuilder.createParagraphByContentIndex(1);
        assertTrue(this.basicParagraphBuilder.getDocument().getParagraphs().size() == 1);
    }

    
//----------- addStyle()
    @Test
    void addStyle_paragraphNull_shouldNotThrow() {

        assertDoesNotThrow(() -> this.basicParagraphBuilder.addStyle(null, this.style));
    }


    @Test
    void addStyle_styleNull_shouldNotThrow() {

        assertDoesNotThrow(() -> this.basicParagraphBuilder.addStyle(this.document.createParagraph(), null));
    }


    @Test
    void addStyle_shouldApplyStyleCorrectly() {

        // add some test content
        XWPFParagraph paragraph = this.document.createParagraph();
        XWPFRun run = paragraph.createRun();
        this.basicParagraphBuilder.addStyle(paragraph, this.style);

        // check each style attribute
        assertEquals(this.style.getFontSize(), (int) Math.round(run.getFontSizeAsDouble()));
        assertEquals(this.style.getFontFamily(), run.getFontFamily());
        assertEquals(this.style.getColor().getRGB(), run.getColor());
        assertEquals(this.style.getBold(), run.isBold());
        assertEquals(this.style.getItalic(), run.isItalic());
        assertEquals(this.style.getUnderline(), run.getUnderline().equals(UnderlinePatterns.SINGLE));
        assertEquals(this.style.getTextAlign(), paragraph.getAlignment());
    }


    @Test
    void addStyle_souldApplyIndentFirstLineCorrectly() {

        XWPFParagraph paragraph = this.document.createParagraph();

        this.style.setIndentFirstLine(false);
        this.basicParagraphBuilder.addStyle(paragraph, style);
        assertTrue(paragraph.getIndentationFirstLine() == -1);

        this.style.setIndentFirstLine(true);
        this.basicParagraphBuilder.addStyle(paragraph, style);
        assertTrue(paragraph.getIndentationFirstLine() == INDENT_ONE_THIRD_PORTRAIT);
    }


    @Test
    void addStyle_souldApplyIndentParagraphCorrectly() {

        XWPFParagraph paragraph = this.document.createParagraph();

        this.style.setIndentParagraph(false);
        this.basicParagraphBuilder.addStyle(paragraph, style);
        assertTrue(paragraph.getIndentFromLeft() == -1);

        this.style.setIndentParagraph(true);
        this.basicParagraphBuilder.addStyle(paragraph, style);
        assertTrue(paragraph.getIndentFromLeft() == INDENT_ONE_THIRD_PORTRAIT);
    }


    @Test 
    void addStyle_breakTypeNull_shouldNotThrow() {

        this.style.setBreakType(null);
        assertDoesNotThrow(() -> this.basicParagraphBuilder.addStyle(this.document.createParagraph(), this.style));
    }


//----------- writeDocxFile()
    @Test
    void writeToDocxFile_fileNameWithoutSlash_shouldBeTrue() {

        // file should not exist yet
        assertFalse(new File(RESOURCE_FOLDER + "/" + this.docxFileName).exists());

        // should return true
        assertTrue(this.basicParagraphBuilder.writeDocxFile());

        // file should exist
        assertTrue(new File(RESOURCE_FOLDER + "/" + this.docxFileName).exists());
    }


    @Test
    void writeToDocxFile_fileNameWithSlash_shouldBeTrue() {

        // file should not exist yet
        assertFalse(new File(RESOURCE_FOLDER + "/" + this.docxFileName).exists());

        this.basicParagraphBuilder.setDocxFileName("/" + this.docxFileName);
        assertTrue(this.basicParagraphBuilder.writeDocxFile());

        // file should exist
        assertTrue(new File(RESOURCE_FOLDER + "/" + this.docxFileName).exists());
    }


//----------- convertDocxToPdf()
    @Test
    void convertDocxToPdf_() {
        
        // BasicDocumentBuilder.convertDocxToPdf(new File(TEST_RESOURCE_FOLDER + "/test.docx"), pdfFileName);
    }


//----------- prependSlash()
    @Test
    void prependSlash_strNull_shouldReturnSlash() {
        
        assertEquals("/", BasicDocumentBuilder.prependSlash(null));
    }


    @Test
    void prependSlash_emptyStr_shouldReturnSlash() {

        assertEquals("/", BasicDocumentBuilder.prependSlash(""));
    }


    @Test 
    void prependSlash_strWithSlash_shouldReturnSameStr() {

        String str = "/test.docx";

        assertEquals(str, BasicDocumentBuilder.prependSlash(str));
    }


    @Test 
    void prependSlash_strWithoutSlash_shouldReturnStrWithSlash() {

        String str = "test.docx";

        assertEquals("/" + str, BasicDocumentBuilder.prependSlash(str));
    }


    @AfterAll
    void cleanUp() throws IOException {

        this.document.close();

        new File(RESOURCE_FOLDER + "/" + this.docxFileName).delete();
        // new File(RESOURCE_FOLDER + "/" + this.pdfFileName).delete();
    }
}