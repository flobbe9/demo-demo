package de.word_light.documentBuilder;

import static de.word_light.utils.Utils.DOCX_FOLDER;
import static de.word_light.utils.Utils.PDF_FOLDER;
import static de.word_light.utils.Utils.RESOURCE_FOLDER;
import static de.word_light.utils.Utils.prependSlash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabStop;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STSectionMark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabJc;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import de.word_light.documentParts.BasicParagraph;
import de.word_light.documentParts.TableConfig;
import de.word_light.documentParts.style.Style;
import de.word_light.exception.ApiException;
import de.word_light.exception.ApiExceptionHandler;
import de.word_light.utils.Utils;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;


/**
 * Class to build and write a .docx document.
 * 
 * @since 0.0.1
 * @see BasicParagraph
 * @see Style
 */
@Log4j2
@Getter
@Setter
@NoArgsConstructor
// TODO: reconsider table size for multiple columns
// TODO: make more methods public and chainable, which fields are mandatory?
// TODO: consider offering multiple sections with differen num cols, change num cols to map or something
public class DocumentBuilder {

    /** paragraph indentation */
    public static final int INDENT_ONE_THIRD_PORTRAIT = 2000;

    /** tab stops in twips (twentieth of an inch point) */
    public static final int TAB_STOP_ONE_SIXTH_OF_LINE = 1440; 
    public static final int TAB_STOP_LINE_CENTER = TAB_STOP_ONE_SIXTH_OF_LINE * 3;
    public static final int TAB_STOP_LINE_END = TAB_STOP_ONE_SIXTH_OF_LINE * 6;

    /** table dimensions */
    public static final int PAGE_LONG_SIDE_WITH_BORDER = 13300;

    /** orientation dimensions  */
    public static final BigInteger PAGE_LONG_SIDE = BigInteger.valueOf(842 * 20);
    public static final BigInteger PAGE_SHORT_SIDE = BigInteger.valueOf(595 * 20);

    /** picture dimensions in centimeters. */
    public static final int PICTURE_WIDTH_PORTRAIT = 15;
    public static final int PICTURE_WIDTH_LANDSCAPE_HALF = 11;
    public static final int PICTURE_HEIGHT_LANDSCAPE_HALF = 7;

    /** document margins */
    public static final int MINIMUM_MARGIN_TOP = 240;
    public static final int MINIMUM_MARGIN_BOTTOM = 240;

    /** minimum line space (Zeilenabstand) */
    public static final int NO_LINE_SPACE = 1;

    /** declares that a tab should be added here instead of the actual text */
    public static final String TAB_SYMBOL = "\\t";
    
    // comes from request body
    @NotNull(message = "'content' cannot be null.")
    private List<BasicParagraph> content;
    
    @NotEmpty(message = "'docxFileName' cannot be empty or null.")
    @Pattern(regexp = ".*\\.docx$", message = "Wrong format of 'docxFileName'. Only '.docx' permitted.")
    private String docxFileName;

    @Nullable
    private PictureUtils pictureUtils;

    @Nullable
    private TableUtils tableUtils;  

    private XWPFDocument document;

    private boolean landscape;

    private int numColumns;

    @Min(value = 0, message = "'numSingleColumnLines' too small. Min: 0") 
    private int numSingleColumnLines;

    
    /**
     * Reading the an empty document from an existing file.<p>
     * 
     * Pictures may be added.
     * 
     * @param content list of {@link BasicParagraph}s
     * @param docxFileName file name to write the .docx file to
     * @param numColumns number of columns a page will be devided in
     * @param landscape true if document should be in landscape mode, else portrait is used
     * @param pictures map of filename and bytes of pictures in the document
     * @see PictureType for allowed formats
     */
    public DocumentBuilder(List<BasicParagraph> content, String docxFileName, int numColumns, int numSingleColumnLines, boolean landscape, Map<String, byte[]> pictures) {

        this.content = content;
        this.docxFileName = Utils.prependDateTime(docxFileName);
        this.pictureUtils = new PictureUtils(pictures);
        this.landscape = landscape;
        this.numColumns = numColumns;
        this.numSingleColumnLines = numSingleColumnLines;
        this.document = new XWPFDocument();
    }


