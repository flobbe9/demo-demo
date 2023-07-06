package com.example.vorspiel.docxContent.specific;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;


/**
 * Class holding necessary table information.
 * 
 * @since 0.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
public class TableData {
     
    @NotNull(message = "'numColumns' cannot be null.")
    @Min(value = 1, message = "'numColumns' has to be greater than equal 1.")
    private Integer numColumns;

    @NotNull(message = "'numRows' cannot be null.")
    @Min(value = 1, message = "'numRows' has to be greater than equal 1.")
    private Integer numRows;

    /** The index in content list with the first table element. */
    @NotNull(message = "'startIndex' cannot be null.")
    @Min(value = 0, message = "'startIndex' has to be greater than equal 0.")
    private Integer startIndex;

    /** The index in content list with the last table element. */
    @NotNull(message = "'endIndex' cannot be null.")
    @Min(value = 0, message = "'endIndex' has to be greater than equal 0.")
    private Integer endIndex;


    /**
     * Calls all neccessary validation methods on fields.
     * 
     * @return true if all fields are valid
     */
    boolean isValid() {

        return isTableBigEnough();
    }
    

    /**
     * Checks that product of table columns and rows is greater equal than the number of cells that will 
     * actually be filled.
     * 
     * @return true if table has enough cells
     */
    boolean isTableBigEnough() {

        int numTableCells = getNumColumns() * getNumRows();
        int numFilledCells = getEndIndex() - getStartIndex() + 1;

        // case: less cells than cell data
        if (numTableCells < numFilledCells) {
            log.error("Invalid 'tableData'. Not enough cells for content.");
            return false;
        }

        return true;
    }
}