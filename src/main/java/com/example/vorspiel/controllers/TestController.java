package com.example.vorspiel.controllers;

import static com.example.vorspiel.docxBuilder.Test.RESOURCE_FOLDER;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.vorspiel.docxBuilder.Test;
import com.example.vorspiel.docxBuilder.specific.SpecificlHeaderBuilder;
import com.example.vorspiel.docxContent.basic.BasicParagraph;
import com.example.vorspiel.docxContent.basic.style.Style;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/test")
public class TestController {
    
    @GetMapping("/writeTestDocx")
    @ResponseStatus(value = HttpStatus.OK, reason = "Created .docx file.")
    public void writeTestDocx() {

        Test.createTestDocument();
    }


    @GetMapping("/convertDocxToPdf")
    @ResponseStatus(value = HttpStatus.OK, reason = "Converted .docx to .pdf.")
    public synchronized void convertDocxToPdf() throws FileNotFoundException {

        Test.convertDocxToPdf(new File(RESOURCE_FOLDER + "./testDoc.docx"), "testPdf.pdf");
    }


    /**
     * Assuming that: <p>
     * first element is the header <p>
     * the second is the title <p>
     * last element is the footer <p>
     * anything in between is main content <p>
     * 
     * @param content
     * @return
     */
    @PostMapping
    public String test(@RequestBody @Validated List<BasicParagraph> content) {


        return "test";
    }
}