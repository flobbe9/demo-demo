package com.example.vorspiel_backend.documentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
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
public class DocumentBuilder {

    public static final String RESOURCE_FOLDER = "./resources";

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

    
    /**
     * Reading the an empty document from an existing file.<p>
     * 
     * Pictures may be added.
     * 
     * @param content list of {@link BasicParagraph}s
     * @param docxFileName file name to write the .docx file to
     * @see PictureType for allowed formats
     */
    public DocumentBuilder(List<BasicParagraph> content, String docxFileName) {

        this.content = content;
        this.docxFileName = prependDateTime(docxFileName);
        this.pictureUtils = new PictureUtils();
        this.document = readDocxFile("EmptyDocument_2Columns.docx");
    }


    /**
     * Reading the an empty document from an existing file.<p>
     * 
     * Pictures and/or one table may be added.
     * 
     * @param content list of {@link BasicParagraph}s
     * @param docxFileName file name to write the .docx file to
     * @param tableConfig wrapper with configuration data for the table to insert
     * @see PictureType for allowed formats    
     */
    public DocumentBuilder(List<BasicParagraph> content, String docxFileName, TableConfig tableConfig) {

        this.content = content;
        this.docxFileName = prependDateTime(docxFileName);
        this.pictureUtils = new PictureUtils();
        this.document = readDocxFile("EmptyDocument_2Columns.docx");
        this.tableUtils = tableConfig != null ? new TableUtils(this.document, tableConfig) : null;
    }


    /**
     * Builds a the document with given list of {@link BasicParagraph}s and writes it to a .docx file which will
     * be located in the {@link #RESOURCE_FOLDER}.
     * 
     * @return true if document was successfully written to a .docx file
     */
    public void build() {
        
        log.info("Starting to build document...");
        
        setOrientation(STPageOrientation.LANDSCAPE);

        addContent();
        
        setDocumentMargins(MINIMUM_MARGIN_TOP_BOTTOM, null, MINIMUM_MARGIN_TOP_BOTTOM, null);
        
        boolean buildSuccessful = writeDocxFile();

        if (buildSuccessful)
            log.info("Finished building document without errors.");
        else
            log.error("Finished building document with errors.");
    }
    

