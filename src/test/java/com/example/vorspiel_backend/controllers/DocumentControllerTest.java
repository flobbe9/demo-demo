package com.example.vorspiel_backend.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.example.vorspiel_backend.documentParts.BasicParagraph;
import com.example.vorspiel_backend.documentParts.DocumentWrapper;
import com.example.vorspiel_backend.documentParts.TableConfig;
import com.example.vorspiel_backend.documentParts.style.Style;
import com.example.vorspiel_backend.exception.ApiException;
import com.example.vorspiel_backend.exception.ApiExceptionFormat;
import com.example.vorspiel_backend.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;


// TODO: don't use spring boot test, mock session and service
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(OrderAnnotation.class)
public class DocumentControllerTest {

    @Value("${BASE_URL}")
    private String baseUrl;

    @Autowired
    private MockMvc mockMvc;

    private String requestMapping;

    private Style style;
    private BasicParagraph basicParagraph;
    private TableConfig tableConfig;
    private DocumentWrapper documentWrapper;


    @BeforeEach
    void setup() {
        
        this.requestMapping = this.baseUrl + "/api/documentBuilder";
        this.style = new Style(8, "Calibri", "000000", true, true, true, ParagraphAlignment.LEFT, null);
        this.basicParagraph = new BasicParagraph("text", this.style);
        this.tableConfig = new TableConfig(2, 1, 0);
        this.documentWrapper = new DocumentWrapper(List.of(basicParagraph), tableConfig, false, 1);
    }


    @Test
    @Order(0)
    void createDocument_shouldBeStatus200() throws Exception {

        MvcResult response = performPost("/createDocument", this.documentWrapper)
                             .andExpect(status().isOk())
                             .andReturn();

        checkApiExceptionFormatPrettySuccess(response.getResponse().getContentAsString(), OK);
    }


    @Test
    void createDocument_shouldBeStatus400_null() throws Exception {

        MvcResult response = performPost("/createDocument", null)
                            .andExpect(status().isBadRequest())
                            .andReturn();

        String jsonResponse = response.getResponse().getContentAsString();

        checkJsonApiExceptionFormat(jsonResponse, HttpStatus.BAD_REQUEST);
    }


    @Test 
    @Order(1)
    void createDocument_shouldBeStatus400_emptyContent() throws Exception {

        this.documentWrapper.setContent(List.of());

        MvcResult response = performPost("/createDocument", this.documentWrapper)
                            .andExpect(status().isBadRequest())
                            .andReturn();

        String jsonResponse = response.getResponse().getContentAsString();

        checkJsonApiExceptionFormat(jsonResponse, HttpStatus.BAD_REQUEST);
    }


    @Test 
    @Order(2)
    void createDocument_shouldBeStatus400_invalidContent() throws Exception {

        this.documentWrapper.getContent().get(0).setText(null);
        
        MvcResult response = performPost("/createDocument", this.documentWrapper)
                            .andExpect(status().isBadRequest())
                            .andReturn();

        String jsonResponse = response.getResponse().getContentAsString();

        checkJsonApiExceptionFormat(jsonResponse, HttpStatus.BAD_REQUEST);
    }


    @Test
    @Order(4)
    void createDocument_shouldBeStatus400_invalidNumColumns() throws Exception {

        this.documentWrapper.setNumColumns(0);
        
        MvcResult response = performPost("/createDocument", this.documentWrapper)
                            .andExpect(status().isBadRequest())
                            .andReturn();

        String jsonResponse = response.getResponse().getContentAsString();

        checkJsonApiExceptionFormat(jsonResponse, HttpStatus.BAD_REQUEST);
    }


    @Test
    void download_shouldBeStatus500() throws Exception {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("pdf", "false");
        params.add("fileName", "test.docx");

        MvcResult response = performGet("/download", params)
                            .andExpect(status().isInternalServerError())
                            .andReturn();

        checkJsonApiExceptionFormat(response.getResponse().getContentAsString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private ResultActions performPost(String path, Object body) throws Exception {

        return this.mockMvc.perform(post(this.requestMapping + path)
                                    .contentType(APPLICATION_JSON)
                                    .content(Utils.objectToJson(body)));
    }
    

    private ResultActions performGet(String path, MultiValueMap<String, String> params) throws Exception {

        return this.mockMvc.perform(get(this.requestMapping + path)
                                    .params(params == null ? new LinkedMultiValueMap<>() : params)
                                    .contentType(APPLICATION_JSON));
    }



    /**
     * Assert that given json String is an {@link ApiExceptionFormat} object. Also check the {@code status} and {@code error} values match
     * the given HttpStatus.
     * 
     * @param json String formatted as json to check
     * @param status http status
     */
    private void checkJsonApiExceptionFormat(String json, HttpStatus status) {

        JsonNode jsonNode = jsonToNode(json);

        assertEquals(4, jsonNode.size());

        assertTrue(jsonNode.has("status"));
        assertTrue(jsonNode.has("error"));
        assertTrue(jsonNode.has("message"));
        assertTrue(jsonNode.has("path"));

        assertEquals(status.value(), jsonNode.get("status").asInt());
        assertEquals(status.getReasonPhrase(), jsonNode.get("error").asText());
    }


    /**
     * Check given jsonResponse and assert {@link ApiExceptionFormat}s {@code returnPrettySuccess()} as result.
     * 
     * @param jsonResponse json response to check
     * @param status http status to assert
     */
    private void checkApiExceptionFormatPrettySuccess(String jsonResponse, HttpStatus status) {

        assertTrue(jsonResponse.contains("\"error\":null"));

        // modify response for the sake of check method
        jsonResponse = jsonResponse.replace("null", "\"" + status.name() + "\"");
        checkJsonApiExceptionFormat(jsonResponse, OK);
    }


    private JsonNode jsonToNode(String json) {

        ObjectReader objectReader = new ObjectMapper().reader();

        try {
            return objectReader.readTree(json);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Failed to convert object to json String.", e);
        }
    }
}
