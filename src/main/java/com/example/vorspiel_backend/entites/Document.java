package com.example.vorspiel_backend.entites;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.lang.Nullable;

import com.example.vorspiel_backend.utils.Utils;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.MapKeyColumn;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * Entity persisting document related files, like the .docx itself, pictures and pdfs.
 * 
 * @since 0.0.5
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
public class Document extends AbstractEntity {

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "picture_file_name", unique = true)
    @Column(name = "picture_bytes", length = 16777215)
    @Nullable
    private Map<String, byte[]> pictures;

    @Nullable
    private String docxFileName;

    @Lob
    @Column(length = 16777215)
    @Nullable
    private byte[] docxBytes;

    @Column(unique = true)
    @Nullable
    private String pdfFileName;

    @Lob
    @Column(length = 16777215)
    @Nullable
    private byte[] pdfBytes;


    public Document() {
        
        this.pictures = new HashMap<>();
    }


    public Document(@NotBlank(message = "'docxFileName' cannot be blank or null") String docxFileName, byte[] docxBytes) {
       
        this.docxFileName = docxFileName;
        this.docxBytes = docxBytes;
        this.pictures = new HashMap<>();
    }


    public Document(Map<String, byte[]> pictures, @NotBlank(message = "'docxFileName' cannot be blank or null") String docxFileName, byte[] docxBytes) {
        
        this.pictures = pictures;
        this.docxFileName = docxFileName;
        this.docxBytes = docxBytes;
        this.pictures = new HashMap<>();
    }


    public Document(@NotBlank(message = "'docxFileName' cannot be blank or null") String docxFileName, byte[] docxBytes, String pdfFileName, byte[] pdfBytes) {
        
        this.docxFileName = docxFileName;
        this.docxBytes = docxBytes;
        this.pdfFileName = pdfFileName;
        this.pdfBytes = pdfBytes;
        this.pictures = new HashMap<>();
    }


    /**
     * Converts {@link #docxBytes} into {@link File} and stores it in {@link Utils.STATIC_FOLDER}.
     * 
     * @return {@link #docxBytes} as {@link File}
     */
    public File getDocxFile() {

        return Utils.byteArrayToFile(this.docxBytes, this.docxFileName);        
    }


    /**
     * Converts {@link #pdfBytes} into {@link File} and stores it in {@link Utils.STATIC_FOLDER}.
     * 
     * @return {@link #pdfBytes} as {@link File}
     */
    public File getPdfFile() {

        return Utils.byteArrayToFile(this.pdfBytes, this.pdfFileName);        
    }
}