    /**
     * Iterates {@link #content} list and adds all paragraphs to the document.
     */
    void addContent() {

        log.info("Adding content...");

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

        // get content
        BasicParagraph basicParagraph = this.content.get(currentContentIndex);
    
        XWPFParagraph paragraph = createParagraphByContentIndex(currentContentIndex);

        if (basicParagraph != null) {
            // add text
            addText(paragraph, basicParagraph, currentContentIndex);

            // add style
            addStyle(paragraph, basicParagraph.getStyle());

        // case: break intended
        } else if (paragraph != null) 
            paragraph.setSpacingAfter(NO_LINE_SPACE);
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
     * @return created paragraph
     */
    XWPFParagraph createParagraphByContentIndex(int currentContentIndex) {

        // case: table does not need paragrahp from this method
        if (this.tableUtils != null && this.tableUtils.isTableIndex(currentContentIndex))
            return null;

        BasicParagraph basicParagraph = this.content.get(currentContentIndex);

        // case: header
        if (currentContentIndex == 0) {
            if (basicParagraph != null)
                return this.document.createHeader(HeaderFooterType.DEFAULT).createParagraph();

            return null;
        }

        // case: footer
        if (currentContentIndex == this.content.size() - 1) {
            if (basicParagraph != null)
                return this.document.createFooter(HeaderFooterType.DEFAULT).createParagraph();

            return null;
        }

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
        if (this.pictureUtils != null && this.pictureUtils.isPicture(text)) {
            this.pictureUtils.addPicture(paragraph.createRun(), text);
        
        // case: table cell
        } else if (this.tableUtils != null && this.tableUtils.isTableIndex(currentContentIndex)) {
            this.tableUtils.addTableCell(currentContentIndex, text, basicParagraph.getStyle());
            
        // case: plain text
        } else
            paragraph.createRun().setText(text);
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

        if (style.getIndentFirstLine()) 
            paragraph.setIndentationFirstLine(INDENT_ONE_THIRD_PORTRAIT);

        if (style.getIndentParagraph()) 
            paragraph.setIndentFromLeft(INDENT_ONE_THIRD_PORTRAIT);

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
    private CTPageSz getPageSz() {

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
     * File is expected to be located in {@link #RESOURCE_FOLDER}. <p>
     * 
     * Creates and returns a new document if exception is caught.
     * 
     * @param fileName name and suffix of the .docx file
     * @return XWPFDocument of the file or an empty one in case of exception
     */
    XWPFDocument readDocxFile(String fileName) {

        log.info("Starting to read .docx file...");

        try {
            fileName = prependSlash(fileName);

            XWPFDocument document = new XWPFDocument(new FileInputStream(RESOURCE_FOLDER + fileName));

            // clean up document
            document.removeBodyElement(0);

            return document;
        
        } catch (Exception e) {
            ApiExceptionHandler.handleApiException(new ApiException("Failed to read docx file. Returning an empty document instead.", e));

            return new XWPFDocument();
        }
    }
    

    /**
     * Writes the {@link XWPFDocument} to a .docx file. Stores it in {@link #RESOURCE_FOLDER}.
     * 
     * @return true if conversion was successful
     */
    boolean writeDocxFile() {

        log.info("Writing .docx file...");

        try (OutputStream os = new FileOutputStream(RESOURCE_FOLDER + prependSlash(this.docxFileName))) {

            this.document.write(os);
            this.document.close();

            return true;

        } catch (IOException e) {
            throw new ApiException("Failed to write .docx file.", e);
        }
    }


    /**
     * Deletes any file in {@link RESOURCE_FOLDER} that fulfills the conditions of {@link #shouldBeRemovedFromResources(File)}.
     * 
     * @return false if a deletion attempt has failed, else true
     */
    public static boolean clearResourceFolder() {

        File resources = new File(RESOURCE_FOLDER);
        boolean clearedFolder = true;

        if (resources.exists()) {
            // iterate files in ./resources
            for (File docxFile : resources.listFiles()) 
                if (shouldBeRemovedFromResourceFolder(docxFile)) 
                    if (!docxFile.delete()) 
                        clearedFolder = false;
            
            // iterate files in ./resources/picture
            for (File picture : new File(PictureUtils.PICTURES_FOLDER).listFiles())
                if (!picture.delete())
                    clearedFolder = false;
        }

        if (!clearedFolder) 
            log.warn("Failed to clear resourceFolder completely.");

        return clearedFolder;
    }


    /**
     * Convert any .docx file to .pdf file and store in {@link #RESOURCE_FOLDER}.

     * @param docxInputStream inputStream of .docx file
     * @param pdfFileName name and suffix of pdf file (no relative path, file is expected to be located inside {@link #RESOURCE_FOLDER})
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
     * Convert any .docx file to .pdf file and store in {@link #RESOURCE_FOLDER}.<p>
     * 
     * @param docxInputStream inputStream of .docx file
     * @param pdfFileName name and suffix of pdf file (no relative path, file is expected to be located inside {@link #RESOURCE_FOLDER})
     * @return pdf file if conversion was successful
     * @throws ApiException
     */
    public static File docxToPdfDocx4j(InputStream docxInputStream, String pdfFileName) {

        log.info("Converting .docx to .pdf...");
        
        try (OutputStream os = new FileOutputStream(pdfFileName = RESOURCE_FOLDER + prependSlash(pdfFileName))) {
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
        }
    }


    /**
     * Overloading {@link #docxToPdfDocx4j(InputStream, String)}.
     * 
     * @param docxFile
     * @param pdfFileName
     * @return
     * @throws ApiException if docxFile cannot be found
     */
    public static File docxToPdfDocx4j(File docxFile, String pdfFileName) {

        try {
            return docxToPdfDocx4j(new FileInputStream(docxFile), pdfFileName);

        } catch (IOException e) {
            throw new ApiException("Failed to convert .docx to .pdf.", e);
        }
    }


    /**
     * Prepends a '/' to given String if there isn't already one.
     * 
     * @param str String to prepend the slash to
     * @return the altered (or not altered) string or "/" if given str is null
     */
    public static String prependSlash(String str) {

        if (str == null || str.equals(""))
            return "/";

        return str.charAt(0) == '/' ? str : "/" + str;
    }


    /**
     * Prepends current date and time to given string. Replace ':' with '-' due to
     * .docx naming conditions.
     * 
     * @param str String to format
     * @return current date and time plus str
     */
    private static String prependDateTime(String str) {

        return LocalDateTime.now().toString().replace(":", "-") + "_" + str;
    }


    /**
     * Checks if given file is user generated and hence should be deleted after beeing used.
     * 
     * @param file to check
     * @return true true for user generated files
     */
    private static boolean shouldBeRemovedFromResourceFolder(File file) {

        String fileName = file.getName();

        return isInteger(Character.toString(fileName.charAt(0))) && 
               fileName.endsWith(".docx");
    }


    /**
     * Checks if given string is an integer.
     * 
     * @param str to check
     * @return true if str is integer, else false (even in case it's a double)
     */
    private static boolean isInteger(String str) {

        try {
            Integer.parseInt(str);

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }
}