    /**
     * Reading the an empty document from an existing file.<p>
     * 
     * Pictures and/or one table may be added.
     * 
     * @param content list of {@link BasicParagraph}s
     * @param docxFileName file name to write the .docx file to
     * @param numColumns number of columns a page will be devided in
     * @param landscape true if document should be in landscape mode, else portrait is used
     * @param tableConfig wrapper with configuration data for the table to insert
     * @param pictures map of filename and bytes of pictures in the document
     * @see PictureType for allowed formats    
     */
    public DocumentBuilder(List<BasicParagraph> content, String docxFileName, int numColumns, int numSingleColumnLines, boolean landscape, Map<String, byte[]> pictures, List<TableConfig> tableConfigs) {

        this.content = content;
        this.docxFileName = Utils.prependDateTime(docxFileName);
        this.pictureUtils = new PictureUtils(pictures);
        this.landscape = landscape;
        this.numColumns = numColumns;
        this.numSingleColumnLines = numSingleColumnLines;
        this.document = new XWPFDocument();
        this.tableUtils = !tableConfigs.isEmpty() ? new TableUtils(this.document, tableConfigs) : null;
    }


    /**
     * Builds a the document with given list of {@link BasicParagraph}s and writes it to a .docx file which will
     * be located in the {@link #DOCX_FOLDER}.<p>
     * 
     * Configure document settings before adding content.
     */
    public DocumentBuilder build() {
        
        setOrientation();

        setDocumentMargins(MINIMUM_MARGIN_TOP, null, MINIMUM_MARGIN_BOTTOM, null);
        
        addContent();

        // do this after addContent()!
        setDocumentColumns();

        return this;
    }
    

    /**
     * Iterates {@link #content} list and adds all paragraphs to the document. Separate heading section if necessary.
     */
    public DocumentBuilder addContent() {

        log.info("Adding content...");

        int numParagraphs = this.content.size();

        // case: no content
        if (numParagraphs == 0) {
            log.warn("Not adding any paragraphs because content list is empty.");
            return this;
        }

        // add empty paragraph above very first column to even out empty column break paragraphs
        // TODO: is this always necessary? 
        this.content.add(this.numSingleColumnLines + 1, new BasicParagraph("", Style.getDefaultInstance()));

        for (int i = 0; i < numParagraphs; i++) {
            XWPFParagraph paragraph = addParagraph(i);

            // case: is last single column line and heading section cols differ from rest
            if (i == this.numSingleColumnLines && this.numColumns > 1 && this.numSingleColumnLines >= 1) 
                separateSection(paragraph);
        }

        return this;
    }


    /**
     * Set the orientation for the whole document.
     * If called multiple times the last call will be the effectiv one.
     * 
     * @param landscape if true use landscape mode, else portrait
     */
    // TODO: add test
    public DocumentBuilder setOrientation() {

        log.info("Setting orientation...");

        STPageOrientation.Enum orientation = this.landscape ? STPageOrientation.LANDSCAPE : STPageOrientation.PORTRAIT;

        setPageSizeDimensions(orientation);
        getPgSz().setOrient(orientation);

        return this;
    }
            

    /**
     * Set margins for the whole document.<p>
     * 
     * If null no value will be set.
     * 
     * @param top margin
     * @param right margin
     * @param bottom margin
     * @param left margin
     */
    // TODO: add test
    public DocumentBuilder setDocumentMargins(Integer top, Integer right, Integer bottom, Integer left) {

        log.info("Setting document margins...");

        CTPageMar pageMar = getSectPr().addNewPgMar();

        if (top != null) 
            pageMar.setTop(BigInteger.valueOf(top));

        if (right != null) 
            pageMar.setRight(BigInteger.valueOf(right));

        if (bottom != null) 
            pageMar.setBottom(BigInteger.valueOf(bottom));

        if (left != null) 
            pageMar.setLeft(BigInteger.valueOf(left));

        return this;
    }


    /**
     * Add MS Word columns (min 1, max 3) to {@code this.document}.
     * 
     */
    // TODO: add test
    public DocumentBuilder setDocumentColumns() {

        log.info("Setting document columns...");

        if (this.numColumns < 1 || this.numColumns > 3) {
            log.warn("'setDocumentColumns()' parameter 'numColumns' must be less than equal 3 and greater than equal 1 but is: " + this.numColumns + ". Using 1 as default");
            this.numColumns = 1;
        }

        for (int i = 0; i < this.numColumns; i++) 
            getSectPr().addNewCols().setNum(BigInteger.valueOf(i + 1));

        return this;
    }


