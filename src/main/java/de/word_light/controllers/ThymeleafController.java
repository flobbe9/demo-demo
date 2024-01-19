package de.word_light.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import de.word_light.WordLightApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * Class handling endpoints using html pages in '/resources/templates' folder.
 * 
 * @since 0.0.5
 */
@Controller
@Tag(name = "Html template endpoints")
public class ThymeleafController {

    @Value("${WEBSITE_NAME}")
    private String websiteName;

    @Value("${API_NAME}")
    private String apiName;

    @Value("${BASE_URL}")
    private String baseUrl;

    @Value("${DB_VERSION}")
    private String dbVersion;

    
    @GetMapping("/")
    @Operation(summary = "View basic information about api.")
    public String getIndex(Model model) {

        model.addAttribute("WEBSITE_NAME", this.websiteName);
        model.addAttribute("API_NAME", this.apiName);
        model.addAttribute("BASE_URL", this.baseUrl);
        model.addAttribute("API_VERSION", WordLightApplication.getApiVersion());
        model.addAttribute("DB_VERSION", this.dbVersion);

        return "index";
    }
}