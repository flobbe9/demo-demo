package com.example.vorspiel.controllers;

import static com.example.vorspiel.documentBuilder.DocumentBuilder.RESOURCE_FOLDER;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.example.vorspiel.documentBuilder.DocumentBuilder;
import com.example.vorspiel.documentParts.BasicParagraph;
import com.example.vorspiel.documentParts.DocumentWrapper;
import com.example.vorspiel.exception.ApiExceptionFormat;
import com.example.vorspiel.exception.ApiExceptionHandler;


@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/convertDocxToPdf")
    @ResponseStatus(value = HttpStatus.OK, reason = "Converted .docx to .pdf.")
    public synchronized void convertDocxToPdf() throws FileNotFoundException {

        DocumentBuilder.convertDocxToPdf(new File(RESOURCE_FOLDER + "/test/test.docx"), "vorspiel.pdf");
    }


    /**
     * Assuming that: <p>
     * first element is the header <p>
     * the second is the title <p>
     * last element is the footer <p>
     * anything in between is main content <p>.
     * @throws ApiExceptionFormat
     * 
     */
    @PostMapping("/createDocument")
    public ApiExceptionFormat createDocument(@RequestBody @Validated DocumentWrapper wrapper, BindingResult bindingResult) {

        // case: http 400
        if (bindingResult.hasErrors()) 
            return ApiExceptionHandler.returnPretty(HttpStatus.BAD_REQUEST, bindingResult);

        // build and write document
        if (wrapper.getTableConfig() != null) {
            new DocumentBuilder(wrapper.getContent(), 
                                "vorspiel.docx", 
                                wrapper.getTableConfig(),
                                new File(RESOURCE_FOLDER + "/logo.png")).build();
        
        } else
            new DocumentBuilder(wrapper.getContent(), "vorspiel.docx").build();

        // case: http 200
        return ApiExceptionHandler.returnPrettySuccess(HttpStatus.OK);
    }


    @GetMapping("/clearResourceFolder")
    public boolean clearResourceFolder() {
        
        return DocumentBuilder.clearResourceFolder();
    }


    @PostMapping
    public String test(@RequestBody @Validated List<BasicParagraph> content) {

        return "test";
    }
}