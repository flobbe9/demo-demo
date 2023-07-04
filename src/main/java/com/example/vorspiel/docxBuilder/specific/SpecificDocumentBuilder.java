package com.example.vorspiel.docxBuilder.specific;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;

import com.example.vorspiel.docxBuilder.basic.BasicDocumentBuilder;
import com.example.vorspiel.docxContent.basic.BasicParagraph;
import com.example.vorspiel.docxContent.specific.TableData;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;


@Log4j2
@Getter
public class SpecificDocumentBuilder extends BasicDocumentBuilder {

    /** Picture dimensions in centimeters. */
    public static final Integer PICTURE_WIDTH_PORTRAIT = 15;
    public static final Integer PICTURE_WIDTH_LANDSCAPE_HALF = 11;
    public static final Integer PICTURE_HEIGHT_LANDSCAPE_HALF = 7;

    private PictureUtils pictureUtils;

    private TableUtils tableUtils;

    
    public SpecificDocumentBuilder(List<BasicParagraph> content, String docxFileName, TableData tableData, File... pictures) {

        super(content, docxFileName);
        super.setDocument(readDocxFile("2Columns.docx"));

        this.pictureUtils = new PictureUtils(Arrays.asList(pictures));
        this.tableUtils = new TableUtils(getDocument(), tableData);
    }
    
    
    @Override
    public boolean build() {
        
        log.info("Starting to build document...");
        
        setOrientation(STPageOrientation.LANDSCAPE);

        addContent();

        return writeDocxFile();
    }


    /**
     * Adds {@link BasicParagraph} from content list at given index to the document. This includes text and style.
     * 
     * @param contentIndex index of the {@link #content} element currently processed
     */
    @Override
    protected void addParagraph(int contentIndex) {

        // get content
        BasicParagraph basicParagraph = getContent().get(contentIndex);
    
        XWPFParagraph paragraph = createParagraphByContentIndex(contentIndex);
        
        if (basicParagraph != null) {
            // add text
            addText(paragraph, basicParagraph, contentIndex);

            // add style
            addStyle(paragraph, basicParagraph.getStyle());
        }
    }


    /**
     * Adds the "text" class variable of {@link BasicParagraph} to given {@link XWPFRun}. <p>
     * "text" will be added as plain string, as picture or inside a table.
     * 
     * @param run to add the text or picture to
     * @param text of the basicParagraph
     */
    private void addText(XWPFParagraph paragraph, BasicParagraph basicParagraph, int contentIndex) {

        String text = basicParagraph.getText();
        PictureType pictureType = this.pictureUtils.getPictureType(text);
        
        // case: insert into table
        if (this.tableUtils.isTableIndex(contentIndex)) {
            this.tableUtils.addTableCell(contentIndex, text, basicParagraph.getStyle());

        // case: text is a picture file name
        } else if (pictureType != null) {
            this.pictureUtils.addPicture(paragraph.createRun(), text, pictureType);
            
        // case: plain text
        } else
            paragraph.createRun().setText(text);
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

        // table wont need paragraph
        if (this.tableUtils.isTableIndex(contentIndex))
            return null;
        
        // header
        if (contentIndex == 0)
            return super.getDocument().createHeader(HeaderFooterType.DEFAULT).createParagraph();

        // footer
        if (contentIndex == getContent().size() - 1)
            return getDocument().createFooter(HeaderFooterType.DEFAULT).createParagraph();

        // any other
        return getDocument().createParagraph();
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

        CTBody ctBody = getDocument().getDocument().getBody();

        return ctBody.getSectPr() == null ? ctBody.addNewSectPr() : ctBody.getSectPr();
    }


    /**
     * Reads given .docx file to an {@link XWPFDocument}. File is expected to be located in {@link #RESOURCE_FOLDER}. <p>
     * Returns an emtpy document if exception is caught.
     * 
     * @param fileName name and suffix of the .docx file
     * @return XWPFDocument of the file or an empty one in case of exception
     */
    private XWPFDocument readDocxFile(String fileName) {

        log.info("Starting to read .docx file...");

        try {
            fileName = prependSlash(fileName);

            log.info("Finished reading .docx file.");

            return new XWPFDocument(new FileInputStream(RESOURCE_FOLDER + fileName));
        
        } catch (Exception e) {
            log.warn("Failed to read docx file. Returning an empty document instead. Cause: " + e.getMessage());
            e.printStackTrace();

            return new XWPFDocument();
        }
    }
}