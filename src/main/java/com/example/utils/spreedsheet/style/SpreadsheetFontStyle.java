package com.example.utils.spreedsheet.style;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
@AllArgsConstructor
public class SpreadsheetFontStyle {
    private String fontName;
    private int fontSize;
    private boolean bold;
    private boolean italic;
}
