package com.minio.minio_api_test.dto;

import lombok.Data;

@Data
public class UploadRequestDto {

    private String bucketName;

    private String fileName;

}
