package com.example.vorspiel_backend.documentParts.style;

import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.springframework.lang.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Holds all styling information for a document.
 * 
 * @since 0.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Style {
    
    /**  */
    @NotNull(message = "'fontSize' cannot be null.")
    @Min(value = 8, message = "'fontSize' has to be greater than equal 8.")
    private Integer fontSize;

    @NotEmpty(message = "'fontFamily' cannot be empty or null.")
    private String fontFamily;

    @NotNull(message = "'color' cannot be null.")
    @Pattern(regexp = "^([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$", message = "'color' hex string invalid.")
    @Schema(example = "000000")
    private String color;

    @NotNull(message = "'bold' cannot be null.")
    private Boolean bold;

    @NotNull(message = "'italic' cannot be null.")
    private Boolean italic;

    @NotNull(message = "'underline' cannot be null.")
    private Boolean underline;

    @NotNull(message = "'textAlign' cannot be null.")
    private ParagraphAlignment textAlign;

    @Nullable
    private BreakType breakType;
}