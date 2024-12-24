package com.minio.minio_api_test.dto;

import lombok.Data;

@Data
public class ApiResponseDto {
    private boolean success;
    private Object data;
    private String message;

    public ApiResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
