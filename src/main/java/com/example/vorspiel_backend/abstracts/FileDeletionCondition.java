package com.example.vorspiel_backend.abstracts;

import java.io.File;


/**
 * Functional interface defining a boolean function to determine if the file in the param should be deleted or not.
 * 
 * @since 0.0.5
 * @see com.example.vorspiel_backend.utils.Utils
 */
@FunctionalInterface
public interface FileDeletionCondition {
    
    boolean shouldFileBeDeleted(File file);
}
