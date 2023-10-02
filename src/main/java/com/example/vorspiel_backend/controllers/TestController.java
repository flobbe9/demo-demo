package com.example.vorspiel_backend.controllers;

import static com.example.vorspiel_backend.documentBuilder.DocumentBuilder.RESOURCE_FOLDER;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.multipart.MultipartFile;

import com.example.vorspiel_backend.documentBuilder.DocumentBuilder;
import com.example.vorspiel_backend.documentBuilder.PictureUtils;
import com.example.vorspiel_backend.documentParts.DocumentWrapper;
import com.example.vorspiel_backend.exception.ApiException;
import com.example.vorspiel_backend.exception.ApiExceptionFormat;
import com.example.vorspiel_backend.exception.ApiExceptionHandler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.log4j.Log4j2;


@RestController
@RequestMapping("/test")
@Validated
@Log4j2
public class TestController {

    private String fileName;


    /**
     * Assuming that: <p>
     * first element is the header <p>
     * the second is the title <p>
     * last element is the footer <p>
     * anything in between is main content <p>.
     * @throws ApiExceptionFormat
     */
    @PostMapping("/createDocument")
    public ApiExceptionFormat createDocument(@RequestBody @Validated DocumentWrapper wrapper, BindingResult bindingResult) {

        // case: http 400
        if (bindingResult.hasErrors()) 
            return ApiExceptionHandler.returnPretty(BAD_REQUEST, bindingResult);

        // build and write document
        DocumentBuilder documentBuilder = new DocumentBuilder(wrapper.getContent(), 
                                                             "vorspiel.docx", 
                                                             wrapper.getNumColumns(),
                                                             wrapper.isLandscape(),
                                                             wrapper.getTableConfig());
        documentBuilder.build();

        // set file name for download
        this.fileName = documentBuilder.getDocxFileName();

        // case: http 200
        return ApiExceptionHandler.returnPrettySuccess(OK);
    }
    

    @GetMapping("/clearResourceFolder")
    public ApiExceptionFormat clearResourceFolder() {

        return DocumentBuilder.clearResourceFolder() ? ApiExceptionHandler.returnPrettySuccess(OK) : 
                                                       ApiExceptionHandler.returnPretty(INTERNAL_SERVER_ERROR, "Failed to clear resource folder. See logs for more information.");
    }
    

    @GetMapping("/convertDocxToPdf")
    @ResponseStatus(value = OK, reason = "Converted .docx to .pdf.")
    public void convertDocxToPdf() throws FileNotFoundException {

        DocumentBuilder.docxToPdfDocuments4j(new File(RESOURCE_FOLDER + "/test/test.docx"), "vorspiel.pdf");
    }


    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> download(@RequestParam boolean pdf) {

        log.info("Downloading file...");

        try {
            // docxFile
            File file = new File(RESOURCE_FOLDER + DocumentBuilder.prependSlash(this.fileName));
            
            // case: convert to pdf
            if (pdf) 
                file = DocumentBuilder.docxToPdfDocuments4j(file, "test.pdf");
                // file = DocumentBuilder.docxToPdfIText(new FileInputStream(file), "test.pdf");
            
            InputStreamResource isr = new InputStreamResource(new FileInputStream(file));

            // download
            return ResponseEntity.ok()
                                .headers(getHttpHeaders(file.getName()))
                                .contentLength(file.length())
                                .contentType(MediaType.parseMediaType("application/octet-stream"))
                                .body(isr);

        } catch (IOException e) {
            throw new ApiException("Failed to download file.", e);
        }
    }


    @PostMapping("/uploadFile")
    public ApiExceptionFormat uploadFile(@RequestBody @NotNull(message = "Failed to upload picture. 'file' cannot be null.") MultipartFile file,
                                         @RequestParam @NotBlank(message = "Failed to upload picture. 'fileName' cannot be blank or null.") String fileName) {

        log.info("Starting to upload files...");

        // write to file
        try (OutputStream os = new FileOutputStream(PictureUtils.PICTURES_FOLDER + DocumentBuilder.prependSlash(fileName));
             InputStream is = file.getInputStream()) {
                
            os.write(is.readAllBytes());

            if (new File(PictureUtils.PICTURES_FOLDER + DocumentBuilder.prependSlash(fileName)).exists())
                return ApiExceptionHandler.returnPrettySuccess(OK);
                
            else
                throw new ApiException("Failed to write stream to file.");

        } catch (Exception e) {
            throw new ApiException("Failed to upload picture.", e);
        }
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