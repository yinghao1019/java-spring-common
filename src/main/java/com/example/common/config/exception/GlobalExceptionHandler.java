package com.example.common.config.exception;

import com.example.common.constant.ErrorCode;
import com.example.common.constant.ErrorMsg;
import com.example.common.constant.LogParam;
import com.example.common.exception.BadRequestException;
import com.example.common.exception.InternalServerErrorException;
import com.example.common.model.dto.ErrorMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageDTO> handleMethodArgumentNotValidException(
        HttpServletRequest req,
        MethodArgumentNotValidException e) {
        List<String> fieldErrorMessages = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fieldError -> String.format("%s %s", fieldError.getField(),
                fieldError.getDefaultMessage()))
            .collect(Collectors.toList());
        String errorMessageForOutput = String.join(", ", fieldErrorMessages);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(newErrorMessageDTO(null, errorMessageForOutput));
    }

    /**
     * 處理用戶端相關錯誤，回傳status  code 400 MaxUploadSizeExceededException: 處理超過檔案上傳上限的例外，回傳status code 400
     * IllegalArgumentException: 處理參數錯誤(如格式錯誤、沒有輸入等)的例外
     */
    @ExceptionHandler({
        MaxUploadSizeExceededException.class,
        IllegalArgumentException.class,
        BadRequestException.class
    })
    public ResponseEntity<ErrorMessageDTO> handleBadRequestException(
        HttpServletRequest request,
        Exception e,
        HandlerMethod handlerMethod) {
        String errMsg;
        if (e instanceof MaxUploadSizeExceededException) {
            errMsg = "上傳的檔案大小超過上限。";
            log.error(errMsg);
        } else if (e instanceof IllegalArgumentException) {
            errMsg = "輸入之參數錯誤。";
            log.error(errMsg, e);
        } else {
            errMsg = String.format("%s", e.getMessage());
            log.error(errMsg);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(newErrorMessageDTO(ErrorCode.BAD_REQUEST, errMsg));
    }

    /**
     * 處理未能識別的例外，一律視為系統錯誤並回傳HTTP status code 500 NoMatchingSysParamSettingException: 沒有對應的系統參數例外
     * OfficialDocJsonMappingException: 公文系統傳入的JSON解析例外
     */
    @ExceptionHandler(value = {
        HttpMessageNotReadableException.class,
        Exception.class,
    })
    public ResponseEntity<ErrorMessageDTO> defaultInternalServerErrorHandler(
        HttpServletRequest request,
        Exception e,
        HandlerMethod handlerMethod) {
        String errMsg;
        if (e instanceof HttpMessageNotReadableException) {
            errMsg = "非法的 JSON 輸入，請您確認輸入之資料內容是否符合格式。";
        } else {
            errMsg = "未知的錯誤，請您記錄操作流程與所見問題後回報客服人員，感謝您的協助。";
        }
        log.error(errMsg, e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(newErrorMessageDTO(ErrorCode.UNKNOWN_ERROR, errMsg));
    }

    /**
     * GET/POST請求方法錯誤的攔截器 因為開發時可能比較常見, 而且發生在進入 controller 之前, 上面的攔截器攔截不到這個錯誤 所以定義了這個攔截器
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorMessageDTO> httpRequestMethodHandler() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(newErrorMessageDTO(ErrorCode.UNKNOWN_ERROR, ErrorMsg.INTERNAL_ERR_MSG));
    }

    @ExceptionHandler({ InternalServerErrorException.class, UnsupportedEncodingException.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorMessageDTO> internalServerErrorExceptionHandler(
        InternalServerErrorException e
    ) {
        log.error(e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(newErrorMessageDTO(ErrorCode.UNKNOWN_ERROR, ErrorMsg.INTERNAL_ERR_MSG));
    }

    private ErrorMessageDTO newErrorMessageDTO(String errorCode, String errorMessage) {
        return ErrorMessageDTO.builder()
            .errorCode(errorCode)
            .message(errorMessage)
            .traceId(MDC.get(LogParam.REQUEST_OID))
            .dateTime(LocalDateTime.now().toString()).build();
    }
}
