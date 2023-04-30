package com.example.common.spreedsheet.report;

import com.example.common.exception.NoMatchingResourceException;

public enum ReportType {
    EXCEL, ODS;

    public String getExtension() {
        switch (this) {
            case EXCEL:
                return ".xlsx";
            case ODS:
                return ".ods";
            default:
                throw new NoMatchingResourceException("不支援此報表格式。");
        }
    }
}
