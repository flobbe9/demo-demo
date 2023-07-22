package com.example.vorspiel.documentParts.style;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Simple colors and corresponding hex strings.
 * 
 * @since 0.0.1
 */
@Getter
@AllArgsConstructor
public enum Color {
    RED("FF0101"),
    BLUE("2B01FF"),
    YELLOW("FFFF01"),
    GREEN("13ED13"),
    ORANGE("FF9900"),
    PURPLE("800080"),
    PINK("FF3399"),
    GREY("808080"),
    BROWN("993300"),
    WHITE("FFFFFF"),
    BLACK("000000");


    private String RGB;
}