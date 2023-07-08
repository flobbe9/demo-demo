package com.example.vorspiel.docxBuilder.specific;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.Arrays;
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

import com.example.vorspiel.docxBuilder.basic.BasicDocumentBuilder;
import com.example.vorspiel.docxContent.basic.BasicParagraph;
import com.example.vorspiel.docxContent.basic.style.BasicStyle;
import com.example.vorspiel.docxContent.specific.TableData;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;


@Log4j2
@Getter
public class SpecificDocumentBuilder extends BasicDocumentBuilder {

    /** Picture dimensions in centimeters. */
    public static final int PICTURE_WIDTH_PORTRAIT = 15;
    public static final int PICTURE_WIDTH_LANDSCAPE_HALF = 11;
    public static final int PICTURE_HEIGHT_LANDSCAPE_HALF = 7;

    /** Document margins */
    public static final int MINIMUM_MARGIN_TOP_BOTTOM = 240;

    public static final int NO_LINE_SPACE = 1;

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
        
        setDocumentMargins(MINIMUM_MARGIN_TOP_BOTTOM, null, MINIMUM_MARGIN_TOP_BOTTOM, null);
        
        return writeDocxFile();
    }


    /**
     * Adds {@link BasicParagraph} from content list at given index to the document. This includes text and style.
     * 
     * @param currentContentIndex index of the {@link #content} element currently processed
     */
    @Override
    protected void addParagraph(int currentContentIndex) {

        // get content
        BasicParagraph basicParagraph = getContent().get(currentContentIndex);
    
        XWPFParagraph paragraph = createParagraphByContentIndex(currentContentIndex);

        if (basicParagraph != null) {
            // add text
            addText(paragraph, basicParagraph, currentContentIndex);

            // add style
            addStyle(paragraph, basicParagraph.getStyle());
        }
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
    @Override
    protected XWPFParagraph createParagraphByContentIndex(int currentContentIndex) {

        // case: table does not need paragrahp from here
        if (tableUtils.isTableIndex(currentContentIndex))
            return null;

        BasicParagraph basicParagraph = getContent().get(currentContentIndex);

        // case: header
        if (currentContentIndex == 0) {
            if (basicParagraph != null)
                return getDocument().createHeader(HeaderFooterType.DEFAULT).createParagraph();

            return null;
        }

        // case: footer
        if (currentContentIndex == this.getContent().size() - 1) {
            if (basicParagraph != null)
                return getDocument().createFooter(HeaderFooterType.DEFAULT).createParagraph();

            return null;
        }

        // case: any other
        return getDocument().createParagraph();
    }


    /**
     * Adds the "text" class variable of {@link BasicParagraph} to given {@link XWPFRun}. <p>
     * 
     * "text" will be added as plain string, as picture or inside a table.
     * 
     * @param run to add the text or picture to
     * @param text of the basicParagraph
     */
    private void addText(XWPFParagraph paragraph, BasicParagraph basicParagraph, int currentContentIndex) {

        String text = basicParagraph.getText();
        PictureType pictureType = this.pictureUtils.getPictureType(text);
        
        // case: insert into table
        if (this.tableUtils.isTableIndex(currentContentIndex)) {
            XWPFParagraph cellParagraph = this.tableUtils.addTableCell(currentContentIndex, text, basicParagraph.getStyle());
            addStyle(cellParagraph, basicParagraph.getStyle());

        // case: text is a picture file name
        } else if (pictureType != null) {
            this.pictureUtils.addPicture(paragraph.createRun(), text, pictureType);
            
        // case: plain text
        } else
            paragraph.createRun().setText(text);
    }


    /**
     * Add style to given {@link XWPFParagraph}. Is skipped if either paragraph or style are null.
     * 
     * @param paragraph to apply the style to
     * @param style information to use
     * @see BasicStyle
     */
    public void addStyle(XWPFParagraph paragraph, BasicStyle style) {

        if (paragraph == null || style == null)
            return;

        paragraph.getRuns().forEach(run -> {
            run.setFontSize(style.getFontSize());

            run.setFontFamily(style.getFontFamily());

            run.setColor(style.getColor().getRGB());

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

        CTSectPr sectPr = getDocument().getDocument().getBody().addNewSectPr();
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

        CTBody ctBody = getDocument().getDocument().getBody();

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
    private XWPFDocument readDocxFile(String fileName) {

        log.info("Starting to read .docx file...");

        try {
            fileName = prependSlash(fileName);

            XWPFDocument document = new XWPFDocument(new FileInputStream(RESOURCE_FOLDER + fileName));

            // clean up document
            document.removeBodyElement(0);

            return document;
        
        } catch (Exception e) {
            log.warn("Failed to read docx file. Returning an empty document instead. Cause: " + e.getMessage());
            e.printStackTrace();

            return new XWPFDocument();
        }
    }
}