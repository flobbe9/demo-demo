package com.example.vorspiel_backend.documentParts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Test class for {@link TableConfig}.
 * 
 * @since 0.0.1
 */
public class TableConfigTest {

    private TableConfig tableConfig;

    
    @BeforeEach
    void setup() {

        this.tableConfig = new TableConfig(3, 5, 1, 15);
    }


    // table bigger than expected -> true
    @Test
    void isValid_notEnoughRows_shouldBeFalse() {

        // decrease rows
        this.tableConfig.setNumRows(this.tableConfig.getNumRows() - 1);
        assertFalse(this.tableConfig.isValid());
    }


    @Test
    void isValid_notEnoughColumns_shouldBeFalse() {

        // decrease columns
        this.tableConfig.setNumColumns(this.tableConfig.getNumColumns() - 1);
        assertFalse(this.tableConfig.isValid());
    }


    @Test
    void isValid_wrongStartIndex_tooManyCells_shouldBeFalse() {

        // set lower start index -> more cells
        this.tableConfig.setStartIndex(this.tableConfig.getStartIndex() - 1);
        assertFalse(this.tableConfig.isValid());
    }


    @Test
    void isValid_wrongEndIndex_tooManyCells_shouldBeFalse() {

        // set higher end index -> more cells
        this.tableConfig.setEndIndex(this.tableConfig.getEndIndex() + 1);
        assertFalse(this.tableConfig.isValid());
    }


    @Test
    void isValid_tableBiggerThanContent_shouldBeTrue() {

        // make table bigger
        this.tableConfig.setNumColumns(this.tableConfig.getNumColumns() + 1);
        assertTrue(this.tableConfig.isValid());
    }

    @Test
    void isValid_useExactValues_shouldBeTrue() {

        assertTrue(this.tableConfig.isValid());
    }
}