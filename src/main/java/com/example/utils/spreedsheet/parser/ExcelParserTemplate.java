package com.example.utils.spreedsheet.parser;

import com.example.utils.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class ExcelParserTemplate<T> {
    protected List<T> dataList = new ArrayList<>();

    public void parse(MultipartFile file, int sheetNum)
        throws InvocationTargetException, IllegalAccessException, ParseException {
        Workbook wb = getWorkbook(file);
        assert wb != null;
        parseExcel(wb, sheetNum);
    }

    public void parse(Workbook wb, int sheetNum)
        throws InvocationTargetException, IllegalAccessException, ParseException {
        parseExcel(wb, sheetNum);
    }

    public void parse(byte[] excelBytes, int sheetNum)
        throws InvocationTargetException, IllegalAccessException, ParseException {
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes));) {
            parseExcel(workbook, sheetNum);
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    public List<T> getDataList() {
        return dataList;
    }

    private Workbook getWorkbook(MultipartFile file) {
        Workbook wb;
        if (file == null) {
            return null;
        }

        try (InputStream is = file.getInputStream()) {
            wb = new XSSFWorkbook(is);
            return wb;
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        return null;
    }

    private void parseExcel(Workbook workbook, int sheetNum)
        throws InvocationTargetException, IllegalAccessException, ParseException {
        Sheet sheet = workbook.getSheetAt(sheetNum);
        int firstRowNum = sheet.getFirstRowNum();
        int rowEnd = sheet.getPhysicalNumberOfRows();
        Row firstRow = sheet.getRow(firstRowNum);
        if (null == firstRow) {
            throw new BadRequestException("解析Excel失敗");
        }
        parseEachRowData(firstRowNum, rowEnd, sheet);
    }

    protected Cell getCell(Row row, int cellNum) {
        return row.getCell(cellNum);
    }

    protected boolean cellIsEmpty(Cell cell) {
        return cell == null || cell.getCellType() == CellType.BLANK;
    }

    protected String convertCellValueToString(Cell cell) {
        if (cell == null) {
            return null;
        }
        String returnValue = null;
        switch (cell.getCellType()) {
            case NUMERIC:
                Double doubleValue = cell.getNumericCellValue();
                DecimalFormat df = new DecimalFormat("0");
                returnValue = df.format(doubleValue);
                break;
            case STRING:
                returnValue = cell.getStringCellValue();
                break;
            default:
                break;
        }
        return returnValue;
    }

    protected boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        if (row.getLastCellNum() <= 0) {
            return true;
        }
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (!cellIsEmpty(cell)) {
                return false;
            }
        }
        return true;
    }

    protected abstract void parseEachRowData(int rowStart, int rowEnd, Sheet sheet) throws ParseException;
}
