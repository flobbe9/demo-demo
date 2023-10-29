package com.example.vorspiel_backend.controllers;

import static com.example.vorspiel_backend.utils.Utils.DOCX_FOLDER;
import static com.example.vorspiel_backend.utils.Utils.PDF_FOLDER;
import static com.example.vorspiel_backend.utils.Utils.PICTURES_FOLDER;
import static com.example.vorspiel_backend.utils.Utils.STATIC_FOLDER;
import static com.example.vorspiel_backend.utils.Utils.prependSlash;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.example.vorspiel_backend.documentBuilder.DocumentBuilder;
import com.example.vorspiel_backend.documentBuilder.PictureUtils;
import com.example.vorspiel_backend.documentParts.BasicParagraph;
import com.example.vorspiel_backend.documentParts.DocumentWrapper;
import com.example.vorspiel_backend.entites.Document;
import com.example.vorspiel_backend.exception.ApiException;
import com.example.vorspiel_backend.exception.ApiExceptionFormat;
import com.example.vorspiel_backend.exception.ApiExceptionHandler;
import com.example.vorspiel_backend.services.DocumentService;
import com.example.vorspiel_backend.utils.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.log4j.Log4j2;


@RestController
@RequestMapping("/api/documentBuilder")
@Validated
@Log4j2
@Tag(name = "Document builder logic")
@SessionScope
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /** Document of current session */
    private Document document = new Document();


    /**
     * Builds word document, writes to .docx file and saves it to db. <p>
     * 
     * Assuming that: <p>
     * first {@link BasicParagraph} is the header <p>
     * second {@link BasicParagraph} is the title <p>
     * last {@link BasicParagraph} is the footer <p>
     * anything in between is main content <p>.
     * 
     * @param documentWrapper object containing all document information
     * @throws ApiExceptionFormat
     */
    @PostMapping("/createDocument")
    @Operation(summary = "Build document and write to .docx.")
    public ApiExceptionFormat createDocument(@RequestBody @Valid DocumentWrapper documentWrapper, BindingResult bindingResult) {

        // case: http 400
        if (bindingResult.hasErrors()) 
            return ApiExceptionHandler.returnPretty(BAD_REQUEST, bindingResult);

        DocumentBuilder documentBuilder = new DocumentBuilder(documentWrapper.getContent(), 
                                                             "document.docx", 
                                                             documentWrapper.getNumColumns(),
                                                             documentWrapper.isLandscape(),
                                                             documentWrapper.getTableConfig(),
                                                             this.document.getPictures());
        // build
        documentBuilder.build();

        // write
        File docxFile = documentBuilder.writeDocxFile();

        // save
        this.document = this.documentService.save(new Document(documentBuilder.getDocxFileName(), Utils.fileToByteArray(docxFile)));

        // clean up
        Utils.clearFolderByFileName(DOCX_FOLDER, this.document.getDocxFileName());

        return ApiExceptionHandler.returnPrettySuccess(OK);
    }
    

    /**
     * Convert {@code this.document} to pdf and save pdf to {@code this.document}.
     *  
     * @throws FileNotFoundException
     */
    @GetMapping("/docxToPdf")
    @Operation(summary = "Write existing .docx file to .pdf. Needs to call '/createDocument' before. Currently only works locally.")
    public ApiExceptionFormat convertDocxToPdf() throws FileNotFoundException {

        File docxFile = this.document.getDocxFile();

        // convert
        String pdfFileName = Utils.prependDateTime("document.pdf");
        File pdfFile = DocumentBuilder.docxToPdfDocuments4j(docxFile, pdfFileName);

        // update current document
        this.document.setPdfFileName(pdfFileName);
        this.document.setPdfBytes(Utils.fileToByteArray(pdfFile));
        this.document = this.documentService.save(this.document);

        // clean up
        Utils.clearFolderByFileName(STATIC_FOLDER, docxFile.getName());
        Utils.clearFolderByFileName(PDF_FOLDER, pdfFileName);

        return ApiExceptionHandler.returnPrettySuccess(OK);
    }


    /**
     * Upload a {@link MultipartFile} file and save it to {@code this.document} as picture.
     * 
     * @param picture picture as multipart file
     */
    @PostMapping(path = "/uploadPicture", consumes = "multipart/form-data")
    @Operation(summary = "Upload a picture as multipart file to filesystem in backend.")
    public ApiExceptionFormat uploadFile(@RequestBody @NotNull(message = "Failed to upload picture. 'file' cannot be null.") MultipartFile picture) {

        log.info("Starting to upload files...");

        String fileName = picture.getOriginalFilename();
        // case: not a picture
        if (!PictureUtils.isPicture(fileName)) 
            throw new ApiException(UNPROCESSABLE_ENTITY, "Faile to upload picture. File " + fileName + " is not recognized as picture.");

        String completeFileName = PICTURES_FOLDER + prependSlash(fileName);
        try (OutputStream os = new FileOutputStream(completeFileName);
             InputStream is = picture.getInputStream()) {
            
            // write to file
            os.write(is.readAllBytes());

            // check file exists
            File uploadedFile = new File(completeFileName);
            if (!uploadedFile.exists()) 
                throw new ApiException("Failed to write stream to file.");

            // updated document
            this.document.getPictures().put(fileName, Utils.fileToByteArray(uploadedFile));
            this.document = this.documentService.save(this.document);

        } catch (Exception e) {
            throw new ApiException("Failed to upload picture.", e);

        } finally {
            // clean up
            Utils.clearFolderByFileName(PICTURES_FOLDER, fileName);
            
            log.info("Upload finished");
        }

        return ApiExceptionHandler.returnPrettySuccess(OK);
    }


    /**
     * Download {@code this.document} as .docx or pdf.
     * 
     * @param pdf if true, {@code this.document} is converted to pdf before download
     * @return {@link StreamingResponseBody} triggering a download in the browser
     * @throws FileNotFoundException
     */
    @GetMapping("/download")
    @Operation(summary = "Download existing .docx or .pdf file. Needs  to call '/createDocument' before.")
    public ResponseEntity<StreamingResponseBody> download(@RequestParam boolean pdf) throws FileNotFoundException {

        log.info("Downloading document...");

        try {
            File file = pdf ? this.document.getPdfFile() : this.document.getDocxFile();

            // download
            return ResponseEntity.ok()
                                .headers(getHttpHeaders(file.getName()))
                                .contentLength(file.length())
                                .contentType(MediaType.parseMediaType("application/octet-stream"))
                                .body(os -> {
                                    // read to stream
                                    Files.copy(file.toPath(), os);
                                    file.delete();
                                });

        } catch (Exception e) {
            throw new ApiException("Failed to download file.", e);

        } finally {
            log.info("Download finished");
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