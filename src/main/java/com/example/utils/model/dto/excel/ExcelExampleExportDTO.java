package com.example.utils.model.dto.excel;

import com.example.utils.spreedsheet.ExcelColumn;
import lombok.Data;

@Data
public class ExcelExampleExportDTO {
    @ExcelColumn(colIndex = 0, colName = "id")
    private String id;
    @ExcelColumn(colIndex = 1, colName = "name")
    private String name;
}
