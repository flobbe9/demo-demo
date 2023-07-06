package com.example.vorspiel.docxContent.specific;

import java.util.List;

import com.example.vorspiel.docxContent.basic.BasicParagraph;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class RequestBodyWrapper {
    
    @NotEmpty(message = "'content' cannot be null or empty.")
    private List<@Valid BasicParagraph> content;

    @Valid
    private TableData tableData;


    /**
     * Calls all neccessary validation methods on fields.
     * 
     * @return true if all fields are valid
     */
    public boolean isValid() {

        return this.tableData.isValid();
    }
}