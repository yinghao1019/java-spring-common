package com.example.common.constant;

public class ErrorCode {
    /*
     * 授權相關 E-1XX
     */
    // 未登入(沒帶入 token)
    public static final String TOKEN_NOT_FOUND = "E-100";
    // token 無效
    public static final String TOKEN_INVALID = "E-101";
    // token 過期
    public static final String TOKEN_EXPIRED = "E-102";
    // token 正確，但無權限存取該 api
    public static final String ACCESS_DENIED = "E-103";
    /*
     * 使用者帶入之參數錯誤 E-200
     */
    public static final String BAD_REQUEST = "E-200";
    /*
     * 未知的錯誤 E-999
     */
    public static final String UNKNOWN_ERROR = "E-999";

    private ErrorCode() {
    }
}
