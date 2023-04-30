package com.example.utils.spreedsheet.style;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.FillPatternType;

@Builder
@Setter
@Getter
public class SpreedSheetCellColor {
    @Builder.Default
    FillPatternType fillPattern = FillPatternType.SOLID_FOREGROUND;
    String fillForeGroundHexCode;
}
