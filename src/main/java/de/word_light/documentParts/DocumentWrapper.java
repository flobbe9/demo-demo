package de.word_light.documentParts;

import java.util.List;
import java.util.Map;

import org.springframework.lang.Nullable;

import de.word_light.entites.AbstractEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Wrapper defining the request body that is expected from frontend.
 * 
 * @since 0.0.1
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class DocumentWrapper extends AbstractEntity {
    
    @NotNull(message = "'content' cannot be null.")
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "document_wrapper_basic_paragraphs",
        inverseJoinColumns = @JoinColumn(name = "basic_paragraph_id"))
    private List<@Valid @NotNull(message = "'basicParagraph' cannot be null") BasicParagraph> content;

    @Valid
    @NotNull(message = "'tableConfigs' cannot be null.")
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "document_wrapper_table_configs",
        inverseJoinColumns = @JoinColumn(name = "table_config_id"))
    private List<@Valid @NotNull(message = "'tableConfig cannot be null") TableConfig> tableConfigs;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "picture_file_name", unique = true)
    @Column(name = "picture_bytes", length = 16777215)
    @Nullable
    @Schema(hidden = true)
    private Map<String, byte[]> pictures;

    @NotEmpty(message = "'fileName' cannot be empty.")
    private String fileName;

    private boolean landscape = false;

    /** Refers to 'Columns' in MS Word */
    @Min(1) @Max(3)
    @Schema(defaultValue = "1")
    private int numColumns = 1;

    /** Number of lines on top of the first page in one single column ignoring 'numColumns' */
    @Min(value = 0, message = "'numSingleColumnLines' too small. Min: 0") 
    @Schema(defaultValue = "0")
    private int numSingleColumnLines;


    public DocumentWrapper(
            @NotNull(message = "'content' cannot be null.") List<@Valid @NotNull(message = "'basicParagraph' cannot be null") BasicParagraph> content,
            @Valid @NotNull(message = "'tableConfigs' cannot be null.") List<@Valid @NotNull(message = "'tableConfig cannot be null") TableConfig> tableConfigs,
            boolean landscape,
            @NotEmpty(message = "'fileName' cannot be empty.") String fileName, 
            @Min(1) @Max(3) int numColumns,
            @Min(value = 0, message = "'numSingleColumnLines' too small. Min: 0") @Max(value = 5, message = "'numSingleColumnLines' too large. Max: 5") int numSingleColumnLines) {
        
        this.content = content;
        this.tableConfigs = tableConfigs;
        this.fileName = fileName;
        this.landscape = landscape;
        this.numColumns = numColumns;
        this.numSingleColumnLines = numSingleColumnLines;
    }


    @AssertTrue(message = "'tableConfigs' invalid. Start and end indices cannot overlap.")
    @Schema(hidden = true)
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