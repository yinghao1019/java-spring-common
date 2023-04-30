package com.example.common.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Schema(name = "ErrorMessageDTO", description = "錯誤訊息傳輸物件")
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessageDTO {
    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("traceId")
    private String traceId;

    @JsonProperty("dateTime")
    private String dateTime;
}
