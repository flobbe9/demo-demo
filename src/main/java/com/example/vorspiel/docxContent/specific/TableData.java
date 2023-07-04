package com.example.vorspiel.docxContent.specific;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Class holding necessary table information.
 * 
 * @since 0.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TableData {
     
    @NotNull(message = "numColumns cannot be null.")
    @Min(value = 1, message = "numColumns has to be greater than equal 1.")
    private Integer numColumns;

    @NotNull(message = "numRows cannot be null.")
    @Min(value = 1, message = "numRows has to be greater than equal 1.")
    private Integer numRows;

    /** The index in content list with the first table element. */
    @NotNull(message = "startIndex cannot be null.")
    @Min(value = 0, message = "startIndex has to be greater than equal 0.")
    private Integer startIndex;

    /** The index in content list with the last table element. */
    @NotNull(message = "endIndex cannot be null.")
    @Min(value = 0, message = "endIndex has to be greater than equal 0.")
    private Integer endIndex;
}