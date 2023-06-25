package com.example.vorspiel.docxContent.basic.style;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * TODO: not sure if this will be used
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Placing {
    
    @NotNull(message = "indentFirstLine cannot be null.")
    private Boolean indentFirstLine;

    @NotNull(message = "indentParagraph cannot be null.")
    private Boolean indentParagraph;

    @NotNull(message = "textAlign cannot be null.")
    private ParagraphAlignment textAlign;
}