package com.example.common.spreedsheet;

import com.example.common.spreedsheet.style.TpHorizontalAlignment;
import com.example.common.spreedsheet.style.TpVerticalAlignment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD })
public @interface ExcelColumn {

    int colIndex();

    String colName();

    int columnWidth() default 20;

    String fontName() default "新細明體";

    int fontSize() default 11;

    TpHorizontalAlignment hAlign() default TpHorizontalAlignment.LEFT;

    TpVerticalAlignment vAlign() default TpVerticalAlignment.BOTTOM;

    int rowSize() default 1;

    boolean isWrap() default true;

    String mergeGroup() default "";

    int mergeGroupSize() default 1;

    boolean mergeGroupAutoColWidth() default true;
}