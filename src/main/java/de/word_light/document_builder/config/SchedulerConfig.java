package de.word_light.document_builder.config;

import static de.word_light.document_builder.utils.Utils.DOCX_FOLDER;
import static de.word_light.document_builder.utils.Utils.PDF_FOLDER;
import static de.word_light.document_builder.utils.Utils.PICTURES_FOLDER;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import de.word_light.document_builder.abstracts.FileDeletionCondition;
import de.word_light.document_builder.utils.Utils;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;


/**
 * Class difining all cron jobs.
 * 
 * @since 0.0.6
 */
@Configuration
@EnableScheduling
@Log4j2
public class SchedulerConfig {

    @PostConstruct
    void init() {

        log.info("Starting scheduler...");
    }

    /**
     * Delete user generated files that have been modified at least or more than a day ago.<p>
     * 
     * Folder are {@link #DOCX_FOLDER}, {@link #PDF_FOLDER} and {@link #PICTURES_FOLDER}
     */
    @Scheduled(cron = "0 0 23 * * *") // every day at 23:00 pm
    public void deleteUserGeneratedFiles() {

        log.info("Deleting user generated files...");

        // delete files that are at least a day old
        FileDeletionCondition fileDeletionCondition = (File file) -> {
            LocalDateTime lastModified = Utils.millisToLocalDateTime(file.lastModified(), null);

            return Duration.between(lastModified, LocalDateTime.now()).toDays() >= 1;
        };

        Utils.clearFolder(DOCX_FOLDER, fileDeletionCondition);
        Utils.clearFolder(PDF_FOLDER, fileDeletionCondition);
        Utils.clearFolder(PICTURES_FOLDER, fileDeletionCondition);

        log.info("Finished deleting user generated files.");
    }
}