package com.example.vorspiel.docxBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import lombok.extern.log4j.Log4j2;
import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;


@Log4j2
public abstract class Test {
    
    private static final XWPFDocument document = new XWPFDocument();
    private static final XWPFHeader header = document.createHeader(HeaderFooterType.DEFAULT);
    private static final XWPFFooter footer = document.createFooter(HeaderFooterType.DEFAULT);

    public static final String RESOURCE_FOLDER = "./resources";


    public static void createTestDocument() {

        // header
        XWPFParagraph headeParagraph = header.createParagraph();
        XWPFRun headerRun = headeParagraph.createRun();
        headerRun.setText("Header");
        
        // content
        /// title
        XWPFParagraph paragraph = document.createParagraph();   
        XWPFRun titleRun = paragraph.createRun();
        titleRun.setText("Title");
        titleRun.setFontSize(30);

        /// p1
        XWPFParagraph paragraph1 = document.createParagraph();
        XWPFRun run1 = paragraph1.createRun();
        run1.setText("paragraph1");

        /// p2
        XWPFParagraph paragraph2 = document.createParagraph();
        XWPFRun run2 = paragraph2.createRun();
        run2.setText("paragraph2");        

        // footer
        XWPFParagraph footeParagraph = footer.createParagraph();
        XWPFRun footerRun = footeParagraph.createRun();
        footerRun.setText("Footer");

        writeDocxFile("testDoc.docx");
    }


    /**
     * Writes an {@link XWPFDocument} to a .docx file. Stores it to {@link #RESOURCE_FOLDER}.
     * 
     * @param docxFileName name and suffix of the file to be created
     * @return true if conversion was successful
     */
    private static boolean writeDocxFile(String docxFileName) {

        log.info("Start writing .docx file...");

        try (OutputStream os = new FileOutputStream(RESOURCE_FOLDER + prependSlash(docxFileName))) {

            document.write(os);
            document.close();
            
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
            
        // start async conversion
        log.info("Start converting .docx to .pdf...");
        
        try (OutputStream os = new FileOutputStream(RESOURCE_FOLDER + prependSlash(pdfFileName))) {
            IConverter converter = LocalConverter.builder().build();
            
            converter.convert(docxInputStream)
                .as(DocumentType.DOCX)
                .to(os)
                .as(DocumentType.PDF)
                .execute();

            // finished
            converter.shutDown();
            log.info("Finished converting .docx to .pdf.");
                
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
    private static String prependSlash(String str) {

        return str.charAt(0) == '/' ? str : "/" + str;
    }
}