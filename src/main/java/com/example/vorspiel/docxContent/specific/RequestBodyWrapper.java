package com.example.vorspiel.docxContent.specific;

import java.util.List;

import com.example.vorspiel.docxContent.basic.BasicParagraph;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;


@Getter
@ValidWrapper(groups = {ValidWrapper.class})
public class RequestBodyWrapper {
    
    @NotEmpty(message = "'basicParagraphs' cannot be null or empty.")
    private List<BasicParagraph> basicParagraphs;

    @Valid
    private TableData tableData;
}