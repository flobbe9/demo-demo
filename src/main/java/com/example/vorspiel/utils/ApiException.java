package com.example.vorspiel.utils;


/**
 * General format any exception thrown in this api should have so the front end can rely
 * on this object's fields. <p>
 * Don't change this class for above reason!
 * 
 * @since 0.0.1
 */
public record ApiException(

    int status,

    String error,

    String message,

    String path
) { }