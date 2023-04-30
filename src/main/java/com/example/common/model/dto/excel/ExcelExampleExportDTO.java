package com.example.common.model.dto.excel;

import com.example.common.spreedsheet.ExcelColumn;
import lombok.Data;

@Data
public class ExcelExampleExportDTO {
    @ExcelColumn(colIndex = 0, colName = "id")
    private String id;
    @ExcelColumn(colIndex = 1, colName = "name")
    private String name;
}
