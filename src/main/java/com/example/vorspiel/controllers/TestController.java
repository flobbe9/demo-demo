package com.example.vorspiel.controllers;

import static com.example.vorspiel.docxBuilder.basic.BasicDocumentBuilder.RESOURCE_FOLDER;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.vorspiel.docxBuilder.basic.BasicDocumentBuilder;
import com.example.vorspiel.docxBuilder.specific.SpecificDocumentBuilder;
import com.example.vorspiel.docxContent.basic.BasicParagraph;
import com.example.vorspiel.docxContent.specific.RequestBodyWrapper;
import com.example.vorspiel.docxContent.specific.TableData;
import com.example.vorspiel.docxContent.specific.ValidWrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;


@RestController
@RequestMapping("/test")
@Log4j2
public class TestController {
    
    @GetMapping("/convertDocxToPdf")
    @ResponseStatus(value = HttpStatus.OK, reason = "Converted .docx to .pdf.")
    public synchronized void convertDocxToPdf() throws FileNotFoundException {

        BasicDocumentBuilder.convertDocxToPdf(new File(RESOURCE_FOLDER + "./specificTest.docx"), null);
    }


    /**
     * Assuming that: <p>
     * first element is the header <p>
     * the second is the title <p>
     * last element is the footer <p>
     * anything in between is main content <p>.
     * 
     * @param basicParagraphs list of all paragraphs in the document
     */
    @PostMapping("/createDocument")
    @ResponseStatus(value = HttpStatus.OK, reason = "Created document.")
    public synchronized void createDocument(@RequestBody @ValidWrapper RequestBodyWrapper wrapper, BindingResult bindingResult) {

        // TODO: TableUtils validation

        // catch validation errors
        if (bindingResult.hasErrors()) {
            log.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
            return;
        } 

        new SpecificDocumentBuilder(wrapper.getBasicParagraphs(), "specificTest.docx", wrapper.getTableData(), new File(RESOURCE_FOLDER + "/logo.png")).build();
    }


    @PostMapping
    public String test(@RequestBody @Validated List<BasicParagraph> content) {

        return "test";
    }
}