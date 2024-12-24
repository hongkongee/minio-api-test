package com.minio.minio_api_test.service;

import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class MinioService {
    private final MinioClient minioClient;

    @Autowired
    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    // 버킷이 없으면 생성
    public void createBucketIfNotExists(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {


        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

        if (!found) { // 버킷이 존재 하지 않으면
            // 새 버킷 생성
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } else {
            log.info("Bucket '{}' already exists.", bucketName);
        }
    }

    // 파일 업로드
    public void uploadFile(String bucketName, String clientFileName, InputStream inputStream, String minioObjectName) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        try {
//            minioClient.uploadObject(
//                    UploadObjectArgs.builder()
//                            .bucket(bucketName)
//                            .object(minioObjectName)
//                            .filename(clientFileName)
//                            .build());

            // InputStream 크기 확인
            long fileSize = inputStream.available();




            if (fileSize < 5 * 1024 * 1024) { // 5MiB 미만인 경우
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(minioObjectName)
                                .stream(inputStream, fileSize, -1)
                                .contentType("application/octet-stream") // 적절한 MIME 타입 설정
                                .build()
                );
            } else { // 5MiB 이상인 경우
                minioClient.uploadObject(
                        UploadObjectArgs.builder()
                                .bucket(bucketName)
                                .object(minioObjectName)
                                .filename(clientFileName)
                                .build()
                );
            }
            log.info("'{}' is successfully uploaded as "
                    + "object '{}' to bucket '{}'.", clientFileName, minioObjectName, bucketName);

        } catch (MinioException e) {
            log.error("Failed to upload '{}' to bucket '{}'. Error: {}", minioObjectName, bucketName, e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn("Failed to close InputStream: {}", e.getMessage());
                }
            }
        }
    }

    public InputStream downloadFile(String bucketName, String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }


    public boolean bucketOrObjectExists(String bucketName, String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) { // 버킷이 존재하지 않을 시
            return false;
        } else {
            try {
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build()
                );
                return true; // 버킷 안에 오브젝트가 존재할 시
            } catch (ErrorResponseException e) {
                if (e.errorResponse().code().equals("NoSuchKey")) { // 버킷 안에 오브젝트가 존재하지 않을 시
                    return false;
                }
                throw new RuntimeException("Error checking object existence: " + e.getMessage());

            } catch (Exception e) {
                throw new RuntimeException("Unexpected error: " + e.getMessage());
            }


        }

    }
}


