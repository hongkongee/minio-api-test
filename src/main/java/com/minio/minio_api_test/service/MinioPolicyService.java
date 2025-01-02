package com.minio.minio_api_test.service;

import io.minio.BucketExistsArgs;
import io.minio.GetBucketPolicyArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioPolicyService {
    private final MinioClient minioClient;

    @Autowired
    public MinioPolicyService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void setBucketPolicy(String bucketName, String policyJson) throws ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException, IOException, NoSuchAlgorithmException, InvalidKeyException {

        try {


            // 버킷 정책을 설정
            SetBucketPolicyArgs setBucketPolicyArgs = SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policyJson) // JSON 형식의 정책
                    .build();

            minioClient.setBucketPolicy(setBucketPolicyArgs);

            System.out.println("Successfully set policy for bucket: " + bucketName);

        } catch (MinioException e) {
            System.err.println("Error occurred: " + e);
            throw e;
        }
    }

    public String getBucketPolicy(String bucketName) throws Exception {
        try {
            // MinIO에서 버킷 정책을 가져옴
            String policyJson = minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
            return policyJson;
        } catch (ErrorResponseException e) {
            // 버킷이 존재하지 않는 경우 처리
            if ("NoSuchBucket".equals(e.errorResponse().code())) {
                throw new Exception("Bucket '" + bucketName + "' does not exist.");
            }
            throw e;  // 다른 예외는 그대로 던짐
        } catch (Exception e) {
            throw new Exception("Failed to get policy for bucket: " + bucketName, e);
        }
    }


}
