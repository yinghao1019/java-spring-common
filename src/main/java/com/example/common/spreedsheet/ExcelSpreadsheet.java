package com.example.common.spreedsheet;

import com.example.common.exception.BadRequestException;
import com.example.common.exception.InternalServerErrorException;
import com.example.common.spreedsheet.report.SheetType;
import com.example.common.spreedsheet.style.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.*;

public class ExcelSpreadsheet implements Spreadsheet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelSpreadsheet.class);
    private static final String DEFAULT_SHEET_NAME = "Sheet1";

    private final Workbook workbook;
    private final Map<SpreadsheetStyle, CellStyle> styleMap = new HashMap<>();
    private Sheet workingSheet;
    private Row workingRow;
    private Cell workingCell;
    private SpreadsheetStyle headerStyle;

    public ExcelSpreadsheet() {
        workbook = new SXSSFWorkbook();
    }

    public ExcelSpreadsheet(int rowAccessWindowSize) {
        workbook = new SXSSFWorkbook(rowAccessWindowSize);
    }

    public ExcelSpreadsheet(MultipartFile excelFile, ExcelType excelType) {
        try {
            XSSFWorkbook xssfWorkbook = (XSSFWorkbook) WorkbookFactory.create(
                excelFile.getInputStream());
            switch (excelType) {
                case XSS:
                    workbook = xssfWorkbook;
                    break;
                case SXSS:
                default:
                    workbook = new SXSSFWorkbook(xssfWorkbook, SXSSFWorkbook.DEFAULT_WINDOW_SIZE);
                    break;
            }
        } catch (IOException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    public ExcelSpreadsheet(File file, ExcelType excelType) {
        try {
            XSSFWorkbook xssfWorkbook = (XSSFWorkbook) WorkbookFactory.create(file);
            switch (excelType) {
                case XSS:
                    workbook = xssfWorkbook;
                    break;
                case SXSS:
                default:
                    workbook = new SXSSFWorkbook(xssfWorkbook, SXSSFWorkbook.DEFAULT_WINDOW_SIZE);
                    break;
            }
        } catch (EncryptedDocumentException | IOException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    public ExcelSpreadsheet(InputStream is, int rowAccessWindowSize) {
        try {
            XSSFWorkbook xssfWorkbook = (XSSFWorkbook) WorkbookFactory.create(is);
            workbook = new SXSSFWorkbook(xssfWorkbook, rowAccessWindowSize);
        } catch (EncryptedDocumentException | IOException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    public static ExcelSpreadsheet createWorkbook() {
        return createWorkbook(DEFAULT_SHEET_NAME);
    }

    public static ExcelSpreadsheet createWorkbook(String sheetName) {
        ExcelSpreadsheet xlsx = new ExcelSpreadsheet();
        xlsx.createSheet(sheetName);
        return xlsx;
    }

    public static ExcelSpreadsheet createWorkbook(int rowAccessWindowSize) {
        ExcelSpreadsheet xlsx = new ExcelSpreadsheet(rowAccessWindowSize);
        xlsx.createSheet(DEFAULT_SHEET_NAME);
        return xlsx;
    }

    // 會修改原檔案
    public static ExcelSpreadsheet loadWorkbook(File excelFile) {
        return loadWorkbook(excelFile, ExcelType.SXSS);
    }

    // 會修改原檔案
    public static ExcelSpreadsheet loadWorkbook(File excelFile, ExcelType excelType) {
        return new ExcelSpreadsheet(excelFile, excelType);
    }

    // 不會修改原檔案
    public static ExcelSpreadsheet loadWorkbook(InputStream input) {
        return new ExcelSpreadsheet(input, SXSSFWorkbook.DEFAULT_WINDOW_SIZE);
    }

    // 不會修改原檔案 自訂AccessWindowSize
    public static ExcelSpreadsheet loadWorkbook(InputStream input, int rowAccessWindowSize) {
        return new ExcelSpreadsheet(input, rowAccessWindowSize);
    }

    /*
     * cell style
     */
    public static SpreadsheetStyle cellCenterStyle() {
        SpreadsheetStyle style = SpreadsheetStyle.builder()
            .borderPosition(SpreadsheetBorderPosition.builder().build())
            .build();
        style.setHAlign(TpHorizontalAlignment.CENTER);
        style.setVAlign(TpVerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setFontStyle(cellFont());

        return style;
    }

    public static SpreadsheetStyle cellLeftStyle() {
        SpreadsheetStyle style = SpreadsheetStyle.builder()
            .borderPosition(SpreadsheetBorderPosition.builder().build())
            .build();
        style.setHAlign(TpHorizontalAlignment.LEFT);
        style.setVAlign(TpVerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setFontStyle(cellFont());

        return style;
    }

    public static SpreadsheetStyle cellRightStyle() {
        SpreadsheetStyle style = SpreadsheetStyle.builder()
            .borderPosition(SpreadsheetBorderPosition.builder().build())
            .build();
        style.setHAlign(TpHorizontalAlignment.RIGHT);
        style.setVAlign(TpVerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setFontStyle(cellFont());

        return style;
    }

    public static SpreadsheetStyle cellLeftBoldStyle() {
        SpreadsheetStyle style = SpreadsheetStyle.builder()
            .borderPosition(SpreadsheetBorderPosition.builder().build())
            .build();
        style.setHAlign(TpHorizontalAlignment.LEFT);
        style.setVAlign(TpVerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setFontStyle(cellFontBold());

        return style;
    }

    public static SpreadsheetFontStyle cellFont() {
        SpreadsheetFontStyle font = SpreadsheetFontStyle.builder().build();
        font.setFontName("標楷體");
        font.setFontSize(14);
        font.setBold(false);
        font.setItalic(false);
        return font;
    }

    public static SpreadsheetFontStyle cellFontBold() {
        SpreadsheetFontStyle font = SpreadsheetFontStyle.builder().build();
        font.setFontName("標楷體");
        font.setFontSize(14);
        font.setBold(true);
        font.setItalic(false);
        return font;
    }

    public static String safeGetStringCellValue(Cell cell) {
        // 有可能因為格式問題，導致讀取不到資料，所以先轉成 String 格式，讀完再轉回原格式
        CellType originalCellType = cell.getCellType();
        String formula = originalCellType == CellType.FORMULA ? cell.getCellFormula() : null;
        try {
            cell.setCellType(CellType.STRING);
            return cell.getStringCellValue();
        } catch (Exception ex) {
            return "";
        } finally {
            if (originalCellType == CellType.FORMULA) {
                cell.setCellFormula(formula);
            } else {
                cell.setCellType(originalCellType);
            }
        }
    }

    public static double safeGetNumericCellValue(Cell cell) {
        try {
            return cell.getNumericCellValue();
        } catch (Exception ex) {
            return 0.0;
        }
    }

    public <T> ExcelSpreadsheet generateSheet(List<T> dataList, Class<T> clazz) throws
        IllegalAccessException {
        if (dataList.isEmpty()) {
            return this.generateHeader(clazz, 0, 0,
                null, true, 0, false);
        }
        return this.generateSheet(dataList);
    }

    public <T> ExcelSpreadsheet generateSheet(List<T> dataList) throws
        IllegalAccessException {
        return this.generateSheet(dataList, 0, 0, null, true, 0);
    }

    public <T> ExcelSpreadsheet generateSheet(
        List<T> dataList,
        int startRowIndex,
        int startColIndex,
        String title,
        boolean showColumnHeader,
        int titleShiftRowSize) throws IllegalAccessException {
        return this.generateSheet(dataList, startRowIndex, startColIndex, title,
            showColumnHeader,
            titleShiftRowSize, false);
    }

    public <T> ExcelSpreadsheet generateHeader(
        Class<T> columnType,
        int startRowIndex,
        int startColIndex,
        String title,
        boolean showColumnHeader,
        int titleShiftRowSize,
        boolean hasCreateDate) {

        Map<Integer, ExcelColumn> map = parseExcelColumn(columnType);
        int shiftRowSize = hasCreateDate ? 1 + titleShiftRowSize : titleShiftRowSize;
        buildTitle(map, startRowIndex, startColIndex, title, hasCreateDate);
        if (showColumnHeader) {
            if (StringUtils.isNotBlank(title)) {
                shiftRowSize++;
            }

            buildHeader(map, startRowIndex + shiftRowSize);
        }

        buildHeader(map, 0);
        setColumnWidth(map);
        return this;
    }

    public <T> ExcelSpreadsheet generateSheet(
        List<T> dataList,
        int startRowIndex,
        int startColIndex,
        String title,
        boolean showColumnHeader,
        int titleShiftRowSize,
        boolean hasCreateDate) throws IllegalAccessException {
        if (CollectionUtils.isEmpty(dataList)) {
            throw new IllegalArgumentException("No data to generate!");
        }
        //key:model(dto) field idx, value: colum
        Map<Integer, ExcelColumn> map = parseExcelColumn(dataList.get(0).getClass());
        if (map.size() == 0) {
            throw new IllegalArgumentException(dataList.get(0).getClass().getName() +
                " have no fields annotated with @ExcelColumn!");
        }
        int shiftRowSize = hasCreateDate ? 1 + titleShiftRowSize : titleShiftRowSize;
        buildTitle(map, startRowIndex, startColIndex, title, hasCreateDate);
        if (showColumnHeader) {
            if (StringUtils.isNotBlank(title)) {
                shiftRowSize++;
            }

            buildHeader(map, startRowIndex + shiftRowSize);
        }
        shiftRowSize++;
        buildContent(dataList, map, startRowIndex + shiftRowSize);
        setColumnWidth(map);
        return this;
    }

    public void close() throws IOException {
        workbook.close();
    }

    /**
     * sheet
     */
    public ExcelSpreadsheet createSheet(String sheetName) {
        workingSheet = workbook.createSheet(sheetName);
        return this;
    }

    public ExcelSpreadsheet getSheetAt(int index) {
        workingSheet = workbook.getSheetAt(index);
        if (workingSheet == null) {
            createSheet("sheet" + index);
        }
        return this;
    }

    public ExcelSpreadsheet useSheet(String sheetName) {
        workingSheet = workbook.getSheet(sheetName);
        if (workingSheet == null) {
            createSheet(sheetName);
        }
        return this;
    }

    public void removeSheet(int sheetNum) {
        if (this.isSheetExist(sheetNum)) {
            workbook.removeSheetAt(sheetNum);
        }
    }

    public SXSSFSheet cloneSheet(int sheetNum) {
        return (SXSSFSheet) workbook.cloneSheet(sheetNum);
    }

    public ExcelSpreadsheet setSheetColumnWidth(List<Integer> columnWidth) {
        int columnNum = 0;
        for (Integer column : columnWidth) {
            workingSheet.setColumnWidth(columnNum, column * 4 * 256);
            columnNum++;
        }
        return this;
    }

    public boolean isSheetExist(String sheetName) {
        workingSheet = workbook.getSheet(sheetName);
        return workingSheet != null;
    }

    public boolean isSheetExist(int sheetNum) {
        return workbook.getSheetAt(sheetNum) != null;
    }

    /**
     * row
     */
    public ExcelSpreadsheet createRow(int rowIndex) {
        if (workingSheet == null) {
            createSheet(DEFAULT_SHEET_NAME);
        }
        workingRow = workingSheet.createRow(rowIndex);
        return this;
    }

    public ExcelSpreadsheet useRow(int rowIndex) {
        workingRow = workingSheet.getRow(rowIndex);
        if (workingRow == null) {
            createRow(rowIndex);
        }
        return this;
    }

    /**
     * cell
     */
    public ExcelSpreadsheet createCell(int columnIndex) {
        if (workingRow == null) {
            throw new BadRequestException("workingRow must not be null");
        }
        workingCell = workingRow.createCell(columnIndex);
        return this;
    }

    public ExcelSpreadsheet useCell(int columnIndex) {
        workingCell = workingRow.getCell(columnIndex);
        if (workingCell == null) {
            createCell(columnIndex);
        }
        return this;
    }

    public ExcelSpreadsheet setCellValue(int rowIndex, int columnIndex, Object value) {
        setCellValue(rowIndex, columnIndex, value, null);
        return this;
    }

    public ExcelSpreadsheet setCellValue(
        int rowIndex, int columnIndex, Object value,
        SpreadsheetStyle style) {
        workingRow = workingSheet.getRow(rowIndex);
        if (workingRow == null) {
            createRow(rowIndex);
        }
        workingCell = workingRow.getCell(columnIndex);
        if (workingCell == null) {
            createCell(columnIndex);
        }

        this.setCellValueByType(value);

        buildAndSetCellStyle(style);
        return this;
    }

    public ExcelSpreadsheet setCellValueByType(Object value) {
        if (value == null) {
            workingCell.setCellValue("");
        } else if (value instanceof Integer) {
            workingCell.setCellValue((int) value);
        } else if (value instanceof Long) {
            workingCell.setCellValue((long) value);
        } else {
            workingCell.setCellValue(value.toString());
        }

        return this;
    }

    public List<List<String>> readFields() {
        return readFields(DEFAULT_SHEET_NAME);
    }

    public List<List<String>> readFields(String sheetName) {
        return readFields(sheetName, null);
    }

    public List<List<String>> readFields(String sheetName, Integer maxReadCellNum) {
        useSheet(sheetName);
        List<List<String>> data = new ArrayList<>();
        int rowNum = workingSheet.getLastRowNum();
        for (int i = 0; i <= rowNum; i++) {
            List<String> cellData = new ArrayList<>();
            workingRow = workingSheet.getRow(i);
            if (workingRow != null) {
                int cellNum = workingRow.getLastCellNum() - 1;
                cellNum =
                    maxReadCellNum == null || cellNum < maxReadCellNum ? cellNum
                        : maxReadCellNum;
                for (int j = 0; j <= cellNum; j++) {
                    String val = "";
                    workingCell = workingRow.getCell(j);
                    if (workingCell != null) {
                        val = getCellValue();
                    }
                    cellData.add(val);
                }
            }
            data.add(cellData);
        }
        return data;
    }

    public String getCellValue() {
        switch (workingCell.getCellType()) {
            case STRING:
                return workingCell.getRichStringCellValue().getString();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(workingCell)) {
                    return workingCell.getDateCellValue().toString();
                } else {
                    return String.valueOf(workingCell.getNumericCellValue());
                }
            case FORMULA:
                return workingCell.getCellFormula();
            case BOOLEAN:
                return String.valueOf(workingCell.getBooleanCellValue());
            case BLANK:
            case ERROR:
            default:
                return StringUtils.EMPTY;
        }
    }

    /**
     * ["yyyy/mm/dd", "yyyy/m/d hh:mm", ...]
     */
    public ExcelSpreadsheet setCellValue(Date value) {
        workingCell.setCellValue(value);
        return this;
    }

    /**
     * ["0.0", "#,##0.0000", ...]
     */
    public ExcelSpreadsheet setCellValue(double value) {
        workingCell.setCellValue(value);
        return this;
    }

    private int getCellValueLength(Cell cell) {
        if (cell == null) {
            return 0;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getRichStringCellValue().getString().length();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString().length();
                } else {
                    return String.valueOf(cell.getNumericCellValue()).length();
                }
            case FORMULA:
                return cell.getCellFormula().length();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).length();
            case BLANK:
            case ERROR:
            default:
                return 0;
        }
    }

    public ExcelSpreadsheet mergeCells(
        int rowStartIndex,
        int mergedRowCount,
        int colStartIndex,
        int mergedColumnCount
    ) {
        return mergeCells(rowStartIndex, mergedRowCount, colStartIndex, mergedColumnCount, null,
            false);
    }

    public ExcelSpreadsheet mergeCells(
        int rowStartIndex, int mergedRowCount,
        int colStartIndex, int mergedColumnCount, SpreadsheetStyle style) {
        return mergeCells(rowStartIndex, mergedRowCount, colStartIndex, mergedColumnCount,
            style,
            false);
    }

    public ExcelSpreadsheet mergeCells(
        int rowStartIndex, int mergedRowCount,
        int colStartIndex, int mergedColumnCount, SpreadsheetStyle style,
        boolean isWorkingCellAutoRowHeight) {
        int rowEndIndex =
            (mergedRowCount == 0) ? rowStartIndex : (rowStartIndex + mergedRowCount - 1);
        int colEndIndex =
            (mergedColumnCount == 0) ? colStartIndex : (colStartIndex + mergedColumnCount - 1);
        CellRangeAddress range = new CellRangeAddress(
            rowStartIndex,
            rowEndIndex,
            colStartIndex,
            colEndIndex
        );

        buildAndSetCellStyle(style, range);
        if (mergedRowCount != 1 || mergedColumnCount != 1) {
            workingSheet.addMergedRegion(range);
        }

        if (isWorkingCellAutoRowHeight) {
            int textSize = getCellValueLength(workingCell);
            XSSFFont cellFont = this.getWorkingCellFont();
            if (cellFont != null) {
                int fontSize = cellFont.getFontHeightInPoints();
                setRowHeight(rowStartIndex, this.getColWidthSum(colStartIndex, colEndIndex),
                    textSize, fontSize);
            }
        }
        return this;
    }

    private XSSFFont getWorkingCellFont() {
        XSSFCellStyle cellStyle = (XSSFCellStyle) workingCell.getCellStyle();
        if (cellStyle != null) {
            return cellStyle.getFont();
        } else {
            return null;
        }
    }

    private int getColWidthSum(int colStartIndex, int colEndIndex) {
        int count = 0;
        if (colEndIndex - colStartIndex >= 0) {
            for (int i = colStartIndex; i < colEndIndex + 1; i++) {
                count += (workingSheet.getColumnWidth(i) / 256);
            }
        } else {
            throw new InternalServerErrorException("合併儲存格不得為負值");
        }
        return count;
    }

    public void setRowHeight(int rowIndex, short height) {
        Row row = workingSheet.getRow(rowIndex);
        if (row == null) {
            return;
        }
        row.setHeight(height);
    }

    private void buildAndSetCellStyle(SpreadsheetStyle style) {
        if (style != null) {
            CellStyle cellStyle = putStyleToMap(style);
            workingCell.setCellStyle(cellStyle);
        }
    }

    private void buildAndSetCellStyle(SpreadsheetStyle style, CellRangeAddress region) {
        if (style != null) {
            CellStyle cellStyle = putStyleToMap(style);
            RegionUtil.setBorderBottom(cellStyle.getBorderBottom(), region, workingSheet);
            RegionUtil.setBorderTop(cellStyle.getBorderTop(), region, workingSheet);
            RegionUtil.setBorderLeft(cellStyle.getBorderLeft(), region, workingSheet);
            RegionUtil.setBorderRight(cellStyle.getBorderRight(), region, workingSheet);
        }
    }

    private CellStyle putStyleToMap(SpreadsheetStyle style) {
        return styleMap.computeIfAbsent(style, this::buildCellStyle);
    }

    public CellStyle buildCellStyle(SpreadsheetStyle style) {
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();

        SpreadsheetFontStyle fontStyle = style.getFontStyle();
        if (fontStyle != null) {
            Font font = workbook.createFont();
            font.setFontName(fontStyle.getFontName());
            font.setFontHeightInPoints((short) fontStyle.getFontSize());
            font.setBold(style.getFontStyle().isBold());
            cellStyle.setFont(font);
        }
        //set Color
        SpreedSheetCellColor cellColor = style.getCellColor();
        if (cellColor != null) {
            IndexedColorMap colorMap = new DefaultIndexedColorMap();
            XSSFColor fillForeGroundColor = new XSSFColor(java.awt.Color.decode(cellColor.getFillForeGroundHexCode()),
                colorMap);
            cellStyle.setFillPattern(cellColor.getFillPattern());
            cellStyle.setFillForegroundColor(fillForeGroundColor);
        }

        cellStyle.setVerticalAlignment(verticalAlignmentTypeConverter(style.getVAlign()));
        cellStyle.setAlignment(horizontalAlignmentTypeConverter(style.getHAlign()));
        cellStyle.setWrapText(style.isWrapText());
        if (style.getBorderPosition().isBottom()) {
            cellStyle.setBorderBottom(borderConverter(style.getBorderStyle()));
        }
        if (style.getBorderPosition().isLeft()) {
            cellStyle.setBorderLeft(borderConverter(style.getBorderStyle()));
        }
        if (style.getBorderPosition().isRight()) {
            cellStyle.setBorderRight(borderConverter(style.getBorderStyle()));
        }
        if (style.getBorderPosition().isTop()) {
            cellStyle.setBorderTop(borderConverter(style.getBorderStyle()));
        }
        return cellStyle;
    }

    // getDefaultRowHeightInPoints 為15的情況適用
    public void autoSetRowHeight() {
        int rowLastNum = workingSheet.getLastRowNum() + 1;
        for (int rowIndex = 0; rowIndex < rowLastNum; rowIndex++) {
            Row row = workingSheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            int cellLastNum = row.getLastCellNum();
            int neededRowsMax = 1;

            for (int cellIndex = 0; cellIndex < cellLastNum; cellIndex++) {
                Cell cell = row.getCell(cellIndex);
                if (cell == null) {
                    continue;
                }
                // 欄寬 *4.8 (計算出來的可能有更好的?)
                double columnWidth =
                    4.8 * (workingSheet.getColumnWidth(cell.getColumnIndex()) / 256D);
                int fontSize = ((XSSFCellStyle) cell.getCellStyle()).getFont()
                    .getFontHeightInPoints();
                // 字數*大小
                int chartSize = this.getCellValueLength(cell) * fontSize;

                //無條件進位
                int neededRows = (int) Math.ceil(
                    Math.ceil(chartSize / columnWidth) * Math.ceil(fontSize / 12.0));

                if (neededRows > neededRowsMax) {
                    neededRowsMax = neededRows;
                }
            }
            float defaultRowHeight = workingSheet.getDefaultRowHeightInPoints();
            row.setHeightInPoints(neededRowsMax * defaultRowHeight);
        }
    }

    public void setRowHeight(int rowIndex, int totalWidth, int textSize, int fontSize) {
        // 欄寬 *4.8 (計算出來的可能有更好的?)
        double columnWidth = 4.8 * totalWidth;
        // 字數*大小
        int chartSize = textSize * fontSize;

        // 無條件進位
        int neededRows = (int) Math.ceil(
            Math.ceil(chartSize / columnWidth) * Math.ceil(fontSize / 12.0));
        float defaultRowHeight = workingSheet.getDefaultRowHeightInPoints();
        if (neededRows * defaultRowHeight > workingSheet.getRow(rowIndex).getHeightInPoints()) {
            workingSheet.getRow(rowIndex).setHeightInPoints(neededRows * defaultRowHeight);
        }
    }

    public ExcelSpreadsheet autoCellWidth() {
        if (workingSheet instanceof SXSSFSheet) {
            ((SXSSFSheet) workingSheet).trackAllColumnsForAutoSizing();
        }
        Row header = workingSheet.getRow(0);
        int cellCnt = header.getLastCellNum();
        for (int i = 0; i < cellCnt; i++) {
            workingSheet.autoSizeColumn(i);
        }
        return this;
    }

    public void autoAllCellWidth(int rowIndex) {
        Row row = workingSheet.getRow(rowIndex);
        if (row != null) {
            int cellCnt = row.getLastCellNum();
            if (workingSheet instanceof SXSSFSheet) {
                ((SXSSFSheet) workingSheet).trackAllColumnsForAutoSizing();
            }
            for (int j = 0; j < cellCnt; j++) {
                workingSheet.autoSizeColumn(j);
            }
        }
    }

    public Resource toResource() throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
            workbook.write(byteArrayOutputStream);
            return new ByteArrayResource(byteArrayOutputStream.toByteArray());
        }
    }

    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    public ExcelSpreadsheet exportFile(String path, String fileName) throws IOException {
        final String fileNameWithExtension = appendExtensionIfNot(fileName);
        File file = new File(path, fileNameWithExtension);
        return exportFile(file);
    }

    public ExcelSpreadsheet exportFile(File path, String fileName) throws IOException {
        final String fileNameWithExtension = appendExtensionIfNot(fileName);
        File file = new File(path, fileNameWithExtension);
        return exportFile(file);
    }

    private ExcelSpreadsheet exportFile(File file) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            exportFile(fileOut);
        } catch (FileNotFoundException ex) {
            LOGGER.error("No file found with file name: {}", file.getAbsoluteFile(), ex);
        }
        return this;
    }

    private String appendExtensionIfNot(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new BadRequestException("非法的檔案名稱");
        }
        String extension = SheetType.EXCEL.getExtension();
        if (!fileName.endsWith(extension)) {
            fileName = fileName + extension;
        }
        return fileName;
    }

    public ExcelSpreadsheet exportFile(OutputStream outputStream) throws IOException {
        try {
            workbook.write(outputStream);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            outputStream.close();
            workbook.close();
        }
        return this;
    }

    @Override
    public String getExtension() {
        return SheetType.EXCEL.getExtension();
    }

    private <T> Map<Integer, ExcelColumn> parseExcelColumn(Class<T> clazz) {
        Map<Integer, ExcelColumn> map = new HashMap<>();

        Field[] fields = clazz.getDeclaredFields();
        int idx = 0;
        for (Field field : fields) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null) {
                map.put(idx++, annotation);
            }
        }
        return map;
    }

    private ExcelSpreadsheet buildTitle(
        Map<Integer, ExcelColumn> map,
        int startRowIndex,
        int startColIndex,
        String title,
        boolean hasCreateDate) {
        if (StringUtils.isBlank(title)) {
            return this;
        }
        int mergedColumnCount = map.entrySet().size();
        SpreadsheetFontStyle titleStyle = new SpreadsheetFontStyle("標楷體", 20, false, false);
        SpreadsheetStyle style = SpreadsheetStyle.builder().build();
        style.setFontStyle(titleStyle);
        style.setHAlign(TpHorizontalAlignment.CENTER);
        style.setWrapText(true);
        mergeCells(startRowIndex, 1, startColIndex, mergedColumnCount, style);
        setCellValue(startRowIndex, startColIndex, title, style);
        setRowHeight(startRowIndex, 20 * mergedColumnCount, title.length(),
            titleStyle.getFontSize());

        if (hasCreateDate) {
            SpreadsheetFontStyle dateFontStyle = new SpreadsheetFontStyle("標楷體", 12, false,
                false);
            SpreadsheetStyle dateStyle = SpreadsheetStyle.builder().build();
            dateStyle.setFontStyle(dateFontStyle);
            dateStyle.setHAlign(TpHorizontalAlignment.RIGHT);
            dateStyle.setWrapText(true);

            mergeCells(startRowIndex + 1, 1, startColIndex, mergedColumnCount, dateStyle);
            OffsetDateTime now = OffsetDateTime.now();
            setCellValue(startRowIndex + 1, startColIndex,
                "產生日期:" + now.getYear() + "年" + now.getMonthValue() + "月" + now.getDayOfMonth()
                    + "日", dateStyle);
        }

        return this;
    }

    private ExcelSpreadsheet buildHeader(
        Map<Integer, ExcelColumn> map, int startRowIndex) {
        SpreadsheetStyle headerStyle = getHeaderStyle();

        for (Map.Entry<Integer, ExcelColumn> entry : map.entrySet()) {
            ExcelColumn annotation = entry.getValue();
            String colName = annotation.colName();
            int columnIndex = annotation.colIndex();
            if (StringUtils.isNotBlank(annotation.mergeGroup())) {
                mergeCells(startRowIndex - annotation.rowSize(), 1, columnIndex,
                    annotation.mergeGroupSize(), headerStyle);
                setCellValue(startRowIndex - annotation.rowSize(), columnIndex, annotation.mergeGroup(),
                    headerStyle);
                setRowHeight(startRowIndex - annotation.rowSize(),
                    annotation.columnWidth() * annotation.mergeGroupSize(),
                    annotation.mergeGroup().length(), headerStyle.getFontStyle().getFontSize()
                );
            }

            if (annotation.rowSize() > 1) {
                // 包含此格往上找size格
                mergeCells(startRowIndex - (annotation.rowSize() - 1), annotation.rowSize(),
                    columnIndex, 1, headerStyle);
                setCellValue(startRowIndex - (annotation.rowSize() - 1), columnIndex, colName, headerStyle);
            } else {
                setCellValue(startRowIndex, columnIndex, colName, headerStyle);
            }
        }
        return this;
    }

    private <T> ExcelSpreadsheet buildContent(
        List<T> dataList,
        Map<Integer, ExcelColumn> map,
        int startRowIndex)
        throws IllegalAccessException {
        int rowIndex = startRowIndex;
        Map<Integer, SpreadsheetStyle> contentStyleMap = buildStyleMapByExcelColumn(map);

        for (T data : dataList) {
            Field[] fields = data.getClass().getDeclaredFields();
            for (Map.Entry<Integer, ExcelColumn> entry : map.entrySet()) {
                int idx = entry.getKey();
                int columnIndex = entry.getValue().colIndex();
                Object value = FieldUtils.readField(fields[idx], data, true);
                setCellValue(
                    rowIndex, columnIndex, value, contentStyleMap.get(entry.getKey())
                );
            }
            rowIndex++;
        }
        return this;
    }

    private Map<Integer, SpreadsheetStyle> buildStyleMapByExcelColumn(
        Map<Integer, ExcelColumn> map) {
        Map<Integer, SpreadsheetStyle> contentStyleMap = new HashMap<>();
        for (Map.Entry<Integer, ExcelColumn> entry : map.entrySet()) {
            contentStyleMap
                .put(entry.getKey(),
                    SpreadsheetStyle.builder().hAlign(entry.getValue().hAlign())
                        .vAlign(entry.getValue().vAlign())
                        .wrapText(entry.getValue().isWrap())
                        .fontStyle(
                            SpreadsheetFontStyle.builder().fontName(entry.getValue().fontName())
                                .fontSize(entry.getValue().fontSize())
                                .build()).build()
                );
        }

        return contentStyleMap;
    }

    private VerticalAlignment verticalAlignmentTypeConverter(
        TpVerticalAlignment verticalAlignment) {
        switch (verticalAlignment) {
            case TOP:
                return VerticalAlignment.TOP;
            case CENTER:
                return VerticalAlignment.CENTER;
            case BOTTOM:
            default:
                return VerticalAlignment.BOTTOM;
        }
    }

    private HorizontalAlignment horizontalAlignmentTypeConverter(
        TpHorizontalAlignment horizontalAlignment) {
        switch (horizontalAlignment) {
            case LEFT:
                return HorizontalAlignment.LEFT;
            case RIGHT:
                return HorizontalAlignment.RIGHT;
            case CENTER:
                return HorizontalAlignment.CENTER;
            case JUSTIFY:
                return HorizontalAlignment.JUSTIFY;
            case FILL:
                return HorizontalAlignment.FILL;
            case GENERAL:
            default:
                return HorizontalAlignment.GENERAL;
        }
    }

    private BorderStyle borderConverter(TpBorderStyle tpBorderStyle) {
        switch (tpBorderStyle) {
            case THIN:
                return BorderStyle.THIN;
            case MEDIUM:
                return BorderStyle.MEDIUM;
            case THICK:
                return BorderStyle.THICK;
            case DOUBLE:
                return BorderStyle.DOUBLE;
            case NONE:
            default:
                return BorderStyle.NONE;
        }
    }

    private ExcelSpreadsheet setColumnWidth(Map<Integer, ExcelColumn> map) {
        for (Map.Entry<Integer, ExcelColumn> entry : map.entrySet()) {
            ExcelColumn annotation = entry.getValue();
            int colWidth = annotation.columnWidth();
            this.workingSheet.setColumnWidth(annotation.colIndex(), colWidth * 256);
        }

        return this;
    }

    public Sheet getWorkingSheet() {
        return this.workingSheet;
    }

    public void setWorkingCell(Cell workingCell) {
        this.workingCell = workingCell;
    }

    public SpreadsheetStyle getHeaderStyle() {
        if (this.headerStyle == null) {
            this.headerStyle = SpreadsheetStyle.builder().hAlign(TpHorizontalAlignment.CENTER)
                .vAlign(TpVerticalAlignment.CENTER).wrapText(true)
                .fontStyle(SpreadsheetFontStyle.builder().fontName("新細明體").fontSize(12).bold(true).build())
                .build();
        }
        return this.headerStyle;
    }

    public void setHeaderColumnStyle(
        SpreedSheetCellColor cellColor,
        TpHorizontalAlignment hAlign,
        TpVerticalAlignment vAlign,
        String fontName, Integer fontSize, boolean isBold) {
        this.headerStyle = SpreadsheetStyle.builder().hAlign(hAlign).vAlign(vAlign).wrapText(true)
            .fontStyle(SpreadsheetFontStyle.builder().fontName(fontName).fontSize(fontSize).bold(isBold).build())
            .cellColor(cellColor)
            .build();
    }

    // SXSS：適合大量寫入，但寫過的欄位不能重複寫入，且不能讀取內容
    // XSS：適合讀取內容，或是需要重複寫入用
    public enum ExcelType {
        SXSS, XSS
    }
}
