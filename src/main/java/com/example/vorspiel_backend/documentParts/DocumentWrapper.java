package com.example.vorspiel_backend.documentParts;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * Wrapper defining the request body that is expected from frontend.
 * 
 * @since 0.0.1
 */
@Getter
@NoArgsConstructor
public class DocumentWrapper {
    
    @NotEmpty(message = "'content' cannot be null or empty.")
    private List<@Valid BasicParagraph> content;

    @Valid
    private TableConfig tableConfig;
}