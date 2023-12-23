package com.example.vorspiel_backend.documentParts;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Wrapper defining the request body that is expected from frontend.
 * 
 * @since 0.0.1
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentWrapper {
    
    @NotEmpty(message = "'content' cannot be null or empty.")
    private List<@Valid @NotNull(message = "'basicParagraph' cannot be null") BasicParagraph> content;

    @Valid
    @NotNull(message = "'tableConfigs' cannot be null.")
    private List<@Valid @NotNull(message = "'tableConfig cannot be null") TableConfig> tableConfigs;

    private boolean landscape = false;

    /** Refers to 'Columns' in MS Word */
    @Min(1) @Max(3)
    @Schema(defaultValue = "1")
    private int numColumns = 1;


    @AssertTrue(message = "'tableConfigs' invalid. Start and end indices cannot overlap.")
    public boolean isTableConfigsValid() {

        // sort by startIndex
        List<TableConfig> tableConfigs = sortTableConfigsByStartIndex(this.tableConfigs);
        
        for (int i = 0; i < tableConfigs.size(); i++) {
            // case: last tableConfig
            if (i == tableConfigs.size() - 1)
                break;

            TableConfig tableConfig = tableConfigs.get(i);
            TableConfig nextTableConfig = tableConfigs.get(i + 1);

            // case: tableConfigs are overlapping
            if (tableConfig.getEndIndex() >= nextTableConfig.getStartIndex())
                return false;
        }

        return true;
    }


    /**
     * @param tableConfigs to sort
     * @return given list by {@code startIndex} ascending
     */
    private List<TableConfig> sortTableConfigsByStartIndex(List<TableConfig> tableConfigs) {

        tableConfigs.sort((TableConfig t1, TableConfig t2) -> {
            return Integer.compare(t1.getStartIndex(), t2.getStartIndex());
        });

        return tableConfigs;
    }
}