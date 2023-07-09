package com.example.vorspiel.docxBuilder.specific;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TableRowAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

import com.example.vorspiel.docxContent.basic.BasicParagraph;
import com.example.vorspiel.docxContent.basic.style.BasicStyle;
import com.example.vorspiel.docxContent.specific.TableConfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * Util class for adding tables to an {@link XWPFDocument}.
 * 
 * @since 0.0.1
 */
@Getter
@Setter
@AllArgsConstructor
public class TableUtils {

    public static final Integer TABLE_CELL_MARGIN = 80;

    private XWPFDocument document;

    private TableConfig tableConfig;


    /**
     * Adds content to a single table cell and adds a new table to the document or uses an existing one. <p>
     * 
     * Currently only one table per document can be used. <p>
     * 
     * Adds one {@link BasicParagraph} per cell.
     * 
     * @param currentContentIndex index of the {@link #content} element currently processed
     * @param text
     */
    XWPFParagraph addTableCell(int currentContentIndex, String text, BasicStyle style) {

        // get current row and colum
        int startIndex = this.tableConfig.getStartIndex();
        int currentRow = (currentContentIndex - startIndex) / this.tableConfig.getNumColumns();
        int currentCol = (currentContentIndex - startIndex) % this.tableConfig.getNumColumns();
        
        // create table or use existing one
        XWPFTable table = this.document.getTables().isEmpty() ? createNewTable(style, SpecificDocumentBuilder.PAGE_LONG_SIDE_WITH_BORDER / 2) : 
                                                                this.document.getTables().get(0);

        // add paragraph or use existing one
        XWPFTableCell tableCell = table.getRow(currentRow).getCell(currentCol);
        XWPFParagraph paragraph = tableCell.getParagraphs().isEmpty() ? tableCell.addParagraph() : 
                                                                        tableCell.getParagraphs().get(0);

        // add text
        paragraph.createRun().setText(text);

        return paragraph;
    }


    /**
     * Adds new table to the document and style it.
     * 
     * @param style to apply
     * @param tableWidth width of a single row
     * @return created table
     */
    private XWPFTable createNewTable(BasicStyle style, int tableWidth) {

        // create table
        XWPFTable table = this.document.createTable(this.tableConfig.getNumRows(), this.tableConfig.getNumColumns());

        // add style
        addTableStyle(style, tableWidth, table);

        return table;
    }


    /**
     * Add table specific styles to given table.
     * 
     * @param style to apply
     * @param table to style
     */
    private void addTableStyle(BasicStyle style, int tableWidth, XWPFTable table) {

        if (style == null || table == null)
            return;

        TableRowAlign tableRowAlign;
            
        // text align, use CENTER as default
        if (style.getTextAlign() == ParagraphAlignment.LEFT) {
            tableRowAlign = TableRowAlign.LEFT;

        } else if (style.getTextAlign() == ParagraphAlignment.RIGHT) {
            tableRowAlign = TableRowAlign.RIGHT;

        } else
            tableRowAlign = TableRowAlign.CENTER;

        table.setTableAlignment(tableRowAlign);

        // set table cell margins
        table.setCellMargins(TABLE_CELL_MARGIN, TABLE_CELL_MARGIN, TABLE_CELL_MARGIN, TABLE_CELL_MARGIN);    
        
        // set table width
        table.setWidth(tableWidth);
    }    


    /**
     * Checks if given content index is currently inside a table cell.
     * 
     * @param currentContentIndex index of the {@link #content} element currently processed
     * @return true if tableConfig not null and index at a table cell
     */
    boolean isTableIndex(int currentContentIndex) {

        if (this.tableConfig == null) 
            return false;

        boolean hasTableStarted = currentContentIndex >= this.tableConfig.getStartIndex();

        boolean hasTableNotEnded = currentContentIndex <= this.tableConfig.getEndIndex();

        return hasTableStarted && hasTableNotEnded;
    }
}