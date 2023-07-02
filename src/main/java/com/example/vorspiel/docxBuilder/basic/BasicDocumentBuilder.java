package com.example.vorspiel.docxBuilder.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;

import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import com.example.vorspiel.docxContent.basic.BasicParagraph;
import com.example.vorspiel.docxContent.basic.style.BasicStyle;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;


/**
 * Class to build and write any simple document that contains only text and simple styles.
 * 
 * @since 0.0.1
 * @see BasicParagraph
 * @see BasicStyle
 */
@Log4j2
@Getter
@Setter
public class BasicDocumentBuilder {

    public static final String RESOURCE_FOLDER = "./resources";

    // TODO: add description for dimension fields
    public static final int INDENT_ONE_THIRD_PORTRAIT = 2000;

    public static final int PAGE_LONG_SIDE_WITH_BORDER = 14000;

    public static final BigInteger PAGE_LONG_SIDE = BigInteger.valueOf(842 * 20);

    public static final BigInteger PAGE_SHORT_SIDE = BigInteger.valueOf(595 * 20);
    
    private XWPFDocument document;
    
    @NotNull(message = "content cannot be null.")
    private List<BasicParagraph> content;

    @NotEmpty(message = "docxFileName cannot be empty or null.")
    private String docxFileName;

    
    /**
     * Standard constructor setting the document field to an empty {@link XWPFDocument}.
     * 
     * @param content list of {@link BasicParagraph}s
     * @param docxFileName file name to write the .docx file to
     */
    public BasicDocumentBuilder(@NotNull(message = "content cannot be null.") List<BasicParagraph> content,
                                 @NotEmpty(message = "docxFileName cannot be empty or null.") String docxFileName) {

        this.content = content;
        this.docxFileName = docxFileName;
        this.document = new XWPFDocument();
    }


    /**
     * Builds a the document with given list of {@link BasicParagraph}s and writes it to a .docx file which will
     * be located in the {@link #RESOURCE_FOLDER}.
     * 
     * @return true if document was successfully written to a .docx file
     */
    public boolean build() {

        log.info("Starting to build document...");

        addContent();

        return writeDocxFile();
    }
    

    /**
     * Iterates {@link #content} list and adds all paragraphs to the document.
     */
    protected void addContent() {

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
     * If the basicParagraph is null, an {@link XWPFPargraph} will be add anyway an hence appear as a line break.
     * 
     * @param contentIndex index of the {@link #content} element currently processed
     */
    protected void addParagraph(int contentIndex) {
    
        XWPFParagraph paragraph = createParagraphByContentIndex(contentIndex);
        XWPFRun run = paragraph.createRun();

        BasicParagraph basicParagraph = this.content.get(contentIndex);
        
        if (basicParagraph != null) {
            BasicStyle style = basicParagraph.getStyle();
            
            // add text
            run.setText(this.content.get(contentIndex).getText());

            // add style
            addStyle(paragraph, style);
        }
    }


    /**
     * Adds an {@link XWPFParagraph} to the document either for the header, the footer or the main content. <p>
     * For the fist element (index = 0) a header paragraph will be generated, for the last element a footer paragraph
     * and for any other element a normal paragraph.
     * 
     * @param contentIndex index of the {@link #content} element currently processed
     * @return created paragraph
     */
    protected XWPFParagraph createParagraphByContentIndex(int contentIndex) {
        
        // header
        if (contentIndex == 0)
            return this.document.createHeader(HeaderFooterType.DEFAULT).createParagraph();

        // footer
        if (contentIndex == this.content.size() - 1)
            return this.document.createFooter(HeaderFooterType.DEFAULT).createParagraph();

        // any other
        return this.document.createParagraph();
    }

        
    protected void addStyle(XWPFParagraph paragraph, BasicStyle style) {

        paragraph.getRuns().forEach(run -> {
            run.setFontSize(style.getFontSize());

            run.setFontFamily(style.getFontFamily());

            run.setColor(style.getColor().getRGB());

            run.setBold(style.getBold());

            run.setItalic(style.getItalic());

            if (style.getBreakType() != null) run.addBreak(style.getBreakType());

            if (style.getUnderline()) run.setUnderline(UnderlinePatterns.SINGLE);
        });

        if (style.getIndentFirstLine()) paragraph.setIndentationFirstLine(INDENT_ONE_THIRD_PORTRAIT);

        if (style.getIndentParagraph()) paragraph.setIndentFromLeft(INDENT_ONE_THIRD_PORTRAIT);

        paragraph.setAlignment(style.getTextAlign());
    }
    
    
    /**
     * Writes the {@link XWPFDocument} to a .docx file. Stores it in {@link #RESOURCE_FOLDER}.
     * 
     * @return true if conversion was successful
     */
    protected boolean writeDocxFile() {

        log.info("Starting to write.docx file...");

        docxFileName = prependSlash(docxFileName);
        
        try (OutputStream os = new FileOutputStream(RESOURCE_FOLDER + docxFileName)) {

            this.document.write(os);
            this.document.close();
            
            log.info("Finished writing .docx file.");
            
            return true;

        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return false;
    }


    /**
     * Convert any .docx file to .pdf file and store in {@link #RESOURCE_FOLDER}.<p>
     * Thread safe, since accessessing existing files.
     * 
     * @param docxInputStream inputStream of .docx file
     * @param pdfFileName name and suffix of pdf file
     * @return true if conversion was successful
     */
    public static synchronized boolean convertDocxToPdf(InputStream docxInputStream, String pdfFileName) {
            
        log.info("Starting to convert .docx to .pdf...");

        pdfFileName = prependSlash(pdfFileName);
        
        try (OutputStream os = new FileOutputStream(RESOURCE_FOLDER + pdfFileName)) {
            IConverter converter = LocalConverter.builder().build();
            
            converter.convert(docxInputStream)
                .as(DocumentType.DOCX)
                .to(os)
                .as(DocumentType.PDF)
                .execute();

            converter.shutDown();
            log.info("Finished converting .docx to .pdf.");

            return true;
                
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return false;
    }


    /**
     * Overloading {@link #convertDocxToPdf(InputStream, String)}.
     * 
     * @param docxFile
     * @param pdfFileName
     * @return
     */
    public static boolean convertDocxToPdf(File docxFile, String pdfFileName) {

        try {
            return convertDocxToPdf(new FileInputStream(docxFile), pdfFileName);

        } catch (IOException e) {
            log.error(e.getMessage());

            return false;
        }
    }


    /**
     * Prepends a '/' to given String if there isn't already one.
     * 
     * @param str String to prepend the slash to
     * @return the altered (or not altered) string
     */
    protected static String prependSlash(String str) {

        return str.charAt(0) == '/' ? str : "/" + str;
    }
}