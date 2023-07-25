package com.example.vorspiel.controllers;

import static com.example.vorspiel.documentBuilder.DocumentBuilder.RESOURCE_FOLDER;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.SessionScope;

import com.example.vorspiel.documentBuilder.DocumentBuilder;
import com.example.vorspiel.documentParts.BasicParagraph;
import com.example.vorspiel.documentParts.DocumentWrapper;
import com.example.vorspiel.exception.ApiExceptionFormat;
import com.example.vorspiel.exception.ApiExceptionHandler;


@RestController
@RequestMapping("/test")
public class TestController {

    private String fileName;


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
        DocumentBuilder documentBuilder;
        if (wrapper.getTableConfig() != null) {
             documentBuilder = new DocumentBuilder(wrapper.getContent(), 
                                                                    "vorspiel.docx", 
                                                                    wrapper.getTableConfig(),
                                                                    new File(RESOURCE_FOLDER + "/logo.png"));
                                                                    
        } else
            documentBuilder = new DocumentBuilder(wrapper.getContent(), "vorspiel.docx");
                                                                
        documentBuilder.build();

        this.fileName = documentBuilder.getDocxFileName();

        // case: http 200
        return ApiExceptionHandler.returnPrettySuccess(HttpStatus.OK);
    }
    

    @GetMapping("/clearResourceFolder")
    public boolean clearResourceFolder() {

        return DocumentBuilder.clearResourceFolder();
    }
    

    @GetMapping("/convertDocxToPdf")
    @ResponseStatus(value = HttpStatus.OK, reason = "Converted .docx to .pdf.")
    public void convertDocxToPdf() throws FileNotFoundException {

        DocumentBuilder.convertDocxToPdf(new File(RESOURCE_FOLDER + "/test/test.docx"), "vorspiel.pdf");
    }


    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam boolean pdf) {

        if (pdf) {
            // TODO: 
        }
        
        try {
            File file = new File(RESOURCE_FOLDER + DocumentBuilder.prependSlash(this.fileName));
            InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
            
            return ResponseEntity.ok()
                                .headers(getHttpHeaders(file.getName()))
                                .contentLength(file.length())
                                .contentType(MediaType.parseMediaType("application/octet-stream"))
                                .body(isr);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping
    public String test(@RequestBody @Validated List<BasicParagraph> content) {

        return "test";
    }


    /**
     * Create http headers for the download request.
     * 
     * @param fileName to use for the downloaded file.
     * @return {@link HttpHeaders} object.
     */
    private HttpHeaders getHttpHeaders(String fileName) {

        HttpHeaders header = new HttpHeaders();

        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        return header;
    }
}