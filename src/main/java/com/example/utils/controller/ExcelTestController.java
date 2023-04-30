package com.example.utils.controller;

import com.example.utils.file.FileUtils;
import com.example.utils.model.dto.DataDTO;
import com.example.utils.model.dto.excel.ExcelExampleExportDTO;
import com.example.utils.spreedsheet.ExcelSpreadsheet;
import com.example.utils.spreedsheet.Spreadsheet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("excel")
@Api(tags = "Excel Example API")
public class ExcelTestController {

    @ApiOperation(value = "匯出範例Excel")
    @GetMapping(value = "/export/base64",
        produces = "application/json;charset=UTF-8")
    public ResponseEntity<DataDTO> exportExcel() throws IllegalAccessException, IOException {
        Spreadsheet spreadsheet = new ExcelSpreadsheet();
        spreadsheet.createSheet("Sheet1");
        spreadsheet.generateSheet(genTestData());
        spreadsheet.createSheet("Sheet2");
        spreadsheet.generateSheet(genTestData());

        var dataDTO = new DataDTO();
        dataDTO.setData(FileUtils.getBas64String(spreadsheet.getBytes()));
        return ResponseEntity.ok(dataDTO);
    }

    private List<ExcelExampleExportDTO> genTestData() {
        List<ExcelExampleExportDTO> testDataList = new ArrayList<>();

        ExcelExampleExportDTO dto1 = new ExcelExampleExportDTO();
        dto1.setId("1");
        dto1.setName("Pino");
        testDataList.add(dto1);

        ExcelExampleExportDTO dto2 = new ExcelExampleExportDTO();
        dto2.setId("2");
        dto2.setName("Mark");
        testDataList.add(dto2);

        return testDataList;
    }
}