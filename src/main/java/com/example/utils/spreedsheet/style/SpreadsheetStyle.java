package com.example.utils.spreedsheet.style;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class SpreadsheetStyle {

    private SpreadsheetFontStyle fontStyle;
    private SpreedSheetCellColor cellColor;
    @Builder.Default
    private TpHorizontalAlignment hAlign = TpHorizontalAlignment.GENERAL;
    @Builder.Default
    private TpVerticalAlignment vAlign = TpVerticalAlignment.CENTER;
    @Builder.Default
    private TpBorderStyle borderStyle = TpBorderStyle.THIN;
    @Builder.Default
    private SpreadsheetBorderPosition borderPosition = SpreadsheetBorderPosition.builder().build();
    private boolean wrapText;
}
