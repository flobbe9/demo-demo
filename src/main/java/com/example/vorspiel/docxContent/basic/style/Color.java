package com.example.vorspiel.docxContent.basic.style;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@AllArgsConstructor
public enum Color {
    RED(""),
    BLUE(""),
    YELLOW(""),
    GREEN(""),
    ORANGE(""),
    PURPLE(""),
    PINK(""),
    GREY(""),
    BROWN(""),
    WHITE("");


    private String RGB;
}