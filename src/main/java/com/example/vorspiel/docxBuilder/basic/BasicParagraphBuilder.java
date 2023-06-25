package com.example.vorspiel.docxBuilder.basic;

import java.util.List;

import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.example.vorspiel.docxContent.basic.BasicParagraph;
import com.example.vorspiel.docxContent.basic.style.Style;

import lombok.extern.log4j.Log4j2;


@Log4j2
public class BasicParagraphBuilder {

    public static final String RESOURCE_FOLDER = "./resources";
    private static final XWPFDocument document = new XWPFDocument();

    // TODO: add null checks and empty checks
    private List<BasicParagraph> content;
    
    
    public void build() {

        // add header

        // add title

        // add main paragraphs

        // add footer
    }
    
    // add header
    protected void addHeader() {
        
        // get header object
        BasicParagraph paragraph = content.get(0);
        Style style = paragraph.getStyle();

        XWPFHeader header = document.createHeader(HeaderFooterType.DEFAULT);

        // add text
        XWPFParagraph p = header.createParagraph();
        XWPFRun run = p.createRun();
        run.setText("Header");
        
        // add style
        addStyle(p, paragraph, style);
    }

    // add title
    
    // add main paragraphs
    
    private void addStyle(XWPFParagraph p, BasicParagraph paragraph, Style style) {

        p.getRuns().forEach(run -> {
            run.setFontSize(style.getFontSize());
            run.setFontFamily(style.getFontFamily());
            run.setColor(style.getColor().getRGB());
            run.setBold(style.getBold());
            run.setItalic(style.getItalic());
            if (style.getUnderline()) run.setUnderline(UnderlinePatterns.DASH);
        });

        // TODO: continue here!!!
    }

    // add footer
    private static final XWPFFooter footer = document.createFooter(HeaderFooterType.DEFAULT);

    // writeToDocx

    // docxToPdf
}