package com.example.vorspiel_backend.documentBuilder;

import static com.example.vorspiel_backend.utils.Utils.DOCX_FOLDER;
import static com.example.vorspiel_backend.utils.Utils.PDF_FOLDER;
import static com.example.vorspiel_backend.utils.Utils.STATIC_FOLDER;
import static com.example.vorspiel_backend.utils.Utils.RESOURCE_FOLDER;
import static com.example.vorspiel_backend.utils.Utils.prependSlash;

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
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import com.example.vorspiel_backend.documentParts.BasicParagraph;
import com.example.vorspiel_backend.documentParts.TableConfig;
import com.example.vorspiel_backend.documentParts.style.Style;
import com.example.vorspiel_backend.exception.ApiException;
import com.example.vorspiel_backend.exception.ApiExceptionHandler;
import com.example.vorspiel_backend.utils.Utils;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
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
// TODO: reconsider table size for multiple columns
public class DocumentBuilder {

    /** paragraph indentation */
    public static final int INDENT_ONE_THIRD_PORTRAIT = 2000;

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
    public static final int MINIMUM_MARGIN_TOP_BOTTOM = 240;

    /** minimum line space (Zeilenabstand) */
    public static final int NO_LINE_SPACE = 1;

    /** declares that a tab should be added here instead of the actual text */
    public static final String TAB_SYMBOL = "//TAB";
    
    @NotNull(message = "'content' cannot be null.")
    private List<BasicParagraph> content;
    
    @NotEmpty(message = "'docxFileName' cannot be empty or null.")
    @Pattern(regexp = ".*\\.docx$", message = "Wrong format of 'docxFileName'. Only '.docx' permitted.")
    private String docxFileName;

    /** may be null */
    private PictureUtils pictureUtils;

    /** may be null */
    private TableUtils tableUtils;  

    private XWPFDocument document;

