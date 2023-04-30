package com.example.common.spreedsheet.report;

import com.example.common.exception.NoMatchingResourceException;

public enum SheetType {
    ODS,
    EXCEL;

    public static SheetType fromReportType(ReportType reportType) {
        switch (reportType) {
            case ODS:
                return ODS;
            case EXCEL:
                return EXCEL;
            default:
                throw new NoMatchingResourceException("此報表格式無法傳換為表單。");
        }
    }

    public ReportType toReportType() {
        switch (this) {
            case ODS:
                return ReportType.ODS;
            case EXCEL:
                return ReportType.EXCEL;
            default:
                throw new NoMatchingResourceException("此報表格式無法傳換為表單。");
        }
    }

    public String getExtension() {
        switch (this) {
            case ODS:
                return ".ods";
            case EXCEL:
                return ".xlsx";
            default:
                throw new NoMatchingResourceException("不支援此報表格式。");
        }
    }
}
