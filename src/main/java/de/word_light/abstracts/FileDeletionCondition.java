package de.word_light.abstracts;

import java.io.File;


/**
 * Functional interface defining a boolean function to determine if the file in the param should be deleted or not.
 * 
 * @since 0.0.5
 * @see de.word_light.utils.Utils
 */
@FunctionalInterface
public interface FileDeletionCondition {
    
    boolean shouldFileBeDeleted(File file);
}
