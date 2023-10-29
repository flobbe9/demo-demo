package com.example.vorspiel_backend.documentParts;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
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
    private List<@Valid BasicParagraph> content;

    @Valid
    private TableConfig tableConfig;

    private boolean landscape = false;

    @Min(1) @Max(3)
    @Schema(defaultValue = "1")
    private int numColumns = 1;
}