package com.example.vorspiel.utils;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.log4j.Log4j2;


/**
 * Return nicely formatted responses for http requests.
 * 
 * @since 0.0.1
 */
@Log4j2
public class RequestExceptionHandler {

    /**
     * Log given error message and return {@link ApiException} with http status, error message and servlet path.
     * 
     * @param status http status
     * @param errorMessage brief description of the error
     * @return formatted response holding status, error message and servlet path
     */
    public static ApiException returnPretty(HttpStatus status, String errorMessage) {

        log.error(errorMessage);
        return new ApiException(status.value(), 
                                status.getReasonPhrase(), 
                                errorMessage, 
                                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                                                                                .getRequest()
                                                                                .getServletPath());
    }
    
    
    /**
     * Overloading {@link #returnPretty(HttpStatus, String)}.
     * 
     * @param status
     * @param bindingResult
     * @return
     */
    public static ApiException returnPretty(HttpStatus status, BindingResult bindingResult) {

        String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
        
        return returnPretty(status, errorMessage);
    }


    /**
     * Overloading {@link #returnPretty(HttpStatus, String)}.
     * 
     * @param status
     * @param bindingResult
     * @return
     */
    public static ApiException returnPretty(HttpStatus status) {

        return returnPretty(status, "Failed to process http request");
    }

    
    /**
     * Overloading {@link #returnPretty(HttpStatus, String)}.
     * 
     * @param status
     * @param bindingResult
     * @return
     */
    public static ApiException returnPrettySuccess(HttpStatus status) {

        return new ApiException(status.value(), 
                                null, 
                                "Http request successful",
                                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                                                                                .getRequest()
                                                                                .getServletPath());    
    }
}