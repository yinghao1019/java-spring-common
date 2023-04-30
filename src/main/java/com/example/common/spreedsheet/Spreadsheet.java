package com.example.common.spreedsheet;

import com.example.common.spreedsheet.style.SpreadsheetStyle;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

public interface Spreadsheet {
    <T> Spreadsheet generateSheet(List<T> dataList) throws IllegalAccessException;

    <T> Spreadsheet generateSheet(
        List<T> dataList,
        int startRowIndex,
        int startColIndex,
        String title,
        boolean showColumnHeader,
        int titleShiftRowSize) throws IllegalAccessException;

    <T> Spreadsheet generateSheet(
        List<T> dataList,
        int startRowIndex,
        int startColIndex,
        String title,
        boolean showColumnHeader,
        int titleShiftRowSize,
        boolean hasCreateDate) throws IllegalAccessException;

    void close() throws IOException;

    Spreadsheet createSheet(String sheetName);

    Spreadsheet useSheet(String sheetName);

    void removeSheet(int sheetNum);

    Spreadsheet setSheetColumnWidth(List<Integer> columnWidth);

    boolean isSheetExist(String sheetName);

    boolean isSheetExist(int sheetNum);

    Spreadsheet createRow(int rowIndex);

    Spreadsheet useRow(int rowIndex);

    Spreadsheet createCell(int columnIndex);

    Spreadsheet useCell(int columnIndex);

    Spreadsheet setCellValue(int rowIndex, int columnIndex, Object value);

    Spreadsheet setCellValue(int rowIndex, int columnIndex, Object value, SpreadsheetStyle style);

    Spreadsheet setCellValueByType(Object value);

    List<List<String>> readFields();

    List<List<String>> readFields(String sheetName);

    List<List<String>> readFields(String sheetName, Integer maxReadCellNum);

    String getCellValue();

    Spreadsheet setCellValue(Date value);

    Spreadsheet setCellValue(double value);

    Spreadsheet mergeCells(
        int rowStartIndex,
        int mergedRowCount,
        int colStartIndex,
        int mergedColumnCount
    );

    Spreadsheet mergeCells(
        int rowStartIndex, int mergedRowCount,
        int colStartIndex, int mergedColumnCount,
        SpreadsheetStyle style
    );

    Spreadsheet mergeCells(
        int rowStartIndex, int mergedRowCount,
        int colStartIndex, int mergedColumnCount,
        SpreadsheetStyle style, boolean isWorkingCellAutoRowHeight
    );

    void setRowHeight(int rowIndex, int totalWidth, int textSize, int fontSize);

    Spreadsheet autoCellWidth();

    void autoAllCellWidth(int rowIndex);

    Spreadsheet exportFile(String path, String fileName) throws IOException;

    Spreadsheet exportFile(File path, String fileName) throws IOException;

    Spreadsheet exportFile(OutputStream outputStream) throws IOException;

    Resource toResource() throws IOException;

    byte[] getBytes() throws IOException;

    String getExtension();
}