    /**
     * Adds {@link BasicParagraph} from content list at given index to the document. This includes text and style. <p>

     * If basicParagraph is null, an {@link XWPFPargraph} will be added anyway an hence appear as a line break. 
     * This applies <strong>not</strong> for header and footer.
     * 
     * @param currentContentIndex index of the {@link #content} element currently processed
     */
    XWPFParagraph addParagraph(int currentContentIndex) {

        // get line
        BasicParagraph basicParagraph = this.content.get(currentContentIndex);
        if (basicParagraph == null)
            throw new ApiException("Failed to add paragraph. 'basicParagraph' cannot be null");
    
        XWPFParagraph paragraph = createParagraphByContentIndex(currentContentIndex, basicParagraph.getStyle());
        if (paragraph == null)
            return null;

        // add text
        addText(paragraph, basicParagraph, currentContentIndex);

        // add style
        addStyle(paragraph, basicParagraph.getStyle());

        return paragraph;
    }


    /**
     * Adds an {@link XWPFParagraph} to the document either for the header, the footer or the main content. <p>
     * 
     * For the fist element (index = 0) a header paragraph will be generated and for the last element a footer paragraph. <p>
     * 
     * Tables will get a table paragraph.<p>
     * 
     * For any other element a new normal paragraph is appended.
     * 
     * @param currentContentIndex index of the {@link #content} element currently processed
     * @param style style of {@link BasicParagraph}
     * @return created paragraph or null if is table
     */
    XWPFParagraph createParagraphByContentIndex(int currentContentIndex, Style style) {

        // case: table
        if (this.tableUtils != null && this.tableUtils.isTableIndex(currentContentIndex))
            return this.tableUtils.createTableParagraph(currentContentIndex, style);

        // case: header
        if (currentContentIndex == 0)
            // case: no blank
            if (!this.content.get(currentContentIndex).getText().isBlank())
                return this.document.createHeader(HeaderFooterType.DEFAULT).createParagraph();
            else
                return null;

        // case: footer
        if (currentContentIndex == this.content.size() - 1)
            // case: no blank
            if (!this.content.get(currentContentIndex).getText().isBlank())
                return this.document.createFooter(HeaderFooterType.DEFAULT).createParagraph();
            else
                return null;

        // case: any other
        return this.document.createParagraph();
    }


    /**
     * Adds the "text" class variable of {@link BasicParagraph} to given {@link XWPFRun}. <p>
     * 
     * "text" will be added as plain string, as picture or inside a table.<p>
     * 
     * A picture cannot be added inside a table, the plain text of the {@link  BasicParagraph} plus an 
     * error message will be added instead.
     * 
     * @param paragraph to add text and style to
     * @param basicParagraph to use the text and style information from
     * @param currentContentIndex index of the {@link #content} element currently processed
     */
    void addText(XWPFParagraph paragraph, BasicParagraph basicParagraph, int currentContentIndex) {

        String text = basicParagraph.getText();

        // case: picture inside table
        if (this.tableUtils != null && this.tableUtils.isTableIndex(currentContentIndex) && PictureUtils.isPicture(text)) {
            log.warn("Failed to picture " + text + ". Cannot add picture inside table. Adding plain text instead.");
            addPlainTextToRun(paragraph.createRun(), text + "(Cannot add picture inside table)");
            return;
        }

        // case: picture
        if (PictureUtils.isPicture(text))
            this.pictureUtils.addPicture(paragraph.createRun(), text);
        
        // case: table cell
        else if (this.tableUtils != null && this.tableUtils.isTableIndex(currentContentIndex))
            this.tableUtils.fillTableCell(paragraph, currentContentIndex, text, basicParagraph.getStyle());
            
        // case: plain text
        else
            addPlainTextToRun(paragraph.createRun(), text);
    }


    /**
     * Add plain text to given {@link XWPFRun}. <p>
     * 
     * Any {@link #TAB_SYMBOL} will be replaced with an actual tab.
     * 
     * @param run to add the text to
     * @param text to add
     */
    private void addPlainTextToRun(XWPFRun run, String text) {

        String[] textArr = text.split(TAB_SYMBOL);

        for (int i = 0; i < textArr.length; i++) {
            run.setText(textArr[i]);

            // case: is not last element
            if (i != textArr.length - 1) 
                run.addTab();
        }

        // case: text ends with tab
        if (text.endsWith(TAB_SYMBOL))
            run.addTab();
    }


