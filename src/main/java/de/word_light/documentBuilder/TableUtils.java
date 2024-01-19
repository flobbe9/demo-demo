package de.word_light.documentBuilder;

import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TableRowAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

import de.word_light.documentParts.BasicParagraph;
import de.word_light.documentParts.TableConfig;
import de.word_light.documentParts.style.Style;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;


/**
 * Util class for adding tables to an {@link XWPFDocument}.
 * 
 * @since 0.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@Log4j2
public class TableUtils {

    public static final Integer TABLE_CELL_MARGIN = 80;

    private XWPFDocument document;

    private List<TableConfig> tableConfigs;


    /**
     * Uses {@link TableConfig} to get or create a table and returns a new paragraph or an existing one in the current table cell.
     * 
     * @param currentContentIndex index of the {@link #content} element currently processed
     * @param style to apply to table and text
     * @return paragraph in table cell holding text and style information
     */
    XWPFParagraph createTableParagraph(int currentContentIndex, Style style) {

        // get current table config
        TableConfig currentTableConfig = getCurrentTableConfig(currentContentIndex);
        
        // create table or use existing one
        XWPFTable currentTable = getCurrentTable(currentContentIndex, style);

        // case: not inside a table
        if (currentTable == null) {
            log.warn("Failed to create table paragraph. 'currentContentIndex':" + currentContentIndex + " is not inside a table.");
            return null;
        }

        // get current row and colum
        int startIndex = currentTableConfig.getStartIndex();
        int currentRow = (currentContentIndex - startIndex) / currentTableConfig.getNumColumns();
        int currentCol = (currentContentIndex - startIndex) % currentTableConfig.getNumColumns();
        
        // create cell in current position
        XWPFTableCell tableCell = currentTable.getRow(currentRow).getCell(currentCol);

        // add paragraph or use existing one
        return tableCell.getParagraphs().isEmpty() ? tableCell.addParagraph() : 
                                                     tableCell.getParagraphs().get(0);
    }
    

    /**
     * Adds content to a single table cell. Adds one {@link BasicParagraph} per cell.
     * 
     * @param currentContentIndex index of the {@link #content} element currently processed
     * @param text to add to table cell
     * @param style to apply to text
     */
    XWPFParagraph fillTableCell(XWPFParagraph paragraph, int currentContentIndex, String text, Style style) {

        // add text
        paragraph.createRun().setText(text);

        // add style
        DocumentBuilder.addStyle(paragraph, style);

        return paragraph;
    }


    /**
     * Get {@link XWPFTable} in document related to given {@code currentContentIndex} or create a new one if not
     * exists yet.
     * 
     * @param currentContentIndex index of the {@link #content} element currently processed
     * @param style to apply to table and text
     * @return the current {@link XWPFTable} from {@link #document} or null if {@code currentContentIndex} is not inside a table
     */
    private XWPFTable getCurrentTable(int currentContentIndex, Style style) {

        TableConfig currentTableConfig = getCurrentTableConfig(currentContentIndex);
        int currentTableIndex = currentTableConfig == null ? -1 : this.tableConfigs.indexOf(currentTableConfig);

        // case: currently not inside a table
        if (currentTableIndex == -1)
            return null;
        
        // case: table does already exist for this config
        try {
            return this.document.getTables().get(currentTableIndex);

        // case: no table for this config yet
        } catch (IndexOutOfBoundsException e) {
            return createNewTable(currentTableConfig, style, DocumentBuilder.PAGE_LONG_SIDE_WITH_BORDER / 2);
        }
    }


    /**
     * Get the {@link TableConfig} from {@link #tableConfigs} list where {@code currentContentIndex} is in between
     * {@code startIndex} and {@code endIndex}.
     * 
     * @param currentContentIndex index of the {@link #content} element currently processed
     * @return the {@link TableConfig} matching the {@code currentContentIndex} or null if no match is found
     */
    private TableConfig getCurrentTableConfig(int currentContentIndex) {

        for (TableConfig tableConfig : this.tableConfigs)
            if (isTableIndex(tableConfig, currentContentIndex))
                return tableConfig;

        return null;
    }


    /**
     * Adds new table to the document and style it.
     * 
     * @param style to apply
     * @param tableWidth width of a single row
     * @return created table
     */
    private XWPFTable createNewTable(TableConfig tableConfig, Style style, int tableWidth) {

        // create table
        XWPFTable table = this.document.createTable(tableConfig.getNumRows(), tableConfig.getNumColumns());

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
    private void addTableStyle(Style style, int tableWidth, XWPFTable table) {

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
     * Overloading {@link #isTableIndex(TableConfig, int)} using {@link #getCurrentTable(int, Style)} as
     * {@link TableConfig}.
     * 
     * @param currentContentIndex index of the {@link #content} element currently processed
     * @return true if tableConfig not null and index at a table cell
     */
    boolean isTableIndex(int currentContentIndex) {

        TableConfig tableConfig = getCurrentTableConfig(currentContentIndex);

        return tableConfig != null && isTableIndex(tableConfig, currentContentIndex);
    }


    /**
     * Checks if given content index is currently inside a table cell in given {@link TableConfig}.
     * 
     * @param tableConfig to check
     * @param currentContentIndex index of the {@link #content} element currently processed
     * @return true if tableConfig not null and index at a table cell
     */
    private boolean isTableIndex(TableConfig tableConfig, int currentContentIndex) {

        boolean hasTableStarted = currentContentIndex >= tableConfig.getStartIndex();

        boolean hasTableNotEnded = currentContentIndex <= tableConfig.getEndIndex();

        return hasTableStarted && hasTableNotEnded;
    }
}