package com.example.vorspiel.documentParts;

import com.example.vorspiel.documentParts.style.Style;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Represents a simple paragraph in a document with some style information. <p>
 * 
 * Should be extended by any class that holds any kind of text content.
 * 
 * @since 0.0.1
 * @see Style
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BasicParagraph {
    
    @NotNull(message = "'text' cannot be null.")
    private String text;
    
    @NotNull(message = "'style' cannot be null.")
    private Style style;
}