    /**
     * Add style to given {@link XWPFParagraph}. Is skipped if either paragraph or style are null.
     * 
     * @param paragraph to apply the style to
     * @param style information to use
     * @see Style
     */
    static void addStyle(XWPFParagraph paragraph, Style style) {

        if (paragraph == null || style == null)
            return;

        paragraph.getRuns().forEach(run -> {
            run.setFontSize(style.getFontSize());

            run.setFontFamily(style.getFontFamily());

            run.setColor(style.getColor());

            run.setBold(style.getBold());

            run.setItalic(style.getItalic());

            if (style.getBreakType() != null) 
                run.addBreak(style.getBreakType());

            if (style.getUnderline()) 
                run.setUnderline(UnderlinePatterns.SINGLE);
        });

        paragraph.setAlignment(style.getTextAlign());

        paragraph.setSpacingAfter(NO_LINE_SPACE);

        setTabSize(paragraph, style.getFontSize());
    }


    // TODO: continue here, make this more generic
    private static void setTabSize(XWPFParagraph paragraph, int fontSize) {

        for (int i = 0; i < 17; i++) {
            CTTabStop tabStop = paragraph.getCTP().getPPr().addNewTabs().addNewTab();
            tabStop.setPos(BigInteger.valueOf((i + 1) * 36 * fontSize));
        }
    }


    /**
     * Set height and width of the CTPageSz according to given orientation(landscape or portrait) and dimension constants.
     * 
     * @param orientation the page should have
     * @param pageSize CTPageSz object of page
     * @return altered pageSize
     */
    private CTPageSz setPageSizeDimensions(STPageOrientation.Enum orientation) {

        CTPageSz pageSize = getPgSz();

        // case: landscape
        if (orientation.equals(STPageOrientation.LANDSCAPE)) {
            pageSize.setW(PAGE_LONG_SIDE);
            pageSize.setH(PAGE_SHORT_SIDE);

        // case: portrait
        } else {
            pageSize.setW(PAGE_SHORT_SIDE);
            pageSize.setH(PAGE_LONG_SIDE);
        }

        return pageSize;
    }


    /**
     * Get existing {@link CTPageSz} or add new one.
     * 
     * @return pageSz object of document
     */
    CTPageSz getPgSz() {

        CTSectPr sectPr = getSectPr();

        return sectPr.getPgSz() == null ? sectPr.addNewPgSz() : sectPr.getPgSz();
    }


    /**
     * 
     * @param ctSectPr
     * @return
     */
    // TODO: dont make this generic
    private CTSectPr setUpSectPr(CTSectPr ctSectPr) {

        if (ctSectPr == null)
            ctSectPr = getSectPr();

        CTSectType ctSectType = CTSectType.Factory.newInstance();
        ctSectType.setVal(STSectionMark.CONTINUOUS);
        ctSectPr.setType(ctSectType);

        return ctSectPr;
    }


    /**
     * Always gets first {@link CTSectPr} object of document (even if multiple are present) or adds new one if non has been created yet. Call {@link #setUpSectPr()} on it.
     * 
     * @return first sectPr object of document
     */
    private CTSectPr getSectPr() {

        CTBody ctBody = this.document.getDocument().getBody();
        CTSectPr ctSectPr = ctBody.getSectPr();

        // case: no sectPr created yet
        if (ctSectPr == null)
            return addNewSectPr();

        // case: no rsidR yet
        setUpSectPr(ctSectPr);

        return ctSectPr;
    }


    /**
     * @return append new {@link CTSectPr} and call {@link #setUpSectPr(CTSectPr)}
     */
    private CTSectPr addNewSectPr() {

        CTSectPr newSectPr = this.document.getDocument().getBody().addNewSectPr();

        setUpSectPr(newSectPr);

        return newSectPr;
    }


    /**
     * Add {@link CTSectPr} to given paragraph in order to separate paragraphs above (including given one) from paragraphs below.
     * 
     * @param paragraph to pass the {@link CTSectPr} to
     */
    private DocumentBuilder separateSection(XWPFParagraph paragraph) {

        if (paragraph == null)  
            return this;

        paragraph.getCTP().getPPr().setSectPr(getSectPr());

        return this;
    }


