package com.example.vorspiel.controllers;

import static com.example.vorspiel.documentBuilder.DocumentBuilder.RESOURCE_FOLDER;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.vorspiel.documentBuilder.DocumentBuilder;
import com.example.vorspiel.documentParts.BasicParagraph;
import com.example.vorspiel.documentParts.DocumentWrapper;
import com.example.vorspiel.utils.ApiException;
import com.example.vorspiel.utils.RequestExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;


@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/convertDocxToPdf")
    @ResponseStatus(value = HttpStatus.OK, reason = "Converted .docx to .pdf.")
    public synchronized void convertDocxToPdf() throws FileNotFoundException {

        DocumentBuilder.convertDocxToPdf(new File(RESOURCE_FOLDER + "./specificTest.docx"), null);
    }


    /**
     * Assuming that: <p>
     * first element is the header <p>
     * the second is the title <p>
     * last element is the footer <p>
     * anything in between is main content <p>.
     * @throws ApiException
     * 
     */
    @PostMapping("/createDocument")
    public synchronized Object createDocument(@RequestBody @Validated DocumentWrapper wrapper, BindingResult bindingResult) {

        // case: http 400
        if (bindingResult.hasErrors()) 
            return RequestExceptionHandler.returnPretty(HttpStatus.BAD_REQUEST, bindingResult);

        // case: http 422
        if (!wrapper.isValid())
            return RequestExceptionHandler.returnPretty(HttpStatus.UNPROCESSABLE_ENTITY);

        // build and write document
        Boolean buildSuccessful = new DocumentBuilder(wrapper.getContent(), 
                                                    "specificTest.docx", 
                                                    wrapper.getTableConfig(), 
                                                    new File(RESOURCE_FOLDER + "/logo.png")).build();

        // case: http 500
        if (!buildSuccessful)
            return RequestExceptionHandler.returnPretty(HttpStatus.INTERNAL_SERVER_ERROR);

        // case: http 200
        return RequestExceptionHandler.returnPrettySuccess(HttpStatus.OK);
    }
    

    @PostMapping
    public String test(@RequestBody @Validated List<BasicParagraph> content) {

        return "test";
    }
}