    private boolean landscape;

    
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
    public DocumentBuilder(List<BasicParagraph> content, String docxFileName, int numColumns, boolean landscape, Map<String, byte[]> pictures) {

        this.content = content;
        this.docxFileName = Utils.prependDateTime(docxFileName);
        this.pictureUtils = new PictureUtils(pictures);
        this.landscape = landscape;
        this.document = numColumns == 1 ? new XWPFDocument() : 
                                          readDocxFile(STATIC_FOLDER + prependSlash(getDocumentTemplateFileName(numColumns)));
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
    public DocumentBuilder(List<BasicParagraph> content, String docxFileName, int numColumns, boolean landscape, TableConfig tableConfig, Map<String, byte[]> pictures) {

        this.content = content;
        this.docxFileName = Utils.prependDateTime(docxFileName);
        this.pictureUtils = new PictureUtils(pictures);
        this.landscape = landscape;
        this.document = numColumns == 1 ? new XWPFDocument() : 
                                          readDocxFile(STATIC_FOLDER + prependSlash(getDocumentTemplateFileName(numColumns)));
        this.tableUtils = tableConfig != null ? new TableUtils(this.document, tableConfig) : null;
    }


    /**
     * Builds a the document with given list of {@link BasicParagraph}s and writes it to a .docx file which will
     * be located in the {@link #DOCX_FOLDER}.
     */
    public void build() {
        
        log.info("Starting to build document...");
        
        setOrientation(this.landscape ? STPageOrientation.LANDSCAPE : STPageOrientation.PORTRAIT);

        addContent();
        
        setDocumentMargins(MINIMUM_MARGIN_TOP_BOTTOM, null, MINIMUM_MARGIN_TOP_BOTTOM, null);
        
        log.info("Finished building document");
    }
    

    /**
     * Iterates {@link #content} list and adds all paragraphs to the document.
     */
    void addContent() {

        int numParagraphs = this.content.size();

        // case: no content
        if (numParagraphs == 0) 
            log.warn("Not adding any paragraphs because content list is empty.");

        for (int i = 0; i < numParagraphs; i++) 
            addParagraph(i);
    }


    /**
     * Adds {@link BasicParagraph} from content list at given index to the document. This includes text and style. <p>

     * If basicParagraph is null, an {@link XWPFPargraph} will be added anyway an hence appear as a line break. 
     * This applies <strong>not</strong> for header and footer.
     * 
     * @param currentContentIndex index of the {@link #content} element currently processed
     */
    void addParagraph(int currentContentIndex) {

        // get line
        BasicParagraph basicParagraph = this.content.get(currentContentIndex);
        if (basicParagraph == null)
            throw new ApiException("Failed to add paragraph. 'basicParagraph' cannot be null");
    
        XWPFParagraph paragraph = createParagraphByContentIndex(currentContentIndex);

        // case: inside table and is picture
        if (paragraph == null && PictureUtils.isPicture(basicParagraph.getText())) {
            log.warn("Failed to picture " + basicParagraph.getText() + ". Cannot add picture inside table.");
            return;    
        }

        // add text
        addText(paragraph, basicParagraph, currentContentIndex);

        // add style
        addStyle(paragraph, basicParagraph.getStyle());

        // case: break intended
        // TODO: not sure if this is necessary
        // if (paragraph != null)
        //     paragraph.setSpacingAfter(NO_LINE_SPACE);
    }


    /**
     * Adds an {@link XWPFParagraph} to the document either for the header, the footer or the main content. <p>
     * 
     * For the fist element (index = 0) a header paragraph will be generated and for the last element a footer paragraph.
     * Tables wont get a paragraph since it's generated in {@link TableUtils}. <p>
     * 
     * Any other element gets a normal paragraph.
     * 
     * @param currentContentIndex index of the {@link #content} element currently processed
     * @return created paragraph or null if is table
     */
    XWPFParagraph createParagraphByContentIndex(int currentContentIndex) {

        // case: table does not need paragrahp from this method
        if (this.tableUtils != null && this.tableUtils.isTableIndex(currentContentIndex))
            return null;

        // case: header (not blank)
        if (currentContentIndex == 0 && !this.content.get(currentContentIndex).getText().isBlank())
            return this.document.createHeader(HeaderFooterType.DEFAULT).createParagraph();

        // case: footer (not blank)
        if (currentContentIndex == this.content.size() - 1 && !this.content.get(currentContentIndex).getText().isBlank())
            return this.document.createFooter(HeaderFooterType.DEFAULT).createParagraph();

        // case: any other
        return this.document.createParagraph();
    }


    /**
     * Adds the "text" class variable of {@link BasicParagraph} to given {@link XWPFRun}. <p>
     * 
     * "text" will be added as plain string, as picture or inside a table.
     * 
     * @param paragraph to add text and style to
     * @param basicParagraph to use the text and style information from
     * @param currentContentIndex index of the {@link #content} element currently processed
     */
    void addText(XWPFParagraph paragraph, BasicParagraph basicParagraph, int currentContentIndex) {

        String text = basicParagraph.getText();

        // case: picture
        if (this.pictureUtils != null && PictureUtils.isPicture(text))
            this.pictureUtils.addPicture(paragraph.createRun(), text);
        
        // case: table cell
        else if (this.tableUtils != null && this.tableUtils.isTableIndex(currentContentIndex))
            this.tableUtils.addTableCell(currentContentIndex, text, basicParagraph.getStyle());
            
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
    }


    /**
     * Set margins for the whole document.<p>
     * 
     * If null value will be set.
     * 
     * @param top margin
     * @param right margin
     * @param bottom margin
     * @param left margin
     */
    private void setDocumentMargins(Integer top, Integer right, Integer bottom, Integer left) {

        CTSectPr sectPr = this.document.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.addNewPgMar();

        if (top != null) 
            pageMar.setTop(BigInteger.valueOf(top));

        if (right != null) 
            pageMar.setRight(BigInteger.valueOf(right));

        if (bottom != null) 
            pageMar.setBottom(BigInteger.valueOf(bottom));

        if (left != null) 
            pageMar.setLeft(BigInteger.valueOf(left));
    }


    /**
     * Possible values are landscape or portrait.
     * If called multiple times the last call will be the effectiv one.
     * 
     * @param orientation landscape or portrait
     */
    private void setOrientation(STPageOrientation.Enum orientation) {

        if (orientation == null)
            return;

        setPageSizeDimensions(orientation);
        getPageSz().setOrient(orientation);
    }


    /**
     * Set height and width of the CTPageSz according to given orientation(landscape or portrait).
     * 
     * @param orientation the page should have
     * @param pageSize CTPageSz object of page
     * @return altered pageSize
     */
    private CTPageSz setPageSizeDimensions(STPageOrientation.Enum orientation) {

        CTPageSz pageSize = getPageSz();

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
    public CTPageSz getPageSz() {

        CTSectPr sectPr = getSectPr();

        return sectPr.getPgSz() == null ? sectPr.addNewPgSz() : sectPr.getPgSz();
    }


    /**
     * Get existing {@link CTSectPr} or add new one.
     * 
     * @return sectPr object of document
     */
    private CTSectPr getSectPr() {

        CTBody ctBody = this.document.getDocument().getBody();

        return ctBody.getSectPr() == null ? ctBody.addNewSectPr() : ctBody.getSectPr();
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
    

    public static String getDocumentTemplateFileName(int numColumns) {

        return "Empty_" + numColumns + "Columns.docx";
    }
}