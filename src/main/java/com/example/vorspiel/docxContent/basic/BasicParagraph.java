package com.example.vorspiel.docxContent.basic;

import com.example.vorspiel.docxContent.basic.style.Style;

import jakarta.validation.constraints.NotEmpty;
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
    
    @NotEmpty(message = "content String cannot be empty or null.")
    private String text;
    
    @NotNull(message = "style cannot be null.")
    private Style style;
}