    /**
     * Reads given .docx file to an {@link XWPFDocument} and cleans up any content. <p>
     * 
     * Creates and returns a new document if exception is caught.
     * 
     * @param fileName name and suffix of the .docx file
     * @return XWPFDocument of the file or an empty one in case of exception
     */
    XWPFDocument readDocxFile(String fileName) {

        log.info("Starting to read .docx file...");

        try {
            XWPFDocument document = new XWPFDocument(new FileInputStream(fileName));

            // clean up document
            document.removeBodyElement(0);

            return document;
        
        } catch (Exception e) {
            ApiExceptionHandler.handleApiException(new ApiException("Failed to read docx file. Returning an empty document instead.", e));

            return new XWPFDocument();
        }
    }


    /**
     * Writes the {@link XWPFDocument} to a .docx file. Checks if exists and stores it in {@link #DOCX_FOLDER}.
     * 
     * @return the .docx file
     */
    public File writeDocxFile() {

        log.info("Writing .docx file...");

        String completeFileName = DOCX_FOLDER + prependSlash(this.docxFileName);

        try (OutputStream os = new FileOutputStream(completeFileName)) {

            this.document.write(os);
            this.document.close();

            File docxFile = new File(completeFileName);

            if (!docxFile.exists())
                throw new ApiException("Failed to create document. 'docxFile' does not exist.");

            log.info("Finished writing .docx file");

            return docxFile;

        } catch (IOException e) {
            throw new ApiException("Failed to write .docx file.", e);
        }
    }


    /**
     * Convert any .docx file to .pdf file and store in {@link #PDF_FOLDER}.

     * @param docxInputStream inputStream of .docx file
     * @param pdfFileName name and suffix of pdf file (no relative path, file is expected to be located inside {@link #PDF_FOLDER})
     * @return pdf file if conversion was successful
     * @throws ApiException
     */
    public static File docxToPdfIText(InputStream docxInputStream, String pdfFileName) {

        log.info("Converting .docx to .pdf...");

        try (XWPFDocument document = new XWPFDocument(docxInputStream);
            OutputStream pdfOutputStream = new FileOutputStream(pdfFileName = RESOURCE_FOLDER + prependSlash(pdfFileName))) {

            Document pdfDocument = new Document();
            PdfWriter.getInstance(pdfDocument, pdfOutputStream);
            pdfDocument.open();

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                Paragraph pdfParagraph = new Paragraph(paragraph.getText());
                pdfParagraph.cloneShallow(false);
                
                pdfDocument.add(pdfParagraph);
            }
            
            pdfDocument.close();

            return new File(pdfFileName);

        } catch (Exception e) {
            throw new ApiException("Failed to convert .docx to .pdf.", e);
        }
    }


    /**
     * Convert any .docx file to .pdf file and store in {@link #PDF_FOLDER}.<p>
     * 
     * @param docxInputStream inputStream of .docx file
     * @param pdfFileName name and suffix of pdf file (no relative path, file is expected to be located inside {@link #PDF_FOLDER})
     * @return pdf file if conversion was successful
     * @throws ApiException
     */
    public static File docxToPdfDocuments4j(InputStream docxInputStream, String pdfFileName) {

        log.info("Converting .docx to .pdf...");
        
        try (OutputStream os = new FileOutputStream(pdfFileName = PDF_FOLDER + prependSlash(pdfFileName))) {
            IConverter converter = LocalConverter.builder().build();
            
            converter.convert(docxInputStream)
                     .as(DocumentType.DOCX)
                     .to(os)
                     .as(DocumentType.PDF)
                     .execute();

            converter.shutDown();

            return new File((pdfFileName));

        } catch (Exception e) {
            throw new ApiException("Failed to convert .docx to .pdf.", e);
            
        } finally {
            log.info("Finished converting .docx to .pdf");
        }
    }


    /**
     * Overloading {@link #docxToPdfDocuments4j(InputStream, String)}.
     * 
     * @param docxFile
     * @param pdfFileName
     * @return
     * @throws ApiException if docxFile cannot be found
     */
    public static File docxToPdfDocuments4j(File docxFile, String pdfFileName) {

        try {
            return docxToPdfDocuments4j(new FileInputStream(docxFile), pdfFileName);

        } catch (IOException e) {
            throw new ApiException("Failed to convert .docx to .pdf.", e);
        }
    }
}