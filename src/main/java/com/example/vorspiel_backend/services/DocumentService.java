package com.example.vorspiel_backend.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.example.vorspiel_backend.entites.Document;
import com.example.vorspiel_backend.exception.ApiException;
import com.example.vorspiel_backend.repositories.DocumentRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


// TODO: add more methods
// TODO: add tests
/**
 * Class handling {@link Document} logic.
 * 
 * @since 0.0.5
 */
@Service
@Validated
public class DocumentService extends AbstractService<Document, DocumentRepository> {

    @Autowired
    private DocumentRepository repository;
    

    public byte[] getPictureByFileName(@NotBlank(message = "'fileName' cannot be blank or null") String fileName, @NotNull(message = "'documentId' cannot be null") Long documentId) {

        Document document = getById(documentId);

        Map<String, byte[]> pictures = document.getPictures();
        if (pictures == null || pictures.isEmpty())
            return null;

        return pictures.entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().equals(fileName))
                        .findAny()
                        .orElseThrow(() -> new ApiException("Failed to get picture by file name: " + fileName))
                        .getValue();
    }
}