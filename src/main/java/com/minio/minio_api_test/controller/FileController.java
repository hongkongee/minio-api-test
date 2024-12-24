package com.minio.minio_api_test.controller;

import com.minio.minio_api_test.dto.ApiResponseDto;
import com.minio.minio_api_test.dto.UploadRequestDto;
import com.minio.minio_api_test.service.MinioService;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.core.Validate;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final MinioService minioService;

    // 파일 업로드
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@Validate @RequestPart("uploadName") UploadRequestDto uploadName,
                                        @RequestPart(value = "file", required = false) MultipartFile file,
                                        BindingResult result) {

        log.info("/api/files/upload POST - {}", uploadName.toString());

        if (file != null) {
            log.info("Received file: " + file.getOriginalFilename());
        }

        ResponseEntity<List<FieldError>> resultEntity = getFieldErrorResponseEntity(result);
        if (resultEntity != null) return resultEntity;

        try {

            // 버킷 존재 여부 확인 및 생성
            minioService.createBucketIfNotExists(uploadName.getBucketName());

            // 파일 업로드
            minioService.uploadFile(uploadName.getBucketName(), file.getOriginalFilename(), file.getInputStream(), uploadName.getFileName());

            return ResponseEntity.ok().body(new ApiResponseDto(true, "File uploaded successfully"));

        } catch (Exception e) {
            log.error("Error occurred: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    // 파일 다운로드
    @GetMapping("/download/{bucketName}/{objectName}")
    public ResponseEntity<?> downloadFile(@PathVariable("bucketName") String bucketName, @PathVariable("objectName") String objectName) {
        log.info("/api/files/download/{}/{} GET ", bucketName, objectName);

        try {
            // 버킷, 오브젝트 존재 확인
            Integer findObjectCode = minioService.bucketOrObjectExists(bucketName, objectName);
            if (findObjectCode != 0) { // 버킷이 없거나 해당 버킷 내에 오브젝트가 없을 시 404
                String message = findObjectCode == 1 ? "There is no bucket '" + bucketName + "'." : "In bucket '" + bucketName + "', object '" + objectName + "' does not exist.";
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDto(false, message));
            }



            // 파일 다운로드
            InputStream fileStream = minioService.downloadFile(bucketName, objectName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
                    .body(new InputStreamResource(fileStream));
            
        } catch (Exception e) {
            log.error("Error occurred: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 유효성 검사 메서드
    private static ResponseEntity<List<FieldError>> getFieldErrorResponseEntity(BindingResult result) {
        if (result.hasErrors()) {
            log.warn(result.toString());
            return ResponseEntity.badRequest()
                    .body(result.getFieldErrors());
        }
        return null;
    }


}
